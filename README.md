# Team 45 -- Naturhendelser

Android app for Case 3 (Naturhendelser) in IN2000 at UiO.
The app lets homeowners and cabin owners check climate risk
(flood, quick clay, radon, wind, landslide, avalanche) and
climate statistics for their area.

## Features

The app has three main screens accessible from a bottom navigation bar:

### Map (left button)
- Mapbox base map of Norway
- Six hazard overlays from NVE and NGU, picked from a bottom-left
  pill: flood, quick clay, radon, wind, landslide, avalanche. A
  "no overlay" option turns the layer off
- Each overlay has its own minimum zoom (e.g. flood activates at 11.5,
  avalanche at 10). The overlay image re-fetches as the camera moves
- An info button next to the pill opens a legend sheet that explains
  what the colors mean and names the source dataset
- Long-press on the map opens a save dialog that adds the spot to
  Saved Places (up to 4 places)
- A search bar at the top accepts a place name (Mapbox autocomplete)
  and recenters the map
- A location button on the bottom-right centers on the user. The app
  works fully without granting location access
- Zoom in/out buttons on the right side

### Climate statistics (right button)
- Search by place name (Mapbox autocomplete) or pick a Frost station
  directly from the suggestions
- Once a station is picked, the screen shows monthly and annual data
  for temperature, precipitation, peak wind gust, peak snow depth,
  and number of days per year with >= 20 mm rain ("extreme precipitation")
- Time-period chips switch between 5, 10, 20, 30 and 50 years. Charts
  and trend arrows (compared to the 30-year climate normals) update
  with the chosen window
- When normals are not available for a station, the screen falls back
  to historical observations from 2010-2024
- `StationPicker` assigns elements to the closest station with enough
  history. If no single station covers everything for 50+ years, it
  splits per element (see Algorithms below). The stations actually
  used are listed under the overview

### Saved Places (middle button)
- See risk assessment for places saved in the map screen
- Jump to saved places in either the map or the statistics screen to see in-depth climate statistics
- Receive precautionary measures based on risk assessment profile

## Tech stack

- Language: Kotlin
- UI: Jetpack Compose + Material 3
- Map: Mapbox SDK + Mapbox Search SDK
- Network: Ktor (CIO) + kotlinx.serialization JSON
- Image loading: Coil
- Persistence: AndroidX DataStore Preferences
- Navigation: Jetpack Navigation Compose
- Architecture: MVVM with datasource / repository / view-model layers
  (see [ARCHITECTURE.md](ARCHITECTURE.md))

## Libraries

Versions and the full list live in `app/build.gradle.kts` and
`gradle/libs.versions.toml`. Grouped by purpose:

UI / Compose. Jetpack Compose is our UI toolkit. The `compose-bom` pins consistent versions across the Compose artifacts. `material3` plus the extended icons set give us the Material 3 design system. `activity-compose` hooks Compose into `MainActivity`. `appcompat` is pulled in because Mapbox Search's UI components extend `AppCompatActivity`.
- `androidx.compose:compose-bom`
- `androidx.compose.ui`, `ui-graphics`, `ui-tooling-preview`
- `androidx.compose.material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.activity:activity-compose`
- `androidx.appcompat:appcompat` 1.7.1

Navigation and lifecycle. Jetpack Navigation drives the bottom-bar routing between Map, Statistics and Saved Places screens. `lifecycle-runtime-ktx` provides Kotlin extensions for AndroidX lifecycle components. `viewModelScope`, used in our ViewModels, is pulled in transitively via `activity-compose`.
- `androidx.navigation:navigation-compose`
- `androidx.navigation:navigation-runtime-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`

Networking. Ktor is the HTTP client we use to call Frost, NVE and NGU. The CIO engine is a coroutine-based Kotlin engine (no Java HttpURLConnection underneath). `content-negotiation` plus `kotlinx-serialization-kotlinx-json` plug in JSON parsing for the API responses.
- `io.ktor:ktor-client-core` 3.4.0
- `io.ktor:ktor-client-cio` 3.4.0
- `io.ktor:ktor-client-content-negotiation` 3.4.0
- `io.ktor:ktor-serialization-kotlinx-json` 3.4.0

Map and search. The Mapbox map SDK renders the map and our NVE/NGU image overlays. `maps-compose` is the Compose binding for the map view. `sdk-turf` does the geospatial math we need (point-in-polygon for flood zones, distance calculations for nearest weather stations). The Mapbox Search packages power the place autocomplete in the search bar; `place-autocomplete` is the one we call directly, the rest are required transitive UI/discover/offline pieces of the Search SDK.
- `com.mapbox.maps:android-ndk27` 11.20.1
- `com.mapbox.extension:maps-compose-ndk27` 11.20.1
- `com.mapbox.mapboxsdk:mapbox-sdk-turf` 7.10.0
- `com.mapbox.search:autofill-ndk27` 2.21.0
- `com.mapbox.search:discover-ndk27` 2.21.0
- `com.mapbox.search:place-autocomplete-ndk27` 2.21.0
- `com.mapbox.search:offline-ndk27` 2.21.0
- `com.mapbox.search:mapbox-search-android-ndk27` 2.21.0
- `com.mapbox.search:mapbox-search-android-ui-ndk27` 2.21.0

Image loading. Coil loads the NVE/NGU image overlays into the Compose UI; we also use it for any other remote images (for example flag icons in settings).
- `io.coil-kt:coil-compose` 2.7.0

Persistence. AndroidX DataStore Preferences is our on-device key-value store. We use it for the saved places list, recent searches, last camera position, theme, language, sound and onboarding-completed flags.
- `androidx.datastore:datastore-preferences` 1.1.7

Permissions. Accompanist Permissions wraps the runtime permission API for the location permission flow on the map screen.
- `com.google.accompanist:accompanist-permissions` 0.35.0-alpha

Browser (url launching). AndroidX Browser opens external links (NVE, NGU, Frost source pages) in a Chrome custom tab instead of leaving the app.
- `androidx.browser:browser` 1.10.0

Testing. JUnit 4 is the test runner. MockK is a Kotlin-friendly mocking library used in unit tests (it handles `object`, `coroutines` and Kotlin defaults better than Mockito). `kotlinx-coroutines-test` provides `runTest` and the test dispatcher for testing suspend functions. The AndroidX Test pieces plus Espresso and `ui-test-junit4` are for instrumented tests on the emulator.
- `junit` 4.13.2
- `io.mockk:mockk` 1.14.9
- `org.jetbrains.kotlinx:kotlinx-coroutines-test` 1.10.2
- `androidx.test.ext:junit`
- `androidx.test.espresso:espresso-core`
- `androidx.compose.ui:ui-test-junit4`

## APIs

- **MET Frost API v0** -- climate data (stations, normals, observations)
- **NVE** -- flood-, landslide-, avalanche-, quick clay-, and wind overlay on map 
- **NGU** -- Radon information
- **MapBox** -- Geographical information (map and search suggestions)

> **Note: Frost** Data is fetched directly from the Frost API v0 using Basic auth
> with the client ID as the username and an empty password. The IFI proxy is
> not used, as Frost is an external API with its own access control. 

## API-level 
The app is developed and tested on API level 36.1 (Android 16, “Baklava”) using a Medium Phone emulator.

We use compileSdk 36 and targetSdk 36, with a minimum SDK of minSdk 24 (Android 7.0, “Nougat”). minSdk 24 was chosen to support modern Android development with Jetpack Compose, Material 3, and coroutines, while still covering the majority of actively used devices. Lower API levels would require legacy solutions, while higher levels would exclude a significant portion of users without a major benefit. targetSdk 36 ensures the app follows the latest Android 16 behavior changes (such as predictive back, edge-to-edge rendering, notification permissions, and scoped storage) and meets Google Play requirements.

## Accessibility
The app is built with WCAG 2.1 (levels A and AA) in mind. The app also uses adaptive layouts and scalable units to support varying screen sizes.

## Network behaviour
The app handles connectivity loss gracefully. If the network becomes unavailable, the user is informed through an error message rather than experiencing a crash or blank screen. Any content that was already loaded before the connection was lost remains visible, so the user can continue navigating the app without a live connection.

## Lifecycle and state persistence
The app supports the Android Activity Lifecycle and handles configuration changes such as screen rotation without losing important data. State is preserved when the device rotates, the user navigates between screens, or the app is interrupted or temporarily closed. This ensures a stable user experience and prevents important information from disappearing during lifecycle events.

## Location Permission
The app requests access to the device's location to center the map on the
user's current position. The permission prompt is handled by the OS via the standard Android runtime permission flow. The app functions
fully without granting location access, the map simply defaults to a standard view of Norway.

## Algorithms

### Station picking for a place search

When the user searches for a place (for example "Stavanger") we fetch a list
of nearby weather stations sorted by distance. `StationPicker` then decides
which stations actually get used:

1. First we look for a single station that covers all measurements
   (temperature, precipitation, wind, snow depth) and has at least 50 years
   of history. For Oslo, Blindern fits, and we avoid mixing data.
2. If no single station is good enough, we split per element and pick the
   station with the longest history for each measurement. For Stavanger,
   Sola wins on temperature (1935-) while wind comes from another station.

The result is a small list of `StationAssignment`, where each station is
tagged with the elements it should serve. That keeps network traffic low.

### Days with heavy precipitation

The yearly statistics include "days with >= 20 mm precipitation", roughly
the level that overloads urban drainage. We pull daily sums from Frost for
the precipitation station and count days per year. Numbers vary heavily
between regions (Bergen ~40 days/yr, Karasjok ~1), so it works as a
concrete risk indicator for homeowners.

## Testing
Unit tests are written with JUnit 4 (4.13.2), MockK (1.14.9) for mocking
dependencies, and `kotlinx-coroutines-test` (1.10.2) for running suspend
functions in a controlled coroutine scope via `runTest`. All tests live in
`app/src/test/java` and are run with:

```bash
./gradlew testDebugUnitTest
```

There are 47 tests across seven classes:

| Class | Tests | What is covered |
|---|---|---|
| `StatisticDatasourceTest` | 9 | JSON parsing of Frost station responses and Mapbox geocode features (empty, malformed, missing fields) |
| `StatisticRepositoryTest` | 2 | Mocks the datasource and verifies that `getClimateStats` uses normals when available |
| `StatisticViewModelTest` | 1 | Mocks `Application` and checks that `searchStations` sets `uiState` to `Loading` |
| `MapDatasourceTest` | 6 | URL construction for each overlay type (flood, radon, quick clay, landslide, wind, avalanche) and that bbox values are included |
| `MapRepositoryTest` | 2 | Verifies that the repository delegates correctly to the datasource |
| `StationPickerTest` | 18 | Single-station vs split-per-element selection, history thresholds, edge cases (empty inputs, ties, missing availability) |
| `PlaceSelectorTest` | 9 | Place-selection logic for the statistics search-result list |

**46 tests pass, 1 fails.** The failure is pre-existing on `main`:

- `StatisticViewModelTest.testSearchStationsShouldSetLoadingState` — fails
  because `StatisticViewModel` calls `PlaceAutocomplete.create()` as a
  property initializer, which requires the Android runtime and is not
  available in unit tests.

**Not covered:** The `androidTest` folder contains only the Android Studio
default (`ExampleInstrumentedTest`). `SavedPlacesViewModel` and `MapViewModel`
are not unit tested due to their Mapbox and DataStore dependencies requiring
the Android runtime.
  
## Location Restriction Using GeoJSON Boundary Data
The application only allows saving locations within Norway, based on GeoJSON boundary data. This prevents users from saving areas without available underlying data and ensures valid and consistent risk assessments. Link to the repo used: [https://github.com/georgique/world-geojson](https://raw.githubusercontent.com/georgique/world-geojson/refs/heads/develop/countries/norway.json).

## Screen Loading Strategy / Performance Considerations
The application loads all screens in parallel at startup instead of using lazy loading. With lazy loading, screens would only be initialized when the user navigates to them, which could introduce noticeable delays and a less smooth user experience. Since the statistics and saved places screens are relatively lightweight and users are likely to switch between these and the map during a session, parallel loading was considered the most suitable approach to ensure a responsive and seamless user experience.

## Images
Circular flags used in the language sections of the settings screen were generated by AI. This choice was made after we found it difficult to find three similar circular Norwegian, British and Sami flags, that all had the same resolution, size and did not have any copyright text across it.


## Build and run

```bash
./gradlew assembleDebug
```

Requires:
- **Mapbox access token** in `app/src/main/res/values/mapbox_access_token.xml`
- **Frost client ID** in `app/src/main/res/values/frost_client_id.xml`
  - Register at https://frost.met.no/auth/requestCredentials.html

## Warnings and errors from IDE
The Android Studio IDE flags a number of issues across the project. The team policy is to either fix them or list them here with the reasoning for keeping them. The list below was last verified against `./gradlew :app:lintDebug` and Android Studio code analysis on 2026-05-14.

### Kotlin/Compose code-analysis warnings
- **MapContent.kt** -- "Calling a Mapbox Map Composable function where a UI Composable composable was expected". The `MapEffect` blocks live inside the `MapboxMap` block, which is the canonical pattern in the Mapbox Compose extension. The IDE's general Compose scope analysis doesn't know about Mapbox's nested scopes, so it flags this. No runtime impact.
- **MapSavedPlacesEffect.kt** -- "Calling a UI Composable composable function where a Mapbox Map Composable was expected". Mirror of the above. Same explanation: the Mapbox-specific scope is fine, the IDE's generic Compose scope analysis flags it anyway.
- **MapScreen.kt** -- "Assigned value is never read" for `showDialog`, `locationName`, `pendingPoint`. These are remembered state variables whose writes feed downstream effects (e.g., dialog visibility flows through composition rather than a direct read). The IDE's simple read-tracking doesn't follow that path.
- **SavedPlacePopup.kt** -- "Parameter `onDismiss` is never used". Dismissal is wired through the Mapbox annotation gesture instead of the lambda body. The parameter is kept as part of the function contract so callers can pass an explicit `onDismiss` if the dismissal mechanism changes.
- **OnBoardingScreen.kt** -- "Value of parameter `total` is always `OnBoardingScreen.PAGE_COUNT`". Kept as a parameter so the progress indicator stays decoupled from the global page count constant.
- **ResetAppDataSection.kt** -- "Assigned value is never read" for four `showDialog` state variables. Same shape as `MapScreen.kt` -- the values gate visibility through composition, not via direct reads.

### Lint resource warnings/errors
- **MissingDefaultResource** (error) -- `drawable-normal/safeproperty_logo_sharp.xml`, `drawable-normal/safeproperty_logo_v6.png`. The `-normal` qualifier matches the screen-size class of our target devices (phones). The app is not designed for `xlarge` tablets, so the missing fallback is intentional.
- **PluralsCandidate** (4) -- `months_count`, `years_count`, `days_per_year`, `max_saved_places_message`. The app always shows counts of two or more (e.g., "12 months", "30 years"), so the singular form never appears. Switching to `<plurals>` would add complexity without changing what users see.
- **Untranslatable** (4) -- `language_bokmal` / `language_nynorsk` are translated in `values-nb` and `values-nn` even though they're marked `translatable="false"` in `values/`. The values are the labels shown in the Settings language picker, and we want each option to render in its own language consistently.
- **UnusedResources** (~33) -- color tokens from the Compose template (`purple_200`, `teal_700`, etc.), API name strings, legend body strings, an unused sound file. Removing them piecemeal is risky because lint's translation completeness checks treat absence as `MissingTranslation`. A consolidated cleanup pass is a planned follow-up.
- **TypographyOther** (3) -- `average_annual_temperature` ("Average annual temperature (C)") and its translations. The `(C)` means Celsius, not copyright. We keep the parenthesised unit because the strings serve as chart labels where the unit needs to follow the metric name.
- **IconLocation** (3) -- `gb_flag.png`, `norway_flag.png`, `sami_flag.png` live in `res/drawable/`. They're rendered at a fixed small size in the Settings language picker, so density-specific variants would not improve quality.
- **VectorRaster** -- `safeproperty_logo_sharp.xml` declares a 400dp width. This is the main launcher/splash logo and is intentionally large; lint's 200dp guideline is for inline icons.
- **ButtonCase / Typos** -- `string/ok` is "Ok". The team-approved capitalisation matches the rest of the dialog vocabulary in the app.

### Lint Gradle warnings
- **AndroidGradlePluginVersion** (2) -- gradle 9.2.1 and AGP 9.0.1 are pinned to the versions recommended by the IN2000 course materials. Upgrading is deferred until course exit.
- **GradleDependency** (5) -- `datastore-preferences`, `core-ktx`, `activity-compose`, `compose-bom`, `navigation-compose` are pinned to versions verified in the course environment.
- **NewerVersionAvailable** (6) -- Ktor 3.4.0, accompanist-permissions 0.35.0-alpha, kotlinx-coroutines-test 1.10.2, kotlin compose plugin 2.0.21 are pinned likewise. Upgrades are deferred until course exit to avoid mid-project Kotlin/AGP churn.
- **UseTomlInstead** (~20) -- several Mapbox SDK modules, Ktor modules, Coil, accompanist, mockk, browser, appcompat dependencies are declared inline in `app/build.gradle.kts` instead of via `libs.versions.toml`. Migrating them to the version catalog is a planned follow-up; doing it during the course adds review noise without functional value.
- **UnstableApiUsage** (4, `settings.gradle.kts:18-19`) -- `RepositoriesMode.FAIL_ON_PROJECT_REPOS` is marked `@Incubating` in Gradle. This is the standard pattern Android Studio writes when you opt into centralised dependency resolution. We keep it because rolling back to per-project repositories would break the build.

## Note: emulator keyboard setup

For the search bar and other text inputs to work properly in the emulator,
you may need to change the following emulator settings:
- Go to `Settings` -> `System` -> `Keyboard` -> `On-screen keyboard` -> `Gboard`
- Tap `Physical keyboard` and turn ON the `Show on-screen keyboard` switch
- Go one step back (to Gboard's settings)
- Tap `Write in text fields` and turn OFF `Use stylus to write in text fields`

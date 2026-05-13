# Team 45 -- Naturhendelser

Android app for Case 3 (Naturhendelser) in IN2000 at UiO.
The app lets homeowners and cabin owners check climate risk (flood, landslide)
and climate statistics for their area.

## Features

The app has three main screens accessible from a bottom navigation bar:

### Map (left button)
- Mapbox map of Norway
- NVE flood-zone overlay rendered on top of the map
- The overlay updates dynamically as the user moves the map

### Climate statistics (right button)
- Search for a weather station by name
- Shows climate normals (30-year averages) for temperature, precipitation and wind
- Falls back to historical observations (2010-2024) when normals are not available

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

Navigation and lifecycle. Jetpack Navigation drives the bottom-bar routing between Map, Statistics and Saved Places screens. `lifecycle-runtime-ktx` adds Kotlin coroutine support to `ViewModel`s (`viewModelScope`).
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
- **NVE flood-zone service** -- flood-zone map as overlay
- **NGU** -- Radon information
- **MapBox** -- Geographical information (map and search suggestions)

## API-level 
The app is developed and tested on API level 36.1 (Android 16, “Baklava”) using a Medium Phone emulator.

We use compileSdk 36 and targetSdk 36, with a minimum SDK of minSdk 24 (Android 7.0, “Nougat”). minSdk 24 was chosen to support modern Android development with Jetpack Compose, Material 3, and coroutines, while still covering the majority of actively used devices. Lower API levels would require legacy solutions, while higher levels would exclude a significant portion of users without a major benefit. targetSdk 36 ensures the app follows the latest Android 16 behavior changes (such as predictive back, edge-to-edge rendering, notification permissions, and scoped storage) and meets Google Play requirements.
  
## Location Restriction Using GeoJSON Boundary Data
The application only allows saving locations within Norway, based on GeoJSON boundary data. This prevents users from saving areas without available underlying data and ensures valid and consistent risk assessments. Link to the repo used: [https://github.com/georgique/world-geojson](https://raw.githubusercontent.com/georgique/world-geojson/refs/heads/develop/countries/norway.json).

## Screen Loading Strategy / Performance Considerations
The application loads all screens in parallel at startup instead of using lazy loading. With lazy loading, screens would only be initialized when the user navigates to them, which could introduce noticeable delays and a less smooth user experience. Since the statistics and saved places screens are relatively lightweight and users are likely to switch between these and the map during a session, parallel loading was considered the most suitable approach to ensure a responsive and seamless user experience.

## Images
Circular flags used in the language sections of the settings screen were generated by AI. This choice was made after we found it difficult to find three similar circular Norwegian, British and Sami flags, that all had the same resolution, size and did not have any copyright text across it.

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

## Build and run

```bash
./gradlew assembleDebug
```

Requires:
- **Mapbox access token** in `app/src/main/res/values/mapbox_access_token.xml`
- **Frost client ID** in `app/src/main/res/values/frost_client_id.xml`
  - Register at https://frost.met.no/auth/requestCredentials.html

## Warnings from IDE
The project contains some warnings in the Android Studio IDE. These warnings do not affect functionality, but indicate parts of the code that could be cleaned up to improve readability and maintainability. The warning and the corresponding file are listed below:
- MapContent.kt: "Calling a Mapbox Map Composable function where a UI Composable composable was expected" Warning due to incorrect composable usage, where a Mapbox composable is used in a context expecting a standard UI composable, indicating a mismatch in composable scope. These warnings originate from Android Studio / Kotlin Compose analysis of Mapbox-specific composable scopes. They do not affect compilation or runtime behavior, and the application functions as expected.
- MapSavedPlacesEffect.kt: "Calling a UI Composable composable function where a Mapbox Map Composable was expected" Similar composable scope warning where a UI composable is called inside a Mapbox-specific composable context, which may lead to unexpected UI behavior. These warnings originate from Android Studio / Kotlin Compose analysis of Mapbox-specific composable scopes. They do not affect compilation or runtime behavior, and the application functions as expected.
- MapScreen.kt: "Assigned value is never read" Warning for multiple variables (showDialog, locationName, pendingPoint) that are assigned values but never read, indicating unused or incomplete UI state handling. 
- SavedPlacePopup.kt: "Parameter "onDismiss" is never used. (onDismiss is the contract" Warning that the onDismiss parameter is never directly used in the function body; dismissal is instead handled through map interaction, but the parameter is kept to allow external control of popup lifecycle.
- OnBoardingScreen.kt: "Value of parameter "total" is always "OnBoardingScrenn.kt.PAGE_COUNT"" Warning that the total parameter is always equal to OnBoardingScreen.PAGE_COUNT, making it redundant and unnecessary as a parameter. 
- ResetAppDataSection.kt: "Assigned value is never read (four are showDialog)" Warning that multiple showDialog state variables are assigned but never read, indicating unused or incomplete dialog state management in the UI logic.

## Note: emulator keyboard setup

For the search bar and other text inputs to work properly in the emulator,
you may need to change the following emulator settings:
- Go to `Settings` -> `System` -> `Keyboard` -> `On-screen keyboard` -> `Gboard`
- Tap `Physical keyboard` and turn ON the `Show on-screen keyboard` switch
- Go one step back (to Gboard's settings)
- Tap `Write in text fields` and turn OFF `Use stylus to write in text fields`

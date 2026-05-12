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

UI / Compose
- `androidx.compose:compose-bom`
- `androidx.compose.ui`, `ui-graphics`, `ui-tooling-preview`
- `androidx.compose.material3`
- `androidx.compose.material:material-icons-extended`
- `androidx.activity:activity-compose`
- `androidx.appcompat:appcompat` 1.7.1

Navigation and lifecycle
- `androidx.navigation:navigation-compose`
- `androidx.navigation:navigation-runtime-ktx`
- `androidx.lifecycle:lifecycle-runtime-ktx`

Networking
- `io.ktor:ktor-client-core` 3.4.0
- `io.ktor:ktor-client-cio` 3.4.0
- `io.ktor:ktor-client-content-negotiation` 3.4.0
- `io.ktor:ktor-serialization-kotlinx-json` 3.4.0

Map and search
- `com.mapbox.maps:android-ndk27` 11.20.1
- `com.mapbox.extension:maps-compose-ndk27` 11.20.1
- `com.mapbox.mapboxsdk:mapbox-sdk-turf` 7.10.0
- `com.mapbox.search:autofill-ndk27` 2.21.0
- `com.mapbox.search:discover-ndk27` 2.21.0
- `com.mapbox.search:place-autocomplete-ndk27` 2.21.0
- `com.mapbox.search:offline-ndk27` 2.21.0
- `com.mapbox.search:mapbox-search-android-ndk27` 2.21.0
- `com.mapbox.search:mapbox-search-android-ui-ndk27` 2.21.0

Image loading
- `io.coil-kt:coil-compose` 2.7.0

Persistence
- `androidx.datastore:datastore-preferences` 1.1.7

Permissions
- `com.google.accompanist:accompanist-permissions` 0.35.0-alpha

Browser (url launching)
- `androidx.browser:browser` 1.10.0

Testing
- `junit` 4.13.2
- `io.mockk:mockk` 1.14.9
- `org.jetbrains.kotlinx:kotlinx-coroutines-test` 1.10.2
- `androidx.test.ext:junit`
- `androidx.test.espresso:espresso-core`
- `androidx.compose.ui:ui-test-junit4`

## Dependency injection

We do not use Hilt or Dagger. Dependencies are wired manually:

- `MainActivity` creates the long-lived repositories
  (`ThemePreferenceRepository`, `LanguagePreferenceRepository`,
  `MapCameraRepository`, `SoundPreferenceRepository`, ...).
- Those repositories are passed to a `*ViewModelFactory`
  (`MapViewModelFactory`, `SoundViewModelFactory`) that constructs the
  matching ViewModel.
- `viewModel(factory = ...)` inside the Composable obtains the
  instance, scoped to the `NavBackStackEntry` or Activity.

ViewModels with no expensive dependencies use `AndroidViewModel` with
default constructor arguments. `StatisticViewModel` uses `@JvmOverloads`
so the default `AndroidViewModelFactory` finds the single-arg
`Application` constructor.

We picked manual DI because the dependency graph is small. Hilt would
add an annotation processor and Kapt config without payoff at this
scale.

## APIs

- **MET Frost API v0** -- climate data (stations, normals, observations)
- **NVE flood-zone service** -- flood-zone map as overlay
- **NGU** -- Radon information
- **MapBox** -- Geographical information (map and search suggestions)

## API-level 
We use compileSdk 36 and targetSdk 36 (Android 16.0 “Baklava”, API level 36.1). This means the app is built and tested against the latest Android version, ensuring compatibility with current system behavior, security updates, and API changes. 
The app is tested on a Medium Phone emulator running Android 16.0 (API level 36.1, arm64) to ensure stability on newer Android versions.
  
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

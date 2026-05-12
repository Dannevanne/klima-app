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

- Kotlin + Jetpack Compose + Material 3
- Mapbox SDK for the map
- Ktor (CIO) + kotlinx.serialization for network calls
- MVVM architecture with datasource / repository / view-model layers

## APIs

- **MET Frost API v0** -- climate data (stations, normals, observations)
- **NVE flood-zone service** -- flood-zone map as overlay
- **NGU** -- Radon information
- **MapBox** -- Geographical information (map and search suggestions)

## API-level 
We use compileSdk 36 and targetSdk 36 (Android 16.0 “Baklava”, API level 36.1). This means the app is built and tested against the latest Android version, ensuring compatibility with current system behavior, security updates, and API changes. 
The app is tested on a Medium Phone emulator running Android 16.0 (API level 36.1, arm64) to ensure stability on newer Android versions.
  
## Location Restriction Using GeoJSON Boundary Data
The application only allows saving locations within Norway, based on GeoJSON boundary data. This prevents users from saving areas without available underlying data and ensures valid and consistent risk assessments. Link to the repo used: https://github.com/georgique/world-geojson

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

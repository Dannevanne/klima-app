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

## Note: emulator keyboard setup

For the search bar and other text inputs to work properly in the emulator,
you may need to change the following emulator settings:
- Go to `Settings` -> `System` -> `Keyboard` -> `On-screen keyboard` -> `Gboard`
- Tap `Physical keyboard` and turn ON the `Show on-screen keyboard` switch
- Go one step back (to Gboard's settings)
- Tap `Write in text fields` and turn OFF `Use stylus to write in text fields`

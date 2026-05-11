## 1. Use Case Diagram
 
**Choice and rationale:** The use case diagram provides a high-level overview of the functions the system offers and the actors involved. The six use cases shown represent the core functional requirements of the app. `«include»` is used where one use case *always* requires another, and `«extend»` where a use case is optional or conditional.

```mermaid
flowchart LR
    User(["👤 User"])

    UC1(["Show map with overlay"])
    UC2(["Search for place"])
    UC3(["Save place"])
    UC4(["View risk overview for saved place"])
    UC5(["Show measures and\nrecommendations"])
    UC6(["Explore historical\nclimate statistics"])

    User --- UC1
    User --- UC2
    User --- UC3
    User --- UC4
    User --- UC5
    User --- UC6

    %% <<extend>>
    UC3 -.->|«extend»| UC2

    %% <<include>>
    UC5 -.->|«include»| UC4

```

**`«extend»` explained:**
- `Save place` extends `Search for place` — the user can optionally search for a place before saving it, but may also long-press directly on the map.

**`«include»` explained:**
- `Show measures and recommendations` always requires `View risko verview for saved place` — relevant recommendations are always derived from the identified risk level and will show default without it.
## 2. Textual Description of the Most Important Use Cases

### UC1 – Show Map with Overlay
 
| Field | Content |
|---|---|
| **System** | Climate Risk App – (map functionallity) |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External map provider (Mapbox), external hazard data provider (NVE/NGU)|
| **Goal** | View natural hazards for a geographic area visualised as colour-coded map overlays |
| **Stakeholders** | User wants clear and understandable risk visualization. NVE/NGU want correct representation of their data|
| **Preconditions** | App is installed and open and device has network access |
| **Trigger** | User opens the app or selects a map overlay from the overlay menu |
| **Basic flow** | 1. User opens the app. 2. Map loads with the last used overlay. 3. User selects a map overlay (e.g. flood). 4. Zoom level is adjusted to the overlay's valid range . 5. Overlay tiles are fetched from NVE/NGU and rendered on the map. 6. User reads the colour-code legend to interpret risk levels. |
| **Alternate flow 1** | API unavailable → error message is shown and map is displayed without the overlay. |
| **Alternate flow 2** | User switches to a different overlay → zoom is re-adjusted and new tiles are fetched. |
| **Alternate flow 3** | User deactivates the overlay → map returns to base layer with no risk data shown. |

---

### UC3 / UC4 – Save Place and Risk Overview
 
| Field | Content |
|---|---|
| **System** | Climate Risk App – MapScreen and SavedPlacesScreen |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External map provider (Mapbox), external hazard data provider (NVE/NGU) |
| **Goal** | Save a property to a personal list and view a combined climate risk overview with recommended measures |
| **Stakeholders** | User wants a persistent, personalised risk overview for their own properties |
| **Preconditions** | App is open on MapScreen and device has network access |
| **Trigger** | User long-presses a point on the map |
| **Basic flow** | 1. User long-presses a location on the map. 2. A popup appears and. 3. The location is saved locally with coordinates. 4. User navigates to SavedPlacesScreen. 5. System loads saved places. 6. Risk data is fetched from NVE/NGU per coordinate and displayed with colour coding and a textual risk level. 7. Recommended measures and relevant external resources are presented based on the identified risk level. |
| **Alternate flow 1** | No network access -> last known risk data is shown from cache |
| **Alternate flow 2** | User optionally searches for an address first (UC2 extends UC3) before long-pressing to save the exact location. |
| **Alternate flow 3** | User deletes a saved place -> it is removed from local storage and disappears from SavedPlacesScreen. |
 
---

### UC6 – Explore Historical Climate Statistics
 
| Field | Content |
|---|---|
| **System** | Climate Risk App – StatisticScreen |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External climate data provider (MET Frost) |
| **Goal** | Explore aggregated historical climate data for a chosen place, presented as graphs over time |
| **Stakeholders** | User wants to understand long-term climate trends for a location |
| **Preconditions** | App is open and device has network access |
| **Trigger** | User searches for a place |
| **Basic flow** | 1. User opens the statistics feature. 2. System displays a search field and recent searches. 3. User enters a place name and submits the search. 4. System saves the search in recent search history. 5. System shows a loading indicator. 6. System requests historical climate data from the external data provider. 7. System receives aggregated data (e.g. temperature, precipitation, wind, snow depth). 8. System displays the data as graphs over time.|
| **Alternate flow 1** | Search field is empty -> system does not perform a search and keeps the input active. |
| **Alternate flow 2** | Incomplete data is returned for the query -> system shows an error message and shows what it got. |
| **Alternate flow 3** | User resets the view → system clears displayed data but retains the search history and returns to the initial state|
 
---

## 3. Sequence Diagrams

To improve clarity, the sequence diagrams use a simplified architectural view consisting of UI, ViewModel, Repository, and external APIs. The Repository layer encapsulates access to both remote data sources and local persistence, and lower-level components such as DataSources are intentionally omitted

### 3.1 Show Map with Overlay (UC1)
 
**Choice and rationale:** Shows the asynchronous flow between the user, UI layer, ViewModel, Repository (including local storage) and external APIs when selecting a map overlay. Illustrates how the MVVM pattern handles network calls and state updates, and how zoom is constrained per overlay type.
 
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant ViewModel
    participant Repository
    participant Mapbox
    participant NVE_NGU as NVE / NGU API
 
    Note over User, Mapbox: App launch
    User->>UI: Opens app
    ViewModel->>Repository: Fetch last saved camera position
    Repository-->>ViewModel: Camera position restored
    UI->>Mapbox: Load map with last camera position
    Mapbox-->>UI: Base map rendered
 
    Note over User, NVE_NGU: User selects overlay
    User->>UI: Selects overlay (e.g. Flood)
    UI->>ViewModel: setOverlay(FLOOD)
    ViewModel->>ViewModel: Adjusts zoom to overlay's valid range
    ViewModel->>Repository: Request overlay data
    Repository->>NVE_NGU: Fetch overlay tiles
    NVE_NGU-->>Repository: Overlay data returned
    Repository-->>ViewModel: Overlay data
    ViewModel-->>UI: Update state with overlay
    UI->>Mapbox: Display overlay on map
    Mapbox-->>User: Map shown with colour-coded risk overlay
```
 
---

### 3.2 Save Place and View Risk Overview (UC3 / UC4)
 
**Choice and rationale:** Shows the long-press flow from user interaction to local storage and retrieval of risk data, including interaction between the Saved Places feature and external hazard data APIs. UC5 has a `«include»` relation to UC4, but is not taken into account here to simplify the diagram.
 
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant ViewModel
    participant Repository
    participant NVE_NGU as NVE / NGU API
 
    Note over User, NVE_NGU: User saves a place
    User->>UI: Long-press on map
    UI->>ViewModel: addLocation(lat, lng, label)
    ViewModel->>Repository: addLocation(SavedPlacesLocation)
    Repository-->>ViewModel: Saved OK (DataStore)
    ViewModel-->>UI: Show confirmation dialog
 
    Note over User, NVE_NGU: User views risk overview
    User->>UI: Navigates to Saved Places
    UI->>ViewModel: fetchStatusForLocation(location)
    ViewModel->>Repository: Get saved locations
    Repository-->>ViewModel: List of SavedPlacesLocation
    ViewModel->>Repository: fetching hazard data
    Repository->>NVE_NGU: WMS/GeoJSON identify requests per hazard type
    NVE_NGU-->>Repository: Hazard zone data per category
    Repository-->>ViewModel: Hazard results
    ViewModel->>ViewModel: Calculate risk level (High / Moderate / Low)
    ViewModel-->>UI: Update UI with risk overview
    UI-->>User: Display risk levels and recommended measures
```
 
---

### 3.3 Explore Historical Climate Statistics (UC6)
 
**Choice and rationale:** Illustrates the search and retrieval flow for historical climate statistics, including fallback geocoding when no matching weather stations are found, and how climate data is aggregated and presented through the MVVM architecture.
 
```mermaid
sequenceDiagram
    actor User
    participant UI
    participant ViewModel
    participant Repository
    participant Frost as Frost API (MET)
    participant Mapbox
 
    Note over User, Frost: User searches for a place
    User->>UI: Types place name and submits search
    UI->>ViewModel: searchStations(query)
    ViewModel->>Repository: searchStations(query, clientId)
    Repository->>Frost: searchStations(query, clientId)
    Frost-->>Repository: Station results
    Repository-->>ViewModel: List of WeatherStation
 
    alt No stations found
        ViewModel->>Repository: geocodePlaces(query, mapboxToken)
        Repository->>Mapbox: geocode(query)
        Mapbox-->>Repository: Place coordinates
        Repository-->>ViewModel: List of Place
    end
 
    ViewModel->>Repository: addSearch(query)
    ViewModel-->>UI: uiState = PlaceResults / StationResults
    UI-->>User: Display list of matching places or stations
 
    Note over User, Frost: User selects a place
    User->>UI: Selects a place from results
    UI->>ViewModel: selectPlace(place)
    ViewModel->>Repository: findNearestStations(lat, lon, clientId)
    Repository->>Frost: findNearestStations(lat, lon)
    Frost-->>Repository: Nearby stations
    Repository->>Frost: getAvailableTimeSeries(stationIds, elements)
    Frost-->>Repository: Station availability data
    Repository-->>ViewModel: StationAssignment (best stations per element)
    ViewModel->>Repository: getAnnualObservations(stationId, clientId, yearFrom, yearTo)
    ViewModel->>Repository: getClimateStats(stationId, clientId)
    Repository->>Frost: Fetch observations per station
    Frost-->>Repository: Climate data
    Repository-->>ViewModel: Aggregated climate statistics
    ViewModel-->>UI: uiState = ClimateData(station, stats, annualData)
    UI-->>User: Display climate graphs over time
```

---
## 4. Class Diagram
 
**Choice and rationale:** The class diagram reflects the MVVM architecture with manual factory classes and the Repository pattern. It shows the central classes from the three most important use cases and their dependencies. Only the most central classes are included to keep the diagram readable.
 
```mermaid
classDiagram
    class MapViewModel {
        -mapRepository: MapRepository
        -cameraRepository: MapCameraRepository
        -activeOverlay: OverlayType
        +setOverlay(type: OverlayType, context: Context)
        +updateOverlay(bbox: List~Double~, width: Int, height: Int, context: Context)
        +saveCamera(center: Point, zoom: Double)
    }
 
    class MapCameraRepository {
        -dataStore: DataStore
        +cameraFlow: Flow~Triple~Double, Double, Double~~
        +saveCamera(lat: Double, lng: Double, zoom: Double)
    }
 
    class MapRepository {
        -mapDatasource: MapDatasource
        +getOverlayURL(type: OverlayType, bbox: List~Double~, width: Int, height: Int): String
        +isInFloodZone(lat: Double, lon: Double): Boolean?
        +isInLandslideZone(lat: Double, lon: Double): Boolean?
        +isInAvalancheZone(lat: Double, lon: Double): Boolean?
        +getQuickClayHazard(lat: Double, lon: Double): String?
        +getRadonHazard(lat: Double, lon: Double): String?
    }
 
    class MapDatasource {
        +buildOverlayURL(type: OverlayType, bbox: List~Double~, width: Int, height: Int): String
        +fetchNveIdentifyBody(service: String, layers: String, lat: Double, lon: Double): String?
        +fetchRadonHazardBody(lat: Double, lon: Double): String?
        +fetchQuickClayHazardBody(lat: Double, lon: Double): String?
    }
 
    class OverlayType {
        <<enumeration>>
        NONE
        FLOOD
        QUICK_CLAY
        RADON
        WIND
        LANDSLIDE
        AVALANCHE
        +minZoom: Double
        +maxZoom: Double
    }
 
    class SavedPlacesViewModel {
        -repository: SavedPlacesRepository
        -mapRepository: MapRepository
        +locations: StateFlow~List~SavedPlacesLocation~~
        +locationStatuses: Map~String, LocationStatus~
        +addLocation(lat: Double, lng: Double, label: String): Boolean
        +removeLocation(location: SavedPlacesLocation)
        +fetchStatusForLocation(location: SavedPlacesLocation)
        +removeAllSavedPlaces()
    }
 
    class SavedPlacesRepository {
        -dataStore: DataStore
        +savedPlacesLocationsFlow: Flow~List~SavedPlacesLocation~~
        +addLocation(location: SavedPlacesLocation)
        +removeLocation(location: SavedPlacesLocation)
        +removeAll()
    }
 
    class SavedPlacesLocation {
        <<data class>>
        +latitude: Double
        +longitude: Double
        +label: String
    }
 
    class LocationStatus {
        <<data class>>
        +floodLevel: String
        +landslideLevel: String
        +avalancheLevel: String
        +quickClayLevel: String
        +radonLevel: String
        +temperature: Double?
        +windSpeed: Double?
        +precipitation: Double?
    }
 
    class StatisticViewModel {
        -repository: StatisticRepository
        -recentSearchRepository: RecentSearchRepository
        +uiState: StatisticUiState
        +searchStations()
        +selectPlace(place: Place)
        +selectStation(station: WeatherStation)
        +resetAllStatisticData()
    }
 
    class StatisticRepository {
        +searchStations(query: String, clientId: String): List~WeatherStation~
        +findNearestStations(lat: Double, lon: Double, clientId: String, count: Int): List~WeatherStation~
        +getAnnualObservations(stationId: String, clientId: String, yearFrom: Int, yearTo: Int): List~AnnualObservation~
        +getClimateStats(stationId: String, clientId: String): List~MonthlyClimateStats~
        +geocodePlaces(query: String, mapboxToken: String): List~Place~
    }
 
    class RecentSearchRepository {
        -dataStore: DataStore
        +recentSearchesFlow: Flow~List~String~~
        +addSearch(query: String)
        +clearHistory()
    }
 
    class WeatherStation {
        <<data class>>
        +id: String
        +name: String
        +latitude: Double
        +longitude: Double
        +municipality: String
        +county: String
    }
 
    class Place {
        <<data class>>
        +name: String
        +subtitle: String
        +latitude: Double
        +longitude: Double
        +stations: List~WeatherStation~
    }
 
    class AnnualObservation {
        <<data class>>
        +year: Int
        +meanTemp: Double?
        +totalPrecip: Double?
        +maxWindGust: Double?
        +maxSnowDepth: Double?
    }
 
    class MonthlyClimateStats {
        <<data class>>
        +month: Int
        +meanTemp: Double?
        +precipitation: Double?
        +windSpeed: Double?
        +snowDepth: Double?
    }
 
    MapViewModel --> MapRepository : uses
    MapViewModel --> MapCameraRepository : uses
    MapViewModel --> OverlayType : uses
    MapRepository --> MapDatasource : uses
    MapRepository --> OverlayType : uses
    SavedPlacesViewModel --> SavedPlacesRepository : uses
    SavedPlacesViewModel --> MapRepository : uses
    SavedPlacesViewModel --> LocationStatus : produces
    SavedPlacesViewModel --> SavedPlacesLocation : uses
    SavedPlacesRepository --> SavedPlacesLocation : stores
    StatisticViewModel --> StatisticRepository : uses
    StatisticViewModel --> RecentSearchRepository : uses
    StatisticRepository --> WeatherStation : returns
    StatisticRepository --> Place : returns
    StatisticRepository --> AnnualObservation : returns
    StatisticRepository --> MonthlyClimateStats : returns
    Place --> WeatherStation : contains
```
 
---

## 5. Activity Diagram – Search and View Climate Statistics (UC6)
 
**Choice and rationale:** The activity diagram shows the step-by-step flow when the user searches for a place in StatisticScreen, including decision points for empty searches, missing results and API failures. This perspective complements the sequence diagram by highlighting the business-level flow and branching logic rather than the technical implementation.
 
```mermaid
flowchart TD
    Start(( )) --> ShowSearch[Show search field and history]
    ShowSearch --> TypeSearch[User types place name and submits]
    TypeSearch --> EmptyCheck{Search empty?}
    EmptyCheck -- No --> SearchStations[Search for matching stations]
    EmptyCheck -- Yes --> TypeSearch
 
    SearchStations --> StationsFound{Stations found?}
    StationsFound -- Yes --> ShowResults[Show list of matching places]
    StationsFound -- No --> Geocode[Geocode place name via Mapbox]
 
    Geocode --> GeoFound{Place found?}
    GeoFound -- Yes --> ShowResults
    GeoFound -- No --> ShowError[Show error message]
    ShowError --> ShowSearch
 
    ShowResults --> SelectPlace[User selects a place]
    SelectPlace --> FetchClimate[Fetch climate data from Frost API]
    FetchClimate --> FetchOK{Data available?}
    FetchOK -- No --> ShowError
    FetchOK -- Yes --> ShowGraphs[Display climate graphs over time]
 
    ShowGraphs --> UserAction{User action}
    UserAction -- New search --> ShowSearch
    UserAction -- Reset --> ClearData[Clear data and search history]
    UserAction -- Exit --> End((( )))
    ClearData --> ShowSearch
```
 
---

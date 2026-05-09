## 2. Textual Description of the Most Important Use Cases

### UC1 – Show Map with Overlay
 
| Field | Content |
|---|---|
| **System** | Climate Risk App – (map functionallity) |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External map provider (Mapbox), external hazard data provider (NVE/NGU)|
| **Goal** | View natural hazards for a geographic area visualised as colour-coded map overlays |
| **Stakeholders** | User wants clear and understandable risk visualization. NVE/NGU want correct representation of their data|
| **Preconditions** | App is installed and open; device has network access |
| **Trigger** | User opens the app or selects a map overlay from the overlay menu |
| **Basic flow** | 1. User opens the app. 2. Map loads with the last used overlay. 3. User selects a map overlay (e.g. flood). 4. Zoom level is adjusted to the overlay's valid range . 5. Overlay tiles are fetched from NVE/NGU and rendered on the map. 6. User reads the colour-code legend to interpret risk levels. |
| **Alternate flow 1** | API unavailable → error message is shown; map is displayed without the overlay. |
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
| **Preconditions** | App is open on MapScreen; device has network access |
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
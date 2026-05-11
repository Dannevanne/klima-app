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
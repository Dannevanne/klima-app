## 2. Textual Description of the Most Important Use Cases

### UC1 – Show Map with Overlay
 
| Field | Content |
|---|---|
| **System** | Climate Risk App – (map functionallity) |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External map provider (Mapbox), external hazard data provider (NVE/NGU)|
| **Goal** | View natural hazards for a geographic area visualised as colour-coded map overlays |
| **Stakeholders** | User wants clear and understandable risk visualization. NVE/NGU want correct representation of their data|
| **Preconditions** | App is open and device has network access |
| **Trigger** | User opens the app or selects a map overlay from the overlay menu |
| **Basic flow** | 1. User opens the app. 2. Map loads with the last used overlay. 3. User selects a map overlay (e.g. flood). 4. Zoom level is adjusted to the overlay's valid range . 5. System retrieves overlay data from the external data provider |
| **Alternate flow 1** | API unavailable -> error message is shown; map is displayed without the overlay |
| **Alternate flow 2** | User switches to a different overlay -> zoom is re-adjusted and new tiles are fetched |
| **Alternate flow 3** | User deactivates the overlay -> map returns to base layer with no risk data shown |

---

### UC3 / UC4 – Save Place and Risk Overview
 
| Field | Content |
|---|---|
| **System** | Climate Risk App (map and saved places functionality) |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External map provider (Mapbox), external hazard data provider (NVE/NGU) |
| **Goal** | Save a place and view a risk overview|
| **Stakeholders** | User wants a persistent, personalised risk overview for their own properties |
| **Preconditions** | App is open and device has network access |
| **Trigger** | User selects a location on the map (e.g. long-press) |
| **Basic flow** | 1. User selects a location on the map. 2. System displays information about the selected location. 3. User chooses to save the location. 4. System saves the location. 5. User navigates to saved places. 6. System displays the list of saved places. 7. User selects a saved place. 8. System retrieves risk data for the selected place from the external data provider. 9. System displays risk levels using colour coding and textual descriptions |
| **Alternate flow 1** |No network access -> system shows last available risk data if available|
| **Alternate flow 2** | User searches for a place before selecting a location -> user selects searched location to save |
| **Alternate flow 3** | User deletes a saved place -> system removes the place from the saved list and updates the view |
 
---

### UC6 – Explore Historical Climate Statistics
 
| Field | Content |
|---|---|
| **System** | Climate Risk App (statistics functionality) |
| **Primary actor** | User (home or cabin owner) |
| **Secondary actor** | External climate data provider (MET Frost) |
| **Goal** | Explore aggregated historical climate data for a chosen place, presented as graphs over time |
| **Stakeholders** | User wants to understand long-term climate trends for a location |
| **Preconditions** | App is open and device has network access |
| **Trigger** | User searches for a place |
| **Basic flow** | 1. User opens the statistics feature. 2. System displays a search field and recent searches. 3. User enters a place name and submits the search. 4. System saves the search in recent search history. 5. System shows a loading indicator. 6. System requests historical climate data from the external data provider. 7. System receives aggregated data (e.g. temperature, precipitation, wind, snow depth). 8. System displays the data as graphs over time|
| **Alternate flow 1** | Search field is empty -> system does not perform a search and keeps the input active |
| **Alternate flow 2** | Incomplete data is returned for the query -> system shows an error message and shows what it got |
| **Alternate flow 3** | User resets the view -> system clears displayed data but retains the search history and returns to the initial state|
 
---
```mermaid
flowchart LR
    User(["👤 User"])

    UC1(["Show map with overlay"])
    UC2(["Search for place"])
    UC3(["Save place"])
    UC4(["Show climatestatistics\nfor saved place"])
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
    UC5 -.->|«include»| UC4

```
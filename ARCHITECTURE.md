# Architecture - Team 45: Naturhendelser
This document describes the architecture of the Naturhendelser app, developed for case 3 in IN2000. It is intended for people who will work with the codebase, add features, or maintain it over time. It gives an overview of the project structure, the architectural decisions, and the patterns we follow.

The reader is assumed to be familiar with Kotlin and basic Android development. For the library list and how dependency injection is wired, see [README.md](README.md). For use cases, sequence diagrams and the class diagram, see [MODELING.md](MODELING.md). For git and commit rules, see [CONTRIBUTE.md](CONTRIBUTE.md).

## Technologies and dependencies
### Programming language and UI framework
The application is written in Kotlin, Google's recommended language for Android development. Kotlin provides more concise and safer code than Java through features such as null safety, extension functions, and first-class coroutine support.
The user interface is built using Jetpack Compose, Google's modern declarative UI framework. With Compose, the UI is defined through functions ("Composables") that automatically recompose when the underlying state changes. 

## Libraries
...

## Android API-level
We use `compileSdk` 36 and `targetSdk` 36 with `minSdk` 24 (Android 7.0, "Nougat").

`minSdk` 24 was picked because it gives us a modern Compose, Material 3 and coroutines stack without backports while still reaching the vast majority of phones in active use. Going lower would force a fragment-based UI. Going higher (26 or 30) would cut off older devices common in our user group (homeowners and cabin owners) without a clear gain.

`targetSdk` 36 matches the current Google Play submission requirement and gives us the newest platform defaults (predictive back, edge-to-edge, notification permissions, scoped storage). The app is tested on a Medium Phone emulator running Android 16.0 (API level 36.1, arm64).

## Layered structure
The application follows Google's recommended layered architecture with two main layers:
  - UI layer - handles everything the user sees and interacts with
  - Data layer - manages all data handling, fetching, and storage
    
Each layer has clearly defined responsibilities (Separation of Concerns) and communicates only downwards in the hierarchy. The UI layer depends on the data layer, but never the other way around.

### UI layer
The UI layer consists of two parts: Composable functions (UI elements) and ViewModels (state holders), which communicate through Unidirectional Data Flow (UDF).

Composables: 
All screens are implemented as Composable functions. A Composable is a stateless function that describes the UI based on the state it receives. When the state changes, Jetpack Compose automatically recomposes only the affected parts of the UI tree, resulting in efficient rendering.
Where possible, Composables are designed to be stateless. They receive data as parameters and propagate events upward through lambda functions, keeping a clear separation between UI and state management.

ViewModel and State: 
Each screen has a corresponding ViewModel that holds and manages UI-related state. The ViewModel survives configuration changes such as screen rotation, unlike Composables, ensuring that state is preserved during lifecycle events.

### Data layer
The data layer is responsible for fetching, storing, and providing data to the UI layer. It consists of two sublayers: DataSources and Repositories.

DataSource: 

Repository: 


## MVVM
The application implements the Model-View-ViewModel (MVVM) architecture, which is Google's recommended architectural pattern for Android development.
- Model: Repository, DataSource, and data classes
- View: The visual presentation layer, including Composable functions
- ViewModel: ViewModel classes using StateFlow and UI state management

## Object-oriented principles

### Low coupling
...

### High cohesion
...

### Single source of truth
...

## Project structure
(diagram here)
..

## Guidelines for further development
...



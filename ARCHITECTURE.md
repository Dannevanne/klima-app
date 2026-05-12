# Architecture - Team 45: SafeProperty
This document describes the architecture used in SafeProperty, an Android application developed in the course IN2000. The document is intended for people who will work with the codebase, further develop the application, or maintain it over time. It provides an overview of the project structure, the architectural decisions that were made, and the principles and patterns that were followed.
The document assumes familiarity with Kotlin and basic Android development, but explains all key decisions so that new developers and future maintainers can quickly become familiar with the project.

## Technologies and Dependencies
### Programming Language and UI Framework
The application is written in Kotlin, Google’s recommended language for Android development. Kotlin provides more concise and safer code than Java through features such as null safety, extension functions, and first-class coroutine support.
The user interface is built using Jetpack Compose, Google’s modern declarative UI framework. With Compose, the UI is defined through functions (“Composables”) that automatically recompose when the underlying state changes. 

## Libraries
...

## Android API-level
The application requires a minimum API level of 36 (Android 16.0 “Baklava”). This API level was chosen to ensure access to modern Android functionality and APIs used throughout the project, including improved UI handling, performance optimizations, updated security mechanisms, and better support for contemporary development tools and libraries. Using a recent API level also simplifies development by reducing the need for backward compatibility handling and legacy support.

We use compileSdk 36 and targetSdk 36 (Android 16.0 “Baklava”, API level 36.1), meaning the app is built and tested against the latest Android version to ensure compatibility with current system behavior, security updates, and API changes.

The application is tested on a Medium Phone emulator running Android 16.0 (API level 36.1, arm64) to ensure stability on newer Android versions.

## Architecture – Layered Structure
The application follows Google’s recommended layered architecture with two main layers:
  - UI layer – handles everything the user sees and interacts with
  - Data layer – manages all data handling, fetching, and storage
    
Each layer has clearly defined responsibilities (Separation of Concerns) and communicates only downwards in the hierarchy. The UI layer depends on the data layer, but never the other way around.

# UI-layer
The UI layer consists of two parts: Composable functions (UI elements) and ViewModels (state holders), which communicate through Unidirectional Data Flow (UDF).

Composables: 
All screens are implemented as Composable functions. A Composable is a stateless function that describes the UI based on the state it receives. When the state changes, Jetpack Compose automatically recomposes only the affected parts of the UI tree, resulting in efficient rendering.
Where possible, Composables are designed to be stateless. They receive data as parameters and propagate events upward through lambda functions, keeping a clear separation between UI and state management.

ViewModel and State: 
Each screen has a corresponding ViewModel that holds and manages UI-related state. The ViewModel survives configuration changes such as screen rotation, unlike Composables, ensuring that state is preserved during lifecycle events.

# Data-layer
The data layer is responsible for fetching, storing, and providing data to the UI layer. It consists of two sublayers: DataSources and Repositories.

DataSource: 

Repository: 


## Designpattern: MVVM
The application implements the Model–View–ViewModel (MVVM) architecture, which is Google’s recommended architectural pattern for Android development.
- Model: Repository, DataSource, and data classes
- View: The visual presentation layer, including Composable functions
- ViewModel: ViewModel classes using StateFlow and UI state management

## Object-Oriented Principles

# Low Coupling
...

# High Cohesion
...

# Single Source of Truth
...

## Project Structure
(diagram here)
..

## Guidelines for Further Development
...



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

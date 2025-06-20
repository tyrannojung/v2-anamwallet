# ANAM Wallet V2

Modern Android wallet application built with Clean Architecture, MVVM/MVI patterns, and Jetpack Compose.

## 🏗️ Architecture Overview

This project follows **Clean Architecture** principles with a **multi-module** structure, ensuring separation of concerns, testability, and scalability.

### Module Structure

```
v2-anamwallet/
├── app/                          # Main application module
├── core/
│   └── ui/                       # Shared UI components and theme
└── feature/                      # Feature modules
    ├── main/
    ├── hub/
    ├── browser/
    ├── identity/
    └── settings/
```

## 📁 Standard Feature Module Structure

Each feature module follows a consistent Clean Architecture structure:

```
feature/{name}/
├── ui/                              # Presentation Layer
│   ├── {Name}Screen.kt              # Compose UI
│   ├── {Name}ViewModel.kt           # State management
│   ├── {Name}Contract.kt            # MVI Contract (State, Intent, Effect)
│   └── components/                  # Reusable UI components
│
├── domain/                          # Business Layer
│   ├── model/                       # Business models
│   ├── repository/                  # Repository interfaces
│   └── usecase/                     # Business logic
│
├── data/                            # Data Layer
│   └── repository/                  # Repository implementations
│
└── di/                              # Dependency Injection
    └── {Name}Module.kt
```

## 🔄 Architecture Flow

### Unidirectional Data Flow

```
┌─────────────────┐
│   User Action   │
└────────┬────────┘
         ▼
┌─────────────────┐     ┌──────────────┐
│   UI (Screen)   │────▶│  Contract    │
└────────┬────────┘     │  - State     │
         │              │  - Intent    │
         ▼              │  - Effect    │
┌─────────────────┐     └──────────────┘
│   ViewModel    │
└────────┬────────┘
         ▼
┌─────────────────┐
│    UseCase      │ (Business Logic)
└────────┬────────┘
         ▼
┌─────────────────┐
│   Repository    │ (Interface)
│   (Domain)      │
└────────┬────────┘
         ▼
┌─────────────────┐
│ Repository Impl │ (Implementation)
│    (Data)       │
└────────┬────────┘
         ▼
┌─────────────────┐
│   Data Source   │ (Local/Remote)
└─────────────────┘
```

## 🎯 Key Architectural Patterns

### 1. MVI-lite Pattern

Combines MVVM simplicity with MVI's unidirectional data flow:

- **State**: Single immutable state object per screen
- **Intent**: User actions as sealed classes/interfaces
- **Effect**: One-time events (navigation, toasts, etc.)

### 2. Clean Architecture Layers

- **UI Layer**: Compose screens and ViewModels
- **Domain Layer**: Business logic (UseCases) and repository interfaces
- **Data Layer**: Repository implementations and data sources

### 3. Multi-Module Benefits

- **Parallel Development**: Teams can work on different features
- **Faster Builds**: Only changed modules rebuild
- **Clear Boundaries**: Enforced separation of concerns
- **Reusability**: Features can be shared across apps

### 4. Dependency Rule

Dependencies only point inward:

- UI → Domain ← Data
- Domain has no dependencies on UI or Data layers

## Real-time Language System

The app supports instant language switching without Activity restart using CompositionLocal:

```kotlin
// Define language provider
val LocalLanguage = compositionLocalOf { Language.KOREAN }
val LocalStrings = staticCompositionLocalOf { Strings() }

// Use in any Composable
val strings = LocalStrings.current
Text(text = strings.welcomeMessage)
```

## 🚀 Getting Started

### Prerequisites

- Android Studio Hedgehog or newer
- Kotlin 2.0+
- Minimum SDK 24

### Building the Project

```bash
./gradlew assembleDebug
```

### Running Tests

```bash
./gradlew test
./gradlew connectedAndroidTest
```

## 📦 Tech Stack

### Core

- **Jetpack Compose**: Modern declarative UI
- **Hilt**: Compile-time dependency injection
- **Coroutines & Flow**: Asynchronous programming
- **DataStore**: Modern data persistence

### Architecture Components

- **ViewModel**: UI state management
- **Navigation Compose**: Type-safe navigation
- **StateFlow**: Observable state holder

### UI

- **Material 3**: Latest design system
- **Compose Animation**: Smooth transitions

## 🔧 Development Guidelines

### Creating a New Feature Module

1. **Module Setup**

   ```
   feature/{name}/
   ├── build.gradle.kts
   └── src/main/java/com/anam145/wallet/feature/{name}/
   ```

2. **Define Contract**

   ```kotlin
   interface {Name}Contract {
       data class State(...)
       sealed interface Intent { ... }
       sealed interface Effect { ... }
   }
   ```

3. **Implement Layers**

   - Create ViewModels with state management
   - Define UseCases for business logic
   - Implement repositories with interfaces

4. **Setup DI**
   - Create Hilt modules
   - Bind interfaces to implementations

### Code Conventions

- **Naming**: `{Feature}Screen`, `{Feature}ViewModel`, `{Feature}UseCase`
- **Package Structure**: Follow the standard module structure
- **State Management**: Single state object per screen
- **Error Handling**: Graceful degradation with user feedback

## 🧪 Testing Strategy

TBD

## 📈 Performance Considerations

TBD

## 🔐 Security

TBD

## 📄 License

TBD

## 👥 Contributors

TBD

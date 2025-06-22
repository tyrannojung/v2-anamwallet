# ANAM Wallet V2

Modern Android wallet application built with Clean Architecture, MVVM/MVI patterns, and Jetpack Compose.

## 🏗️ Architecture Overview

This project follows **Clean Architecture** principles with a **multi-module** structure, ensuring separation of concerns, testability, and scalability.

### Module Structure

```
v2-anamwallet/
├── app/                          # Main application module
│   ├── MainActivity              # Entry point with navigation setup
│   ├── navigation/               # Navigation components
│   │   ├── AnamNavHost          # Navigation graph
│   │   ├── AnamNavRoute         # Type-safe routes
│   │   └── AnamBottomNavigation # Bottom navigation bar
│   └── ui/                      # App-specific UI
│       ├── components/          # App-only components (e.g., Header)
│       ├── theme/               # Theme ViewModel
│       └── language/            # Language ViewModel
│
├── core/
│   ├── common/                  # Pure Kotlin module (no Android deps)
│   │   └── model/               # Shared domain models
│   │       ├── Language.kt      # Language enum
│   │       └── ThemeMode.kt     # Theme enum
│   │
│   └── ui/                      # Shared UI resources
│       ├── theme/               # Material3 theme definitions
│       └── language/            # Language support
│           └── LocalLanguage.kt # CompositionLocal & strings
│
└── feature/                     # Feature modules
    ├── main/                    # Home/Dashboard
    ├── hub/                     # Service hub
    ├── browser/                 # Web browser
    ├── identity/                # Digital ID management
    └── settings/                # App settings
```

### Module Dependencies

```
app ─────────┬──→ core:common
             ├──→ core:ui
             └──→ all features

features ────┬──→ core:common
             └──→ core:ui

core:ui ─────→ core:common

core:common  (no dependencies - pure Kotlin)
```

## 📁 Standard Feature Module Structure

Each feature module follows a consistent Clean Architecture structure:

```
feature/{name}/
├── ui/                              # Presentation Layer
│   ├── {Name}Screen.kt              # Compose UI
│   ├── {Name}ViewModel.kt           # State management
│   ├── {Name}Contract.kt            # MVI Contract (State, Intent, Effect)
│   └── components/                  # Feature-specific UI components
│
├── domain/                          # Business Layer
│   ├── model/                       # Feature-specific models
│   ├── repository/                  # Repository interfaces
│   └── usecase/                     # Business logic (one per action)
│       ├── Get{Name}UseCase.kt      # Query operations
│       └── Set{Name}UseCase.kt      # Command operations
│
├── data/                            # Data Layer
│   └── repository/                  # Repository implementations
│       └── {Name}RepositoryImpl.kt
│
└── di/                              # Dependency Injection
    └── {Name}Module.kt              # Hilt module for bindings
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

## 🧭 Navigation System

The app uses Jetpack Navigation Compose with type-safe routes:

### Navigation Components

- **AnamNavRoute**: Sealed class defining all app destinations
- **AnamNavHost**: Central navigation graph composable
- **AnamBottomNavigation**: Bottom navigation bar with 5 main destinations

### Navigation Flow

```
MainActivity
    │
    ├── Header (App bar)
    ├── AnamNavHost (Content)
    │   ├── MainScreen
    │   ├── HubScreen
    │   ├── BrowserScreen
    │   ├── IdentityScreen
    │   └── SettingsScreen
    │
    └── AnamBottomNavigation (Bottom bar)
```

Navigation is handled directly at the UI layer without UseCase/Repository patterns, as it's purely a UI concern.

## Real-time Language System

The app supports instant language switching without Activity restart using CompositionLocal:

```kotlin
// Define language provider
val LocalLanguage = compositionLocalOf { Language.KOREAN }
val LocalStrings = compositionLocalOf { Strings() }

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

#### Naming Conventions

- **Screens**: `{Feature}Screen.kt` (e.g., `SettingsScreen.kt`)
- **ViewModels**: `{Feature}ViewModel.kt` (e.g., `SettingsViewModel.kt`)
- **Contracts**: `{Feature}Contract.kt` with State, Intent, Effect
- **UseCases**: `{Action}{Feature}UseCase.kt` (e.g., `GetThemeModeUseCase.kt`)
- **Repositories**: `{Feature}Repository.kt` interface, `{Feature}RepositoryImpl.kt` implementation

#### Module Placement Rules

- **core:common**: Domain models shared across multiple features (Language, ThemeMode)
- **core:ui**: UI components and resources used by multiple features
- **app**: Components used only in MainActivity (Header, navigation)
- **feature**: All feature-specific code stays within its module

#### Architecture Rules

- **Single State Object**: One data class per screen containing all UI state
- **UseCase Pattern**: One UseCase per business action (not CRUD operations)
- **Repository Pattern**: Specialized repositories over generic ones (ThemeRepository vs SettingsRepository)
- **Direct Navigation**: Navigation handled at UI layer without abstraction

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

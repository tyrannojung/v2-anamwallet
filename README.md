# ANAM Wallet V2

A modern Android cryptocurrency wallet with decentralized identity support, built with clean MVVM architecture.

## Overview

ANAM Wallet V2 is a complete refactoring of the original ANAM Android wallet, focusing on:

- Clean MVVM architecture with proper separation of concerns
- Modern Android development practices (Jetpack Compose, Kotlin Coroutines, Flow)
- Enhanced security and performance
- Improved testability and maintainability

## Features (Planned)

TBD

## Architecture

### MVI-lite Pattern (MVVM with Unidirectional Data Flow)

We use a pragmatic approach combining the best of MVVM and MVI patterns:

```
┌─────────────────────────────────────────────────────┐
│                   Compose UI                        │
│  - Observes single UiState                         │
│  - Calls ViewModel methods for user actions        │
└────────────────────┬───────────────────────────────┘
                     │ collectAsState()
                     ▼
┌─────────────────────────────────────────────────────┐
│                  ViewModel                          │
│  - Exposes: StateFlow<UiState>                     │
│  - Methods: onButtonClick(), onTextChanged()       │
│  - Updates: _state.update { it.copy(...) }         │
└────────────────────┬───────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────┐
│              Domain/Data Layer                      │
└─────────────────────────────────────────────────────┘
```

#### Key Principles

1. **Single State Object**: Each screen has one `UiState` data class
   ```kotlin
   data class WalletUiState(
       val balance: String = "0",
       val isLoading: Boolean = false,
       val error: String? = null
   )
   ```

2. **Unidirectional Flow**: UI → ViewModel → State → UI
   ```kotlin
   // ViewModel
   private val _uiState = MutableStateFlow(WalletUiState())
   val uiState = _uiState.asStateFlow()
   
   fun onRefreshClick() {
       _uiState.update { it.copy(isLoading = true) }
       // ... fetch data ...
       _uiState.update { it.copy(balance = newBalance, isLoading = false) }
   }
   ```

3. **Direct Method Calls**: No Intent boilerplate
   ```kotlin
   // In Compose UI
   Button(onClick = { viewModel.onRefreshClick() }) {
       Text("Refresh")
   }
   ```

#### Why MVI-lite?

- **Less Boilerplate**: No sealed Intent classes or giant reducer functions
- **Easy to Learn**: Familiar MVVM method calls, but with better state management
- **Compose-Friendly**: Single state object works perfectly with Compose recomposition
- **Scalable**: Can evolve to full MVI for complex screens when needed

### Clean Architecture Layers

```
MVVM + Clean Architecture
├── Presentation Layer (View + ViewModel)
├── Domain Layer (Use Cases + Repositories)
└── Data Layer (Local + Remote Data Sources)
```

## Tech Stack

TBD

## Development Status

TBD

## License

TBD

## Contact

TBD

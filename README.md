# ANAM Wallet V2 - Modular Wallet Platform

> **Next-Generation Modular Blockchain Wallet Platform**  
> An Android-based modular wallet that enables integrated management of various blockchains and services in a single wallet.

## Key Features

### Modular Architecture

- **Plugin-based Blockchain Support**: Add new blockchains as independent modules
- **Web App Integration**: Integrate various web services like Government24, financial services as mini-apps
- **Multi-Process Design**: Isolated execution environment with 5 processes (Main, WebApp, Blockchain, WebView Renderer x2)

### User Experience

- **Unified Interface**: Manage all blockchains and services with one consistent UI
- **One-Click Switching**: Instantly switch active blockchain to support various networks
- **Native Performance**: Modern and fast UI based on Jetpack Compose

## Architecture

### Clean Architecture + MVI

```
┌─────────────────┐
│   Presentation  │ ← Jetpack Compose + MVI Pattern
├─────────────────┤
│     Domain      │ ← Business Logic (Use Cases)
├─────────────────┤
│      Data       │ ← Repository Implementation
└─────────────────┘
```

### Multi-Process Architecture

```
┌─────────────────┐    ┌─────────────┐    ┌─────────────┐    ┌─────────────────┐
│ WebView Renderer│    │   WebApp    │    │    Main     │    │   Blockchain    │
│    Process      │◀──▶│   Process   │◀──▶│   Process   │◀──▶│    Process      │
│  (JavaScript)   │ JS │    (:app)   │AIDL│   (:main)   │AIDL│  (:blockchain)  │
└─────────────────┘    └─────────────┘    └─────────────┘    └─────────────────┘
                                                                        │ JS
                                                                        ▼
                                                              ┌─────────────────┐
                                                              │ WebView Renderer│
                                                              │    Process      │
                                                              │ (Blockchain JS) │
                                                              └─────────────────┘
```

### Module Structure

- **app**: Main application entry point
- **core**: Common features and resources
  - common: Domain models, utilities
  - ui: Common UI components and themes
  - data: Data storage
- **feature**: Feature-specific modules
  - main: Dashboard
  - miniapp: Mini-app management (webapp/blockchain)
  - settings: Settings
  - hub/browser/identity: Additional features

## Getting Started

### Requirements

- Android Studio Ladybug or higher
- JDK 17
- Android SDK 35
- Kotlin 2.0+

### Build and Run

```bash
# Clone the project
git clone https://github.com/anam145/v2-anamwallet.git

# Open in Android Studio
# File > Open > v2-anamwallet

# Build and run
./gradlew assembleDebug
```

## 🔧 Tech Stack

### Core Technologies

- **Kotlin**: 100% Kotlin-based
- **Jetpack Compose**: Declarative UI
- **Coroutines & Flow**: Asynchronous programming
- **Hilt**: Dependency injection

### Architecture Components

- **Navigation Compose**: Type-safe navigation
- **ViewModel + MVI**: State management
- **DataStore**: Data persistence
- **AIDL**: Inter-process communication

### UI/UX

- **Material Design 3**: Latest design system
- **Dark Mode**: Full dark mode support
- **Multilingual**: Real-time Korean/English switching

## Supported Features

### Currently Supported

- Ethereum blockchain
- Government24 integration
- Dark mode
- Multilingual (Korean/English)

### Under Development

- Bitcoin support
- Solana support
- DID (Decentralized Identity)

## Contributing

Contributions are welcome! Please follow these guidelines:

1. Fork the Project
2. Create your Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit your Changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the Branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

For more details, see [CODE_CONVENTIONS.md](CODE_CONVENTIONS.md).
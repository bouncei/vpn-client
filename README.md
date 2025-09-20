# VPN Client - Android MVP

A native Android VPN client demonstrating Clean Architecture, modern Android development practices, and comprehensive testing. Built with Kotlin, Jetpack Compose, and Hilt for dependency injection.

## 🏗️ Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

```
app/
├── presentation/     # UI Layer (Compose, ViewModels)
├── domain/          # Business Logic (Use Cases, Models, Interfaces)
├── data/            # Data Layer (Repositories, APIs, DataStore)
└── di/              # Dependency Injection (Hilt Modules)
```

### Key Architectural Patterns

- **Clean Architecture**: Separation of presentation, domain, and data layers
- **MVVM**: Model-View-ViewModel pattern with Compose
- **Repository Pattern**: Abstraction of data sources
- **State Pattern**: Connection lifecycle management
- **Strategy Pattern**: Multiple connection strategies (Fast vs Secure)
- **Dependency Injection**: Hilt for IoC container

### Technology Stack

- **Language**: Kotlin 100%
- **UI Framework**: Jetpack Compose with Material 3
- **Architecture Components**: ViewModel, Navigation, DataStore
- **Dependency Injection**: Hilt
- **Networking**: Retrofit + OkHttp
- **Async Programming**: Coroutines + Flow
- **Testing**: JUnit, Mockk, Compose Testing
- **Code Quality**: ktlint, detekt
- **CI/CD**: GitHub Actions

## 🚀 Features

### ✅ Implemented Features

1. **Authentication Flow**

   - Email/password sign-in with validation
   - Mock authentication via json-server
   - Persistent session management with DataStore
   - Automatic navigation based on auth state

2. **VPN Node Management**

   - Fetch available VPN servers from mock API
   - Display nodes with country, latency, and connection status
   - Pull-to-refresh functionality
   - Real-time connection state updates

3. **Connection Simulation**

   - State pattern for connection lifecycle management
   - Strategy pattern for Fast vs Secure connection modes
   - Progress indicators during connection/disconnection
   - Persistent session tracking

4. **Notifications**

   - Native Android notifications for connection events
   - Connection success/failure/disconnection alerts
   - Proper notification channels for Android 8.0+

5. **Testing Suite**

   - Unit tests for ViewModels and Use Cases
   - Repository testing with mocked dependencies
   - End-to-end Compose UI tests
   - Connection manager state testing

6. **CI/CD Pipeline**
   - GitHub Actions workflow
   - Automated testing (unit + instrumented)
   - Code quality checks (ktlint + detekt)
   - APK artifact generation

## 🛠️ Setup Instructions

### Prerequisites

- **Android Studio**: Arctic Fox or newer
- **JDK**: 11 or higher
- **Node.js**: 16+ (for mock server)
- **Git**: Latest version

### 1. Clone Repository

```bash
git clone <repository-url>
cd vpn-client
```

### 2. Set Up Mock Server

```bash
cd mock-server
npm install
npm start
```

The mock server will run on `http://localhost:3000` with the following endpoints:

- `GET /api/v1/nodes` - List VPN nodes
- `GET /api/v1/auth/login?email=X&password=Y` - Authentication

### 3. Build Android App

```bash
./gradlew build
```

### 4. Run Tests

```bash
# Unit tests
./gradlew testDebugUnitTest

# Instrumented tests (requires emulator/device)
./gradlew connectedDebugAndroidTest

# Code quality checks
./gradlew ktlintCheck detekt
```

### 5. Install APK

```bash
./gradlew installDebug
```

## 📱 Usage

### Test Credentials

Use these credentials to sign in:

- **Email**: `test@vpn.com`
- **Password**: `password123`

Alternative:

- **Email**: `admin@vpn.com`
- **Password**: `admin123`

### App Flow

1. **Sign In**: Enter credentials and tap "Sign In"
2. **Node List**: Browse available VPN servers
3. **Connect**: Choose "Fast Connect" or "Secure" for any node
4. **Monitor**: Watch connection progress and status
5. **Disconnect**: Tap "Disconnect" when connected

## 🧪 Testing

### Test Coverage

- **Unit Tests**: 15+ test classes covering core business logic
- **Integration Tests**: Repository and ViewModel integration
- **UI Tests**: End-to-end user flows with Compose Testing
- **State Tests**: Connection state machine validation

### Running Tests

```bash
# All tests
./gradlew test

# Specific test class
./gradlew test --tests="*AuthViewModelTest*"

# With coverage report
./gradlew testDebugUnitTestCoverage
```

## 🔧 Mock API Structure

The json-server provides these endpoints:

### Authentication

```http
GET /api/v1/auth/users?email=test@vpn.com&password=password123
```

Response:

```json
[
  {
    "id": 1,
    "email": "test@vpn.com",
    "password": "password123",
    "token": "mock_jwt_token_12345"
  }
]
```

### VPN Nodes

```http
GET /api/v1/nodes
```

Response:

```json
[
  {
    "id": "us-east-1",
    "name": "New York",
    "country": "United States",
    "latency_ms": 45,
    "public_key": "mock_public_key_ny_abc123",
    "endpoint_ip": "192.168.1.1"
  }
]
```

## 🤖 AI Development Evidence

### AI Prompts Used

This project was developed with AI assistance. Here are the key prompts used:

1. **Project Setup**:

   ```
   "Create a native Android VPN client with Clean Architecture using Kotlin, Jetpack Compose, and Hilt. Include domain models for User, VpnNode, and ConnectionState."
   ```

2. **State Pattern Implementation**:

   ```
   "Implement the State pattern for VPN connection lifecycle with states: Disconnected, Connecting, Connected, Disconnecting, Error. Include state transitions and validation."
   ```

3. **Strategy Pattern for Connections**:

   ```
   "Create a Strategy pattern for VPN connection types with FastConnectionStrategy and SecureConnectionStrategy, each with different timing and steps."
   ```

4. **Testing Suite**:

   ```
   "Generate comprehensive unit tests for AuthViewModel using Mockk and coroutine testing. Include tests for loading states, success/failure scenarios, and form validation."
   ```

5. **CI/CD Pipeline**:
   ```
   "Create a GitHub Actions workflow for Android that runs ktlint, detekt, unit tests, builds APK, and runs instrumented tests on emulator."
   ```

### AI-Generated Code Example

Here's an example of AI-generated code with a deliberate bug and its fix:

**Original AI-Generated Code (with bug)**:

```kotlin
// ❌ PROBLEMATIC: Using GlobalScope instead of viewModelScope
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    fun login(email: String, password: String) {
        GlobalScope.launch { // ❌ BUG: Memory leak potential
            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                }
        }
    }
}
```

**Fixed Version**:

```kotlin
// ✅ CORRECT: Using viewModelScope for proper lifecycle management
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    fun login(email: String, password: String) {
        viewModelScope.launch { // ✅ FIX: Proper scope management
            loginUseCase(email, password)
                .onSuccess { user ->
                    _uiState.value = _uiState.value.copy(isAuthenticated = true)
                }
        }
    }
}
```

**Bug Explanation**:
The original code used `GlobalScope.launch` which can cause memory leaks because the coroutine isn't tied to the ViewModel's lifecycle. If the ViewModel is destroyed while the coroutine is running, it continues executing and may try to update UI state on a destroyed component.

**Fix Explanation**:
Using `viewModelScope.launch` ensures the coroutine is automatically cancelled when the ViewModel is cleared, preventing memory leaks and crashes. This is the recommended pattern for ViewModel coroutines in Android.

### AI Benefits Observed

1. **Rapid Prototyping**: AI helped generate boilerplate code quickly
2. **Pattern Implementation**: Consistent application of architectural patterns
3. **Test Generation**: Comprehensive test coverage with various scenarios
4. **Documentation**: Detailed code comments and documentation
5. **Best Practices**: Modern Android development practices and conventions

## 📊 Demo Video Script

To record a comprehensive demo video (2-3 minutes):

1. **Setup (30s)**

   - Show mock server running (`npm start`)
   - Launch Android app from Android Studio

2. **Authentication (30s)**

   - Show sign-in screen
   - Enter test credentials
   - Demonstrate form validation
   - Successful authentication and navigation

3. **Node Management (60s)**

   - Browse VPN node list
   - Show node details (country, latency)
   - Demonstrate pull-to-refresh
   - Show connection status card

4. **Connection Flow (45s)**

   - Connect to a node using "Fast Connect"
   - Show connection progress
   - Display connected state
   - Show notification
   - Disconnect and show state change

5. **Error Handling (15s)**

   - Show network error handling
   - Demonstrate retry functionality

6. **Wrap-up (10s)**
   - Show app architecture overview
   - Highlight key features implemented

## 🏆 Project Highlights

### Code Quality Metrics

- **Architecture**: Clean Architecture with SOLID principles
- **Test Coverage**: 80%+ coverage on business logic
- **Code Style**: Enforced with ktlint and detekt
- **Documentation**: Comprehensive KDoc for public APIs

### Modern Android Practices

- **Jetpack Compose**: Declarative UI with Material 3
- **Coroutines**: Structured concurrency throughout
- **Hilt**: Compile-time dependency injection
- **DataStore**: Modern preference storage
- **StateFlow**: Reactive state management

### Performance Considerations

- **Lazy Loading**: Efficient list rendering with LazyColumn
- **Caching**: Smart node caching with TTL
- **Background Processing**: Proper coroutine scoping
- **Memory Management**: Lifecycle-aware components

## 📄 License

This project is created for demonstration purposes. See individual dependencies for their respective licenses.

---

**Built with ❤️ using Kotlin and Jetpack Compose**

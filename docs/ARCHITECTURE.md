# Architecture Documentation

## Overview

The VPN Client follows Clean Architecture principles with clear separation of concerns across three main layers:

## Layer Structure

### 1. Presentation Layer (`presentation/`)

**Responsibility**: UI components, user interactions, and presentation logic.

**Components**:
- **Composables**: UI screens built with Jetpack Compose
- **ViewModels**: State management and UI logic
- **Navigation**: App routing and navigation logic
- **Themes**: UI styling and theming

**Key Files**:
- `SignInScreen.kt` - Authentication UI
- `NodeListScreen.kt` - VPN nodes display
- `AuthViewModel.kt` - Authentication state management
- `NodeViewModel.kt` - Node list state management

**Patterns Used**:
- MVVM (Model-View-ViewModel)
- Unidirectional Data Flow
- State Hoisting

### 2. Domain Layer (`domain/`)

**Responsibility**: Business logic, use cases, and domain models.

**Components**:
- **Models**: Core business entities
- **Use Cases**: Business operations and rules
- **Repository Interfaces**: Data access contracts
- **Connection Management**: VPN connection logic

**Key Files**:
- `User.kt`, `VpnNode.kt`, `ConnectionState.kt` - Domain models
- `LoginUseCase.kt`, `GetNodesUseCase.kt` - Business operations
- `AuthRepository.kt`, `NodeRepository.kt` - Data contracts
- `ConnectionManager.kt` - Connection orchestration

**Patterns Used**:
- Use Case Pattern
- Repository Pattern
- State Pattern (for connection states)
- Strategy Pattern (for connection strategies)

### 3. Data Layer (`data/`)

**Responsibility**: Data sources, networking, and persistence.

**Components**:
- **Remote**: API services and DTOs
- **Local**: DataStore and caching
- **Repository Implementations**: Data access logic

**Key Files**:
- `AuthApi.kt`, `NodeApi.kt` - Network interfaces
- `AuthDataStore.kt`, `SessionDataStore.kt` - Local storage
- `AuthRepositoryImpl.kt` - Data access implementation

**Patterns Used**:
- Repository Implementation
- DTO (Data Transfer Object)
- Adapter Pattern

## Dependency Flow

```
Presentation → Domain ← Data
```

- **Presentation** depends on **Domain**
- **Data** depends on **Domain**
- **Domain** has no dependencies (pure business logic)

## Design Patterns

### 1. State Pattern (Connection Management)

**Purpose**: Manage VPN connection lifecycle with clear state transitions.

**Implementation**:
```kotlin
sealed class ConnectionState {
    object Disconnected : ConnectionState()
    data class Connecting(val nodeId: String, val progress: Float) : ConnectionState()
    data class Connected(val nodeId: String, val connectedAt: Long) : ConnectionState()
    data class Disconnecting(val nodeId: String) : ConnectionState()
    data class Error(val nodeId: String?, val error: String) : ConnectionState()
}
```

**Benefits**:
- Clear state transitions
- Prevents invalid operations
- Easy to test and debug

### 2. Strategy Pattern (Connection Strategies)

**Purpose**: Support different connection approaches (Fast vs Secure).

**Implementation**:
```kotlin
interface ConnectionStrategy {
    fun getConnectionSteps(): List<String>
    fun getStepDelay(): Long
    fun getStrategyName(): String
}

class FastConnectionStrategy : ConnectionStrategy { ... }
class SecureConnectionStrategy : ConnectionStrategy { ... }
```

**Benefits**:
- Extensible connection types
- Runtime strategy selection
- Separation of concerns

### 3. Repository Pattern

**Purpose**: Abstract data access and provide clean API to domain layer.

**Implementation**:
```kotlin
// Domain interface
interface AuthRepository {
    suspend fun login(email: String, password: String): Result<User>
    fun getCurrentUser(): Flow<User?>
}

// Data implementation
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val authDataStore: AuthDataStore
) : AuthRepository { ... }
```

**Benefits**:
- Testable business logic
- Flexible data sources
- Clean separation of concerns

### 4. Use Case Pattern

**Purpose**: Encapsulate business operations and rules.

**Implementation**:
```kotlin
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Result<User> {
        // Input validation
        if (email.isBlank()) return Result.failure(...)
        
        // Business logic
        return authRepository.login(email, password)
    }
}
```

**Benefits**:
- Single responsibility
- Reusable business logic
- Easy to test

## Data Flow

### Authentication Flow
```
SignInScreen → AuthViewModel → LoginUseCase → AuthRepository → AuthApi
                    ↓
              AuthDataStore (persistence)
```

### Node Management Flow
```
NodeListScreen → NodeViewModel → GetNodesUseCase → NodeRepository → NodeApi
                      ↓
                 ConnectionManager (state management)
```

### Connection Flow
```
NodeListScreen → NodeViewModel → ConnectToNodeUseCase → ConnectionRepository → ConnectionManager
                                                              ↓
                                                        SessionDataStore (persistence)
```

## Testing Strategy

### Unit Tests
- **ViewModels**: State management and user interactions
- **Use Cases**: Business logic validation
- **Repositories**: Data access logic (with mocked dependencies)

### Integration Tests
- **Repository Implementations**: Real data source integration
- **Connection Manager**: State transitions and strategy execution

### UI Tests
- **End-to-End Flows**: Complete user journeys
- **Screen Tests**: Individual screen functionality
- **Navigation Tests**: Routing and state preservation

## Dependency Injection

**Framework**: Hilt (compile-time DI)

**Modules**:
- `NetworkModule`: Retrofit, OkHttp, API interfaces
- `DataStoreModule`: DataStore preferences
- `RepositoryModule`: Repository interface bindings

**Benefits**:
- Compile-time safety
- Automatic lifecycle management
- Easy testing with test doubles

## Error Handling

### Network Errors
- Retrofit exceptions wrapped in `Result<T>`
- Fallback to cached data when available
- User-friendly error messages

### Validation Errors
- Input validation in Use Cases
- Form validation in ViewModels
- Real-time feedback in UI

### Connection Errors
- State machine handles error states
- Automatic retry mechanisms
- Graceful degradation

## Performance Considerations

### UI Performance
- Lazy loading with `LazyColumn`
- State hoisting for recomposition optimization
- Efficient state updates with `StateFlow`

### Network Performance
- Response caching with TTL
- Request deduplication
- Background data refresh

### Memory Management
- `viewModelScope` for proper coroutine lifecycle
- Weak references where appropriate
- Efficient data structures

## Security Considerations

### Data Protection
- Encrypted DataStore preferences
- No sensitive data in logs
- Secure network communication (HTTPS)

### Authentication
- Token-based authentication
- Automatic session expiration
- Secure credential storage

## Scalability

### Adding New Features
1. Create domain models and use cases
2. Add repository interfaces in domain
3. Implement repositories in data layer
4. Create UI components and ViewModels
5. Add dependency injection bindings

### Adding New Connection Strategies
1. Implement `ConnectionStrategy` interface
2. Register in `ConnectionStrategyFactory`
3. Update UI to support new strategy
4. Add tests for new strategy

### Adding New Data Sources
1. Create new API interfaces
2. Implement repository methods
3. Update dependency injection
4. Add integration tests

This architecture provides a solid foundation for a maintainable, testable, and scalable Android application.

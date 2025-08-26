# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

A Spring Boot JWT authentication system with OAuth2 social login (Google, Naver, Kakao), featuring user management, refresh token/blacklist handling, and comprehensive API documentation via Swagger.

## Development Commands

### Running the Application
```bash
# Development with Docker (recommended)
docker-compose up -d

# Manual run (requires PostgreSQL and Redis)
./gradlew bootRun
```

### Testing
```bash
# Run all tests
./gradlew test

# Run tests with coverage
./gradlew test jacocoTestReport

# Run single test class
./gradlew test --tests "ClassName"

# Run single test method
./gradlew test --tests "ClassName.methodName"
```

### Building
```bash
# Build JAR
./gradlew build

# Build Docker image
docker build -t spring-app .
```

### Code Quality
```bash
# Check for compilation errors
./gradlew compileJava

# Run static analysis (if configured)
./gradlew check

# Clean build artifacts
./gradlew clean
```

## Architecture Overview

### Core Structure
- **MemberApplication.java**: Main Spring Boot application class with JPA auditing enabled
- **SecurityConfig.java**: Spring Security configuration with JWT filters and OAuth2 setup
- **Domain-Driven Structure**: Organized by business domains (member, auth, global)

### Key Components

#### Authentication & Authorization
- **JWT-based authentication** with Access/Refresh token pattern
- **OAuth2 social login** integration (Google, Naver, Kakao)
- **Redis-backed token storage** for refresh tokens and blacklisting
- **Role-based authorization** (USER, ADMIN, GUEST)

#### Security Features
- **JWT Filters**: `LoginFilter` (token issuance), `JwtAuthenticationFilter` (validation)
- **Rate Limiting**: `RateLimitInterceptor` protects login endpoints (5 requests/minute per IP)
- **Token Blacklisting**: `BlacklistService` manages invalidated tokens in Redis
- **OAuth2 Integration**: Strategy pattern for multiple social providers
- **Security Headers**: CORS configuration with environment-based settings

#### Domain Organization
```
src/main/java/com/example/login/
├── domain/
│   ├── auth/           # Token management, blacklisting, authentication services
│   │   ├── controller/ # AuthApiController (token refresh, logout)
│   │   ├── service/    # AuthenticationService, TokenValidator, RefreshTokenService, BlacklistService
│   │   ├── entity/     # RefreshToken
│   │   ├── repository/ # RefreshTokenRepository
│   │   └── dto/        # TokenResponse
│   └── member/         # User CRUD, authentication, registration
│       ├── controller/ # JoinApiController (registration), UserApiController (CRUD)
│       ├── service/    # MemberService
│       ├── entity/     # MemberEntity, Role
│       ├── repository/ # MemberRepository
│       ├── security/   # CustomUserDetails, CustomUserDetailsService
│       └── dto/        # MemberSaveRequest, MemberResponse, MemberLoginRequest, etc.
├── global/
│   ├── config/         # SecurityConfig, JwtSecurityConfig, OAuth2SecurityConfig, SwaggerConfig, etc.
│   ├── jwt/            # JWTUtil, LoginFilter, JwtAuthenticationFilter, JwtTokenService
│   ├── oauth2/         # OAuth2 handlers, strategy pattern, user info extraction
│   │   ├── handler/    # OAuth2LoginSuccessHandler, OAuth2LoginFailureHandler
│   │   ├── strategy/   # SocialLoginStrategy implementations (Google, Naver, Kakao)
│   │   ├── userInfo/   # OAuth2UserInfo implementations
│   │   ├── service/    # CustomOAuth2UserService, OAuth2TokenService
│   │   ├── user/       # CustomOAuth2User
│   │   ├── entity/     # SocialType
│   │   └── dto/        # OAuthAttributes, OAuthLoginResponse
│   ├── dto/            # CommonApiResponse
│   ├── entity/         # BaseTimeEntity
│   ├── exception/      # GlobalExceptionHandler, BaseException
│   ├── response/       # ErrorCode, MemberSuccessCode, ApiResponseAdvice, AutoApiResponse, etc.
│   ├── swagger/        # CustomExceptionDescription, SwaggerResponseDescription, ExampleHolder
│   ├── advice/         # ParameterData (for validation errors)
│   └── interceptor/    # RateLimitInterceptor
```

### Database & Storage
- **PostgreSQL**: Primary database for user data
- **Redis**: Session storage for refresh tokens and token blacklisting
- **JPA/Hibernate**: ORM with automatic table creation (ddl-auto: create)

### API Documentation
- **Swagger UI**: Available at `/api-docs`
- **OpenAPI 3**: Auto-generated documentation
- Organized by controller groups (Auth, Member, User)

## Environment Configuration

### Required Environment Variables
```bash
# Database
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Redis
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET_KEY=your_jwt_secret
JWT_ACCESS_EXPIRATION=3600000    # 1 hour in ms
JWT_REFRESH_EXPIRATION=86400000  # 24 hours in ms
```

### OAuth2 Configuration
OAuth2 settings are in `application-oauth.yml` - configure client IDs and secrets for:
- Google OAuth2
- Naver OAuth2  
- Kakao OAuth2

**Strategy Pattern**: `SocialLoginStrategyManager` handles provider-specific user info extraction.

**Duplicate Prevention**: System links existing accounts when same email is used across providers.

## Development Patterns

### Response Standardization
All API responses use `CommonApiResponse<T>` wrapper with consistent structure:
- `code`: Response code with "S[0-9]{3}" (success) or "E[0-9]{3}" (error) pattern
- `message`: Human-readable message
- `data`: Actual data payload (success) or detailed error information (validation failures)

**Auto Response Wrapping**: 
- Controllers use `@AutoApiResponse` annotation for automatic response wrapping via `ApiResponseAdvice`
- `@SuccessCode` annotation specifies exact success response codes
- Void methods automatically wrapped with appropriate success codes

**Error Handling**: 
- `GlobalExceptionHandler` provides 4-tier exception handling (BaseException, InvalidFormatException, MethodArgumentNotValidException, UsernameNotFoundException)
- Bean validation errors return `ParameterData` arrays with field-level details (key, value, reason)
- `CommonApiResponse.failWithDetails()` method for detailed error responses

### Token Management
- **Access tokens**: Short-lived (1 hour), sent in Authorization header
- **Refresh tokens**: Long-lived (24 hours), stored as HttpOnly cookies
- **Token blacklisting**: Logout invalidates tokens via Redis TTL

### Error Handling
- **GlobalExceptionHandler** with 4-tier exception processing:
  1. `BaseException`: Custom business logic exceptions
  2. `InvalidFormatException`: Enum conversion errors (Role validation)
  3. `MethodArgumentNotValidException`: Bean Validation with detailed `ParameterData`
  4. `UsernameNotFoundException`: Spring Security integration
- **ErrorCode enum**: E400-E500 HTTP status-based error codes with specific messages
- **MemberSuccessCode enum**: S200-S209 success codes for different operations

## Testing Strategy

### API Testing Workflow
1. `POST /api/join` - User registration via JoinApiController
2. `POST /login` - Login via Spring Security LoginFilter (not controller)
3. `GET /api/users` - Authenticated request (ADMIN role required)
4. `POST /api/auth/token/refresh` - Access token refresh
5. `POST /api/auth/token/refresh/full` - Full token refresh (Access + Refresh)
6. `POST /api/auth/logout` - Token invalidation and blacklisting
7. `GET /api/users` - Verify blacklist blocking

### Test Data
- Default roles: USER (general), ADMIN (privileged), GUEST (OAuth2 temp)
- Test credentials managed via environment variables

### Correct API Endpoints
Based on the current controller structure:
- **Registration**: `POST /api/join` (JoinApiController)
- **Email Check**: `GET /api/join/email-check?memberEmail=test@example.com`
- **Login**: `POST /login` (Spring Security Filter Chain, not controller)
- **Token Refresh**: `POST /api/auth/token/refresh` (Access token only)
- **Full Token Refresh**: `POST /api/auth/token/refresh/full` (Access + Refresh)  
- **Logout**: `POST /api/auth/logout` (AuthApiController)
- **User Management**: 
  - `GET /api/users` (All users - ADMIN role)
  - `GET /api/users/{id}` (Specific user - USER role)
  - `PUT /api/users/{id}` (Update user - USER role)  
  - `DELETE /api/users/{id}` (Delete user - ADMIN role)

## Security Features

### Rate Limiting
- **Login endpoints**: Limited to 5 requests per minute per IP
- **Implementation**: Guava RateLimiter with IP-based keys
- **Error Response**: HTTP 429 with standardized error message

### OAuth2 Security
- **Account Linking**: Automatically links social accounts to existing email addresses
- **Token Management**: Real user data in JWT tokens (no placeholder values)
- **Error Handling**: Generic error messages to prevent information disclosure

### Input Validation
- **SQL Injection Prevention**: Explicit @Query annotations for repository methods
- **JWT Security**: Debug-level logging for sensitive token information
- **Bean Validation**: Comprehensive field-level validation with detailed error responses

## Swagger Documentation

### Advanced Features
- **Auto Error Documentation**: `@CustomExceptionDescription` with `SwaggerResponseDescription` enum generates HTTP status-based error response examples
- **Success Code Specification**: `@SuccessCode` annotation for specific MemberSuccessCode responses
- **JWT Security Schema**: `@PreAuthorize` annotation automatically adds JWT Bearer token requirements
- **Bean Validation Examples**: `PARAMETER_VALIDATION_ERROR` generates detailed `ParameterData` array examples
- **Error Response Grouping**: HTTP status codes group multiple error types into single responses with multiple examples
- **OperationCustomizer**: Automatically processes annotations and generates comprehensive API documentation

### SwaggerResponseDescription Error Groups
- **MEMBER_ERROR**: E404 (MEMBER_NOT_FOUND), E409 (DUPLICATE_EMAIL)
- **MEMBER_JOIN_ERROR**: E409 (DUPLICATE_EMAIL), E400 (PARAMETER_VALIDATION_ERROR, INVALID_INPUT_VALUE)
- **AUTH_ERROR**: E401 (INVALID_TOKEN, REFRESH_TOKEN_NOT_FOUND, LOGIN_FAIL)
- **COMMON_ERROR**: E500 (INTERNAL_SERVER_ERROR), E400 (INVALID_INPUT_VALUE)

### API Documentation Info
- **Title**: "Spring Login API 문서"
- **Version**: "v1.0.0"
- **Contact**: 김규일 (rlarbdlf222@gmail.com, GitHub: Kimgyuilli)
- **License**: MIT License
- **Server**: http://localhost:8080 (개발 서버)

### Access
- **Swagger UI**: Available at `/api-docs`
- **Security Schema**: JWT Bearer token (Authorization header)
- **Interactive Testing**: Full request/response examples with automatic authentication

## Rate Limiting Implementation

### Current Behavior
Rate limiting is applied **per IP address + endpoint combination**:
- **Key Format**: `IP:endpoint` (e.g., `192.168.1.100:/member/login`)
- **Limitation**: 5 requests per minute per IP per endpoint
- **Scope**: `/member/login` and `/oauth2/` endpoints only
- **Technology**: Guava RateLimiter with ConcurrentHashMap storage

### Important Notes
- **Pre-authentication**: IP-based limiting works before user identification
- **Network Impact**: All users from same IP/network share the rate limit
- **Alternative**: User ID-based limiting possible for post-authentication endpoints

# important-instruction-reminders
Do what has been asked; nothing more, nothing less.
NEVER create files unless they're absolutely necessary for achieving your goal.
ALWAYS prefer editing an existing file to creating a new one.
NEVER proactively create documentation files (*.md) or README files. Only create documentation files if explicitly requested by the User.

      
      IMPORTANT: this context may or may not be relevant to your tasks. You should not respond to this context unless it is highly relevant to your task.
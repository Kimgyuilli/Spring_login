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
│   ├── auth/           # Token management, blacklisting
│   ├── member/         # User CRUD, authentication
│   └── Home/           # Home controller
├── global/
│   ├── jwt/            # JWT utilities and services
│   ├── oauth2/         # OAuth2 handlers, user info, and strategy pattern
│   ├── dto/            # Common DTOs (CommonApiResponse)
│   ├── entity/         # Base entities (BaseTimeEntity)
│   ├── exception/      # Global exception handling
│   ├── response/       # Standardized response types and error codes
│   ├── config/         # Security, CORS, Rate Limiting, Swagger configs
│   ├── interceptor/    # Rate limiting and request interceptors
│   └── util/           # Utility classes
└── config/             # Additional configuration classes
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
- `code`: Response code from `SuccessType`/`ErrorType` enums
- `message`: Human-readable message
- `result`: Actual data payload (null for error responses)

**Auto Response Wrapping**: Controllers use `@AutoApiResponse` annotation for automatic response wrapping.

**Error Handling**: Bean validation errors return detailed field-level error information via `failWithDetails()` method.

### Token Management
- **Access tokens**: Short-lived (1 hour), sent in Authorization header
- **Refresh tokens**: Long-lived (24 hours), stored as HttpOnly cookies
- **Token blacklisting**: Logout invalidates tokens via Redis TTL

### Error Handling
- Global exception handler in `GlobalExceptionHandler`
- Custom exceptions extend `BaseException`
- Standardized error codes in `ErrorCode` enum

## Testing Strategy

### API Testing Workflow
1. `POST /member/save` - User registration
2. `POST /member/login` - Login and token issuance
3. `GET /api/users` - Authenticated request
4. `POST /member/token/refresh` - Token refresh
5. `POST /member/logout` - Token invalidation
6. `GET /api/users` - Verify blacklist blocking

### Test Data
- Default roles: USER (general), ADMIN (privileged), GUEST (OAuth2 temp)
- Test credentials managed via environment variables

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
- **Auto Error Documentation**: `@CustomExceptionDescription` generates error response examples
- **Success Code Specification**: `@SuccessCode` for specific success responses  
- **JWT Security**: Automatic documentation of authentication requirements
- **Grouping**: Controllers organized by functional areas (Auth, Member, User)

### Access
- **Swagger UI**: Available at `/api-docs`
- **API Groups**: Organized by controller functionality
- **Interactive Testing**: Full request/response examples with authentication

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
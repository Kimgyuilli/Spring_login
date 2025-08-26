# 🚀 Spring Boot JWT Authentication System

> **프로젝트 템플릿: Spring Boot JWT 인증 시스템**  
> JWT 기반 인증/인가, OAuth2 소셜 로그인, 사용자 관리 기능을 포함한 완전한 백엔드 시스템

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.4.4-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-blue.svg)](https://www.postgresql.org/)
[![Redis](https://img.shields.io/badge/Redis-7.4-red.svg)](https://redis.io/)
[![Docker](https://img.shields.io/badge/Docker-Compose-blue.svg)](https://www.docker.com/)

---

## 📋 **목차**

- [🏁 빠른 시작](#-빠른-시작)
- [🏗️ 시스템 아키텍처](#-시스템-아키텍처)
- [📁 프로젝트 구조](#-프로젝트-구조)
- [🔐 인증 시스템](#-인증-시스템)
- [📋 API 응답 규격화](#-api-응답-규격화)
- [⚡ 예외 처리 규격](#-예외-처리-규격)
- [📚 Swagger 문서화 규격](#-swagger-문서화-규격)
- [🛠️ 개발 가이드라인](#-개발-가이드라인)
- [🧪 API 테스트](#-api-테스트)
- [🚀 운영 가이드](#-운영-가이드)

---

## 🏁 **빠른 시작**

### **필수 요구사항**
- Java 21+
- Docker & Docker Compose
- Git

### **접속 정보**
- **메인 애플리케이션**: http://localhost:8080
- **Swagger UI**: http://localhost:8080/api-docs
- **PostgreSQL**: localhost:5432 (DB: logindb)
- **Redis**: localhost:6379

---

## 🏗️ **시스템 아키텍처**

### **기술 스택**
```
┌─────────────────┬─────────────────────────────────────────┐
│ Category        │ Technology                              │
├─────────────────┼─────────────────────────────────────────┤
│ Framework       │ Spring Boot 3.4.4, Spring Security 6  │
│ Language        │ Java 21                                 │
│ Database        │ PostgreSQL 15, Redis 7.4               │
│ ORM             │ Spring Data JPA, Hibernate             │
│ Authentication  │ JWT (Access/Refresh Token)              │
│ OAuth2          │ Google, Naver, Kakao                    │
│ Documentation   │ Swagger/OpenAPI 3                       │
│ Build Tool      │ Gradle 8.5                             │
│ Container       │ Docker, Docker Compose                  │
└─────────────────┴─────────────────────────────────────────┘
```

### **시스템 아키텍처**
```
┌─────────────────┐  HTTP  ┌─────────────────┐  JPA  ┌─────────────────┐
│   Client App    │ <───>  │  Spring Boot    │ <───> │   PostgreSQL    │
│   (React/Vue)   │        │   Application   │       │    Database     │
└─────────────────┘        └─────────────────┘       └─────────────────┘
                                      │                                   
                                      ├─────────────────┐                 
                                      │                 │                 
                           ┌─────────────────┐ ┌─────────────────┐     
                           │     Redis       │ │  OAuth2 APIs    │     
                           │  (Token Store)  │ │ (Google/Naver/  │     
                           │                 │ │     Kakao)      │     
                           └─────────────────┘ └─────────────────┘     
```

### **요청 아키텍처**
```
Request Flow:
Client → CORS Filter → Rate Limit → JWT Filter → Security Filter → Controller

Token Management:
┌─────────────────┬─────────────────┬─────────────────┐
│ Access Token    │ Refresh Token   │ Blacklist      │
│ (1 hour)        │ (24 hours)      │ (Redis TTL)    │
│ Authorization   │ HttpOnly Cookie │ Logout Tokens  │
│ Header          │                 │                │
└─────────────────┴─────────────────┴─────────────────┘
```

---

## 📁 **프로젝트 구조**

### **도메인 구조 (Domain-Driven Design)**
```
src/main/java/com/example/login/
├── MemberApplication.java                 # 메인 스프링 부트 애플리케이션
│
├── domain/                               # 도메인 계층
│   ├── auth/                            # 인증 도메인
│   │   ├── controller/                  # 인증 API 컨트롤러
│   │   │   └── AuthApiController.java   # 토큰 관리, 로그아웃
│   │   ├── service/                     # 인증 도메인 서비스
│   │   │   ├── AuthenticationService.java
│   │   │   ├── TokenValidator.java
│   │   │   ├── RefreshTokenService.java
│   │   │   └── BlacklistService.java
│   │   ├── entity/                      # 인증 엔티티
│   │   │   └── RefreshToken.java
│   │   ├── repository/                  # 데이터 접근 계층
│   │   │   └── RefreshTokenRepository.java
│   │   └── dto/                         # 데이터 전송 객체
│   │       └── response/
│   │           └── TokenResponse.java
│   │
│   └── member/                          # 회원 도메인
│       ├── controller/                  # 회원 관련 컨트롤러
│       │   ├── JoinApiController.java   # 회원가입, 이메일 중복확인
│       │   └── UserApiController.java   # 회원 CRUD 관리
│       ├── service/                     # 회원 도메인 서비스
│       │   └── MemberService.java
│       ├── entity/                      # 회원 엔티티
│       │   ├── MemberEntity.java
│       │   └── Role.java
│       ├── repository/                  # 회원 데이터 접근
│       │   └── MemberRepository.java
│       ├── security/                    # 회원별 인증 구현
│       │   ├── CustomUserDetails.java
│       │   └── CustomUserDetailsService.java
│       └── dto/                         # 요청/응답 DTO
│           ├── request/
│           │   ├── MemberSaveRequest.java
│           │   ├── MemberLoginRequest.java
│           │   └── MemberUpdateRequest.java
│           └── response/
│               ├── MemberResponse.java
│               └── MemberLoginResponse.java
│
├── global/                              # 전역 공통기능
│   ├── config/                          # 설정 클래스들
│   │   ├── SecurityConfig.java          # 메인 보안 설정
│   │   ├── JwtSecurityConfig.java       # JWT 관련 설정
│   │   ├── OAuth2SecurityConfig.java    # OAuth2 설정
│   │   ├── SwaggerConfig.java           # API 문서화 설정
│   │   ├── RateLimitConfig.java         # 요청 제한 설정
│   │   ├── CorsConfig.java              # CORS 설정
│   │   ├── RedisConfig.java             # Redis 설정
│   │   ├── WebConfig.java               # 웹 설정
│   │   └── properties/                  # 설정 프로퍼티
│   │       └── SecurityProperties.java
│   │
│   ├── jwt/                             # JWT 관련 유틸리티
│   │   ├── JWTUtil.java                 # JWT 생성/검증 유틸
│   │   ├── LoginFilter.java             # 로그인 필터
│   │   ├── JwtAuthenticationFilter.java # JWT 인증 필터
│   │   └── JwtTokenService.java         # 토큰 서비스
│   │
│   ├── oauth2/                          # OAuth2 소셜 로그인
│   │   ├── handler/                     # 성공/실패 핸들러
│   │   │   ├── OAuth2LoginSuccessHandler.java
│   │   │   └── OAuth2LoginFailureHandler.java
│   │   ├── service/                     # OAuth2 서비스
│   │   │   ├── CustomOAuth2UserService.java
│   │   │   └── OAuth2TokenService.java
│   │   ├── strategy/                    # 제공업체 전략패턴
│   │   │   ├── SocialLoginStrategy.java
│   │   │   ├── SocialLoginStrategyManager.java
│   │   │   ├── GoogleLoginStrategy.java
│   │   │   ├── NaverLoginStrategy.java
│   │   │   └── KakaoLoginStrategy.java
│   │   ├── userInfo/                    # 제공업체 사용자정보
│   │   │   ├── OAuth2UserInfo.java
│   │   │   ├── GoogleOAuth2UserInfo.java
│   │   │   ├── NaverOAuth2UserInfo.java
│   │   │   └── KakaoOAuth2UserInfo.java
│   │   ├── user/                        # OAuth2 사용자
│   │   │   └── CustomOAuth2User.java
│   │   ├── entity/                      # 소셜 타입 엔티티
│   │   │   └── SocialType.java
│   │   └── dto/                         # OAuth2 관련 DTO
│   │       ├── OAuthAttributes.java
│   │       └── OAuthLoginResponse.java
│   │
│   ├── exception/                       # 예외 처리
│   │   ├── GlobalExceptionHandler.java  # 글로벌 예외 처리
│   │   └── BaseException.java           # 기본 예외 클래스
│   │
│   ├── response/                        # 응답 규격화
│   │   ├── ApiResponseAdvice.java       # 응답 래핑 AOP
│   │   ├── AutoApiResponse.java         # 자동 응답 래핑 어노테이션
│   │   ├── ErrorCode.java               # 에러 코드 열거형
│   │   ├── ErrorType.java               # 에러 타입 인터페이스
│   │   ├── ErrorInfo.java               # 에러 정보 클래스
│   │   ├── MemberSuccessCode.java       # 성공 코드 열거형
│   │   ├── SuccessCode.java             # 성공 코드 어노테이션
│   │   └── SuccessType.java             # 성공 타입 인터페이스
│   │
│   ├── swagger/                         # Swagger 확장
│   │   ├── CustomExceptionDescription.java  # 예외 문서화
│   │   ├── SwaggerResponseDescription.java  # 응답 설명
│   │   └── ExampleHolder.java           # 예제 홀더
│   │
│   ├── entity/                          # 전역 엔티티
│   │   └── BaseTimeEntity.java          # 생성/수정 시간 공통 엔티티
│   │
│   ├── dto/                             # 공통 DTO
│   │   └── CommonApiResponse.java       # 통합 응답 래퍼
│   │
│   ├── advice/                          # AOP 어드바이스
│   │   └── ParameterData.java           # 파라미터 데이터
│   │
│   └── interceptor/                     # 전역 인터셉터
│       └── RateLimitInterceptor.java    # 요청 제한 인터셉터
```

### **리소스 구조**
```
src/main/resources/
├── application.yml              # 메인 설정 파일
└── application-oauth.yml        # OAuth2 설정
```

---

## 🔐 **인증 시스템**

### **JWT 토큰 명세**
```
┌─────────────────┬─────────────────────────────────────────┐
│ Token Type      │ Specifications                          │
├─────────────────┼─────────────────────────────────────────┤
│ Access Token    │ • Lifetime: 1 hour                      │
│                 │ • Storage: Authorization header         │
│                 │ • Format: Bearer {token}                │
│                 │ • Purpose: API 인증                     │
├─────────────────┼─────────────────────────────────────────┤
│ Refresh Token   │ • Lifetime: 24 hours                    │
│                 │ • Storage: HttpOnly Cookie              │
│                 │ • Purpose: Access Token 갱신            │
│                 │ • Security: XSS 공격방지                │
└─────────────────┴─────────────────────────────────────────┘
```

### **인증 플로우**
```
1. 로그인 요청 → LoginFilter
2. 인증 성공 → JWT 토큰 발급
3. Access Token → Authorization Header
4. Refresh Token → HttpOnly Cookie & Redis 저장
5. API 요청 → JwtAuthenticationFilter 검증
6. 토큰 만료 → Refresh Token으로 갱신
7. 로그아웃 → Blacklist 등록 (Redis TTL)
```

### **권한 시스템**
```yaml
Roles:
  USER:   # 일반 사용자
    - 개인정보 조회/수정
    - 기본 API 접근
  
  ADMIN:  # 관리자
    - 모든 사용자 정보 접근
    - 시스템 관리 기능
  
  GUEST:  # 소셜 로그인 임시 사용자
    - 추가 정보 입력 대기
    - 제한적 기능 접근
```

### **OAuth2 소셜 로그인**
```
지원 제공업체:
  • Google OAuth2
  • Naver OAuth2
  • Kakao OAuth2

처리절차:
1. 소셜 로그인 요청
2. OAuth2 인증 성공
3. 제공업체별 사용자 정보 추출 (Strategy Pattern)
4. 기존 사용자 확인 또는 신규 사용자 생성
5. JWT 토큰 발급
6. 클라이언트 리다이렉션 (쿠키 포함)
```

---

## 📋 **API 응답 규격화**

### **통합 응답 구조**
모든 API는 `CommonApiResponse<T>` 구조로 응답합니다.

```json
{
  "code": "S205",
  "message": "회원가입 성공",
  "data": {
    "id": 1,
    "memberEmail": "user@example.com",
    "memberName": "사용자",
    "role": "USER"
  }
}
```

### **응답 데이터 명세**
```yaml
CommonApiResponse<T>:
  code:     # 응답 코드
    type: String
    pattern: "S[0-9]{3}" (성공) | "E[0-9]{3}" (실패)
    description: S(Success) | E(Error)
  
  message:  # 사용자용 결과 메시지
    type: String
    description: 클라이언트용 표시가능 메시지
  
  data:     # 실제 데이터 (성공시), null 또는 상세정보 (실패시)
    type: Generic<T>
    description: API별 응답 데이터 또는 에러 상세정보
```

### **성공 응답 코드**
```yaml
MemberSuccessCode:
  S200: "성공"
  S201: "로그인 성공"
  S201: "로그아웃 성공"  
  S202: "Access 토큰 재발급 성공"
  S203: "Access/Refresh 토큰 재발급 성공"
  S204: "이메일 사용 가능"
  S205: "회원가입 성공"
  S206: "회원 정보 수정 성공"
  S207: "회원 삭제 성공"
  S208: "회원 정보 조회 성공"
  S209: "소셜 로그인 성공"
```

### **자동 응답 래핑**
```java
// 컨트롤러에 @AutoApiResponse 적용 → ApiResponseAdvice가 자동 래핑
@RestController
@AutoApiResponse  // 모든 응답을 CommonApiResponse로 래핑
@RequestMapping("/api/join")
public class JoinApiController {
    
    @PostMapping
    @SuccessCode(MemberSuccessCode.MEMBER_CREATED)  // 특정 성공 코드 지정
    public void join(@Valid @RequestBody MemberSaveRequest req) {
        memberService.save(req);
        // void 반환 → CommonApiResponse.success(MEMBER_CREATED)로 자동 변환
    }
    
    @GetMapping("/email-check")
    @SuccessCode(MemberSuccessCode.EMAIL_CHECK_OK)
    public void emailCheck(@RequestParam String memberEmail) {
        // boolean 결과를 예외로 처리, 성공시 자동 래핑
        if (!memberService.isEmailAvailable(memberEmail)) {
            throw new BaseException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
}
```

---

## ⚡ **예외 처리 규격**

### **예외 처리 계층구조**
```
Exception Hierarchy:
├── RuntimeException
    ├── BaseException                    # 커스텀 기본 예외 클래스
    ├── UsernameNotFoundException        # Spring Security 예외
    ├── MethodArgumentNotValidException  # Bean Validation 예외
    └── InvalidFormatException          # JSON 변환 예외 (Enum 등)

Response Types:
├── ErrorType                           # 에러 타입 인터페이스
    └── ErrorCode                       # 에러 코드 열거형 (구현체)
└── SuccessType                         # 성공 타입 인터페이스  
    └── MemberSuccessCode               # 성공 코드 열거형 (구현체)
```

### **실패 코드 체계 (ErrorCode)**
```yaml
ErrorCode (HTTP Status 기준):
  
  E400 (Bad Request):
    - INVALID_ROLE: "잘못된 역할(Role)입니다"
    - TOKEN_MALFORMED: "잘못된 형식의 토큰입니다"  
    - INVALID_INPUT_VALUE: "잘못된 입력값입니다"
    - PARAMETER_VALIDATION_ERROR: "파라미터 검증에 실패했습니다"
  
  E401 (Unauthorized):
    - LOGIN_FAIL: "이메일 또는 비밀번호가 틀렸습니다"
    - INVALID_TOKEN: "유효하지 않은 토큰입니다"
    - TOKEN_EXPIRED: "만료된 토큰입니다"
    - REFRESH_TOKEN_NOT_FOUND: "리프레시 토큰을 찾을 수 없습니다"
    - ACCESS_TOKEN_REQUIRED: "액세스 토큰이 필요합니다"
    - TOKEN_BLACKLISTED: "차단된 토큰입니다"
    - OAUTH2_LOGIN_FAILED: "소셜 로그인에 실패했습니다. 다시 시도해주세요."
  
  E404 (Not Found):
    - MEMBER_NOT_FOUND: "회원을 찾을 수 없습니다"
  
  E409 (Conflict):
    - DUPLICATE_EMAIL: "이미 가입된 이메일입니다"
  
  E429 (Too Many Requests):
    - TOO_MANY_REQUESTS: "너무 많은 요청입니다. 잠시 후 다시 시도해주세요."
  
  E500 (Internal Server Error):
    - INTERNAL_SERVER_ERROR: "내부 서버 오류가 발생했습니다"
```

### **Bean Validation 실패 응답**
```json
{
  "code": "E400",
  "message": "파라미터 검증에 실패했습니다",
  "data": [
    {
      "key": "memberEmail",
      "value": "invalid-email", 
      "reason": "이메일 형식이 올바르지 않습니다."
    },
    {
      "key": "memberPassword",
      "value": "null",
      "reason": "비밀번호는 필수입니다."
    }
  ]
}
```

### **글로벌 예외 처리 (GlobalExceptionHandler)**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // 1. 커스텀 예외 처리
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e);
    
    // 2. Enum 변환 에러 처리 (Role 등)
    @ExceptionHandler(InvalidFormatException.class)
    public ResponseEntity<?> handleInvalidFormat(InvalidFormatException e);
    
    // 3. Bean Validation 실패 예외 (ParameterData 리스트로 상세 정보 제공)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e);
    
    // 4. 회원 없음 예외 (Spring Security)
    @ExceptionHandler(UsernameNotFoundException.class)
    public ResponseEntity<?> handleUsernameNotFound(UsernameNotFoundException e);
}

// ParameterData 구조
public class ParameterData {
    private String key;      // 필드명
    private String value;    // 입력값 (null인 경우 "null" 문자열)
    private String reason;   // 검증 실패 이유
}
```

---

## 📚 **Swagger 문서화 규격**

### **자동 문서화 기능**
- **JWT Bearer 보안 스키마**: `@PreAuthorize` 어노테이션 감지시 자동으로 JWT 인증 요구사항 추가
- **예외 응답 자동생성**: `@CustomExceptionDescription`으로 HTTP 상태코드별 에러 응답 예제 자동생성
- **성공 코드 명시**: `@SuccessCode`를 통한 특정 성공 응답 코드 지정
- **Bean Validation 에러**: `PARAMETER_VALIDATION_ERROR` 시 ParameterData 배열 예제 자동생성
- **API 그룹화**: Controller의 `@Tag` 어노테이션을 통한 기능별 그룹화

### **문서화 어노테이션 예시**
```java
@RestController
@RequestMapping("/api/users")  
@AutoApiResponse
@Tag(name = "회원 관리 API", description = "사용자 조회, 수정, 삭제 관련 API")
public class UserApiController {
    
    @Operation(
        summary = "전체 사용자 목록 조회",
        description = "가입된 모든 사용자의 정보를 조회합니다."
    )
    @ApiResponse(responseCode = "200", description = "조회 성공")
    @CustomExceptionDescription(SwaggerResponseDescription.MEMBER_ERROR)
    @SuccessCode(MemberSuccessCode.MEMBER_VIEW)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")  // JWT 보안 스키마 자동 추가
    public List<MemberResponse> findAll() { ... }
    
    @Operation(summary = "사용자 정보 수정")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "수정 성공"),
        @ApiResponse(responseCode = "400", description = "입력값 유효성 실패", content = @Content),
        @ApiResponse(responseCode = "404", description = "해당 사용자가 존재하지 않음", content = @Content)
    })
    @CustomExceptionDescription(SwaggerResponseDescription.MEMBER_ERROR)
    @SuccessCode(MemberSuccessCode.MEMBER_UPDATED)
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('USER')")
    public void update(@PathVariable Long id, @RequestBody MemberUpdateRequest req) { ... }
}
```

### **SwaggerResponseDescription 에러 그룹**
```java
public enum SwaggerResponseDescription {
    
    MEMBER_ERROR(Set.of(
        ErrorCode.MEMBER_NOT_FOUND,           // E404: "회원을 찾을 수 없습니다"
        ErrorCode.DUPLICATE_EMAIL             // E409: "이미 가입된 이메일입니다"
    )),
    
    MEMBER_JOIN_ERROR(Set.of(
        ErrorCode.DUPLICATE_EMAIL,            // E409: "이미 가입된 이메일입니다"
        ErrorCode.PARAMETER_VALIDATION_ERROR, // E400: "파라미터 검증에 실패했습니다"
        ErrorCode.INVALID_INPUT_VALUE         // E400: "잘못된 입력값입니다"
    )),
    
    AUTH_ERROR(Set.of(
        ErrorCode.INVALID_TOKEN,              // E401: "유효하지 않은 토큰입니다"
        ErrorCode.REFRESH_TOKEN_NOT_FOUND,    // E401: "리프레시 토큰을 찾을 수 없습니다"
        ErrorCode.LOGIN_FAIL                  // E401: "이메일 또는 비밀번호가 틀렸습니다"
    )),
    
    COMMON_ERROR(Set.of(
        ErrorCode.INTERNAL_SERVER_ERROR,      // E500: "내부 서버 오류가 발생했습니다"
        ErrorCode.INVALID_INPUT_VALUE         // E400: "잘못된 입력값입니다"
    ))
}
```

### **자동 생성 문서 요소**
```yaml
OperationCustomizer 기능:
  - JWT 보안 스키마: @PreAuthorize 감지시 자동으로 "JWT" 보안 요구사항 추가
  - 에러 응답 예제: @CustomExceptionDescription의 SwaggerResponseDescription에 정의된 에러코드들을 HTTP 상태코드별로 그룹화하여 응답 예제 자동생성
  - Bean Validation 예제: PARAMETER_VALIDATION_ERROR 시 ParameterData 구조로 상세한 검증 실패 정보 제공
  - HTTP 상태코드별 분류: 동일한 상태코드의 에러들을 하나의 응답으로 묶어서 여러 예제로 표시

SwaggerConfig 설정:
  - OpenAPIDefinition: API 정보, 연락처, 라이선스 정보
  - SecurityScheme: JWT Bearer 토큰 방식 (Authorization 헤더)
  - 개발서버: http://localhost:8080
  - ExampleHolder: 에러 응답 예제를 담는 래퍼 클래스
```

### **Swagger UI 접근 및 API 정보**
```yaml
접속 URL:
  개발환경: http://localhost:8080/api-docs
  
API 문서 정보:
  title: "Spring Login API 문서"
  description: "JWT 인증 기반 로그인 시스템의 REST API 문서입니다."
  version: "v1.0.0"
  contact:
    name: "김규일"
    email: "rlarbdlf222@gmail.com"
    url: "https://github.com/Kimgyuilli"
  license:
    name: "MIT License"
    url: "https://opensource.org/licenses/MIT"

보안 스키마:
  name: "JWT"
  type: "HTTP Bearer"
  scheme: "bearer"
  bearerFormat: "JWT"
  location: "Authorization Header"
```

---

## 🛠️ **개발 가이드라인**

### **새로운 API 개발 체크리스트**
- [ ] `@AutoApiResponse` 어노테이션 적용
- [ ] Request/Response DTO 생성
- [ ] Bean Validation 어노테이션 적용 
- [ ] 도메인 로직은 Service 계층에 구현
- [ ] 커스텀 예외 처리 및 에러 코드 열거형
- [ ] `@PreAuthorize`로 권한 설정
- [ ] Swagger 문서화 어노테이션 적용 
- [ ] 트랜잭션 경계 설정 (`@Transactional`)

### **코딩 컨벤션**
```yaml
레이어 구조:
  - controller: API 엔드포인트 (로직없음)
  - service: 도메인 로직 (트랜잭션 경계)
  - repository: 데이터 접근 (JPA Repository)
  - dto.request: 요청 DTO
  - dto.response: 응답 DTO
  - entity: JPA 엔티티

네이밍:
  - 컨트롤러: {Domain}Controller
  - 서비스: {Domain}Service  
  - 리포지토리: {Domain}Repository
  - DTO: {Action}{Domain}Request/Response
  - 엔티티: {Domain}Entity
```

### **보안 개발 가이드라인**
- JWT 키값 등 민감정보를 환경변수로 분리
- 사용자 입력 검증을 철저히 수행
- SQL Injection 방지를 위한 JPA 쿼리 사용
- XSS 방지를 위한 입력값 검증
- CSRF 방지를 위한 StatelessSessionCreationPolicy 사용
- Rate Limiting으로 과도한 요청 방지

---

## 🧪 **API 테스트**

### **Postman 테스트 환경 변수**
```json
{
  "baseUrl": "http://localhost:8080",
  "accessToken": "",
  "testEmail": "test@example.com",
  "testPassword": "password123",
  "testName": "테스터"
}
```

### **테스트 시나리오**
```
1. 기본 인증 플로우:
   POST /api/join → 로그인 (Security Filter) → GET /api/users → POST /api/auth/logout

2. 토큰 갱신 플로우:
   로그인 → POST /api/auth/token/refresh → GET /api/users
   로그인 → POST /api/auth/token/refresh/full → GET /api/users

3. 회원 관리 플로우 (ADMIN 권한):
   GET /api/users → GET /api/users/{id} → PUT /api/users/{id} → DELETE /api/users/{id}

4. 에러 케이스:
   - 잘못된 이메일 형식 요청 (POST /api/join)
   - 중복 이메일 회원가입 시도
   - 만료된 토큰 사용
   - 권한 부족한 API 호출 (USER → ADMIN 기능)

5. Rate Limiting 테스트:
   - 로그인 엔드포인트 연속 호출 (최대 5회/분 제한)
```

### **핵심 테스트 시나리오**

#### **회원 가입**
```http
POST /api/join
{
  "memberEmail": "test@example.com",
  "memberName": "테스터", 
  "memberPassword": "password123"
}
```

#### **로그인** 
```http
POST /login  # Spring Security Filter Chain
{
  "memberEmail": "test@example.com",
  "memberPassword": "password123"
}
# 응답: Authorization 헤더에 Access Token, 쿠키에 Refresh Token
```

#### **인증 API 호출**
```http
GET /api/users
Authorization: Bearer {accessToken}
```

#### **토큰 갱신**
```http
# Access Token만 갱신
POST /api/auth/token/refresh
Cookie: refresh={refreshToken}

# Access + Refresh Token 모두 갱신
POST /api/auth/token/refresh/full
Cookie: refresh={refreshToken}
```

#### **회원 관리 (ADMIN 권한 필요)**
```http
# 전체 회원 목록 조회
GET /api/users
Authorization: Bearer {accessToken}

# 특정 회원 조회
GET /api/users/{id}
Authorization: Bearer {accessToken}

# 회원 정보 수정
PUT /api/users/{id}
Authorization: Bearer {accessToken}
{
  "memberName": "새이름",
  "memberEmail": "new@example.com"
}

# 회원 삭제
DELETE /api/users/{id}
Authorization: Bearer {accessToken}
```

#### **이메일 중복 확인**
```http
GET /api/join/email-check?memberEmail=test@example.com
```

---

## 🚀 **운영 가이드**

### **환경변수 설정**
```bash
# Database
DB_USERNAME=your_db_user
DB_PASSWORD=your_db_password

# Redis  
REDIS_PASSWORD=your_redis_password

# JWT
JWT_SECRET_KEY=your-secret-key-min-256-bits
JWT_ACCESS_EXPIRATION=3600000    # 1시간
JWT_REFRESH_EXPIRATION=86400000  # 24시간

# OAuth2 (선택사항)
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
NAVER_CLIENT_ID=your_naver_client_id  
NAVER_CLIENT_SECRET=your_naver_client_secret
KAKAO_CLIENT_ID=your_kakao_client_id
KAKAO_CLIENT_SECRET=your_kakao_client_secret

# Security
COOKIE_SECURE=true              # HTTPS 환경에서 true
CORS_ALLOWED_ORIGINS=https://yourdomain.com
```

### **운영환경 체크리스트**
- [ ] 환경변수 설정 완료
- [ ] HTTPS 설정 및 SSL 인증서 적용
- [ ] Database 커넥션 풀 최적화
- [ ] Redis 클러스터 인증 설정
- [ ] CORS 허용 도메인 제한
- [ ] 쿠키 보안 설정 (secure=true)
- [ ] 로깅 레벨 조정 (SQL 로그 비활성화)
- [ ] OAuth2 리다이렉션 URL 등록
- [ ] Rate Limiting 임계값 조정
- [ ] 모니터링 및 알림 설정

### **Docker 운영**
```bash
# 운영환경 실행
docker-compose -f docker-compose.prod.yml up --build -d

# 로그 확인
docker-compose logs -f spring-app

# 헬스 체크
curl http://localhost:8080/actuator/health
```

---

## 📈 **성능 및 확장성**

### **성능 최적화**
```yaml
Database:
  - 이메일, 소셜ID, 권한별 인덱스 적용
  - JPA N+1 문제 방지
  - 배치 쿼리 및 트랜잭션 최적화

Cache:
  - Redis 기반 Refresh Token 저장
  - JWT 블랙리스트 TTL 관리
  - 세션 클러스터링 지원

Security:
  - Stateless 세션 구조
  - JWT 기반 분산 인증
  - Rate Limiting으로 부하 분산
```

### **확장 가능한 아키텍처 설계**
- **도메인 기반 패키지**: 새로운 도메인 추가 용이
- **Strategy Pattern**: OAuth2 제공업체 확장 가능
- **인터셉터 패턴**: 횡단 관심사 처리 용이
- **설정 외부화**: 환경별 설정 분리
- **Docker 컨테이너**: 클라우드 확장 지원

---

## 🤝 **기여 가이드**

### **개발 환경 설정**
1. Java 21 설치
2. Docker 및 Docker Compose 설치
3. IDE 설정 (IntelliJ IDEA 권장)
4. Git hooks 설정 (pre-commit)

### **커밋 메시지**
```
feat: 새로운 기능 추가
fix: 버그 수정  
docs: 문서 수정
style: 코드 포맷팅
refactor: 코드 리팩토링
test: 테스트 코드 추가/수정 
chore: 빌드 관련 설정 파일 수정
```

---

## 📞 **지원 및 문의**

- **Issue 제기**: GitHub Issues 페이지 활용
- **기능 문의**: 프로젝트 Discussions 활용
- **보안 취약점**: 직접 메일 전송

---

## 📄 **라이선스**

이 프로젝트는 MIT 라이선스 하에 공개되었습니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 확인하세요.

---

**이제 이 프로젝트를 사용하여 강력하고 확장 가능한 Spring Boot 인증 시스템을 빠르게 구축하세요!**
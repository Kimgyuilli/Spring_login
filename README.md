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
│   │   ├── controller/                  # 인증 관련 컨트롤러
│   │   ├── service/                     # 인증 도메인 서비스
│   │   ├── entity/                      # 인증 엔티티 (RefreshToken)
│   │   ├── repository/                  # 데이터 접근 계층
│   │   └── dto/                         # 데이터 전송 객체
│   │
│   ├── member/                          # 회원 도메인
│   │   ├── controller/                  # 회원 관련 컨트롤러
│   │   ├── service/                     # 회원 도메인 서비스
│   │   ├── entity/                      # 회원 엔티티
│   │   ├── repository/                  # 회원 데이터 접근
│   │   ├── security/                    # 회원별 인증 구현
│   │   └── dto/                         # 요청/응답 DTO
│   │
│   └── Home/                            # 홈 도메인
│       └── controller/                  # 홈 컨트롤러
│
├── global/                              # 전역 공통기능
│   ├── config/                          # 설정 클래스들
│   │   ├── SecurityConfig.java          # 보안 전체 설정
│   │   ├── JwtSecurityConfig.java       # JWT 관련 설정
│   │   ├── OAuth2SecurityConfig.java    # OAuth2 설정
│   │   ├── SwaggerConfig.java           # API 문서화 설정
│   │   ├── RateLimitConfig.java         # 요청 제한 설정
│   │   └── properties/                  # 설정 프로퍼티 클래스들
│   │
│   ├── jwt/                             # JWT 관련 유틸리티
│   │   ├── JWTUtil.java                 # JWT 생성/검증 유틸
│   │   ├── LoginFilter.java             # 로그인 필터
│   │   ├── JwtAuthenticationFilter.java # JWT 인증 필터
│   │   └── JwtTokenService.java         # 토큰 서비스
│   │
│   ├── oauth2/                          # OAuth2 소셜 로그인
│   │   ├── handler/                     # 성공/실패 핸들러
│   │   ├── service/                     # OAuth2 서비스
│   │   ├── strategy/                    # 제공업체 전략패턴
│   │   ├── userInfo/                    # 제공업체 사용자정보
│   │   └── dto/                         # OAuth2 관련 DTO
│   │
│   ├── exception/                       # 예외 처리
│   │   ├── GlobalExceptionHandler.java  # 글로벌 예외 처리
│   │   └── BaseException.java           # 기본 예외 클래스
│   │
│   ├── response/                        # 응답 규격화
│   │   ├── CommonApiResponse.java       # 통합 응답 래퍼
│   │   ├── ErrorCode.java              # 실패 코드 열거형
│   │   ├── MemberSuccessCode.java      # 성공 코드 열거형
│   │   └── AutoApiResponse.java        # 자동 응답 래핑 어노테이션
│   │
│   ├── swagger/                         # Swagger 확장
│   │   ├── CustomExceptionDescription.java  # 예외 문서화
│   │   └── SwaggerResponseDescription.java  # 응답 설명
│   │
│   └── interceptor/                     # 전역 인터셉터
│       └── RateLimitInterceptor.java    # 요청 제한 인터셉터
```

### **리소스 구조**
```
src/main/resources/
├── application.yml              # 메인 설정 파일
├── application-oauth.yml        # OAuth2 설정
├── application-dev.yml          # 개발환경 설정
└── application-prod.yml         # 운영환경 설정
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
  "code": "M001",
  "message": "회원 등록 성공",
  "result": {
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
  code:     # 응답 코드 (성공: 200대, 실패: 400-500대)
    type: String
    pattern: "[MAEHS][0-9]{3}"
    description: M(Member), A(Auth), E(Error), H(Home), S(System)
  
  message:  # 사용자용 결과 메시지
    type: String
    description: 클라이언트용 표시가능 메시지
  
  result:   # 실제 데이터 (성공시), null (실패시)
    type: Generic<T>
    description: API별 응답 데이터
```

### **성공 응답 코드**
```yaml
Member (M):
  M001: "회원 등록 성공"           (201)
  M002: "이메일 사용가능 확인"         (200)
  M003: "중복 이메일 확인"      (409)
  M004: "로그인 성공"            (200)
  M005: "토큰 재발급 성공"        (200)
  M006: "로그아웃 성공"          (200)
  M007: "회원 정보 조회 성공"      (200)
  M008: "회원 목록 조회 성공"      (200)
  M009: "회원 정보 수정 성공"      (200)
  M010: "회원 삭제 성공"         (200)
  M011: "소셜 로그인 성공"        (200)

Auth (A):
  A001: "토큰 검증 성공"         (200)
  A002: "로그아웃 처리 완료"      (200)
```

### **자동 응답 래핑**
```java
// 컨트롤러에 @AutoApiResponse 적용 → 자동 래핑
@RestController
@AutoApiResponse  // 모든 응답을 CommonApiResponse로 래핑
public class MemberController {
    
    @PostMapping
    public MemberResponse save(@Valid @RequestBody MemberSaveRequest req) {
        // MemberResponse 반환 → CommonApiResponse<MemberResponse>로 자동 변환
        return memberService.save(req);
    }
}
```

---

## ⚡ **예외 처리 규격**

### **예외 처리 계층구조**
```
Exception Hierarchy:
├── RuntimeException
    └── BaseException                    # 기본 예외 클래스
        ├── BusinessLogicException       # 도메인 로직 예외
        ├── AuthenticationException      # 인증 예외  
        └── ValidationException          # 검증 예외
```

### **실패 코드 체계**
```yaml
Error Codes (HTTP Status 기준):
  
  E400 (Bad Request):
    - INVALID_ROLE: "유효하지 않은 권한(Role)입니다"
    - TOKEN_MALFORMED: "형식이 잘못된 토큰입니다"  
    - INVALID_INPUT_VALUE: "잘못된 입력값입니다"
    - PARAMETER_VALIDATION_ERROR: "파라미터 검증에 실패했습니다"
  
  E401 (Unauthorized):
    - LOGIN_FAIL: "이메일 또는 비밀번호가 올바르지 않습니다"
    - INVALID_TOKEN: "유효하지 않은 토큰입니다"
    - TOKEN_EXPIRED: "만료된 토큰입니다"
    - REFRESH_TOKEN_NOT_FOUND: "리프레시 토큰을 찾을 수 없습니다"
    - TOKEN_BLACKLISTED: "무효화된 토큰입니다"
    - OAUTH2_LOGIN_FAILED: "소셜 로그인에 실패했습니다"
  
  E404 (Not Found):
    - MEMBER_NOT_FOUND: "회원을 찾을 수 없습니다"
  
  E409 (Conflict):
    - DUPLICATE_EMAIL: "중복된 이메일입니다"
  
  E429 (Too Many Requests):
    - TOO_MANY_REQUESTS: "너무 많은 요청입니다. 잠시 후 다시 시도해주세요"
  
  E500 (Internal Server Error):
    - INTERNAL_SERVER_ERROR: "서버 내부 오류가 발생했습니다"
```

### **Bean Validation 실패 응답**
```json
{
  "code": "E400",
  "message": "파라미터 검증에 실패했습니다",
  "result": [
    {
      "parameter": "memberEmail",
      "value": "invalid-email",
      "reason": "이메일 형식이 올바르지 않습니다."
    },
    {
      "parameter": "memberPassword",
      "value": "",
      "reason": "비밀번호는 필수입니다."
    }
  ]
}
```

### **글로벌 예외 처리**
```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    // Bean Validation 예외
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidationExceptions(MethodArgumentNotValidException e);
    
    // 기본 도메인 예외
    @ExceptionHandler(BaseException.class)
    public ResponseEntity<?> handleBaseException(BaseException e);
    
    // 기타 예외
    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGenericException(Exception e);
}
```

---

## 📚 **Swagger 문서화 규격**

### **자동 문서화 기능**
- **JWT 보안 스키마**: 자동으로 Authorization 헤더 문서화
- **예외 응답 생성**: `@CustomExceptionDescription`으로 가능한 에러 응답들 자동생성
- **성공 코드 명시**: `@SuccessCode`를 통한 성공 응답 명세
- **API 그룹화**: Controller별 태그 그룹 생성

### **문서화 어노테이션**
```java
@RestController
@RequestMapping("/api/users")
@Tag(name = "사용자 관리 API", description = "사용자 CRUD 및 관련 기능")
public class UserController {
    
    @Operation(
        summary = "사용자 정보 조회",
        description = "현재 로그인한 사용자의 정보를 조회합니다."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "조회 성공"),
        @ApiResponse(responseCode = "401", description = "인증 실패", content = @Content),
        @ApiResponse(responseCode = "403", description = "권한 부족", content = @Content)
    })
    @CustomExceptionDescription({
        ErrorCode.INVALID_TOKEN,
        ErrorCode.TOKEN_EXPIRED,
        ErrorCode.ACCESS_DENIED
    })
    @SuccessCode(MemberSuccessCode.MEMBER_LIST_FOUND)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<MemberResponse> findAll() { ... }
}
```

### **자동 생성되는 문서 요소**
```yaml
자동 문서화 항목:
  - JWT Bearer Token 보안 스키마
  - 컨트롤러별 API 그룹화  
  - @PreAuthorize 기반 권한 요구사항
  - CommonApiResponse 기반 응답 스키마
  - Bean Validation 기반 요청 검증
  - 커스텀 에러 응답 예시
  - 성공/실패 응답 코드 매핑
```

### **Swagger UI 접근**
```
개발환경: http://localhost:8080/api-docs
운영환경: https://api.yourdomain.com/api-docs
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
   POST /api/join → POST /api/auth/login → GET /api/users → POST /api/auth/logout

2. 토큰 갱신 플로우:
   POST /api/auth/login → POST /api/auth/token/refresh → GET /api/users

3. 에러 케이스:
   - 잘못된 이메일 형식 요청
   - 중복 로그인 시도
   - 만료된 토큰 사용
   - 권한 부족한 API 호출

4. Rate Limiting 테스트:
   - 로그인 엔드포인트 연속 호출 (최대 5회 제한)
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
POST /api/auth/login
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
POST /api/auth/token/refresh
Cookie: refresh={refreshToken}
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
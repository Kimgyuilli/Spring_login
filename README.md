# 🛡️ Spring Login Project

> Spring Boot 기반의 JWT 인증 시스템 프로젝트  
> REST API 인증 및 사용자 관리, 리프레시 토큰/블랙리스트 처리를 포함한 인증 흐름 구축

---
```declarative
실행 명령어

1. 처음에만
chmod +x restart.sh

# 2. 실행
./restart.sh
```



## 📌 주요 기능 목록

### ✅ 회원 기능 (User)

- **회원가입**
  - 이메일, 이름, 비밀번호 입력
  - 이메일 중복 체크 (AJAX 기반)
  - 기본 권한: `USER`

- **회원 로그인**
  - JWT Access Token + Refresh Token 발급
  - Access Token: HTTP Header  
  - Refresh Token: `HttpOnly` Cookie

- **회원 조회 및 관리**
  - 전체 사용자 목록 조회
  - 특정 사용자 정보 조회
  - 회원 정보 수정 (이름, 비밀번호)
  - 회원 탈퇴 (삭제)

---

### 🔐 인증 및 보안 (JWT / Redis)

- **JWT 기반 인증**
  - Access Token (Authorization Header)
  - Refresh Token (쿠키 저장)
  - 권한(ROLE) 기반 인가 처리 (`USER`, `ADMIN`)

- **Access Token 재발급**
  - 유효한 Refresh Token을 이용하여 Access Token만 재발급

- **Access + Refresh 토큰 재발급**
  - Refresh Token 검증 후 둘 다 재발급 (토큰 갱신 시점)

- **로그아웃 처리**
  - Refresh Token 삭제 (Redis)
  - Access Token 블랙리스트 등록 (Redis + TTL)

- **블랙리스트 토큰 처리**
  - 블랙리스트에 등록된 토큰은 인증 실패 처리
  - `JwtAuthenticationFilter`에서 검사 및 차단

---

### ⚙️ 기술 스택

- **Spring Boot 3.4.4**
- **Spring Security 6**
- **Spring Data JPA + PostgreSQL**
- **Spring Data Redis + Redis**
- **JWT (io.jsonwebtoken 0.12.5)**
- **Lombok, Validation**
- **Postman / Swagger 연동 테스트용 API 제공**

---

### 📚 API 문서 (Swagger UI)

- **Swagger UI 경로**: `/docs`
- 그룹 분리 예정 (예: User / Auth / Admin 등)
- 다크모드 및 커스텀 로고 지원 예정

---

### 🧪 테스트 순서 가이드 (Postman)

1. `POST /member/save` → 회원가입  
2. `POST /member/login` → 로그인 후 토큰 발급 확인  
3. `GET /api/users` → Access Token으로 인증 요청  
4. `POST /member/token/refresh` → Access Token 재발급  
5. `POST /member/logout` → 토큰 만료 + 블랙리스트 처리  
6. `GET /api/users` → 블랙리스트 토큰 접근 차단 확인

---

### 🔒 인증 관련 설정 요약

- **Stateless**: 세션 미사용 (JWT만 사용)
- **CORS 설정**: 모든 도메인 허용 (`Access-Control-Allow-*`)
- **보안 필터 구성**
  - `LoginFilter` (JWT 발급)
  - `JwtAuthenticationFilter` (토큰 검증 및 인증 설정)

---

### 💾 데이터베이스

- **회원 테이블 (`member_table`)**
  - `id`, `email`, `password`, `name`, `role`, `createdAt`

- **Redis 저장 구조**
  - `refreshToken:{userId}` → 토큰 문자열
  - `blacklist:{accessToken}` → `"logout"` + TTL

---

### 🌐 소셜 로그인 (OAuth2 + JWT 통합)
- Google, Naver, Kakao 소셜 로그인 지원
 - OAuth2 인증 완료 시 JWT Access/Refresh Token 발급
 - 최초 로그인 시 자동 회원가입 (DB 저장: socialType, socialId 필드 추가)
 - 기존 가입자 → 자동 로그인 처리
- 소셜로그인 흐름
- 소셜 로그인 흐름
  1. 사용자가 소셜 로그인 선택
  2. OAuth2 인증 성공 → 서버가 JWT 토큰 발급
  3. AccessToken (헤더), RefreshToken (쿠키) 제공
  4. 일반 로그인과 동일하게 이후 인증 처리
- 소셜 등록 Provider
  - Google
  - Naver
  - Kakao
- 지원 OAuth2 스펙
  - Authorization Code Grant Type
  - Profile, Email, Nickname 등의 기본 정보 획득

---

> 이 프로젝트는 CSR(React, Vue 등) 클라이언트를 위한 백엔드 인증 서버로도 쉽게 확장 가능합니다

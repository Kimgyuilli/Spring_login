# Spring_login

스프링으로 로그인 기능 고도화 및 깃허브 사용

```declarative
실행 명령어
1. 처음에만
chmod +x restart.sh


# 2. 실행
./restart.sh
```

# 전체 아키텍처 요약
## 인증/인가 구조
JWT 기반 인증 시스템 구현

Access Token: Authorization 헤더 (Bearer {token})

Refresh Token: HttpOnly 쿠키 (refresh)

Spring Security + 커스텀 필터 사용

LoginFilter: 로그인 시 토큰 생성 및 응답 헤더/쿠키 설정

JwtAuthenticationFilter: 요청 시 토큰 검증 → 인증 객체 생성

Access Token 만료 시 /token/refresh를 통해 재발급

## 현재까지 한 작업 상세
1. 회원가입 및 로그인
   POST /member/save : 회원가입

POST /member/login :

사용자 인증 (AuthenticationManager)

AccessToken, RefreshToken 생성

Access Token → Authorization 헤더에 담아 전송

Refresh Token → HttpOnly Cookie + Redis 저장

2. JWT 기반 인증 처리
JwtAuthenticationFilter

Authorization 헤더에서 Access Token 추출

유효성 검사 및 권한 설정

블랙리스트 체크도 추가됨

3. 토큰 재발급
POST /token/refresh

Refresh Token을 쿠키에서 추출

Redis에서 토큰 존재 여부 및 일치 확인

Access + Refresh 토큰 재발급

Redis 토큰 갱신 + 쿠키 갱신

4. 로그아웃
POST /member/logout

쿠키에서 Refresh Token 제거

Redis에서 RefreshToken 삭제

Access Token을 Redis 블랙리스트에 등록

Authorization 헤더 제거

5. Redis 통합
RefreshToken: @RedisHash + TTL 14일

RefreshTokenRepository: Redis에 저장/삭제

BlacklistService: 블랙리스트 등록 및 조회 로직

.env 기반 보안 정보(REDIS_PASSWORD, JWT_SECRET_KEY 등) 설정

## 기타 기술 구성
JWTUtil: JWT 생성/파싱/검증 로직 담당

LoginFilter: 로그인 처리 필터 (UsernamePasswordAuthenticationFilter)

SecurityConfig: 커스텀 필터 및 URL 권한 설정

RedisConfig: Redis 연결 설정

application.yml: profile별 환경, JWT 만료시간, Redis 접속 설정 등 포함

## 추가 작업
- [ ] ssr방식 웹 페이지 리펙토링
- [ ] 소셜로그인 구현
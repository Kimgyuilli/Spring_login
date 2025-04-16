# Spring_login

스프링으로 로그인 기능 고도화 및 깃허브 사용

```declarative
실행 명령어
# 1. JAR 빌드(처음에만)
# 테스트 환경은 지금은 안쓰니 제외 나중에 쓰게 되면 추가로 설정 예정
./gradlew clean build -x test
docker rm -f postgres-db spring-app

# 2. Docker Compose로 실행
docker-compose up --build
```
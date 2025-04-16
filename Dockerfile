# Java 17을 사용하는 슬림한 JDK 이미지
FROM openjdk:17-jdk-slim

# 컨테이너 내 작업 디렉토리 생성
WORKDIR /app

# build/libs 폴더에서 JAR 파일 복사 (와일드카드 사용)
COPY build/libs/member-0.0.1-SNAPSHOT.jar app.jar

# 환경 변수 파일 사용을 위해 `.env` 복사 (선택사항, 있으면)
COPY .env .env

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

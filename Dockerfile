# Multi-stage build for better optimization
FROM gradle:8.5-jdk21 AS builder

# 소스코드 복사
WORKDIR /app
COPY . .

# Gradle build (테스트 스킵하고 빌드만)
RUN gradle clean build -x test

# Runtime stage
FROM openjdk:21-jdk-slim

WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]

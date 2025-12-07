#-------- builder stage --------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Gradle wrapper 및 설정 먼저 복사 (의존성 캐시 활용)
COPY gradlew ./
COPY gradle gradle
COPY build.gradle settings.gradle ./

RUN chmod +x gradlew

RUN ./gradlew dependencies --no-daemon

COPY src src

RUN ./gradlew clean bootJar -x test --no-daemon

#-------- runtime stage --------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

COPY --from=builder /app/build/libs/app.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
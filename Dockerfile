FROM bellsoft/liberica-openjdk-alpine:21 AS builder

WORKDIR /app

COPY ./build/libs/*SNAPSHOT.jar app.jar

CMD ["java", "-jar", "app.jar"]
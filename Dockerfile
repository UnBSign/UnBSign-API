FROM maven:3.9.6-eclipse-temurin-17 AS builder

WORKDIR /app

COPY ./sign /app

RUN mvn clean package -DskipTests

FROM openjdk:17-jdk-slim

WORKDIR /app

COPY ./sign/.env /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
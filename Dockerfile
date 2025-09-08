# =========================
# Stage 1: Build
# =========================
FROM maven:3.9.0-eclipse-temurin-17 AS builder
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn clean package -DskipTests

# =========================
# Stage 2: Run
# =========================
FROM openjdk:17-jdk-slim
WORKDIR /application

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

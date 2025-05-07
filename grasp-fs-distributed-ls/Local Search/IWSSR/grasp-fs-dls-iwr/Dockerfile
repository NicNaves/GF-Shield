# Stage 1: Build
FROM maven:3.9.4-eclipse-temurin-17 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Stage 2: Runtime
FROM openjdk:17
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} grasp-fs-dls-iwr.jar
ENTRYPOINT ["java", "-jar", "/grasp-fs-dls-iwr.jar"]

# Stage 1: Build the application using Maven and JDK 17
FROM maven:3.8.4-openjdk-17 AS build
WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
COPY src ./src
RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# Stage 2: Create a lightweight image with Java 17 and the Spring Boot application
FROM adoptopenjdk/openjdk17:alpine-jre
WORKDIR /app
COPY --from=build /app/target/*.jar /app/application.jar
ENTRYPOINT ["java", "-jar", "application.jar"]

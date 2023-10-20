# Stage 1: Build the application using Maven and JDK 17
FROM maven:3.8.4-openjdk-17
WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
COPY src ./src
# RUN chmod +x mvnw && ./mvnw clean package -DskipTests

# # Stage 2: Create a lightweight image with Java 17 and the Spring Boot application
# FROM openjdk:17-jdk-slim
# WORKDIR /app
# COPY --from=build /app/target/*.jar /app/application.jar
ENTRYPOINT ["mvn", "spring-boot:run"]

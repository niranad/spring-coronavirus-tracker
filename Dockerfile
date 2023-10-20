# For dynamic deploys without a jar file in the project root
# FROM maven:3.8.4-openjdk-17-slim AS package
# WORKDIR /app
# COPY mvnw mvnw.cmd pom.xml ./
# COPY src ./src
# RUN mvn package

# FROM openjdk:17-jdk-slim
# WORKDIR /app
# COPY --from=package app/target/*.jar /app/covid-tracker.jar
# ENTRYPOINT ["java", "-jar", "/app/covid-tracker.jar"]


# For faster deploy with pre-built jar artifact
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY *.jar ./covid-tracker.jar
ENTRYPOINT ["java", "-jar", "/app/covid-tracker.jar"]
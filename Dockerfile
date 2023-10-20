FROM maven:3.8.4-openjdk-11 AS build
WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
RUN mvn clean package -DskipTests
RUN mv /target/coronavirus-tracker-0.0.1-SNAPSHOT.jar /app/covid-tracker.jar


FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY --from=build /app/covid-tracker.jar /app/covid-tracker.jar
ENTRYPOINT ["java", "-jar", "/app/covid-tracker.jar"]

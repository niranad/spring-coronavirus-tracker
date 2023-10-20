FROM eclipse-temurin:17-jdk-jammy
WORKDIR /app
COPY mvnw mvnw.cmd pom.xml ./
CMD ["mvn", "package"]
COPY target/*.jar ./covid-tracker.jar
ENTRYPOINT ["java", "-jar", "/app/covid-tracker.jar"]

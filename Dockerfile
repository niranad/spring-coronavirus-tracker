FROM eclipse-temurin:17-jdk-jammy
VOLUME /tmp
COPY target/*.jar ./covid-tracker.jar
ENTRYPOINT ["java", "-jar", "/covid-tracker.jar"]

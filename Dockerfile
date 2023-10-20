FROM maven:3.8.4-openjdk-17
WORKDIR /app
COPY src ./src
COPY mvnw ./
COPY mvnw.cmd ./
COPY pom.xml ./
ENTRYPOINT ["mvnw", "spring-boot:run"]

FROM maven:latest AS build

WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src/ /app/src/
RUN mvn package -DskipTests

FROM openjdk:17-jdk-slim

EXPOSE 8080

COPY --from=build /app/target/PlanIT-1.0.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]
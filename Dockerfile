FROM maven:3.8.2-jdk-11 AS build
COPY . .

FROM openjdk:17
COPY --from=build /target/PlanIT-0.0.1.jar PlanIT.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","PlanIT.jar"]
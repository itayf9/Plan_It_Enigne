FROM openjdk:17
EXPOSE 8080
ADD target/PlanIT-0.0.1.jar PlanIT-0.0.1.jar
ENTRYPOINT ["java", "-jar", "/PlanIT-0.0.1.jar"]

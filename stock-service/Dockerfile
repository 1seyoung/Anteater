FROM openjdk:17-jdk-slim
WORKDIR /app
COPY build/libs/stock-service-0.0.1-SNAPSHOT.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
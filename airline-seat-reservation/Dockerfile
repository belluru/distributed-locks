# Use a base image with Maven and JDK 11
FROM eclipse-temurin:11-jdk-alpine AS build

# Accept main class as build argument
ARG MAIN_CLASS=com.example.DistributedLock

# Install Maven
RUN apk add --no-cache maven

# Copy the project files
COPY src /app/src
COPY pom.xml /app

# Build the project with specified main class
RUN sed -i "s|<mainClass>.*</mainClass>|<mainClass>${MAIN_CLASS}</mainClass>|" /app/pom.xml && \
    mvn -f /app/pom.xml clean package

# Use a smaller base image for the final application
FROM eclipse-temurin:11-jre-alpine

COPY --from=build /app/target/airline-seat-reservation-1.0-SNAPSHOT.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]

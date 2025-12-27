# Use a base image with Maven and JDK 11
FROM maven:3.8.5-openjdk-11 AS build

# Copy the project files
COPY src /app/src
COPY pom.xml /app

# Build the project
RUN mvn -f /app/pom.xml clean package

# Use a smaller base image for the final application
FROM openjdk:11-jre-slim

# Copy the built JAR from the build stage
COPY --from=build /app/target/distributed-lock-example-1.0-SNAPSHOT.jar /app.jar

# Set the entrypoint for the container
ENTRYPOINT ["java", "-jar", "/app.jar"]

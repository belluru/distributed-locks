# Build stage: Cache dependencies and build airline app
FROM eclipse-temurin:11-jdk-alpine AS build
RUN apk add --no-cache maven
WORKDIR /app

# 1. Cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Build the project
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/airline-seat-reservation-1.0-SNAPSHOT.jar app.jar

ENTRYPOINT ["java", "-jar", "app.jar"]

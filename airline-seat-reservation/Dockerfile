# Build stage: Use Alpine-based JDK image and install Maven
FROM eclipse-temurin:11-jdk-alpine AS build
RUN apk add --no-cache maven

WORKDIR /app

# 1. Copy only pom.xml to download dependencies (improves caching)
COPY pom.xml .
RUN mvn dependency:go-offline -B

# 2. Copy source and build
COPY src ./src
RUN mvn package -DskipTests

# Runtime stage: Use minimal JRE Alpine image
FROM eclipse-temurin:11-jre-alpine
WORKDIR /app
COPY --from=build /app/target/airline-seat-reservation-1.0-SNAPSHOT.jar app.jar

# Run the app
ENTRYPOINT ["java", "-jar", "app.jar"]

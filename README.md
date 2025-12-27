# Distributed Lock Prototype

A simple Java prototype demonstrating distributed lock implementation using Redis. This project shows how multiple consumers can safely acquire and release locks in a distributed environment.

## Overview

This is a **simple lock and release use case** where:
- Multiple threads compete to acquire a lock
- Once acquired, the thread holds the lock briefly (500ms)
- The lock is released before the TTL expires (1 second)
- No race conditions occur because locks are always released in time

## Prerequisites

- **Docker** and **Docker Compose** (for containerized execution)
- **Java 11** and **Maven** (for local development)
- **Redis** (for local development)

## How to Run with Docker Compose

Docker Compose orchestrates multiple services (Redis and the Java application) to run together seamlessly.

### What is Docker Compose?

Docker Compose is a tool that uses a `docker-compose.yml` file to define and run multiple Docker containers as a single application. In this project:
- **`redis` service**: A Redis container for the distributed lock
- **`app` service**: The Java application container that connects to Redis

### Running the Application

```bash
# Build and run both Redis and the Java application together
docker compose up --build

# Run without rebuilding (if already built)
docker compose up

# Run in background (detached mode)
docker compose up -d --build

# View logs
docker compose logs -f

# Stop all services
docker compose down
```

### Understanding the Docker Compose Flow

1. **`docker compose up --build`** command:
   - Reads `docker-compose.yml` configuration
   - Builds the Docker image from `Dockerfile`
   - Starts the Redis container
   - Starts the Java application container
   - Connects them via Docker's internal network

2. **`Dockerfile`** (Multi-stage build):
   - **Stage 1 (Build)**: Uses `eclipse-temurin:11-jdk-alpine` to compile the Maven project
   - **Stage 2 (Runtime)**: Uses `eclipse-temurin:11-jre-alpine` to run the JAR (smaller image)
   - Copies the compiled JAR to the final image

3. **`docker-compose.yml`** (Service orchestration):
   - Defines the `redis` service using `redis:alpine` image
   - Defines the `app` service that builds from the `Dockerfile`
   - Sets `depends_on: redis` so Redis starts before the app
   - Sets environment variable `REDIS_HOST=redis` (Docker's internal hostname)

## How to Run Locally (for Development)

If you prefer to run without Docker:

```bash
# 1. Start Redis server
redis-server --daemonize yes

# 2. Build the project with Maven
mvn clean package

# 3. Run the application
java -jar target/distributed-lock-example-1.0-SNAPSHOT.jar
```

### Understanding the Build and Run Process

**Maven Build**: `mvn clean package`
- `clean`: Removes previous build artifacts
- `package`: Compiles code, runs tests, and creates a JAR file
- Output: `target/distributed-lock-example-1.0-SNAPSHOT.jar` (fat JAR with all dependencies included via maven-shade-plugin)

**Java Execution**: `java -jar target/distributed-lock-example-1.0-SNAPSHOT.jar`
- Runs the compiled JAR file
- Automatically connects to Redis at `localhost:6379`
- 5 consumer threads compete for the lock
- Each acquires and releases the lock successfully

## How It Works

### Thread Pool Execution with `executor.submit()`

The code uses `ExecutorService` (a thread pool) instead of creating threads manually:

```java
ExecutorService executor = Executors.newFixedThreadPool(NUM_CONSUMERS);
executor.submit(() -> {
    // Consumer logic runs in a separate thread
});
```

**Why use `executor.submit()`?**
- **Manages threads efficiently**: Reuses threads from the pool instead of creating/destroying new ones
- **Concurrent execution**: Multiple consumers run simultaneously (not sequentially)
- **Coordination**: `CountDownLatch` tracks when all consumers finish

**What happens:**
1. 5 consumer threads are submitted to the thread pool
2. Each tries to acquire the lock (retries up to 15 times if lock is held)
3. Once acquired, holds the lock for 500ms (simulating work)
4. Releases the lock simply by deleting it from Redis
5. Main thread waits for all 5 consumers to complete via `CountDownLatch`

### Lock Behavior

- **Lock Key**: `my-distributed-lock`
- **Lock TTL**: 1 second (auto-expires if not released)
- **Hold Duration**: 500ms (always released before TTL)
- **Lock Release**: Simple `del` command (no value checking since we control the release timing)
- **Retries**: Up to 15 attempts with 200ms backoff between failures

## Expected Output

```
REDIS_HOST: localhost
Application starting...
Consumer 2 acquired the lock.
Consumer 4 waiting to retry (attempt 1)...
Consumer 1 waiting to retry (attempt 1)...
...
Consumer 2 attempting to release the lock.
Lock released successfully.
Consumer 4 acquired the lock.
...
Application finished.
```

All 5 consumers successfully acquire and release the lock.

## Analyzing Logs

### With Docker Compose

```bash
# View live logs from all services
docker compose logs -f

# View logs from specific service
docker compose logs -f app
docker compose logs -f redis

# View logs from last 100 lines
docker compose logs --tail=100
```

### Locally (Replit Environment)

After running the workflow, logs appear in the console output showing:
- Lock acquisition attempts
- Retry messages
- Lock release confirmations
- Application completion status

## Project Structure

```
├── src/main/java/com/example/
│   └── DistributedLock.java      # Main application with lock logic
├── pom.xml                         # Maven build configuration
├── Dockerfile                      # Multi-stage Docker build
├── docker-compose.yml              # Service orchestration
└── README.md                       # This file
```

## Technologies

- **Language**: Java 11
- **Build Tool**: Maven
- **Database**: Redis (for distributed locking)
- **Concurrency**: ExecutorService with CountDownLatch
- **Containerization**: Docker & Docker Compose

## Notes

- This is a **simple lock implementation** suitable for scenarios where:
  - You control when locks are released
  - Work always completes before TTL
  - No distributed clock synchronization issues
- For production use cases with strict timing requirements, consider more sophisticated algorithms (e.g., Redlock by Redis author)
- The multi-stage Docker build keeps the final image size small (~150MB) instead of large (with build tools)

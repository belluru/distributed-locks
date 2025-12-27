# Distributed Lock Prototype

A Java prototype demonstrating distributed lock implementation with Redis. Includes two versions:
1. **Unsafe** - Simple delete (vulnerable to race conditions)
2. **Safe** - Value checking before delete (prevents race conditions)

## Two Implementations

### Unsafe Version (No Value Checking)
- Simply deletes lock from Redis
- Fast, but vulnerable when lock expires during work
- File: `DistributedLock.java`

### Safe Version (Value Checking)
- Checks lock value before deleting
- Prevents wrong consumer from deleting another's lock
- File: `DistributedLockWithValueCheck.java`

## How to Run

### Unsafe Version (No Race Condition)
Work duration is 500ms, which is less than the 1 second lock TTL:

```bash
docker compose --profile unsafe up --build -d & sleep 3 && docker compose logs --tail=50
```

Or with custom work duration:
```bash
WORK_DURATION=500 docker compose --profile unsafe up --build -d & sleep 3 && docker compose logs --tail=50
```

**Expected behavior:**
```
Application starting...
Work duration: 500ms, Lock TTL: 1s
Consumer 3 acquired the lock.
Consumer 3 attempting to release the lock.
Lock released successfully.
...
Application finished.
```

---

### Unsafe Version (Race Condition Scenario)
Work duration exceeds TTL to trigger race condition:

```bash
WORK_DURATION=1500 docker compose --profile unsafe up --build -d & sleep 5 && docker compose logs --tail=100
```

**What happens:**
1. Consumer 1 acquires lock (1500ms work starts)
2. At ~1 second: Lock TTL expires in Redis
3. Consumer 2 acquires the lock
4. At ~1.5 seconds: Consumer 1 finishes and deletes lock
5. **Result:** Consumer 1 deleted Consumer 2's lock ❌

---

### Safe Version (Race Condition Handled)
Same scenario but with value checking:

```bash
WORK_DURATION=1500 docker compose --profile safe up --build -d & sleep 5 && docker compose logs --tail=100
```

**What happens:**
1. Consumer 1 acquires lock (1500ms work starts)
2. At ~1 second: Lock TTL expires, Consumer 2 acquires it
3. At ~1.5 seconds: Consumer 1 tries to release
4. **Result:** Lock NOT released - value mismatch detected ✓

**Evidence in logs:**
```
Consumer 1 acquired the lock.
...
Consumer 2 acquired the lock.
...
Consumer 1 attempting to release.
Lock NOT released - value mismatch (expected: consumer-1, found: consumer-2)
```

---

## Project Structure

```
├── src/main/java/com/example/
│   ├── DistributedLock.java              # Unsafe version (no value checking)
│   └── DistributedLockWithValueCheck.java # Safe version (value checking)
├── pom.xml                                # Maven build configuration
├── Dockerfile                             # Multi-stage Docker build (supports both)
├── docker-compose.yml                     # Services for both implementations
└── README.md                              # This file
```

## Docker Profiles

The project uses Docker Compose profiles to run different implementations:

```bash
# Run unsafe version
docker compose --profile unsafe up --build

# Run safe version  
docker compose --profile safe up --build

# Run both (separate containers)
docker compose up --build
```

## Technical Details

- **Lock Key**: `my-distributed-lock`
- **Lock TTL**: 1 second (auto-expires if not released)
- **Work Duration**: Configurable via `WORK_DURATION` environment variable (default 500ms)
- **Lock Mechanism**: Redis SETNX (atomic "set if not exists")

### Unsafe Release
```java
jedis.del(LOCK_KEY);  // Simple delete, no checking
```

### Safe Release
```java
if (lockValue.equals(jedis.get(LOCK_KEY))) {
    jedis.del(LOCK_KEY);  // Only delete if value matches
}
```

## Lessons

This prototype demonstrates:
- **Race conditions** occur when work duration > lock TTL
- **Value checking is critical** to prevent wrong consumer from deleting lock
- Simple delete is vulnerable but fast; value checking is safe but requires atomic operations
- Production systems use Redlock or Lua scripts for guaranteed atomicity

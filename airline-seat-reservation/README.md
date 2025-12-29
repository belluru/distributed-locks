# Airline Seat Reservation Prototype (Race Condition Demo)

This prototype demonstrates a classic **Race Condition** in a database-backed system. It simulates an airline seat reservation process where multiple passengers attempt to book seats concurrently without proper transaction isolation or locking.

## The Problem: "Check-then-Act"

The application follows a simple but dangerous logic:
1. **Find**: Search the `seats` table for an available seat (`passenger_id IS NULL`).
2. **Delay**: A artificial delay (100ms) is introduced to simulate processing and widen the race condition window.
3. **Assign**: Update the `seats` table to assign the found seat to the passenger.
4. **Update**: Update the `passengers` table to record which seat the passenger *thinks* they have.

Because this sequence is not atomic (it lacks `SELECT ... FOR UPDATE` or serializable transactions), two or more threads can find the **same** empty seat before any of them has a chance to update it.

## Observed Behavior

- **Database Overwrites**: The last passenger to run the `UPDATE seats` statement "wins" the seat in the database.
- **Inconsistency**: Even though only one passenger actually "owns" the seat in the `seats` table, multiple passengers will have that seat number assigned to them in the `passengers` table and will receive a "successfully reserved" message.

## How to Run

### Run the Simulation
To run the simulation with default settings (race condition):
```bash
docker compose down -v && docker compose up --build -d && docker compose logs -f app
```

### Experiment with Different Locking Strategies
You can override the `SELECT_QUERY` via command line when starting docker:
```bash
# 1. Standard (Race Condition) - Default
SELECT_QUERY="SELECT seat_id FROM seats WHERE passenger_id IS NULL LIMIT 1" docker compose up --build -d && docker compose logs -f app

# 2. SELECT ... FOR UPDATE (Safety with performance trade-off)
SELECT_QUERY="SELECT seat_id FROM seats WHERE passenger_id IS NULL LIMIT 1 FOR UPDATE" docker compose up --build -d && docker compose logs -f app

# 3. SELECT ... FOR UPDATE SKIP LOCKED (High performance and safety)
SELECT_QUERY="SELECT seat_id FROM seats WHERE passenger_id IS NULL LIMIT 1 FOR UPDATE SKIP LOCKED" docker compose up --build -d && docker compose logs -f app
```

### Metrics & Reconciliation
At the end of the simulation, the application will output:
- **Total time taken**: The duration for all 100 passengers to attempt their reservation.
- **Seat Assignments**: The current state of the `seats` table.
- **Passenger Intentions**: What each passenger *thinks* they have (from the `passengers` table).

## Project Structure
- `App.java`: Main logic including the vulnerable `reserveSeat` method.
- `db/init.sql`: Schema and initial data (100 passengers, 100 seats).
- `docker-compose.yml`: Orchestrates MySQL and the Java application.

# Ride Sharing App - System Design PoC

A scalable, multi-region ready Ride Hailing API built with Spring Boot, MySQL, and Redis.

## Features implemented
*   **Core Logic**: Ride Creation, Driver Matching (Geospatial), Trip Lifecycle (Start/End).
*   **Scalability**:
    *   **Async Processing**: Location updates via `@Async` (Fire-and-Forget).
    *   **Region-Local**: Architecture designed for isolated regional clusters.
    *   **Caching**: Redis caching for read-heavy entities.
*   **Reliability**:
    *   **Concurrency**: Distributed Locking (Redis `SETNX`) to prevent driver over-allocation.
    *   **Consistency**: `@Transactional` boundaries & Optimistic Locking (`@Version`).
    *   **Resilience**: Circuit-breaker style Exception Handling for Redis.
*   **Security**: HTTP Basic Authentication.

## Tech Stack
*   **Language**: Java 17+
*   **Framework**: Spring Boot 3.2
*   **Database**: MySQL 8.0 (Transactional Data)
*   **Cache/Geo**: Redis 7.0 (Geospatial Indexing & Locks)

## Setup & Run

### Prerequisites
1.  **MySQL**: Running on `localhost:3306` (Schema `ridedb` created automatically).
2.  **Redis**: Running on `localhost:6379`.

### Build
```bash
mvn clean install
```

### Run
```bash
mvn spring-boot:run
```

## API Documentation
**Authentication**: Basic Auth (`admin`/`password`)

### 1. Update Driver Location
*   **POST** `/v1/drivers/{id}/location`
*   **Body**: `{"latitude": 37.7749, "longitude": -122.4194}`
*   **Response**: `200 OK` (Async)

### 2. Request a Ride (Idempotent)
*   **POST** `/v1/rides`
*   **Headers**: `Idempotency-Key: unique-uuid-123`
*   **Body**:
    ```json
    {
      "passengerId": "user_01",
      "pickupLat": 37.7749, "pickupLng": -122.4194,
      "dropoffLat": 37.8044, "dropoffLng": -122.2712,
      "pickupLocation": "Market St",
      "dropoffLocation": "Oakland",
      "tier": "STANDARD",
      "paymentMethod": "CARD"
    }
    ```

### 3. Accept Ride (Driver)
*   **POST** `/v1/trips/{id}/accept?driverId=driver_01`
*   **Consistency**: Fails if driver is already on a trip.

### 4. End Trip & Get Receipt
*   **POST** `/v1/trips/{id}/end`
*   **GET** `/v1/trips/{id}/receipt`

## Design Notes
*   **Why Redis Lock?**: Standard DB locking isn't fast enough for "First to Click" matching at 10k req/s. We use Redis `SETNX` for the critical section.
*   **Why Async Location?**: We can process 200k updates/sec by decoupling the HTTP response from the Redis write using an internal ThreadPool.

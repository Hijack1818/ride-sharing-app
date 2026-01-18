# Ride Sharing App (Simple Ride Hailing System)

Welcome to the **Ride Sharing App**! This is a simplified backend system similar to Uber or Lyft. It handles the core journey of a ride: finding a driver, tracking the trip, and processing payments.

This project is designed to demonstrate **High Scalability** concepts using Spring Boot. It's built to be easy to understand for junior developers while showing how real-world systems handle high traffic.

---

## Technology Stack (And Why We Use It)

*   **Java 21 + Spring Boot 3**: The industry standard for building robust enterprise backends.
*   **MySQL 8.0**: Our primary database. It stores "hard" data that we cannot lose, like Trip History and Receipts.
*   **Redis 7.0**: An in-memory database used for speed.
    *   *Why?* We use it to store **Driver Locations** (because they change every second) and to **Cache** ride details so we don't hit the slow MySQL database too often.
*   **Maven**: The tool used to build the project and manage libraries (dependencies).

---

## Key Features

1.  **Driver Matching (Geospatial)**:
    *   We use Redis's `GEO` commands to find drivers near a passenger instantly. It's much faster than calculating distances in Java or SQL.
2.  **Asynchronous Processing**:
    *   *Concept*: When a driver updates their location, we don't make them wait for the server to say "Saved!". We say "Got it!" immediately (200 OK) and save the data in the background. This allows the system to handle thousands of updates per second.
3.  **Idempotency**:
    *   *Concept*: Prevents accidental double-booking. If a user clicks "Book Ride" twice, the server checks the `Idempotency-Key` and only creates **one** ride.
4.  **Optimistic Locking**:
    *   *Concept*: Prevents two drivers from accepting the same ride. We use a version number in the database (`@Version`) to ensure the first one wins.

---

## How to Run Locally

### 1. Prerequisites
You need these installed on your machine:
*   **Java 21** (JDK)
*   **Maven**
*   **MySQL Server** (Running on port `3306`)
*   **Redis Server** (Running on port `6379`)

### 2. Configure Database
Open `src/main/resources/application.properties` and check your database username/password.
By default:
*   **User**: `root`
*   **Password**: `admin`
*   **DB Name**: `ridedb` (The app will create this automatically if it doesn't exist).

### 3. Start the App
Open your terminal in the project folder and run:
```bash
mvn spring-boot:run
```
You should see `Started RideSharingApplication ...` in the logs.

---

## How to Test (API Guide)

All APIs are protected. You must use **Basic Authentication**:
*   **Username**: `admin`
*   **Password**: `password`

### Step 1: A Driver Comes Online
Update their location so the system knows where they are.
*   **URL**: `POST /v1/drivers/driver_01/location`
*   **Body**:
    ```json
    {
      "latitude": 37.7749,
      "longitude": -122.4194
    }
    ```

### Step 2: Passenger Requests a Ride
*   **URL**: `POST /v1/rides`
*   **Body**:
    ```json
    {
      "passengerId": "user_bob",
      "pickupLat": 37.7749,
      "pickupLng": -122.4194,
      "dropoffLat": 37.8044,
      "dropoffLng": -122.2712,
      "pickupLocation": "Market St",
      "dropoffLocation": "Oakland Area",
      "tier": "STANDARD",
      "paymentMethod": "CARD"
    }
    ```
*   **Response**: The server creates a ride and returns a `rideId` (e.g., `uuid-1234`).

### Step 3: Driver Accepts the Ride
*   **URL**: `POST /v1/drivers/driver_01/accept?rideId={rideId}`
*   *Note*: Copy the `rideId` from Step 2 into the URL.

### Step 4: End the Trip
When the destination is reached.
*   **URL**: `POST /v1/trips/{rideId}/end`
*   **Response**: The status changes to `COMPLETED` and the final fare is calculated.

### Step 5: Get Receipt
*   **URL**: `GET /v1/trips/{rideId}/receipt`

---

## 📂 Project Structure

*   `config/`: Setup files (Security, thread pools).
*   `controller/`: The entry points (API endpoints) where requests land.
*   `service/`: The "Brain". All logic (matching, calculations) happens here.
*   `repository/`: The layer that talks to the Database.
*   `model/`: The shape of our data (e.g., what a `Ride` looks like).
*   `exception/`: Handles errors globally so users get clean messages.

---
## HLD
<img width="4000" height="7973" alt="Ride Service with Redis Flow-2026-01-18-163807" src="https://github.com/user-attachments/assets/661b4920-ae0b-4b91-9b40-5ede09bf85e2" />


---
## Architectural Decisions
**"Hybrid Data Strategy:"**

*   MySQL is the "Source of Truth". We use it for anything that involves billing, audit trails, or complex state transitions.
*   Redis is the "Operational Store". We treat Driver Locations as ephemeral data. If Redis crashes, we simple wait for the next location update (within seconds) rather than trying to persist every coordinate to     disk.

**"Asynchronous Ingestion:"**
*   The updateLocation endpoint is designed as "Fire-and-Forget". The Controller immediately returns 200 OK to the driver app while a separate thread pool handles the Redis write. This prevents the driver app        from freezing if the network is momentarily slow and protects the Tomcat request threads from saturation.

**"Optimistic Concurrency:"**
*   We utilize @Version on the Ride entity. This creates a highly scalable locking mechanism at the database level without the performance penalty of SELECT FOR UPDATE.
 
---

## Troubleshooting

**"Unable to connect to Redis"**
*   Make sure your Redis server is running! Open a new terminal and type `redis-server` (or manage it via Docker/Service).

**"401 Unauthorized"**
*   Check your Basic Auth credentials. (admin / password).

**"Conflict" (409 Error)**
*   This means the data changed while you were trying to update it. Just try the request again.

# SmartCampusAPI

## Build and Run

### Prerequisites
- Java 8
- Maven
- Apache Tomcat 9
- NetBeans
- Postman for testing

### Technology Stack
This project is built using:
- JAX-RS (Jersey)
- Maven Web Application
- Apache Tomcat 9
- Java collections for in-memory storage such as `ConcurrentHashMap` and `ArrayList`

### How to Run
1. Open the project in NetBeans.
2. Make sure Apache Tomcat 9 is set as the server.
3. Clean and build the project.
4. Run the project.
5. Open the API in a browser or Postman using:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

### Notes
- This project uses in-memory storage only.
- Data resets whenever the server restarts.
- No database is used.

---

## API Overview

Base URL:

```text
http://localhost:8080/SmartCampusAPI/api/v1
```

### Main Endpoints
- `GET /api/v1`
- `GET /api/v1/rooms`
- `POST /api/v1/rooms`
- `GET /api/v1/rooms/{roomId}`
- `DELETE /api/v1/rooms/{roomId}`
- `GET /api/v1/sensors`
- `POST /api/v1/sensors`
- `GET /api/v1/sensors?type=CO2`
- `GET /api/v1/sensors/{sensorId}/readings`
- `POST /api/v1/sensors/{sensorId}/readings`

---

## Sample curl Commands

### 1. Discovery endpoint
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1
```

### 2. Create a room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/rooms \
-H "Content-Type: application/json" \
-d "{\"id\":\"LIB-301\",\"name\":\"Library Quiet Study\",\"capacity\":40}"
```

### 3. Get all rooms
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms
```

### 4. Get one room
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

### 5. Create a valid sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"CO2-001\",\"type\":\"CO2\",\"status\":\"ACTIVE\",\"currentValue\":0.0,\"roomId\":\"LIB-301\"}"
```

### 6. Filter sensors by type
```bash
curl -X GET "http://localhost:8080/SmartCampusAPI/api/v1/sensors?type=CO2"
```

### 7. Add a reading to a sensor
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings \
-H "Content-Type: application/json" \
-d "{\"id\":\"READ-001\",\"timestamp\":1710000000000,\"value\":450.7}"
```

### 8. Get readings for a sensor
```bash
curl -X GET http://localhost:8080/SmartCampusAPI/api/v1/sensors/CO2-001/readings
```

### 9. Try to create a sensor with an invalid room
```bash
curl -X POST http://localhost:8080/SmartCampusAPI/api/v1/sensors \
-H "Content-Type: application/json" \
-d "{\"id\":\"TEMP-001\",\"type\":\"Temperature\",\"status\":\"ACTIVE\",\"currentValue\":22.5,\"roomId\":\"FAKE-999\"}"
```

### 10. Try to delete a room that still has sensors
```bash
curl -X DELETE http://localhost:8080/SmartCampusAPI/api/v1/rooms/LIB-301
```

---

## Answers to Coursework Questions

## Part 1: Service Architecture & Setup

### 1. Project and Application Configuration
In JAX-RS, resource classes are usually request-scoped by default, which means a new object is normally created for each incoming request unless it is configured differently. This is important because it means shared application data should not be stored in normal instance fields inside the resource classes. If I did that, the data could be reset or lost between requests. To avoid this, I stored shared data in a separate `DataStore` class using maps and lists. I also used thread-safe collections like `ConcurrentHashMap` so the API is safer when multiple users send requests at the same time.

### 2. Discovery Endpoint
Hypermedia is seen as an important part of advanced RESTful design because it helps clients discover available actions directly from the API response instead of depending only on fixed documentation. In this project, the discovery endpoint helps clients see the main resources, such as `/rooms` and `/sensors`, straight away. This makes the API easier to understand and easier to use because the client can follow links and available paths instead of hardcoding everything.

---

## Part 2: Room Management

### 1. Returning IDs vs Full Room Objects
Returning only IDs has the advantage of using less bandwidth because the response is smaller. However, it also means the client may need to send extra requests to get more details about each room. Returning the full room objects is more convenient because the client gets all the information in one response, although the payload is larger. In my implementation, I chose to return full room objects because it makes the API easier to test and more user-friendly.

### 2. Is DELETE idempotent?
Yes, DELETE is idempotent in my implementation. If a room is deleted successfully, sending the same DELETE request again will not keep changing the system. The first request removes the room, and any later identical request may return `404 Not Found` because the room no longer exists, but it does not cause any extra change to the server state. Because of that, the DELETE operation is still idempotent.

---

## Part 3: Sensor Operations & Linking

### 1. Effect of `@Consumes(MediaType.APPLICATION_JSON)`
The `@Consumes(MediaType.APPLICATION_JSON)` annotation tells JAX-RS that the method only accepts JSON input. If a client sends data in a different format, such as `text/plain` or `application/xml`, the request will not match the supported media type for that method. In that situation, JAX-RS can reject the request with `415 Unsupported Media Type`. This is useful because it stops the API from trying to process data in a format it was not designed to handle.

### 2. Why use `@QueryParam` for filtering?
Using a query parameter like `/sensors?type=CO2` is generally a better design for filtering because it clearly shows that the client is searching within a collection rather than requesting a completely different resource. Query parameters are also more flexible because more filters can be added later, such as status or room ID. A path like `/sensors/type/CO2` works, but it feels less natural for filtering and mixes the actual resource path with search conditions.

---

## Part 4: Deep Nesting with Sub-Resources

### 1. Benefits of the Sub-Resource Locator Pattern
The sub-resource locator pattern makes the code more organised by moving nested functionality into its own class. Instead of putting all sensor and reading logic in one large resource class, `SensorResource` passes requests for `/sensors/{sensorId}/readings` to `SensorReadingResource`. This makes the code cleaner, easier to maintain, and easier to expand in the future. It also helps separate the responsibility of managing sensors from the responsibility of managing reading history.

### 2. Historical Data Management
The history of readings is stored in a list linked to each sensor ID. A `GET` request returns all saved readings for that sensor, while a `POST` request adds a new reading to the history. After a reading is added successfully, the parent sensor’s `currentValue` is updated to match the latest reading. This keeps the API consistent because the current sensor value always reflects the most recent measurement.

---

## Part 5: Advanced Error Handling, Exception Mapping, and Logging

### 1. Why is 422 more accurate than 404?
HTTP `422 Unprocessable Entity` is often more accurate than `404 Not Found` in this case because the endpoint itself is valid and the JSON format is also valid. The real problem is that the `roomId` inside the request body refers to a room that does not exist. Since the path is not missing, `404` is less suitable. `422` better describes a request that is structurally correct but contains invalid data from a business logic point of view.

### 2. Why stack traces should not be exposed
Exposing raw Java stack traces to API users is risky because it can reveal internal details about the application, such as class names, package names, framework information, method names, and even line numbers. An attacker could use this information to understand how the system is built and possibly look for weaknesses more easily. To avoid that, I used a global exception mapper that returns a generic `500 Internal Server Error` message instead of exposing internal technical details.

### 3. Why use filters for logging?
JAX-RS filters are useful for cross-cutting concerns like logging because they allow the logging logic to be placed in one central location instead of repeating it in every resource method. This keeps the code cleaner and makes the logging more consistent across the whole API. In my project, I used a class that implements both `ContainerRequestFilter` and `ContainerResponseFilter` so I could log the request method, request URI, and final response status code for every request.

---

## Error Handling Summary

### Custom Exceptions
- `RoomNotEmptyException`
- `LinkedResourceNotFoundException`
- `SensorUnavailableException`

### Exception Mappers
- `409 Conflict` for deleting a room that still has sensors
- `422 Unprocessable Entity` for creating a sensor with a missing linked room
- `403 Forbidden` for adding readings to a maintenance sensor
- `500 Internal Server Error` for unexpected runtime errors

---

## Logging
The API includes a logging filter that logs:
- incoming HTTP method
- incoming request URI
- outgoing response status code

---

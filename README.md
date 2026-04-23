# Smart Campus – Sensor & Room Management API

**Module:** 5COSC022W Client-Server Architectures
**Stack:** Java 11 · JAX-RS 2 (Jersey 2.39.1) · Apache Tomcat 9 · Jackson JSON
**Base URL:** `http://localhost:8080/smart-campus-api/api/v1`

---

## Table of Contents

1. [API Design Overview](#api-design-overview)
2. [Project Structure](#project-structure)
3. [Build & Run Instructions](#build--run-instructions)
4. [Pre-seeded Data](#pre-seeded-data)
5. [Sample curl Commands](#sample-curl-commands)
6. [Report – Question Answers](#report--question-answers)

---

## API Design Overview

The API follows a strict RESTful resource hierarchy that mirrors the physical structure of the campus. Every response is `application/json`. No raw Java stack traces or HTML error pages are ever returned to clients — all errors are structured JSON produced by dedicated Exception Mappers.

```
/api/v1
├── GET /                          → Discovery (HATEOAS links + metadata)
├── /rooms
│   ├── GET     /                  → List all rooms
│   ├── POST    /                  → Create a room
│   ├── GET     /{roomId}          → Get one room
│   └── DELETE  /{roomId}          → Decommission (blocked if sensors exist → 409)
└── /sensors
    ├── GET     /                  → List sensors (optional ?type= filter)
    ├── POST    /                  → Register sensor (validates roomId → 422 if missing)
    ├── GET     /{sensorId}        → Get one sensor
    ├── DELETE  /{sensorId}        → Remove sensor (unlinks from room)
    └── /{sensorId}/readings
        ├── GET  /                 → Fetch reading history
        └── POST /                 → Append reading (updates sensor.currentValue; 403 if MAINTENANCE)
```

### Error Responses

| Scenario                             | Status                     | Exception                                         |
| ------------------------------------ | -------------------------- | ------------------------------------------------- |
| Room deleted with sensors            | 409 Conflict               | `RoomNotEmptyException`                           |
| Sensor posted with unknown roomId    | 422 Unprocessable Entity   | `LinkedResourceNotFoundException`                 |
| Reading posted to MAINTENANCE sensor | 403 Forbidden              | `SensorUnavailableException`                      |
| Any unexpected runtime error         | 500 Internal Server Error  | `GlobalExceptionMapper` (catch-all)               |
| Wrong Content-Type sent              | 415 Unsupported Media Type | JAX-RS runtime (wrapped by GlobalExceptionMapper) |

---

## Project Structure

```
Smart Campus API/
├── pom.xml
└── src/main/java/
    ├── com.smartcampus.application/
    │   ├── DataStore.java                     # Thread-safe singleton  in-memory store
    │   └── SmartCampusApplication.java        # JAX-RS @ApplicationPath("/api/v1")
    ├── com.smartcampus.exception/
    │   ├── GlobalExceptionMapper.java         # 500 Internal Server Error (catch-all)
    │   ├── LinkedResourceNotFoundException.java
    │   ├── LinkedResourceNotFoundExceptionMapper.java  # 422 Unprocessable Entity
    │   ├── RoomNotEmptyException.java 
    │   ├── RoomNotEmptyExceptionMapper.java   # 409 Conflict
    │   ├── SensorUnavailableException.java
    │   └── SensorUnavailableExceptionMapper.java       # 403 Forbidden
    ├── com.smartcampus.filter/
    │   └── LoggingFilter.java                 # Logs every request & response
    ├── com.smartcampus.model/
    │   ├── Room.java
    │   ├── Sensor.java
    │   └── SensorReading.java
    └── com.smartcampus.resource/
        ├── DiscoveryResource.java             # GET /api/v1
        ├── SensorReadingResource.java         # /api/v1/sensors/{id}/readings
        ├── SensorResource.java                # /api/v1/sensors (+ sub-resource locator)
        └── SensorRoomResource.java            # /api/v1/rooms
```

---

## Build & Deploy Instructions (Apache Tomcat)

### Prerequisites

- Java 11 or higher — verify with `java -version`
- Maven 3.6 or higher — verify with `mvn -version`
- Apache Tomcat 9 (download from https://tomcat.apache.org/)
- 
### Step 1 – Clone the repository

```bash
git clone https://github.com/Nethasha-Fernando/smart-campus-api.git
cd smart-campus-api
```

### Step 2 – Build the WAR file

```bash
mvn clean package

```
This produces target/smart-campus-api.war (a Web Application Archive).

### Step 3 – Deploy to Tomcat

Copy the .war file into Tomcat's webapps/ directory.

```
For example: cp target/smart-campus-api.war /path/to/tomcat/webapps/

```
Start Tomcat:

On Windows: 

```
bin\startup.bat

```
On macOS/Linux:

```
bin/startup.sh

```
Tomcat will automatically extract and deploy the application.
The API will be available at:


```
http://localhost:8080/smart-campus-api/api/v1

```


### Step 4 – Test the API

The server listens on port 8080. Use the curl commands below or import the Postman collection to test all endpoints."

## Postman Collection

You can test all endpoints using the Postman collection:

https://nethasha-6576298.postman.co/...

Includes:

- All endpoints
- Filtering
- Error scenarios

> Recommended: Run requests in order for full demonstration.

Includes:

- All endpoints
- Filtering
- Error scenarios

> Recommended: Run requests in order for full demonstration.
> https://nethasha-6576298.postman.co/workspace/BACKEND~cfd60007-af67-427a-84b7-f90c27266577/collection/48441034-5384ad18-07d0-4749-82f9-a6fdafba0390?action=share&source=copy-link&creator=48441034

All commands use `python3 -m json.tool` to pretty-print the JSON response — omit this if Python is not available.

> **Tip:** Run the commands in order the first time. Commands 2 and 4 create resources that later error-case commands depend on.

---

## Pre-seeded Data

The API starts with the following data already loaded so it is immediately demonstrable without any setup calls:

**Rooms:**

| ID         | Name                 | Capacity |
| ---------- | -------------------- | -------- |
| `LIB-301`  | Library Quiet Study  | 50       |
| `LAB-102`  | Computer Science Lab | 30       |
| `HALL-001` | Main Assembly Hall   | 500      |

**Sensors:**

| ID         | Type        | Status      | Room     |
| ---------- | ----------- | ----------- | -------- |
| `TEMP-001` | Temperature | ACTIVE      | LIB-301  |
| `CO2-001`  | CO2         | ACTIVE      | LIB-301  |
| `OCC-001`  | Occupancy   | MAINTENANCE | LAB-102  |
| `TEMP-002` | Temperature | ACTIVE      | HALL-001 |

**Readings:** Two historical readings are pre-seeded for  `TEMP-001` (values 21.0 and 22.5). . One reading is also pre-seeded for `CO2-001` (value 400.0).

---

## Sample curl Commands

### 1. Discovery – GET /api/v1

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1 | python3 -m json.tool
```

Expected response (`200 OK`):

```json
{
  "api": "Smart Campus Sensor & Room Management API",
  "version": "1.0.0",
  "status": "operational",
  "description",
                "A RESTful API for managing campus rooms and IoT sensors " +
                        "built with JAX-RS (Jersey) deployed on Apache Tomcat",
  "contact": {
    "Team": "University of Westminster - Smart Campus Initiative",
    "Module": "5COSC022W Client-Server Architectures",
    "Email": "smartcampus@westminster.ac.uk"
  },
  "_links": {
    "self": "/api/v1",
    "rooms": "/api/v1/rooms",
    "sensors": "/api/v1/sensors",
    "sensor_by_type": "/api/v1/sensors?type={type}",
    "sensor_readings": "/api/v1/sensors/{sensorId}/readings"
  }
}
```

---

### 2. Create a Room – POST /api/v1/rooms

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/rooms \
  -H "Content-Type: application/json" \
  -d '{"id":"ENG-205","name":"Engineering Lab","capacity":40}' \
  | python3 -m json.tool
```

Expected response (`201 Created`):

```json
{
  "message": "Room created successfully.",
  "room": {
    "id": "ENG-205",
    "name": "Engineering Lab",
    "capacity": 40,
    "sensorIds": []
  }
}
```

---

### 3. List All Rooms – GET /api/v1/rooms

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/rooms | python3 -m json.tool
```

Returns a `200 OK` JSON object with a `count` field and a `rooms` array containing all room objects including the three pre-seeded rooms.

---

### 4. Register a Sensor – POST /api/v1/sensors

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-003","type":"Temperature","status":"ACTIVE","currentValue":21.0,"roomId":"LIB-301"}' \
  | python3 -m json.tool
```

Expected response (`201 Created`). If `roomId` does not exist → `422 Unprocessable Entity`.

```json
{
  "message": "Sensor registered successfully.",
  "sensor": {
    "id": "TEMP-003",
    "type": "Temperature",
    "status": "ACTIVE",
    "currentValue": 21.0,
    "roomId": "LIB-301"
  }
}
```

---

### 5. Filter Sensors by Type – GET /api/v1/sensors?type=CO2

```bash
curl -s "http://localhost:8080/smart-campus-api/api/v1/sensors?type=CO2" | python3 -m json.tool
```

Expected response:
Returns `200 OK` — only sensors whose `type` field matches `CO2` (case-insensitive). With pre-seeded data this returns only `CO2-001`.

```json
{
  "count": 1,
  "filterApplied": {
    "type": "CO2"
  },
  "sensors": [
    {
      "id": "CO2-001",
      "type": "CO2",
      "status": "ACTIVE",
      "currentValue": 412.0,
      "roomId": "LIB-301"
    }
  ]
}
```

---

### 6. Get Reading History – GET /api/v1/sensors/{sensorId}/readings

```bash
curl -s http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings | python3 -m json.tool
```

Expected Response:
Returns `200 OK` — the full historical reading log for `TEMP-001`, including the two pre-seeded readings.

```json
{
  "sensorId": "TEMP-001",
  "totalReadings": 2,
  "readings": [
    {
      "id": "b0484f81-2af7-4f8a-8e9e-d1ec58e7ef5d",
      "timestamp": 1776877423114,
      "value": 21.0
    },
    {
      "id": "ccf979d9-52c0-46c2-9aed-21f461dfc23f",
      "timestamp": 1776877423114,
      "value": 22.5
    }
  ]
}
```

---

### 7. Post a Sensor Reading – POST /api/v1/sensors/{sensorId}/readings

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/TEMP-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":45.5}' \
  | python3 -m json.tool
```

Expected response:
(`201 Created`) — also updates `TEMP-001.currentValue` to `45.5`. Verify by calling `GET /api/v1/sensors/TEMP-001`.

```json
{
  "message": "Reading recorded successfully.",
  "sensorId": "TEMP-001",
  "updatedCurrentValue": 45.5,
  "reading": {
    "id": "35657f64-c561-4ef9-a76a-d103be864c88",
    "timestamp": 1776886049165,
    "value": 45.5
  }
}
```

---

### 8. Delete a Room with Sensors – triggers 409 Conflict

```bash
curl -s -X DELETE http://localhost:8080/smart-campus-api/api/v1/rooms/LIB-301 | python3 -m json.tool
```

Expected response (`409 Conflict`):

```json
{
  "status": 409,
  "error": "Conflict",
  "code": "ROOM_NOT_EMPTY",
  "message": "Room 'LIB-301' cannot be deleted because it still has active sensors assigned to it.",
  "activeSensors": ["TEMP-001", "CO2-001", "TEMP-003"],
  "hint": "Decommission or reassign all sensors before deleting the room.",
  "timestamp": 1776886133164
}
```
Note: TEMP-003 only appears in this list if Command 4 (register sensor) was run first. On a fresh server without Command 4, activeSensors will be ["TEMP-001", "CO2-001"].

---

### 9. Post a Reading to a MAINTENANCE sensor – triggers 403 Forbidden

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors/OCC-001/readings \
  -H "Content-Type: application/json" \
  -d '{"value":5}' \
  | python3 -m json.tool
```

Expected response (`403 Forbidden`):

```json
{
  "status": 403,
  "error": "Forbidden",
  "code": "SENSOR_UNAVAILABLE",
  "message": "Sensor 'OCC-001' is currently under MAINTENANCE and cannot accept new readings. Please wait until the sensor is restored to ACTIVE status.",
  "hint": "Only sensors with status 'ACTIVE' can receive new readings.",
  "timestamp": 1776886172526
}
```

---

### 10. Register a Sensor with a non-existent roomId – triggers 422

```bash
curl -s -X POST http://localhost:8080/smart-campus-api/api/v1/sensors \
  -H "Content-Type: application/json" \
  -d '{"id":"TEMP-999","type":"Temperature","status":"ACTIVE","currentValue":20.0,"roomId":"FAKE-999"}' \
  | python3 -m json.tool
```

Expected response (`422 Unprocessable Entity`):

```json
{
  "status": 422,
  "error": "Unprocessable Entity",
  "code": "LINKED_RESOURCE_NOT_FOUND",
  "message": "The roomId 'FAKE-999' referenced in your request payload does not exist in the system.",
  "field": "roomId",
  "hint": "Ensure the referenced resource (e.g., roomId) exists before creating a dependent resource.",
  "timestamp": 1776885830819
}
```

---

## Report – Question Answers

---

### Part 1 – Q1: 

**Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a singleton? Elaborate on how this architectural decision impacts the way you manage and synchronise your in-memory data structures (maps/lists) to prevent data loss or race conditions.**

The default lifecycle of resource classes in JAX-RS is **per-request**. Thus, each incoming HTTP request will create a completely fresh object of the resource class, use it to process that particular request, and delete the object right after sending the corresponding response.

Resource classes in JAX-RS are **not** treated as singletons. This guarantees that all requests are executed completely independently — resource class instances are inherently stateless, and because no instance is ever shared between threads, there are no concurrency issues within the class itself.

However, there is a very important aspect of this lifecycle that impacts in-memory data management. Since resource instances exist only temporarily, anything stored within their fields would be deleted at the end of that request. Any information shared among different entities — such as rooms, sensors, and readings — must therefore be held by a separate singleton entity; in this case, `DataStore`. This singleton is created once using a static variable and exists throughout the lifecycle of the application.

Since this singleton `DataStore` can be accessed by multiple concurrent request-handler threads simultaneously, using a plain `HashMap` or `ArrayList` to store data would risk race conditions, lost updates, and `ConcurrentModificationException`. To prevent this, the implementation uses `ConcurrentHashMap` for rooms and sensors, and a `ConcurrentHashMap` mapping each sensor ID to its reading history, ensuring all shared state is accessed through thread-safe structures without requiring explicit `synchronized` blocks on every access.

Specifically, whilst the outer map for readings is a `ConcurrentHashMap`, each sensor's reading history is stored in a plain `ArrayList`. In a production system with high concurrency, these lists would be wrapped with `Collections.synchronizedList` or replaced with thread-safe collections to prevent race conditions. For the scope of this coursework, the risk of concurrent writes to the same sensor is minimal, and the outer map ensures safe retrieval of the list reference.

---

### Part 1 – Q2: 

**Question: Why is the provision of "Hypermedia" (links and navigation within responses) considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach benefit client developers compared to static documentation?**

HATEOAS (Hypermedia as the Engine of Application State) refers to the technique of embedding navigational hypermedia into API responses so that a client can discover available resources and appropriate transitions between them dynamically, without needing pre-existing knowledge of URLs or documentation.

For instance, when a `GET` request is made to `/api/v1` in this project, the response contains a `_links` section pointing to `/api/v1/rooms`, `/api/v1/sensors`, and the parametrised reading URL. A client can therefore discover all available resources in the API starting from a single root entry point.

**Benefits over static documentation:**

- **Discoverability** — a single entry point exposes the entire API surface; clients navigate rather than memorise.
- **Reduced coupling** — since clients make use of links provided in the API responses, changing their location on the server will not cause the client to break.
- **Self-describing responses** — responses indicate what actions are currently possible, reducing developer guesswork.
- **Smoother versioning** — new resources and links can be added to responses without breaking existing clients, supporting organic API evolution.

---

### Part 2 – Q1:

**Question: When returning a list of rooms, what are the implications of returning only IDs versus returning the full room objects? Consider network bandwidth and client-side processing.**

When `GET /api/v1/rooms` returns a list, there are two design options:

**Return only IDs:** The response is small, optimising bandwidth usage. However, the client would need to issue a separate `GET /api/v1/rooms/{id}` request per room, generating the infamous **N+1 request problem**, which multiplies latency proportionally with the number of rooms and can make the application feel slow.

**Return full room objects:** The response payload size is higher, but the client retrieves all the information it requires in a single round-trip. This is the chosen design in this implementation, where performance is prioritised over payload efficiency for typical client usage scenarios (such as displaying a page with room names and capacities).

The trade-off shifts when the collection is extremely large (thousands of items). In that case, pagination with a lighter representation (ID and name only) would be more appropriate. For the scale of this coursework, returning full objects is the correct default.

---

### Part 2 – Q2:

**Question: Is the DELETE operation idempotent in your implementation? Provide a detailed justification by describing what happens if a client mistakenly sends the exact same DELETE request for a room multiple times.**

In line with the HTTP/1.1 DELETE idempotency definition outlined in **RFC 9110**, this `DELETE` implementation is idempotent.

An idempotent operation can be applied multiple times but will only modify the server state once, regardless of how many times the operation is called.

- **1st Call:** The room exists and has no sensors → the room is deleted → server returns `200 OK`.
- **2nd Call (same room ID):** The room does not exist → server returns `404 Not Found`.

Despite the different HTTP response codes between the first and second deletion attempts, the end state of the system is identical in both cases — the room is deleted — and the property of idempotency therefore holds.

If a `DELETE` is attempted on a room that still has sensors assigned, the server will continue to return `409 Conflict` until the constraint is resolved. The server state does not change between repeated calls, which also satisfies the definition of idempotent behaviour.

---

### Part 3 – Q1: 

**Question: We explicitly use the `@Consumes(MediaType.APPLICATION_JSON)` annotation on the POST method. Explain the technical consequences if a client attempts to send data in a different format, such as text/plain or application/xml. How does JAX-RS handle this mismatch?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation declares that this endpoint will accept only one media type — JSON. This check is performed automatically by the JAX-RS runtime, without any execution of application-level code prior to invoking the resource method.

If a client sends a request with an incompatible `Content-Type` header value (such as `text/plain` or `application/xml`), a content negotiation procedure occurs in which the runtime checks whether the `Content-Type` header matches the annotated value. Since there is no match, the runtime rejects the request and returns `415 Unsupported Media Type`. The request body will not even be attempted to be deserialised.

The advantages of this approach are that input validation is handled in a declarative manner, without requiring any additional validation code. Furthermore, it eliminates the possibility of partial processing errors that could arise from attempting to parse, for example, an XML body as JSON.

---

### Part 3 – Q2: 

**Question: You implemented this filtering using `@QueryParam`. Contrast this with an alternative design where the type is part of the URL path (e.g., `/api/v1/sensors/type/CO2`). Why is the query parameter approach generally considered superior for filtering and searching collections?**

Both approaches can be used to filter a collection; however, they serve different REST design purposes:

| Approach | Example | Intended Purpose |
|---|---|---|
| Query Parameter | `/api/v1/sensors?type=CO2` | Filter / search a collection |
| Path Segment | `/api/v1/sensors/type/CO2` | Identify a specific sub-resource |

**Why query parameters are preferable for filtering:**

1. **Semantic accuracy** — A URL path segment represents a resource. A filter is not a resource; it is a modifier applied to a collection. Using a path segment implies that a resource such as `/sensors/type/CO2` exists, which it does not.
2. **Composability** — Query parameters are easily combined, e.g. `?type=CO2&status=ACTIVE`, whereas path-based filters require additional segments for each criterion, quickly producing unmaintainable URLs such as `/sensors/type/CO2/status/ACTIVE`.
3. **Optional by design** — Query parameters can simply be omitted when no filtering is required, allowing a single endpoint to handle both filtered and unfiltered requests without any modification to the API. Path-based filters require a separate route for unfiltered requests.
4. **REST conformance** — Filtering and searching collections via query parameters is a widely adopted convention across the REST community and most public APIs, making the interface immediately intuitive for developers.

---

### Part 4 – Q1: 

**Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How does delegating logic to separate classes help manage complexity in large APIs compared to defining every nested path (e.g., `sensors/{id}/readings/{rid}`) in one massive controller class?**

The Sub-Resource Locator pattern involves delegating responsibility for a nested URL path to its own dedicated class, rather than placing that logic inside the parent resource. For instance, `SensorResource` contains a method annotated with `@Path("/{sensorId}/readings")` that returns an instance of `SensorReadingResource` — note that no HTTP method annotation is present, indicating this is a locator rather than a handler. The lifecycle of that instance is then managed independently by the JAX-RS runtime.

**Benefits of this architectural approach:**

1. **Single Responsibility Principle** — `SensorResource` is responsible for managing the lifecycle of a sensor, whilst `SensorReadingResource` is responsible for managing the reading history of that sensor. Each class has a clearly defined responsibility and can be understood, tested, and modified independently.
2. **Reduced complexity** — Placing every nested path inside a single large controller quickly makes that class unwieldy and difficult to read. Delegation divides responsibilities amongst focused, cohesive classes.
3. **Independent testability** — `SensorReadingResource` can be instantiated directly in a unit test with a given `sensorId`, eliminating the need to spin up the full JAX-RS container or route requests through `SensorResource`.
4. **Maintainability and extensibility** — Adding new operations under `/readings` (such as aggregation endpoints) requires changes only within `SensorReadingResource`, with no risk of inadvertently affecting the logic in `SensorResource`.

---

### Part 5 – Q2: 

**Question: Why is HTTP 422 often considered more semantically accurate than a standard 404 when the issue is a missing reference inside a valid JSON payload?**

- **`404 Not Found`** signifies that the requested URI does not exist on the server — implying that the path `/api/v1/sensors` was not found. This is clearly incorrect in this scenario, as the endpoint exists and was reached successfully.
- **`422 Unprocessable Entity`** signifies that the request was syntactically valid — correct `Content-Type` header and well-formed JSON — but semantically invalid, because the server understood the request yet cannot act upon it due to a business rule violation. In this case, the `roomId` field within the payload references an entity that does not exist.

Using `404` would be actively misleading, suggesting to the client that the wrong URL was used. `422` communicates precisely: *"Your request arrived correctly and was parsed — the problem is a broken reference inside your JSON payload."* This is far more actionable; the developer knows to correct the `roomId` value, not the endpoint URL.

---

### Part 5 – Q4: 

**Question: From a cybersecurity standpoint, explain the risks associated with exposing internal Java stack traces to external API consumers. What specific information could an attacker gather from such a trace?**

Whilst raw Java stack traces are intended for debugging purposes, exposing them directly to API clients creates a significant security risk. Stack traces contain sensitive information about the internal operation of a system, which malicious actors could use as a foothold.

An attacker could extract the following from a raw stack trace:

- **Class and package names** — revealing the internal structure and logic of the application, enabling targeted code analysis.
- **Third-party libraries and version numbers** — allowing an attacker to identify known vulnerabilities and craft specific exploits.
- **Server-side file paths** — disclosing information about the server environment, deployment structure, and potentially the username of the running process.
- **Business logic flow** — revealing how requests are processed through the system, helping an attacker identify execution paths and input validation gaps.
- **Database and infrastructure details** — deeper stack traces may expose connection pool class names, ORM internals, or SQL error messages, providing a roadmap to the data layer.

To mitigate this risk, the `GlobalExceptionMapper` logs the full stack trace server-side whilst returning only a generic `500 Internal Server Error` response to the client. This preserves internal debugging capability without exposing sensitive system information externally.

---

### Part 5 – Q5: 

**Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like logging, rather than manually inserting `Logger.info()` statements inside every single resource method?**

Cross-cutting concerns are behaviours that must apply uniformly to every endpoint — such as logging, authentication, CORS, and rate-limiting. JAX-RS filters are the correct mechanism for these because:

1. **DRY principle** — A single `LoggingFilter` class applies automatically to all endpoints. Without filters, the same `Logger.info()` calls would need to be duplicated across every resource method, creating maintenance debt and a risk of omission.
2. **Consistency** — Every request and response is logged in an identical format. Manual insertion risks inconsistent log messages across methods written at different times or by different developers.
3. **Separation of concerns** — Resource methods should contain only business logic. Embedding logging code within them mixes infrastructure concerns with domain logic, reducing readability and violating the Single Responsibility Principle.
4. **Response interception** — `ContainerResponseFilter` runs after the response has been fully constructed, meaning it can log the final HTTP status code, including those set by exception mappers. This is not easily achievable by inserting logger calls inside resource methods, which execute before exception mappers run.
5. **Reliability** — Filters are applied automatically by the JAX-RS runtime for every matched request. A developer cannot accidentally omit logging from a new endpoint, as the filter covers it without requiring any additional code changes.

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

### Part 1 – Q1: JAX-RS Resource Lifecycle
**Question: In your report, explain the default lifecycle of a JAX-RS Resource class. Is a new instance instantiated for every incoming request, or does the runtime treat it as a
singleton? Elaborate on how this architectural decision impacts the way you manage and
synchronize your in-memory data structures (maps/lists) to prevent data loss or race con
ditions.**

In JAX-RS, the default lifecycle of a resource class is **per-request**. This means that for every incoming HTTP request, the runtime creates a brand new instance of the resource class, uses it to handle that single request, and discards it once the response is sent.

The runtime does **not** treat resource classes as singletons by default. This design ensures that each request is processed in complete isolation — resource class instances are inherently stateless, and because no instance is ever shared between threads, there are no concurrency hazards within the class itself.

However, this lifecycle has a critical implication for in-memory data management. Because resource instances are short-lived, any data stored as a field on the resource class will be destroyed after the request completes. All shared state — rooms, sensors, and readings — must therefore live in a **separate singleton component**, which in this project is `DataStore`. The `DataStore` is instantiated once (via a static field) and lives for the entire lifetime of the application.

Because this singleton `DataStore` is accessed by multiple concurrent request-handler threads simultaneously, storing data in a plain `HashMap` or `ArrayList`would risk race conditions, lost updates, and `ConcurrentModificationException`. To prevent this, the implementation uses `ConcurrentHashMap` for rooms and sensors, and a ConcurrentHashMap mapping each sensor ID to its reading history, ensuring all shared state is accessed through thread-safe structures without requiring explicit  `synchronized` blocks on every access.

---

### Part 1 – Q2: HATEOAS
**Question: Why is the provision of ”Hypermedia” (links and navigation within responses)
considered a hallmark of advanced RESTful design (HATEOAS)? How does this approach
benefit client developers compared to static documentation?**

HATEOAS (Hypermedia as the Engine of Application State) is the practice of embedding navigational links inside API responses so that clients can discover available resources and valid next actions dynamically, without relying on hard-coded URLs or consulting external documentation.

For example, the `GET /api/v1` discovery response in this project includes a `_links` block pointing to `/api/v1/rooms`, `/api/v1/sensors`, and the parametric reading URL. A client can start at the root and traverse the entire API without any prior knowledge of its structure.

**Benefits over static documentation:**

- **Discoverability** — a single entry point exposes the entire API surface; clients navigate rather than memorise.
- **Reduced coupling** — because clients follow links rather than hard-coding paths, a server-side URL change does not break clients that were consuming those links dynamically.
- **Self-describing responses** — responses indicate what actions are currently possible, reducing developer guesswork.
- **Smoother versioning** — new resources and links can be added to responses without breaking existing clients, supporting organic API evolution.

---

### Part 2 – Q1: Full Objects vs IDs in List Responses
**Question: When returning a list of rooms, what are the implications of returning only
IDs versus returning the full room objects? Consider network bandwidth and client side
processing**

When `GET /api/v1/rooms` returns a list, there are two design options:

**Return only IDs:** Produces a minimal payload, conserving bandwidth. However, the client must issue a separate `GET /api/v1/rooms/{id}` request for every room it wants to display — this is the classic **N+1 request problem**, which multiplies latency proportionally with the number of rooms and can make the application feel slow.

**Return full room objects:** The payload is larger, but the client receives everything it needs in a single round-trip. This is the approach taken in this implementation, as it prioritises usability and performance for typical client use-cases (e.g. rendering a list page that shows room names and capacities).

The trade-off shifts when the collection is extremely large (thousands of items). In that case, pagination with a lighter representation (ID + name only) is more appropriate. For the scale of this coursework, returning full objects is the correct default.

---

### Part 2 – Q2: Is the DELETE Operation Idempotent?
**Question: IstheDELETEoperationidempotentinyourimplementation? Provideadetailed
justification by describingwhathappensifaclientmistakenlysendstheexactsameDELETE
request for a room multiple times.**

This implementation of the `DELETE` operation is idempotent as per the HTTP/1.1 definition in RFC 9110, and therefore exhibits idempotent behaviour.

An idempotent operation can be applied multiple times but will only modify the server state once, regardless of how many times the operation was called.

- **1st Call:** Room exists and has no sensors → Room has been deleted → Server returns `200 OK`.
- **2nd Call (same room ID):** Room does not exist → Server returns `404 Not Found`.

Even though the HTTP response code is different from the first and second call to delete the room, the **ending state of the system is equal** (the room is deleted) which satisfies idempotency of the system.

If you try to perform a DELETE on a room that has sensors, you will continue to receive `409 Conflict` from the server until the constraint is resolved; therefore, the server state does not change and meets the definition of idempotent behaviour in this case as well.

---

### Part 3 – Q1: @Consumes and Content-Type Mismatch

**Question: Weexplicitly use the @Consumes (MediaType.APPLICATION_JSON) annotation on
the POST method. Explain the technical consequences if a client attempts to send data in
a different format, such as text/plain or application/xml. How does JAX-RS handle this
mismatch?**

The `@Consumes(MediaType.APPLICATION_JSON)` annotation is used to indicate that this endpoint will consume only one media type, JSON. This is checked by the JAX-RS runtime with no execution of application-level code prior to invoking the resource method. 

When a client sends a request that contains an incompatible Content-Type header value (such as text/plain or application/xml), the runtime will perform content negotiation by checking the Content-Type of the request against the @Consumes annotation value. Since there is no match, the runtime will reject the request and return a 415 (Unsupported Media Type) response. The request body will not even be attempted to be deserialized.

The advantages of this method mainly consist of its ability to validate input in a declarative manner, meaning that developers do not have to write code to deal with invalid media types. Additionally, this mechanism eliminates the possibility of having a partially processed error when, for example, an XML body might partially parse as a JSON body, which could otherwise lead to unexpected results.

---


### Part 3 - Q2: Query Parameter vs Path Segment for Filtering

**Question: Youimplementedthisfilteringusing@QueryParam. Contrastthiswithanalterna
tive design where the type is part of the URL path (e.g., /api/vl/sensors/type/CO2). Why
is the queryparameterapproachgenerallyconsideredsuperiorforfilteringandsearching
collections?**

Both methods can be used to filter a collection, however they do follow different REST design purposes:

| Approaches     | Example                 | Intended Purpose             |
|----------------|-------------------------|------------------------------|
| Query Parameter | /api/v1/sensors?type=CO2 | Filter/ search a collection |
| Path Segment    | /api/v1/sensors/type/CO2 | Identify a specific sub-resource |

**Query Parameters Are Better than Path Segments for Filtering** 

1 - **Semantic Accuracy** - A URL path segment represents a resource. Filtering is not considered a resource, it's a modifier applied to a collection, and filtering with a path segment implies there is such a resource (for example, the resource "/sensors/type/CO2") that does not exist.
2 - **Composability** - Query parameters can be easily combined (for example, ?type=CO2&status=ACTIVE), while path-based filters use separate path segments for each filter to create unmaintainable URLs (for example, /sensors/type/CO2/status/ACTIVE).
3 - **Optional By Design** - Query parameters can be omitted if there are no filters applied, while both an endpoint can process both filtered and unfiltered requests with no change to the API. However, path-based filters create a necessity to use a different route for unfiltered requests.
4 - **REST Conformance** - Query parameters for filtering and searching collections for APIs are consistently implemented by the broader REST community and many public APIs, making it intuitive for developers to understand how to use APIs.

---
### Part 4 – Q1: Sub-Resource Locator Pattern
**Question: Discuss the architectural benefits of the Sub-Resource Locator pattern. How
doesdelegating logic to separate classes help manage complexity in large APIs compared
to defining every nested path (e.g., sensors/{id}/readings/{rid}) in one massive con
troller class?**

The Sub-Resource Locator design pattern encapsulates the delegation of a nested URL path's responsibility in its own class rather than inside the parent resource. For example, `SensorResource` has a method annotated with `@Path("/{sensorId}/readings")` that returns a `SensorReadingResource` instance (note that we do not use an HTTP method annotation to indicate how to invoke this method). The lifecycle of that instance will be managed independently by the JAX-RS runtime.

**Benefits of this architectural approach:**

1. **Single Responsibility Principle**: The `SensorResource` class is responsible for managing the lifecycle of a sensor, while the `SensorReadingResource` class is responsible for managing the history of all readings taken from that sensor. Each class has a clear responsibility, which means it is easier to understand, test, and modify independently of one another.
2. **Reduced complexity**: Using one large controller to handle each nested path quickly makes that controller complex and difficult to read. This approach utilizes delegation to divide responsibilities amongst separate classes that all have a clearly defined focus.
3. **Independent testability**: The `SensorReadingResource` class can be instantiated directly in a unit test with a sensorId specified as input, thereby eliminating the need to spin up the full JAX-RS container or route the request through the `SensorResource`.
4. **Maintainability and extensibility**: The addition of a new operation (e.g. aggregation endpoints) under the base path of `/readings` can be done solely within the `SensorReadingResource` class, posing no risk of unwittingly impacting the operation of the logic contained in `SensorResource`.
---

### Part 5 – Q2: Why 422 vs 404 for a Missing Referenced Resource
**Question: WhyisHTTP422oftenconsideredmoresemanticallyaccurate than a standard
404 whenthe issue is a missing reference inside a valid JSON payload?**

- **`404 Not Found`** means the **requested URI** does not exist — the URL path `/api/v1/sensors` was not found on the server. That is clearly wrong here: the endpoint exists and was reached successfully.
- **`422 Unprocessable Entity`** means the request was **syntactically valid** (correct `Content-Type`, well-formed JSON) but **semantically invalid** — the server understood the request but cannot act on it because of a business rule violation. In this case, the `roomId` field inside the payload references an entity that does not exist.

Using `404` would be actively misleading: it would suggest the client used the wrong URL. `422` tells the developer precisely: _"Your request arrived correctly and was parsed — the problem is a broken reference inside your JSON payload."_ This is far more actionable: the developer knows to fix the `roomId` value, not the endpoint URL.

---

### Part 5 – Q4: Cybersecurity Risks of Exposing Stack Traces
**Question: From a cybersecurity standpoint, explain the risks associated with exposing
internal Java stack traces to external API consumers. What specific information could an
attacker gather from such a trace?**

While raw Java stack traces are intended for debugging, exposing them directly to API clients creates a significant security risk. Stack traces contain many sensitive pieces of information about how your internal systems operate, which cybercriminals could use as a foothold in your systems.

Cybercriminals could take advantage of raw stacks to extract information, such as:

- **Class and package names**: which would indicate the structure and logic behind your systems, enabling targeed code analysis.
- **Third-party libraries and version numbers**: which would allow a criminal to look up known vulnerabilities to inject attacks against your systems.
- **Server-side file paths**: which would give the attacker information about your server environment as well as how you deploy your systems, and potentially the username running the process.
- **Business logic flow**: Execution flow that would show an attacker how requests are processed through your system, helping them identify execution paths, input validation gaps and plan attacks.
- **Database and infrastructure details**: in deeper stack traces, connection pool class names, ORM internals, or SQL error messages may appear, providing a roadmap to the data layer.

With this information, cybercriminals can identify your system and create specific exploits against it.

To mitigate this risk, the GlobalExceptionMapper logs the full stack trace of the error on the server, while only returning a generic 500 Internal Server Error response to the client making the API call. This allows for debugging purposes to be kept internally while protecting sensitive information externally.

---

### Part 5 – Q5: Why Filters for Cross-Cutting Concerns
**Question: Why is it advantageous to use JAX-RS filters for cross-cutting concerns like
logging, rather than manually inserting Logger.info() statements inside every single re
source method**

Cross-cutting concerns are behaviours that must apply to every endpoint uniformly — logging, authentication, CORS, rate-limiting. JAX-RS filters are the correct mechanism because:

1. **DRY principle** — A single `LoggingFilter` class applies to all endpoints automatically. Without filters, the same `Logger.info()` calls would have to be copy-pasted into every resource method — creating maintenance debt and risk of omission.
2. **Consistency** — Every single request and response is logged in an identical format. Manual insertion risks inconsistent log messages (different formats, missing fields) across methods written at different times by different developers.
3. **Separation of concerns** — Resource methods should contain only business logic. Embedding logging code inside them mixes infrastructure concerns with domain logic, reducing readability and violating the Single Responsibility Principle.
4. **Response interception** — `ContainerResponseFilter` runs _after_ the response has been fully built, which means it can log the final HTTP status code (including codes set by exception mappers). This is not easily achievable by inserting logger calls inside resource methods, which execute before exception mappers.
5. **Reliability** — Filters are applied automatically by the JAX-RS runtime for every matched request. A developer cannot accidentally forget to add logging to a new endpoint, because the filter covers it without any code change.

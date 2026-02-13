# Country Routing Service

Spring Boot service that calculates land routes between countries.

## Build

```bash
./mvnw clean package
```

## Run

```bash
./mvnw spring-boot:run
```

Or run the JAR:
```bash
java -jar target/routing-0.0.1-SNAPSHOT.jar
```

Service starts on `http://localhost:8080`

## API Usage

### Endpoint

```
GET /routing/{origin}/{destination}
```

Country codes use ISO 3166-1 alpha-3 format (CZE, ITA, USA, etc.)

### Example: Czech Republic to Italy

```bash
curl http://localhost:8080/routing/CZE/ITA
```

**Response (200 OK):**
```json
{
  "route": ["CZE", "AUT", "ITA"]
}
```

### Example: No Land Route

```bash
curl http://localhost:8080/routing/USA/JPN
```

**Response:** `400 Bad Request` (empty body)

### Example: Invalid Country

```bash
curl http://localhost:8080/routing/XXX/ITA
```

**Response (400 Bad Request):**
```json
{
  "message": "Unknown origin country: XXX"
}
```

---

## How It Works

### Startup Flow (Bean Lifecycle)

```
1. Spring Boot starts
    ↓
2. RestClientConfiguration creates RestClient bean
    ↓
3. CountryDataLoader bean created (RestClient + ObjectMapper injected)
    ↓
4. GraphConfiguration.countryGraph() called
    ├─ Fetches JSON from GitHub (~0.5s)
    ├─ Parses 250 countries (~0.2s)
    └─ Builds adjacency list (~0.2s)
    ↓
5. CountryGraph bean ready (immutable, cached in memory)
    ↓
6. BfsRouteFinder bean created (CountryGraph injected)
    ↓
7. RoutingService bean created (CountryGraph + BfsRouteFinder injected)
    ↓
8. RoutingController bean created (RoutingService injected)
    ↓
9. Application ready (~1.9s total)
```

**Fail-fast:** If data loading fails, application won't start.

### Request Flow

```
HTTP Request
    ↓
RoutingController
    ↓
RoutingService (validates countries exist)
    ↓
BfsRouteFinder (finds shortest path)
    ↓
CountryGraph (adjacency list lookup)
    ↓
HTTP Response
```

### BFS Algorithm Example

**Find route: CZE → ITA**

```
Graph:
CZE ←→ AUT ←→ ITA
       ↕
      DEU

Step 1: Start at CZE
  Queue: [CZE]
  Visited: {CZE}

Step 2: Explore CZE neighbors
  Queue: [AUT, DEU]
  Visited: {CZE, AUT, DEU}

Step 3: Explore AUT neighbors
  Queue: [DEU, ITA]
  Visited: {CZE, AUT, DEU, ITA}
  Found ITA! ✓

Step 4: Reconstruct path
  ITA ← AUT ← CZE
  Result: [CZE, AUT, ITA]
```

### Why BFS?

| Algorithm | Time | Shortest Path? | Best For |
|-----------|------|----------------|----------|
| **BFS** | O(V+E) | ✅ Yes | **Unweighted graphs** |
| DFS | O(V+E) | ❌ No | Maze solving |
| Dijkstra | O(V²) | ✅ Yes | Weighted graphs |

**Our case:** All borders = 1 hop (unweighted) → BFS is optimal

## Architecture

```
src/main/java/com/example/routing/
├── RoutingApplication.java          # Main entry
├── api/
│   ├── RoutingController.java       # REST endpoint
│   └── dto/
│       ├── RouteResponse.java       # Success response
│       └── ErrorResponse.java       # Error response
├── service/
│   ├── RouteFinder.java             # Strategy interface
│   ├── BfsRouteFinder.java          # BFS implementation
│   └── RoutingService.java          # Validation logic
├── domain/
│   ├── model/
│   │   └── CountryGraph.java        # Graph data structure
│   └── exception/
│       └── RouteNotFoundException.java
└── infrastructure/
    ├── CountryDataLoader.java       # Loads country JSON
    ├── GraphConfiguration.java      # Bean configuration
    ├── RestClientConfiguration.java # HTTP client bean
    └── model/
        └── Country.java             # JSON mapping
```

### Data Structure

**CountryGraph** uses adjacency list:

```java
Map<String, Set<String>> = {
    "CZE" → {"AUT", "DEU", "POL", "SVK"},
    "AUT" → {"CZE", "DEU", "ITA", ...},
    "ITA" → {"AUT", "FRA", "CHE", ...}
}
```

- Fast neighbor lookup: O(1)
- Memory efficient: O(V + E)
- 250 countries, 649 borders

## Technology Stack

- Java 21
- Spring Boot 3.5.10
- Lombok
- Maven

## Testing

```bash
./mvnw test
```

8 tests covering:
- Context loading
- Route finding (success/no route)
- Country validation
- HTTP responses (200/400)

## Configuration

`src/main/resources/application.yml`:

```yaml
routing:
  data-url: https://raw.githubusercontent.com/mledoze/countries/master/countries.json

logging:
  level:
    com.example.routing: INFO
```

## Extending with New Algorithms

Implement the `RouteFinder` interface:

```java
@Service
public class DijkstraRouteFinder implements RouteFinder {
    
    @Override
    public List<String> findRoute(String origin, String destination) {
        // Your algorithm here
    }
}
```

Spring will auto-wire it to `RoutingService`.

## Performance

- Startup: ~2 seconds
- Response time: <10ms
- Memory: ~150MB
- Data: 250 countries, 649 borders
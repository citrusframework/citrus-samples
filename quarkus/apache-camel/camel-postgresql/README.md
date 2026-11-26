# Apache Camel PostgreSQL Integration Testing with Citrus

This example demonstrates how to test Apache Camel routes in Quarkus applications using Citrus framework with PostgreSQL database validation. The project showcases a Camel route that receives HTTP POST requests and persists data into a PostgreSQL database via the `postgresql-sink` Kamelet.

## Why Citrus for SQL Validation?

When building integration routes that persist data to a database, you need to verify that the data actually arrived and is correct. Citrus provides powerful SQL validation capabilities that let you query the database directly in your integration test and assert the expected rows.

**The key advantage demonstrated in this example:** Citrus shares the same DataSource managed by Quarkus Dev Services. This means your test connects to the exact same PostgreSQL instance that the application writes to — no additional database configuration needed in the test.

**Benefits:**
- Single DataSource shared between Quarkus application and Citrus test
- Quarkus Dev Services automatically provisions the PostgreSQL container
- SQL query validation directly in the Gherkin-style test DSL
- No separate test database infrastructure required
- Real PostgreSQL (via Testcontainers), not mocks

## What You'll Learn

By the end of this guide, you'll understand:

- **Citrus SQL Validation**: How to use Citrus `sql()` actions to query and validate database state
- **Shared DataSource**: How Citrus shares the Quarkus-managed DataSource to connect to the same Dev Services database
- **Kamelet Sink**: How to use the `postgresql-sink` Kamelet to persist data from a Camel route
- **Dev Services Integration**: How `@CitrusSupport(devServicesProperties = "*")` enables Citrus to work alongside Quarkus Dev Services
- **End-to-End Testing**: How to combine HTTP client actions with SQL validation in a single test

## The Application Under Test

The Quarkus application uses Apache Camel to implement an HTTP-to-database integration route:

```
HTTP POST /headline → Apache Camel Route → PostgreSQL (via postgresql-sink Kamelet)
```

### Apache Camel Route

The application consists of a Camel route defined in `Routes.java`:

```java
public class Routes extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("platform-http:/headline?httpMethodRestrict=POST")
            .to("kamelet:postgresql-sink?" +
                    "serverName={{jdbc.server.host}}&" +
                    "serverPort={{jdbc.server.port}}&" +
                    "username={{jdbc.username}}&" +
                    "password={{jdbc.password}}&" +
                    "databaseName={{jdbc.database.name}}&" +
                    "query=INSERT INTO headlines VALUES (:#id,:#headline)")
            .setBody().constant("Headline created!")
            .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201));
    }
}
```

**Route Breakdown:**

1. **from("platform-http:/headline?httpMethodRestrict=POST")**: Exposes an HTTP POST endpoint at `/headline` using the Quarkus HTTP server
2. **to("kamelet:postgresql-sink?...")**: Invokes the `postgresql-sink` Kamelet which:
   - Parses the JSON body into a Map
   - Executes the SQL INSERT using `:#id` and `:#headline` as named parameters bound from the Map keys
   - Uses its own DBCP2 connection pool to connect to PostgreSQL
3. **.setBody().constant("Headline created!")**: Sets the HTTP response body
4. **.setHeader(Exchange.HTTP_RESPONSE_CODE, constant(201))**: Returns HTTP 201 Created

### Database Initialization

The `DatabaseInit` class creates the `headlines` table at application startup using the Quarkus-managed DataSource:

```java
@ApplicationScoped
public class DatabaseInit {

    @Inject
    DataSource dataSource;

    void onStart(@Observes StartupEvent ev) throws SQLException {
        try (var conn = dataSource.getConnection();
             var stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS headlines (id INTEGER PRIMARY KEY, headline VARCHAR(255))");
        }
    }
}
```

This ensures the table exists before any Camel route processes data. In test mode, this runs against the Dev Services PostgreSQL container.

### PostgreSQL Kamelet Sink

The `postgresql-sink` Kamelet is a pre-built Camel Kamelet that simplifies database inserts. It:

- Accepts a JSON body and converts it to a Map
- Uses the Camel SQL component internally to execute parameterized queries
- Creates its own DBCP2 connection pool from the provided connection parameters
- Supports named parameters (`:#paramName`) that bind to Map keys

The connection parameters (`serverName`, `serverPort`, `username`, `password`, `databaseName`) are resolved from Quarkus application properties using Camel property placeholders (`{{...}}`).

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how to verify end-to-end data persistence using Citrus HTTP and SQL actions.

### Test Annotations

```java
@QuarkusTest
@CitrusSupport(devServicesProperties = "*")
class QuarkusApplicationTest implements TestActionSupport {
    // ...
}
```

**What's happening here:**

- `@QuarkusTest`: Starts the Quarkus application (including Camel routes) in test mode
- `@CitrusSupport(devServicesProperties = "*")`: Enables Citrus framework integration with Quarkus
  - The `devServicesProperties = "*"` parameter is critical — it tells Citrus to accept all properties injected by Quarkus Dev Services, ensuring both frameworks share the same configuration context
- `TestActionSupport`: Provides factory methods for test actions (`http()`, `sql()`, `createVariables()`, etc.)

### Test Setup: HTTP Client and DataSource

```java
@CitrusEndpoint
@HttpClientConfig(requestUrl = "http://localhost:8081")
HttpClient httpClient;

@Inject
DataSource dataSource;
```

**What's happening here:**

- `@CitrusEndpoint` + `@HttpClientConfig`: Declares a Citrus HTTP client endpoint pointing to the Quarkus test server (port 8081)
- `@Inject DataSource`: Injects the Quarkus-managed DataSource — the same DataSource connected to the Dev Services PostgreSQL container
- This DataSource is used by `DatabaseInit` for table creation AND by the Citrus `sql()` action for validation — true sharing

### Test Execution: Gherkin-Style DSL

```java
@Test
void shouldPersistData() {
    runner.given(
        createVariables()
            .variable("id", "citrus:randomNumber(4)")
            .variable("headline", "Camel rocks!")
    );

    runner.when(
        http().client(httpClient)
            .send()
            .post("/headline")
            .message()
            .body("""
                { "id": ${id}, "headline": "${headline}" }
                """)
            .contentType("application/json")
    );

    runner.then(
        http().client(httpClient)
            .receive()
            .response(HttpStatus.CREATED)
            .message()
            .body("Headline created!")
    );

    runner.then(
        sql().dataSource(dataSource)
            .query()
            .statement("SELECT headline FROM headlines WHERE id=${id}")
            .validate("HEADLINE", "${headline}")
    );
}
```

**Test Flow Breakdown:**

1. **GIVEN**: Create test variables — a random 4-digit `id` and a `headline` text
2. **WHEN**: Send an HTTP POST to `/headline` with a JSON body containing the variables
3. The Camel route processes the request:
   - The `postgresql-sink` Kamelet parses the JSON and inserts a row into the `headlines` table
   - The route returns "Headline created!" with HTTP 201
4. **THEN** (HTTP): Verify the HTTP response is 201 Created with the expected body
5. **THEN** (SQL): Query the database directly and validate that the inserted row exists with the correct headline value

The test validates:
- The Camel route successfully receives HTTP POST requests
- The `postgresql-sink` Kamelet correctly inserts data into PostgreSQL
- The database contains the expected row with the correct values
- End-to-end integration between HTTP, Camel, Kamelet, and PostgreSQL

## Key Testing Concepts

### 1. Shared DataSource with Quarkus Dev Services

The central concept in this example is **DataSource sharing**. Three components connect to the same PostgreSQL instance:

| Component | Connection Method | Purpose |
|-----------|------------------|---------|
| Quarkus Dev Services | Starts PostgreSQL container automatically | Provides the database infrastructure |
| postgresql-sink Kamelet | Own DBCP2 connection pool (via `jdbc.*` properties) | Writes data from the Camel route |
| Citrus SQL action | Quarkus-managed DataSource (`@Inject DataSource`) | Reads and validates data in the test |

The `jdbc.*` properties in `application.properties` are configured to match the Dev Services container (host, port, username, password, database name), so the Kamelet's connection pool connects to the same database.

### 2. Citrus SQL Module

The `citrus-sql` Maven dependency provides SQL testing capabilities:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-sql</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This module provides:
- **SQL Query Actions**: Execute SELECT queries and validate result sets
- **SQL Execute Actions**: Run DDL/DML statements (CREATE, INSERT, UPDATE, DELETE)
- **Column Validation**: Assert specific column values in query results
- **DataSource Integration**: Accept any `javax.sql.DataSource` for database connectivity
- **Variable Support**: Use Citrus test variables (`${id}`, `${headline}`) in SQL statements

### 3. Citrus HTTP Module

The `citrus-http` dependency provides HTTP client capabilities:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-http</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This provides:
- **HTTP Client Endpoint**: Send requests and receive responses
- **Request Builder**: Fluent API for POST, GET, PUT, DELETE with headers and body
- **Response Validation**: Assert HTTP status codes and response bodies

### 4. Dev Services Configuration

**Test Configuration** (`src/test/resources/application.properties`):

```properties
quarkus.arc.ignored-split-packages=org.citrusframework.*

quarkus.datasource.devservices.port=15432

jdbc.server.host=localhost
jdbc.server.port=15432
jdbc.username=quarkus
jdbc.password=quarkus
jdbc.database.name=quarkus
```

**What's happening here:**

- `quarkus.datasource.devservices.port=15432`: Fixes the Dev Services PostgreSQL container to port 15432 so the Kamelet's `jdbc.*` properties can be statically configured
- `jdbc.*` properties: Override the production values to match the Dev Services container defaults (username `quarkus`, password `quarkus`, database `quarkus`)
- The Quarkus DataSource is automatically configured by Dev Services — no explicit JDBC URL needed

**Production Configuration** (`src/main/resources/application.properties`):

```properties
jdbc.server.host=localhost
jdbc.server.port=5432
jdbc.username=postgres
jdbc.password=postgres
jdbc.database.name=postgres

quarkus.datasource.db-kind=postgresql
```

### 5. Apache Camel Kamelet Dependencies

The `postgresql-sink` Kamelet requires several Camel Quarkus extensions:

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-kamelet</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.kamelets</groupId>
    <artifactId>camel-kamelets</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-sql</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-jackson</artifactId>
</dependency>
<dependency>
    <groupId>org.apache.commons</groupId>
    <artifactId>commons-dbcp2</artifactId>
</dependency>
```

- **camel-quarkus-kamelet**: Runtime support for Camel Kamelets
- **camel-kamelets**: The Kamelet catalog containing `postgresql-sink`
- **camel-quarkus-sql**: The Camel SQL component used internally by the Kamelet
- **camel-quarkus-jackson**: JSON parsing for converting the HTTP body to a Map
- **commons-dbcp2**: Connection pool used by the Kamelet to create its own DataSource

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-postgresql/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus Dev Services starts a PostgreSQL container on port 15432
2. `@CitrusSupport(devServicesProperties = "*")` initializes Citrus with Dev Services properties
3. Quarkus starts the application in test mode
4. `DatabaseInit` creates the `headlines` table using the Quarkus DataSource
5. Apache Camel routes are discovered and started
6. The test creates random test variables (`id`, `headline`)
7. Citrus sends an HTTP POST to `/headline` with a JSON body
8. The Camel route processes the request and the Kamelet inserts a row into PostgreSQL
9. Citrus validates the HTTP 201 response
10. Citrus queries the database using the shared DataSource and validates the inserted row
11. The test completes, and the PostgreSQL container is automatically stopped

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Apache Camel Kamelets](https://camel.apache.org/camel-kamelets/latest/) - Kamelet catalog reference
- [Apache Camel SQL Component](https://camel.apache.org/components/latest/sql-component.html) - SQL component reference
- [Quarkus Dev Services](https://quarkus.io/guides/databases-dev-services) - Database Dev Services guide
- [Citrus SQL Module](https://citrusframework.org/docs/endpoints/sql/) - SQL endpoint reference

---

**Next Steps**: Try extending the example with more SQL validation patterns — multiple row inserts, update operations, or combining SQL validation with other Citrus endpoints. Explore using Citrus SQL execute actions for test data setup alongside query validation.

## Sample contract

**Purpose**: This example demonstrates how to test Apache Camel routes in Quarkus applications using Citrus framework with PostgreSQL database validation.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-http`, `citrus-sql`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with HTTP and SQL actions.
**Prerequisites**: JDK 21 or newer. Docker must be running for Testcontainers or Quarkus Dev Services.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-postgresql/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-postgresql /tmp/citrus-camel-postgresql
cd /tmp/citrus-camel-postgresql
mvn clean verify -Dsystem.under.test.mode=embedded
```

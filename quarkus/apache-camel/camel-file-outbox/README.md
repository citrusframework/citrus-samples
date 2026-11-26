# Apache Camel File Aggregation with Data Format Integration

This example demonstrates how to test Apache Camel routes that aggregate multiple messages into a single file using Citrus framework with **Camel processor integration**. The project showcases the **Aggregator EIP pattern** and how Citrus can leverage Camel's processing capabilities to consume and transform file content in tests.

## What You'll Learn

By the end of this guide, you'll understand:

- How to use the Aggregator EIP pattern in Apache Camel to combine multiple messages
- How to create custom aggregation strategies for message consolidation
- How to write aggregated content to files using Camel's file component
- How Citrus can leverage Camel processors for message transformation during verification
- How to use Camel's Endpoint DSL to consume files in tests
- How to test direct-to-file integration scenarios with message aggregation
- The power of combining Citrus test orchestration with Camel's processing capabilities

## The Application Under Test

The Quarkus application uses Apache Camel to implement a message aggregation pipeline that collects multiple messages and writes them to a file:

```
Direct Endpoint (direct:tasks) → Aggregate (3 messages) → File (outbox/tasks.txt)
```

### Apache Camel Route

The application consists of a sophisticated aggregation route defined in `Routes.java`:

```java
public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(direct("tasks"))
            .aggregate(constant(true), new MultilineAggregationStrategy())
                .completionSize(3)
                .to(file("outbox").fileName("tasks.txt"));
    }

    static class MultilineAggregationStrategy implements AggregationStrategy {
        public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
            if (oldExchange == null) {
                return newExchange;
            }

            String oldBody = oldExchange.getIn().getBody(String.class);
            String newBody = newExchange.getIn().getBody(String.class);
            oldExchange.getIn().setBody(oldBody + "\n" + newBody);
            return oldExchange;
        }
    }
}
```

**Route Breakdown:**

1. **from(direct("tasks"))**:
   - Direct endpoint for synchronous in-memory message passing
   - Uses **Endpoint DSL** (EndpointRouteBuilder) for type-safe endpoint configuration
   - Entry point for task messages

2. **aggregate(constant(true), new MultilineAggregationStrategy())**:
   - **Aggregator EIP**: Combines multiple messages into a single message
   - `constant(true)`: Correlation expression (all messages grouped together)
   - `MultilineAggregationStrategy`: Custom strategy defining how to merge messages

3. **completionSize(3)**:
   - Aggregation completes after receiving 3 messages
   - Alternative completion conditions: timeout, predicate, manual completion
   - Once completed, the aggregated message is released

4. **to(file("outbox").fileName("tasks.txt"))**:
   - File producer that writes to the `outbox` directory
   - Fixed file name: `tasks.txt`
   - Each aggregation cycle overwrites the previous file

### Understanding Key Concepts

#### Aggregator EIP (Enterprise Integration Pattern)

The Aggregator pattern combines related messages into a single consolidated message:

```java
.aggregate(constant(true), new MultilineAggregationStrategy())
    .completionSize(3)
```

**How it works:**

1. **Incoming Messages**:
   - Message 1: "Doctor's appointment 9:00am"
   - Message 2: "Fetch kids from school"
   - Message 3: "Plan next vacation in June"

2. **Correlation**: `constant(true)` groups all messages together
   - More sophisticated: group by header value, body content, or expression

3. **Aggregation Strategy**: Defines how to merge messages
   - First message: becomes the initial aggregate
   - Subsequent messages: merged with aggregate using strategy

4. **Completion Condition**: `completionSize(3)`
   - After 3 messages, aggregation completes
   - Aggregated message is sent downstream

5. **Final Output** (written to file):
   ```
   Doctor's appointment 9:00am
   Fetch kids from school
   Plan next vacation in June
   ```

**Use cases:**
- Batch processing (collect N messages, then process)
- Report generation (aggregate data into single report)
- File consolidation (combine multiple inputs into one file)
- Message buffering (collect messages before expensive operation)

#### Custom Aggregation Strategy

The `MultilineAggregationStrategy` defines the merge logic:

```java
public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
    if (oldExchange == null) {
        return newExchange;  // First message
    }

    String oldBody = oldExchange.getIn().getBody(String.class);
    String newBody = newExchange.getIn().getBody(String.class);
    oldExchange.getIn().setBody(oldBody + "\n" + newBody);
    return oldExchange;
}
```

**Strategy logic:**

1. **First message** (`oldExchange == null`): Return as-is (becomes the aggregate)
2. **Subsequent messages**: 
   - Get existing aggregate body
   - Get new message body
   - Concatenate with newline separator
   - Update aggregate Exchange

**Other aggregation patterns:**

- **Concatenation**: Join with delimiter (this example)
- **Collection**: Build a List or Array of messages
- **Transformation**: Convert messages to different format
- **Calculation**: Sum, average, or compute statistics
- **Selection**: Choose best/worst message based on criteria

#### Endpoint DSL (EndpointRouteBuilder)

The route extends `EndpointRouteBuilder` for type-safe endpoint configuration:

```java
from(direct("tasks"))        // Type-safe
to(file("outbox").fileName("tasks.txt"))  // Fluent API
```

vs. traditional string-based approach:

```java
from("direct:tasks")         // String-based
to("file:outbox?fileName=tasks.txt")  // Error-prone
```

**Benefits:**
- **Type safety**: Compile-time validation
- **IDE support**: Auto-completion for options
- **Refactoring friendly**: Easy to update configurations
- **Cleaner syntax**: Readable method chains

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how **Citrus leverages Camel processors** to consume and transform file content during verification.

### Test Setup: Shared CamelContext

As with other Camel examples, the CamelContext is shared:

```java
@Inject
@BindToRegistry
CamelContext camelContext;
```

This enables:
- Direct invocation of Camel routes via `camel:direct:tasks`
- **Access to Camel processors in Citrus for message transformation**
- Ability to consume from Camel file endpoints
- Shared infrastructure between application and tests

### Test Execution: Aggregation and Verification

The test demonstrates both message aggregation and **file consumption with Camel processor transformation**:

```java
@Test
void shouldAggregateFileContent() {
    runner.when(
        Arrays.asList(
            send()
                .endpoint("camel:direct:tasks")
                .message()
                .body("Doctor's appointment 9:00am"),
            send()
                .endpoint("camel:direct:tasks")
                .message()
                .body("Fetch kids from school"),
            send()
                .endpoint("camel:direct:tasks")
                .message()
                .body("Plan next vacation in June")
        ) // send three messages that should be aggregated
    );

    runner.then(
        camel()
            .receive()
            .endpoint(CamelSupport.camel().endpoints().file("outbox")::getRawUri)
            .process(processor().camel(camelContext).convertBodyTo(String.class))
            .message()
            .header("CamelFileName", "tasks.txt")
            .body("""
            Doctor's appointment 9:00am
            Fetch kids from school
            Plan next vacation in June
            """)
    );
}
```

**Test Flow Breakdown:**

#### Step 1: Send Multiple Messages to Trigger Aggregation

```java
runner.when(
    Arrays.asList(
        send().endpoint("camel:direct:tasks").body("Doctor's appointment 9:00am"),
        send().endpoint("camel:direct:tasks").body("Fetch kids from school"),
        send().endpoint("camel:direct:tasks").body("Plan next vacation in June")
    )
);
```

**What's happening:**

1. **Arrays.asList(...)**: Send multiple messages in sequence
2. **endpoint("camel:direct:tasks")**: Citrus sends to the Camel direct endpoint
3. **Three separate messages** are sent to the aggregator
4. After the 3rd message, the aggregator completes (`.completionSize(3)`)
5. The aggregated message is written to `outbox/tasks.txt`

**Aggregation process:**

```
Message 1 arrives → Aggregate = "Doctor's appointment 9:00am"
Message 2 arrives → Aggregate = "Doctor's appointment 9:00am\nFetch kids from school"
Message 3 arrives → Aggregate = "Doctor's appointment 9:00am\nFetch kids from school\nPlan next vacation in June"
                  → Completion triggered
                  → File written
```

#### Step 2: Receive and Verify File Using Camel Processor

```java
runner.then(
    camel()
        .receive()
        .endpoint(CamelSupport.camel().endpoints().file("outbox")::getRawUri)
        .process(processor().camel(camelContext).convertBodyTo(String.class))
        .message()
        .header("CamelFileName", "tasks.txt")
        .body("""
        Doctor's appointment 9:00am
        Fetch kids from school
        Plan next vacation in June
        """)
);
```

**What's happening here:**

1. **camel().receive()**: Citrus Camel DSL for consuming from Camel endpoints
2. **endpoint(...)**: Uses Camel's Endpoint DSL to create file endpoint
   - `file("outbox")`: Points to the `outbox` directory
   - `getRawUri()`: Gets the endpoint URI string
3. **process(processor().camel(camelContext).convertBodyTo(String.class))**:
   - **This is a key feature!**
   - `processor().camel(camelContext)`: Access Camel's processing capabilities
   - `.convertBodyTo(String.class)`: Use Camel's type converter
   - Citrus executes the Camel processor to convert file bytes to String
   - Enables validation of file content as text
4. **header("CamelFileName", "tasks.txt")**: Validates the file name
5. **body(...)**: Validates the aggregated content with text block syntax

**The Power of Camel Processor Integration:**

Instead of manually reading and converting file content:
```java
// WITHOUT Camel processor (manual approach)
byte[] fileContent = Files.readAllBytes(Paths.get("outbox/tasks.txt"));
String textContent = new String(fileContent, StandardCharsets.UTF_8);
// Then validate textContent
```

You simply use Camel's processor through Citrus:
```java
// WITH Camel processor (declarative approach)
.process(processor().camel(camelContext).convertBodyTo(String.class))
```

**Benefits:**
- **Declarative**: Describe the transformation, not the implementation
- **Reusable**: Same type conversion used in production and tests
- **Powerful**: Access to all Camel type converters and processors
- **Consistent**: Same processing logic everywhere

### End-to-End Flow

1. **Test sends 3 messages** to `camel:direct:tasks`
2. **Camel aggregator** collects the messages
3. **First message**: "Doctor's appointment 9:00am" (initial aggregate)
4. **Second message**: Merged → "Doctor's appointment 9:00am\nFetch kids from school"
5. **Third message**: Merged → Full aggregated content
6. **Completion triggered** (completionSize=3)
7. **File written** to `outbox/tasks.txt`
8. **Citrus consumes** the file from outbox using Camel file endpoint
9. **Camel processor** converts file bytes to String
10. **Citrus verifies** file name and content

## Key Testing Concepts

### 1. Citrus Camel Processor Integration

The ability to use Camel processors in Citrus tests provides powerful transformation capabilities:

```java
.process(processor().camel(camelContext).convertBodyTo(String.class))
```

**Available Camel processors:**

**Type Conversion:**
```java
.process(processor().camel(camelContext).convertBodyTo(String.class))
.process(processor().camel(camelContext).convertBodyTo(byte[].class))
.process(processor().camel(camelContext).convertBodyTo(Integer.class))
```

**Data Format Processing:**
```java
.process(processor().camel(camelContext).unmarshal().zipFile())
.process(processor().camel(camelContext).unmarshal().json())
.process(processor().camel(camelContext).marshal().base64())
```

**Message Manipulation:**
```java
.process(processor().camel(camelContext).setHeader("MyHeader", "value"))
.process(processor().camel(camelContext).removeHeaders("*"))
```

**Why this matters:**

- **Test-Production Parity**: Tests use the same processors as production
- **Rich Functionality**: Access to Camel's extensive processor library
- **Type Conversion**: Automatic conversion between 300+ types
- **Reduced Boilerplate**: No manual transformation code

### 2. Camel File Endpoint in Tests

Citrus can consume from Camel file endpoints using the Endpoint DSL:

```java
.endpoint(CamelSupport.camel().endpoints().file("outbox")::getRawUri)
```

**What this provides:**

- **Type-safe** file endpoint creation
- **Automatic file reading** from the file system
- **Access to file metadata** (file name, size, timestamps)
- **Camel headers** (CamelFileName, CamelFileAbsolute, etc.)

**File endpoint as consumer:**

The test uses the file endpoint to **consume** (read) files, not produce them. This is the opposite of the route, which **produces** (writes) files.

**Consumer vs. Producer:**

```java
// Route: Producer (writes file)
.to(file("outbox").fileName("tasks.txt"))

// Test: Consumer (reads file)
.endpoint(CamelSupport.camel().endpoints().file("outbox")::getRawUri)
```

### 3. Testing Aggregation Patterns

The test verifies the Aggregator EIP by:

1. **Sending required number of messages** (3 in this case)
2. **Verifying aggregation completion** (file is written)
3. **Validating aggregated content** (all messages merged correctly)
4. **Checking aggregation order** (messages appear in sequence)

**Testing considerations:**

- **Completion conditions**: Ensure aggregation completes (size, timeout, predicate)
- **Message order**: Verify correct sequence in aggregated result
- **Aggregation strategy**: Validate merge logic produces expected output
- **Edge cases**: Test with 1 message, exact completion size, more than completion size

### 4. Multi-Line Text Validation

The test uses Java's text block syntax for readable multi-line validation:

```java
.body("""
Doctor's appointment 9:00am
Fetch kids from school
Plan next vacation in June
""")
```

**Benefits:**

- **Readable**: Multi-line strings without escape sequences
- **Maintainable**: Easy to update expected content
- **Precise**: Preserves whitespace and newlines
- **Natural**: Matches the actual file format

### 5. Citrus Camel Module

The `citrus-camel` Maven dependency provides Camel integration:

```xml
<dependency>
    <groupId>org.citrusframework</groupId>
    <artifactId>citrus-camel</artifactId>
    <version>${citrus.version}</version>
    <scope>test</scope>
</dependency>
```

This module adds:
- Camel endpoint support in Citrus (`camel:` URI scheme)
- **Camel processor integration for message transformation**
- Camel Endpoint DSL support in tests
- CamelContext sharing capabilities
- Access to Camel type converters

### 6. Endpoint DSL Benefits

Using the Endpoint DSL in both routes and tests provides consistency:

**Route:**
```java
from(direct("tasks"))
to(file("outbox").fileName("tasks.txt"))
```

**Test:**
```java
.endpoint(CamelSupport.camel().endpoints().file("outbox")::getRawUri)
```

**Benefits:**

- **Consistency**: Same endpoint definition pattern everywhere
- **Type safety**: Compile-time validation
- **Discoverability**: IDE auto-completion shows available options
- **Refactoring**: Easy to change endpoint configuration

### 7. Test Annotations

The test combines Quarkus, Camel, and Citrus:

```java
@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

**Framework integration:**

- **Quarkus**: Application lifecycle and dependency injection
- **Camel**: Route execution and processor capabilities
- **Citrus**: Test orchestration and verification
- **Shared CamelContext**: Enables direct route invocation and processor access

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-file-outbox/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. Apache Camel routes are discovered and started
3. The aggregator initializes with completionSize=3
4. CamelContext is injected into the test
5. Citrus registers the CamelContext
6. **Test sends 3 messages** to `direct:tasks`
7. **Aggregator collects** the messages (1st, 2nd, 3rd)
8. **Aggregation completes** after 3rd message
9. **File is written** to `outbox/tasks.txt` with aggregated content
10. **Citrus consumes** the file from outbox using Camel file endpoint
11. **Camel processor** converts file bytes to String
12. **Citrus verifies** file name matches "tasks.txt"
13. **Citrus verifies** content matches expected multi-line text

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**Execution time**: Typically under 2 seconds (no external dependencies)

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality
- **camel-quarkus-file**: Camel file component for file system integration
- **camel-quarkus-junit**: Camel test support for Quarkus
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-camel**: Adds Camel endpoint support and **processor integration** to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

**Note**: This example is minimal—no Kafka, JMS, or other external transports needed.

## Advanced Use Cases

### Using Other Camel Processors in Tests

The same pattern works with any Camel processor:

**Header Manipulation:**
```java
.process(processor().camel(camelContext).setHeader("Priority", "high"))
```

**JSON Unmarshalling:**
```java
.process(processor().camel(camelContext).unmarshal().json())
```

**Base64 Decoding:**
```java
.process(processor().camel(camelContext).unmarshal().base64())
```

**Custom Expression:**
```java
.process(processor().camel(camelContext).setBody(simple("${body.toUpperCase()}")))
```

### Testing Different Completion Conditions

```java
// Completion by timeout (1 second)
.aggregate(constant(true), new MultilineAggregationStrategy())
    .completionTimeout(1000)
    
// Completion by predicate
.aggregate(constant(true), new MultilineAggregationStrategy())
    .completionPredicate(simple("${body} contains 'DONE'"))
    
// Completion by size OR timeout
.aggregate(constant(true), new MultilineAggregationStrategy())
    .completionSize(10)
    .completionTimeout(5000)
```

### Testing with Different Aggregation Strategies

```java
// Collect into List
static class ListAggregationStrategy implements AggregationStrategy {
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            List<String> list = new ArrayList<>();
            list.add(newExchange.getIn().getBody(String.class));
            newExchange.getIn().setBody(list);
            return newExchange;
        }
        List<String> list = oldExchange.getIn().getBody(List.class);
        list.add(newExchange.getIn().getBody(String.class));
        return oldExchange;
    }
}

// Sum numeric values
static class SumAggregationStrategy implements AggregationStrategy {
    public Exchange aggregate(Exchange oldExchange, Exchange newExchange) {
        if (oldExchange == null) {
            return newExchange;
        }
        Integer oldSum = oldExchange.getIn().getBody(Integer.class);
        Integer newValue = newExchange.getIn().getBody(Integer.class);
        oldExchange.getIn().setBody(oldSum + newValue);
        return oldExchange;
    }
}
```

### Testing File Append Instead of Overwrite

```java
// Route with append
.to(file("outbox").fileName("tasks.txt").fileExist("Append"))

// Test verifies cumulative content
```

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Citrus Camel Module](https://citrusframework.org/docs/endpoints/camel/) - Camel endpoint reference for Citrus
- [Apache Camel File Component](https://camel.apache.org/components/latest/file-component.html) - File component documentation
- [Apache Camel Aggregator EIP](https://camel.apache.org/components/latest/eips/aggregate-eip.html) - Aggregator pattern documentation
- [Apache Camel Endpoint DSL](https://camel.apache.org/manual/Endpoint-dsl.html) - Type-safe endpoint builders
- [Apache Camel Type Converters](https://camel.apache.org/manual/type-converter.html) - Type conversion system
- [Apache Camel Testing](https://camel.apache.org/manual/testing.html) - Official Camel testing guide

---

**Next Steps**: Try different aggregation strategies (sum, collect to List, choose max/min). Test various completion conditions (timeout, predicate, size). Explore correlation expressions to aggregate messages into separate groups. Experiment with the Splitter EIP (opposite of Aggregator) to break aggregated files back into individual messages.

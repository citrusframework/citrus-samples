# Apache Camel File Processing with Data Format Integration

This example demonstrates how to test Apache Camel file-based integration routes using Citrus framework with **Camel data format processors** for complex transformations. The project showcases how Citrus can leverage Camel's powerful data formats (like ZIP file compression) to create realistic test scenarios with minimal code.

## What You'll Learn

By the end of this guide, you'll understand:

- How to create Apache Camel routes that poll directories and process files
- How to use Camel's zipFile data format for compression/decompression
- How to use the Splitter EIP pattern to process file content line-by-line or token-by-token
- How Citrus can leverage Camel data format processors for message transformation
- How to create ZIP files in tests using Camel's marshalling capabilities through Citrus
- How to test file-to-Kafka integration scenarios
- How to use Camel's fluent endpoint builders for type-safe route definitions
- The power of combining Citrus test orchestration with Camel's transformation capabilities

## The Application Under Test

The Quarkus application uses Apache Camel to implement a file processing pipeline that reads ZIP files, extracts content, splits it into words, and publishes to Kafka:

```
File Inbox (ZIP) → Unmarshal → Split → Transform → Kafka Topic (words-out)
```

### Apache Camel Route

The application consists of a sophisticated file processing route defined in `Routes.java`:

```java
public class Routes extends EndpointRouteBuilder {

    @Override
    public void configure() throws Exception {
        from(file("inbox").delete(true))
            .unmarshal(dataFormat().zipFile().end())
            .convertBodyTo(String.class)
            .split(body().tokenize(" "))
                .setBody(exchange -> ">> " + exchange.getIn().getBody().toString().toUpperCase())
                .to("kafka:words-out");
    }
}
```

**Route Breakdown:**

1. **from(file("inbox").delete(true))**:
   - File consumer that polls the `inbox` directory
   - Uses **Endpoint DSL** (EndpointRouteBuilder) for type-safe endpoint configuration
   - `.delete(true)`: Automatically deletes files after successful processing
   - Default polling interval: 500ms
   - Only processes files (not directories)

2. **unmarshal(dataFormat().zipFile().end())**:
   - Applies Camel's **zipFile data format** to decompress the file
   - Extracts the ZIP archive content
   - Unmarshalling: converting from compressed format to usable data
   - The data format uses Java's ZipInputStream under the hood

3. **convertBodyTo(String.class)**:
   - Converts the decompressed byte content to String
   - Ensures the content is in text format for further processing

4. **split(body().tokenize(" "))**:
   - **Splitter EIP**: Breaks the message into multiple messages
   - Tokenizes the content using whitespace as delimiter
   - Each token becomes an individual message
   - Example: "Hello World" → 2 messages: "Hello", "World"

5. **setBody(...toUpperCase())**:
   - Transforms each token to uppercase
   - Prefixes with `">> "`
   - Lambda expression for concise transformation

6. **to("kafka:words-out")**:
   - Publishes each transformed token to Kafka topic `words-out`
   - Each word is sent as a separate Kafka message

### Understanding Key Concepts

#### Endpoint DSL (EndpointRouteBuilder)

The route extends `EndpointRouteBuilder` instead of the traditional `RouteBuilder`:

```java
from(file("inbox").delete(true))  // Type-safe, fluent API
```

vs. traditional string-based approach:

```java
from("file:inbox?delete=true")    // String-based, error-prone
```

**Benefits:**
- **Type safety**: Compile-time checking of endpoint configuration
- **IDE support**: Auto-completion for options
- **Refactoring friendly**: Easier to update endpoint configurations
- **Cleaner syntax**: Fluent, readable method chains

#### Camel Data Formats

Data formats handle marshalling (object → format) and unmarshalling (format → object):

**ZIP File Data Format:**
```java
.unmarshal(dataFormat().zipFile().end())
```

**Other common data formats:**
- `json()`: JSON serialization/deserialization
- `xml()`: XML marshalling/unmarshalling
- `csv()`: CSV parsing/generation
- `base64()`: Base64 encoding/decoding
- `gzip()`: GZIP compression
- `protobuf()`: Protocol Buffers

Data formats are **reusable** and **composable**, making complex transformations simple.

#### Splitter EIP (Enterprise Integration Pattern)

The Splitter pattern divides a single message into multiple messages:

```java
.split(body().tokenize(" "))
```

**How it works:**
- Input: `"Hello World"`
- Split into: `["Hello", "World"]`
- Each element becomes a separate Exchange
- Downstream processors handle each message independently
- All messages are sent to Kafka individually

**Use cases:**
- Processing multi-line files line-by-line
- Parsing CSV rows
- Handling batch data
- Splitting large messages for parallel processing

## Understanding the Citrus Test

The test class `QuarkusApplicationTest` demonstrates how **Citrus leverages Camel data format processors** to create realistic test data.

### Test Setup: Shared CamelContext

As with other Camel examples, the CamelContext is shared:

```java
@Inject
@BindToRegistry
CamelContext camelContext;
```

This enables:
- Access to Camel routes for testing
- **Access to Camel data format processors in Citrus**
- Direct endpoint invocation
- Shared infrastructure between application and tests

### Test Setup: Kafka Endpoint

The test verifies output on a Kafka topic using Citrus Kafka endpoint:

```java
@CitrusEndpoint
@KafkaEndpointConfig(topic = "words-out",
        server = "${kafka.bootstrap.servers}")
KafkaEndpoint wordsOut;
```

**What's happening:**
- Citrus connects to the same Kafka broker as the Camel route
- Uses Quarkus Kafka dev services (auto-started Kafka container)
- `${kafka.bootstrap.servers}`: Property provided by Quarkus dev services

### Test Execution: Leveraging Camel Data Formats in Citrus

The key innovation of this test is how **Citrus uses Camel's zipFile data format to create test data**:

```java
@Test
void shouldConsumeFileContent() {
    runner.when(
        camel()
            .send()
            .endpoint(CamelSupport.camel().endpoints().file("inbox")::getRawUri)
            .message()
            .header("CamelFileName", "words.zip")
            .body("Hello World")
            .transform(processor().camel(camelContext)
                    .marshal()
                    .zipFile())
    );

    runner.then(
        receive()
            .endpoint(wordsOut)
            .message()
            .body(">> HELLO")
    );

    runner.then(
        receive()
            .endpoint(wordsOut)
            .message()
            .body(">> WORLD")
    );
}
```

**Test Flow Breakdown:**

#### Step 1: Send ZIP File Using Camel Data Format

```java
camel()
    .send()
    .endpoint(CamelSupport.camel().endpoints().file("inbox")::getRawUri)
    .message()
    .header("CamelFileName", "words.zip")
    .body("Hello World")
    .transform(processor().camel(camelContext)
            .marshal()
            .zipFile())
```

**What's happening here:**

1. **camel().send()**: Citrus Camel DSL for sending to Camel endpoints
2. **endpoint(...)**: Uses Camel's Endpoint DSL to create file endpoint
   - `file("inbox")`: Points to the `inbox` directory
   - `getRawUri()`: Gets the endpoint URI string
3. **header("CamelFileName", "words.zip")**: Sets the file name
4. **body("Hello World")**: The uncompressed content
5. **transform(processor().camel(camelContext).marshal().zipFile())**:
   - **This is the key feature!**
   - `processor().camel(camelContext)`: Access Camel's processing capabilities
   - `.marshal()`: Convert from object to format (opposite of unmarshal)
   - `.zipFile()`: Use Camel's zipFile data format
   - Citrus executes the Camel data format processor to compress the content
   - The resulting ZIP file is written to the `inbox` directory

**The Power of This Approach:**

Instead of manually creating ZIP files in test code:
```java
// WITHOUT Camel data format (manual approach)
ByteArrayOutputStream baos = new ByteArrayOutputStream();
ZipOutputStream zos = new ZipOutputStream(baos);
zos.putNextEntry(new ZipEntry("content.txt"));
zos.write("Hello World".getBytes());
zos.closeEntry();
zos.close();
byte[] zipContent = baos.toByteArray();
```

You simply use Camel's data format through Citrus:
```java
// WITH Camel data format (declarative approach)
.transform(processor().camel(camelContext)
        .marshal()
        .zipFile())
```

**Benefits:**
- **Declarative**: Describe what you want, not how to do it
- **Reusable**: Same data format used in production and tests
- **Maintainable**: Changes to compression logic apply everywhere
- **Powerful**: Access to all Camel data formats (JSON, XML, CSV, Base64, etc.)

#### Step 2-3: Verify Kafka Messages

```java
runner.then(
    receive()
        .endpoint(wordsOut)
        .message()
        .body(">> HELLO")
);

runner.then(
    receive()
        .endpoint(wordsOut)
        .message()
        .body(">> WORLD")
);
```

**What's being verified:**

1. The file was successfully read from the `inbox` directory
2. The ZIP file was correctly decompressed
3. The content was split into tokens ("Hello" and "World")
4. Each token was transformed to uppercase with prefix
5. Two separate Kafka messages were published
6. Message order is preserved (HELLO before WORLD)

### End-to-End Flow

1. **Test creates ZIP file** using Camel's zipFile data format via Citrus
2. **ZIP file is written** to `inbox/words.zip`
3. **Camel route detects** the new file (file polling)
4. **Camel unmarshal** decompresses the ZIP file
5. **Camel converts** content to String
6. **Camel splits** "Hello World" into ["Hello", "World"]
7. **Camel transforms** each token to uppercase with prefix
8. **Camel publishes** ">> HELLO" to Kafka
9. **Camel publishes** ">> WORLD" to Kafka
10. **Citrus verifies** both messages were received on Kafka
11. **File is deleted** from inbox (delete=true)

## Key Testing Concepts

### 1. Citrus Camel Data Format Integration

The ability to use Camel data formats in Citrus tests is a powerful feature:

```java
.transform(processor().camel(camelContext)
        .marshal()
        .zipFile())
```

**Available operations:**

**Marshalling** (object → format):
```java
.transform(processor().camel(camelContext).marshal().zipFile())
.transform(processor().camel(camelContext).marshal().json())
.transform(processor().camel(camelContext).marshal().base64())
```

**Unmarshalling** (format → object):
```java
.transform(processor().camel(camelContext).unmarshal().zipFile())
.transform(processor().camel(camelContext).unmarshal().json())
.transform(processor().camel(camelContext).unmarshal().base64())
```

**Why this matters:**

- **Test-Production Parity**: Tests use the same transformation logic as production
- **Complex Transformations**: Handle ZIP, GZIP, Base64, JSON, XML, CSV, Protobuf without custom code
- **Reduced Boilerplate**: No manual ZIP creation, JSON serialization, etc.
- **Consistency**: Same data format configuration across application and tests

### 2. Camel File Endpoint in Tests

Citrus can send to Camel file endpoints using the Endpoint DSL:

```java
.endpoint(CamelSupport.camel().endpoints().file("inbox")::getRawUri)
```

**What this provides:**

- Type-safe file endpoint creation
- Access to all file component options
- Same endpoint definition as in production routes
- No string-based URI construction

**File endpoint options available:**
- `delete(true)`: Delete after processing
- `move("archive")`: Move to archive directory
- `include("*.zip")`: Filter by pattern
- `recursive(true)`: Process subdirectories
- `idempotent(true)`: Prevent duplicate processing

### 3. Citrus Camel Module

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
- Camel data format processor integration
- Camel Endpoint DSL support in tests
- CamelContext sharing capabilities
- Message transformation using Camel processors

### 4. Camel ZipFile Data Format

The `camel-quarkus-zipfile` dependency provides ZIP compression:

```xml
<dependency>
    <groupId>org.apache.camel.quarkus</groupId>
    <artifactId>camel-quarkus-zipfile</artifactId>
</dependency>
```

**Capabilities:**

- Compress/decompress ZIP archives
- Handle single or multiple files in archive
- Stream processing (memory efficient)
- Integration with Camel file component

**Route usage:**
```java
.unmarshal(dataFormat().zipFile().end())  // Decompress
```

**Test usage:**
```java
.transform(processor().camel(camelContext).marshal().zipFile())  // Compress
```

### 5. Kafka Dev Services Integration

Quarkus automatically provides a Kafka broker for tests:

```properties
quarkus.kafka.devservices.enabled=true
```

**Benefits:**

- Automatic Kafka container startup (via Testcontainers)
- No manual broker configuration
- `${kafka.bootstrap.servers}` property auto-exposed
- Both Camel and Citrus connect to same broker
- Automatic cleanup after tests

### 6. Splitter EIP Testing

The test verifies the Splitter pattern by expecting multiple messages:

```java
// Input: "Hello World" (2 words)

// Output: 2 separate messages
receive().body(">> HELLO")  // First word
receive().body(">> WORLD")  // Second word
```

**Testing considerations:**

- **Message count**: Verify expected number of split messages
- **Message order**: Ensure correct sequence
- **Transformation**: Each split message is transformed independently
- **Parallel processing**: Splitter can enable parallel processing

### 7. Test Annotations

The test combines multiple framework integrations:

```java
@QuarkusTest
@CitrusSupport
class QuarkusApplicationTest extends CamelQuarkusTestSupport implements TestActionSupport {

    @Inject
    @BindToRegistry
    CamelContext camelContext;

    @CitrusEndpoint
    @KafkaEndpointConfig(topic = "words-out",
            server = "${kafka.bootstrap.servers}")
    KafkaEndpoint wordsOut;

    @CitrusResource
    GherkinTestActionRunner runner;
}
```

**Framework integration:**

- **Quarkus**: Application lifecycle and dependency injection
- **Camel**: Route execution and data format processing
- **Citrus**: Test orchestration and verification
- **Kafka**: Message broker for integration testing

## Running the Tests

Execute the tests using Maven:

```bash
./mvnw -f quarkus/apache-camel/camel-file-inbox/pom.xml clean test
```

**What happens during test execution:**

1. Quarkus starts the application in test mode
2. Kafka dev services start a Kafka container
3. Apache Camel routes are discovered and started
4. File consumer begins polling the `inbox` directory
5. CamelContext is injected into the test
6. Citrus registers the CamelContext
7. **Test creates ZIP file** using Camel zipFile data format through Citrus
8. **ZIP file is written** to `inbox/words.zip`
9. **Camel detects and processes** the file (poll interval: 500ms)
10. File is decompressed, split, transformed, and published to Kafka
11. **Citrus verifies** two messages on Kafka topic
12. File is deleted from inbox
13. Kafka container stops

**Expected output:**
```
[INFO] Tests run: 1, Failures: 0, Errors: 0, Skipped: 0
```

**Execution time**: Typically 3-5 seconds (including Kafka container startup)

As the Citrus test class is an arbitrary JUnit Jupiter test you may also run the test directly from your favorite Java IDE (e.g. Eclipse or IntelliJ).

### Key Dependencies (pom.xml)

- **camel-quarkus-core**: Apache Camel core functionality
- **camel-quarkus-file**: Camel file component for file system integration
- **camel-quarkus-kafka**: Camel Kafka component for Kafka integration
- **camel-quarkus-zipfile**: Camel ZIP file data format for compression/decompression
- **camel-quarkus-junit**: Camel test support for Quarkus
- **citrus-quarkus**: Integrates Citrus with Quarkus test framework
- **citrus-camel**: Adds Camel endpoint support and **data format integration** to Citrus
- **citrus-kafka**: Adds Kafka endpoint support to Citrus
- **citrus-junit-jupiter**: JUnit 5 integration for Citrus

## Advanced Use Cases

### Using Other Camel Data Formats in Tests

The same pattern works with any Camel data format:

**JSON Marshalling:**
```java
.transform(processor().camel(camelContext)
        .marshal()
        .json())
```

**Base64 Encoding:**
```java
.transform(processor().camel(camelContext)
        .marshal()
        .base64())
```

**GZIP Compression:**
```java
.transform(processor().camel(camelContext)
        .marshal()
        .gzip())
```

**CSV Generation:**
```java
.transform(processor().camel(camelContext)
        .marshal()
        .csv())
```

### Testing Multiple Files

```java
// Send multiple ZIP files
for (String word : List.of("Alpha", "Beta", "Gamma")) {
    runner.when(
        camel()
            .send()
            .endpoint(...)
            .header("CamelFileName", word.toLowerCase() + ".zip")
            .body(word)
            .transform(processor().camel(camelContext).marshal().zipFile())
    );
}

// Verify all messages on Kafka
for (String word : List.of("ALPHA", "BETA", "GAMMA")) {
    runner.then(
        receive()
            .endpoint(wordsOut)
            .body(">> " + word)
    );
}
```

### Testing Error Scenarios

```java
// Test invalid ZIP file
runner.when(
    camel()
        .send()
        .endpoint(...)
        .header("CamelFileName", "invalid.zip")
        .body("not a zip file".getBytes())  // Invalid content
);

// Verify error handling (depends on route configuration)
```

## Related Resources

- [Citrus Framework Documentation](https://citrusframework.org/docs/)
- [Citrus Quarkus Demo](https://github.com/christophd/citrus-demo-quarkus) - Comprehensive examples of Citrus with Quarkus
- [Citrus Camel Module](https://citrusframework.org/docs/endpoints/camel/) - Camel endpoint reference for Citrus
- [Apache Camel File Component](https://camel.apache.org/components/latest/file-component.html) - File component documentation
- [Apache Camel ZipFile Data Format](https://camel.apache.org/components/latest/dataformats/zipfile-dataformat.html) - ZipFile data format reference
- [Apache Camel Splitter EIP](https://camel.apache.org/components/latest/eips/split-eip.html) - Splitter pattern documentation
- [Apache Camel Endpoint DSL](https://camel.apache.org/manual/Endpoint-dsl.html) - Type-safe endpoint builders
- [Apache Camel Data Formats](https://camel.apache.org/manual/data-format.html) - Overview of all data formats
- [Quarkus Kafka Dev Services](https://quarkus.io/guides/kafka-dev-services) - Automatic Kafka provisioning

---

**Next Steps**: Try using other Camel data formats in your tests (JSON, Base64, CSV). Explore more complex file patterns with includes/excludes. Test error handling scenarios with invalid ZIP files. Experiment with the Aggregator EIP (opposite of Splitter) to combine multiple messages into a single file.

## Sample contract

**Purpose**: This example demonstrates how to test Apache Camel file-based integration routes using Citrus framework with **Camel data format processors** for complex transformations.
**Modules**: `citrus-quarkus`, `citrus-junit-jupiter`, `citrus-camel`, `citrus-kafka`.
**Runtime**: `quarkus`.
**DSL coverage**: Java DSL with Camel and Kafka actions.
**Prerequisites**: JDK 21 or newer and Docker for Kafka Dev Services.
**Run from repository root**:

```shell
./mvnw -f quarkus/apache-camel/camel-file-inbox/pom.xml clean verify -Dsystem.under.test.mode=embedded
```

**Copy out**: Maven 3.9+ is required for the copied sample.

```shell
cp -R quarkus/apache-camel/camel-file-inbox /tmp/citrus-camel-file-inbox
cd /tmp/citrus-camel-file-inbox
mvn clean verify -Dsystem.under.test.mode=embedded
```

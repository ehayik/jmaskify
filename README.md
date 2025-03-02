# JMaskify

**JMaskify** is an open-source Java library designed to safeguard sensitive data with versatile and customizable
masking techniques. Whether your application handles personal, financial, or confidential information,
_JMaskify_ ensures its security through intuitive APIs and advanced masking strategies.

## Key Features

- **Versatile Masking**: Supports fixed-length anonymization, Base64 encoding, JSON, and multiline text masking.
- **High Performance**: Lightweight and optimized for low memory and processing overhead.
- **Flexible API**: Easily adaptable for use cases involving [JSON structures](#json-masking) and [ multiline text](#multiline-text-masking).
- **Open Source**: MIT-licensed to encourage collaboration and transparency.

## Planned Features

Future versions of _JMaskify_ aim to extend its capabilities by:

- Supporting XML and YAML formats for masking operations.
- Introducing new masking strategies such as credit card numbers and IBANs (International Bank Account Numbers).

## Project Requirements (Review this)

To work with JMaskify, ensure the following tools and dependencies are installed:

- **JDK**: Version 17 or higher
- **Build Tool**: Maven (recommended)

## Dependencies

JMaskify leverages the following key libraries:

- **SLF4J Facade**: Used for logging purposes.
- **Jackson**: JSON processing (`jackson-databind`)
- **Apache Commons Codec**: Encoding utilities
- **SLF4J**: Logging framework

All dependencies and versions are managed in the project's `pom.xml`

## How to install

Includes [jackson-core](https://github.com/FasterXML/jackson-core) and 
[jackson-databind](https://github.com/FasterXML/jackson-databind) for JSON processing.

Add the dependency to `jmaskify`:

    ```xml
    <dependency>
        <groupId>io.github.ehayik</groupId>
        <artifactId>jmaskify</artifactId>
        <version>1.0.0</version>
    </dependency>
    ```

## How to use

### JSON Masking

_JMaskify_ supports JSON field masking through the `Masker.json()` API.
Users can specify individual fields for masking and define strategies, such as fixed-length masking, delegate masking, or Base64 encoding.

Below is an example of masking specific fields in a JSON object, such as `email` and `phone`.
In this example the `name` is masked using a custom strategy, the `email` field is masked with a fixed pattern,
while the `phone` field is masked using Base64 encoding.
Users can add more fields and specify their preferred masking strategies:

```java
String json = """
{
  "name": "John Doe",
  "age": 30,
  "email": "john.doe@example.com",
  "phone": "123-456-7890"
}
""";

// Create a JsonMasker instance
var masker = Masker.json()
    .prettify(true)
    .withProperty("email") //Mask the email field with a fixed pattern
    .withProperty("phone", Masker.base64())
    .withProperty("name", Masker.delegate(value -> "MASKED"))    
    .build();

// Apply masking
var maskedJson = masker.apply(json);

// Output
System.out.println(maskedJson);
```

**Result:**

```json
{
  "name": "MASKED",
  "age": 30,
  "email": "***************",
  "phone": "MTIzLTQ1Ni03ODkw"
}
```

#### Multiline Text Masking

Using `Masker.multilinePattern()`, _JMaskify_ allows masking sensitive patterns in multiline text.
Users define regular expressions to redact sensitive data, such as usernames or IP addresses, in unstructured input:
        
```java
// Create a MultilinePatternMasker instance
var masker = Masker.multilinePattern()
    .withMaskPattern("(\\d+\\.\\d+\\.\\d+\\.\\d+)")
    .withMaskPattern("User\\s*:\\s*([^\\s]+)")
    .build();

var input = """
User: johndoe
IP: 192.168.1.1
""";

// Apply masking
var maskedOutput = masker.apply(input);

// Output
System.out.println(maskedOutput);
```

**Result:**

```plaintext
User: *******
IP: **********
```

## Logging

JMaskify uses the [SLF4J](https://slf4j.org) logging facade, allowing integration with popular logging frameworks
such as [Logback](https://logback.qos.ch), [Log4j](https://logging.apache.org/log4j/2.x/index.html),
[tinylog](https://tinylog.org/v2/) , or [Java Utils Logging](https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html).
Refer to your logging framework's documentation to set the `DEBUG` log level for the package `io.github.ehayik.jmaskify`.

### Logback Configuration Example

Add the following configuration to your `logback.xml` file to enable debug logs:

```xml
<configuration>
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <logger name="io.github.ehayik.jmaskify" level="DEBUG" />
</configuration>
```

#### Handling Edge Cases

JMaskify provides robust handling for invalid inputs.
For instance, attempting to mask invalid JSON results in exceptions with clear error messages:

```java
assertThatThrownBy(() -> masker.apply("///"))
    .isInstanceOf(MaskingException.class)
    .hasMessage("Failed to mask JSON content");
```

## Contributing

If you found a bug or a missing feature - you're very welcome to submit an issue and a pull request with a fix.

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).

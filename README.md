# JMaskify

![](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=java&logoColor=white&style=flat)
[![Quality Gate Status](https://sonarcloud.io/api/project_badges/measure?project=ehayik_jmaskify&metric=alert_status)](https://sonarcloud.io/summary/new_code?id=ehayik_jmaskify)
[![Coverage](https://sonarcloud.io/api/project_badges/measure?project=ehayik_jmaskify&metric=coverage)](https://sonarcloud.io/summary/new_code?id=ehayik_jmaskify)
[![License: GPL v3](https://img.shields.io/badge/License-MIT-blue.svg)](https://opensource.org/licenses/MIT)

**JMaskify** is an open-source Java library designed to safeguard sensitive data with versatile and customizable
masking techniques. Whether your application handles personal, financial, or confidential information,
_JMaskify_ ensures its security through intuitive APIs and advanced masking strategies.

## Table of Contents

- [Key Features](#key-features)
- [Support the Project](#support-the-project)
- [Project Requirements](#project-requirements)
- [Dependencies](#dependencies)
- [Getting Started](#getting-started)
    - [Installation](#installation)
    - [Basic Usage](#basic-usage)
        - [Simple String Masking](#1-simple-string-masking)
        - [JSON Masking](#2-json-masking)
        - [Multiline Text Masking](#3-multiline-text-masking)
- [Advanced Usage](#advanced-usage)
  - [Custom Masking Strategies](#custom-masking-strategies) 
  - [Combining Masking Strategies](#combining-masking-strategies)
  - [Logging](#logging)
  - [Handling Edge Cases](#handling-edge-cases)

## Key Features

- **Versatile Masking**: Supports fixed-length anonymization, Base64 encoding, Debit/Credit Card Numbers, JSON, and multiline text masking.
- **Flexible API**: Easily adaptable for use cases involving [JSON structures](#2-json-masking) and [ multiline text](#3-multiline-text-masking).
- **Open Source**: MIT-licensed to encourage collaboration and transparency.

## Support the Project

If you find JMaskify useful for your projects, consider supporting its development:

<a href="https://www.buymeacoffee.com/eduardoeljaiek" target="_blank"><img src="https://cdn.buymeacoffee.com/buttons/v2/default-blue.png" alt="Buy Me A Coffee" style="height: 35px !important;width: 125px !important;" ></a>

Your support helps ensure that JMaskify continues to improve and remain available as an open-source tool.

## Project Requirements

To work with JMaskify, ensure the following tools and dependencies are installed:

- **JDK**: Version 17 or higher
- **Build Tool**: Maven or Gradle

## Dependencies

JMaskify leverages the following key libraries:

- **Jackson**: JSON processing (`jackson-databind`)
- **Apache Commons Codec**: Encoding utilities
- **SLF4J**: Logging framework

## Getting Started

### Installation

JMaskify is available on Maven Central. Add the dependency to your project:

```xml
<dependency>
    <groupId>io.github.ehayik</groupId>
    <artifactId>jmaskify</artifactId>
    <version>1.0.0</version>
</dependency>
```

This includes [jackson-core](https://github.com/FasterXML/jackson-core) and [jackson-databind](https://github.com/FasterXML/jackson-databind) for JSON processing.
All dependencies and versions are managed in the project's `pom.xml`

### Basic Usage

#### 1. Simple String Masking

The quickest way to mask sensitive data:

```java
// Mask a credit card number
var creditCardNumber = "4289-3874-8064-8976";
var masked = Masker.creditCard().apply(creditCardNumber);
// Result: "****-****-****-8976"

// Mask an email address
var email = "john.doe@example.com";
var maskedEmail = Masker.fixedLength().apply(email);
// Result: "****************"
```

#### 2. JSON Masking

For masking fields within JSON objects:

```java
String json = """
{
  "name": "John Doe",
  "age": 30,
  "contactInfo": {
    "email": "john.doe@example.com",
    "phone": "123-456-7890"
  },
  "creditCard": "1234-5678-9012-3456"
}
""";

// Create a JsonMasker instance
var masker = Masker.json()
    .prettify(true)
    .withProperty("email") // Default fixed pattern masking
    .withProperty("phone", Masker.base64())
    .withProperty("creditCard", Masker.creditCard('X'))    
    .withProperty("name", Masker.delegate(value -> "■■■■■■"));

// Apply masking
var maskedJson = masker.apply(json);
/*
 Result: {
            "name": "■■■■■■",
            "age": 30,
            "contactInfo": {
              "email": "***************",
              "phone": "MTIzLTQ1Ni03ODkw"
            },
            "creditCard": "XXXX-XXXX-XXXX-3456"
          }
 */
```

> **NOTE**:
> Non-string values (numbers, booleans, nulls) are preserved without masking.

##### Masking string values within a JSON array

JMaskify provides powerful capabilities for masking string values within JSON arrays.
Here are examples of different array masking scenarios:

###### Example 1: Simple Array of Strings

When you need to mask an array of sensitive string values, such as credit card numbers:

```java
String json = """
{
    "name": "John Doe",
    "creditCards": [
        "1234-5678-9012-3456",
        "4289-3874-8064-8976"
    ]
}
""";

var masker = JsonMasker.builder()
    .withProperty("creditCards", Masker.creditCard('X'))
    .build();

var maskedJson = masker.apply(json);

/* Result:
{
    "name": "John Doe",
    "creditCards": [
        "XXXX-XXXX-XXXX-3456",
        "XXXX-XXXX-XXXX-8976"
    ]
}
*/
```

###### Example 2: Arrays with Mixed Value Types

JMaskify can handle arrays containing a mix of different value types, masking only the string values:

```java
String json = """
{
    "name": "John Doe",
    "mixedArray": [
        "sensitive-string-data",
        42,
        true,
        null,
        {"nestedKey": "nestedValue"},
        ["nested", "array"]
    ]
}
""";

var masker = JsonMasker.builder()
    .withProperty("mixedArray", Masker.fixedLength())
    .build();

var maskedJson = masker.apply(json);

/* Result:
{
    "name": "John Doe",
    "mixedArray": [
        "*********************",
        42,
        true,
        null,
        {"nestedKey": "***********"},
        ["******", "*****"]
    ]
}
*/
```

###### Example 3: Nested Arrays

JMaskify handles nested arrays with specific masking behavior:

```java
String json = """
{
    "name": "John Doe",
    "nestedArrays": [
        ["sensitive-outer-inner", "another-value"],
        42,
        ["not-masked-1", "not-masked-2"]
    ]
}
""";

var masker = JsonMasker.builder()
    .withProperty("nestedArrays", Masker.fixedLength())
    .build();

var maskedJson = masker.apply(json);

/* Result:
{
    "name": "John Doe",
    "nestedArrays": [
        ["*********************", "*************"],
        42,
        ["************", "************"]
    ]
}
*/
```

> **NOTE**: This example demonstrates how JMaskify recursively processes nested arrays. 
> All string values within nested arrays are masked consistently, regardless of their position or nesting level.

#### 3. Multiline Text Masking

When working with log files or other text that spans multiple lines, you can use multiline text masking to identify and mask patterns:

```java
String logContent = """
          2023-05-15 INFO User john.doe@example.com logged in
          2023-05-15 INFO IP Address: 192.168.1.1
        """;

// Create a MultilinePatternMasker instance
var masker = Masker.multilinePattern()
    .withMaskPattern("(\\d+\\.\\d+\\.\\d+\\.\\d+)");

// Apply masking
var maskedContent = masker.apply(logContent);

// Result:
// 2023-05-15 INFO User john.doe@example.com logged in
// 2023-05-15 INFO IP Address: **********
```

## Advanced Usage

Once you're comfortable with the basic masking operations,
JMaskify offers more sophisticated features to handle complex masking requirements.

### Custom Masking Strategies

Create your own masking strategies for specialized requirements:

```java
var masker = Masker.delegate((String input) -> {
    if (input == null || input.length() <= 2) {
        return input;
    }

    var first = input.charAt(0);
    var last = input.charAt(input.length() - 1);

    return first +
           "*".repeat(input.length() - 2) +
           last;
});

var maskedName = masker.apply("Johnson");
// Result: "J*****n"
```

### Combining Masking Strategies

For complex scenarios, combine different masking approaches:

```java
var json = """
        {
          "name": "John Doe",
          "logs": [
            "2023-05-15 INFO User john.doe@example.com logged in",
            "2023-05-15 INFO IP Address: 192.168.1.1"
          ]
        }
""";

var jsonMasker = Masker.json()
        .withProperty("name", Masker.delegate(value -> "■■■■■■"))
        .build();

var multilineMasker = Masker.multilinePattern()
        .withMaskPattern("(\\d+\\.\\d+\\.\\d+\\.\\d+)")
        .build();

var maskedContent = jsonMasker
        .andThen(multilineMasker)
        .andThen(Masker.base64())
        .apply(json);

// Result applies all masking strategies in sequence
```

### Logging

JMaskify uses the [SLF4J](https://slf4j.org) logging facade, allowing integration with popular logging frameworks
such as [Logback](https://logback.qos.ch), [Log4j](https://logging.apache.org/log4j/2.x/index.html),
[tinylog](https://tinylog.org/v2/) , or [Java Utils Logging](https://docs.oracle.com/javase/8/docs/api/java/util/logging/package-summary.html).
Refer to your logging framework's documentation to set the `DEBUG` log level for the package `io.github.ehayik.jmaskify`.

#### Logback Configuration Example

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

### Handling Edge Cases

JMaskify provides robust handling for invalid inputs.
For instance, attempting to mask invalid JSON results in exceptions with clear error messages:

```java
// Create a masker for email fields
var masker = Masker.json().withProperty("email").build();

// When applying to invalid JSON, a MaskingException will be thrown
// with the message "Failed to mask JSON content"
// masker.apply("///");  // This would throw MaskingException
```

## Contributing

If you found a bug or a missing feature—you're very welcome to submit an issue and a pull request with a fix.

## License

This project is licensed under the [MIT License](https://opensource.org/licenses/MIT).

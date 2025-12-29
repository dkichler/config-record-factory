# config-record-factory

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.dkichler/config-record-factory/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.dkichler/config-record-factory)
[![Build Status](https://github.com/lightbend/config/actions/workflows/ci.yml/badge.svg)](https://github.com/dkichler/config-record-factory/actions/workflows/ci.yml)
<a href="">![Coverage](https://gist.githubusercontent.com/dkichler/f4d7b3bb6eda2e6298814b449dec0bbb/raw/config-record-factory-coverage.svg)</a>

An extension to [Lightbend Config](https://github.com/lightbend/config) for support creating Java Records from `Config` objects.

## Usage

Add the dependency to a Maven (or favourite alternative) build. Latest version on [Maven Central](https://search.maven.org/artifact/io.github.dkichler/config-record-factory).

```xml
<dependency>
    <groupId>io.github.dkichler</groupId>
    <artifactId>config-record-factory</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Creating Records

Define your configuration as a Java `record`:

```java
// In MyConfig.java
public record MyConfig(String foo, int bar, boolean baz) {}
```

Load your configuration file and use `ConfigRecordFactory` to create an instance of your record:

```java
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.github.dkichler.config.ConfigRecordFactory;

// Assumes application.conf is on the classpath
Config config = ConfigFactory.load().getConfig("my-app");

// Create an instance of MyConfig
MyConfig myConfig = ConfigRecordFactory.create(config, MyConfig.class);

System.out.println("foo: " + myConfig.foo());
System.out.println("bar: " + myConfig.bar());
```

Your configuration file (`application.conf`) would look like this:

```hocon
my-app {
  foo = "hello world"
  bar = 42
  baz = true
}
```

### Supported Types

Below is a comprehensive example showing all supported field types.

```java
public record ComprehensiveConfig(
    // Primitives
    int intVal,
    long longVal,
    double doubleVal,
    boolean boolVal,
    String stringVal,

    // Boxed Primitives
    Integer integerObj,
    Long longObj,
    Double doubleObj,
    Boolean booleanObj,

    // Enums
    MyEnum enumVal,

    // Time and Size
    Duration durationVal,
    ConfigMemorySize memorySizeVal,

    // Config specific types
    Config configVal,
    ConfigObject configObjVal,
    ConfigValue configValueVal,
    ConfigList configListVal,

    // Collections
    List<String> stringList,
    Set<Integer> intSet,
    Map<String, String> stringMap,
    Map<String, Integer> intMap,
    Map<String, Double> doubleMap,
    Map<String, Long> longMap,
    Map<String, Boolean> booleanMap,
    Map<String, Duration> durationMap,
    Map<String, ConfigMemorySize> memoryMap,
    Map<String, ConfigObject> objectMap,
    Map<String, Config> configMap,
    Map<String, ConfigValue> configValueMap,
    Map<String, List<Object>> listMap,
    Map<String, ConfigList> configListMap,
    Map<String, MyEnum> enumMap,

    // Optionals
    Optional<String> optionalString,
    Optional<Integer> optionalInt,
    Optional<Double> optionalDouble,
    Optional<Long> optionalLong,
    Optional<Boolean> optionalBoolean,
    Optional<Duration> optionalDuration,
    Optional<ConfigMemorySize> optionalMemorySize,
    Optional<Config> optionalConfig,
    Optional<ConfigObject> optionalConfigObject,
    Optional<ConfigValue> optionalConfigValue,
    Optional<ConfigList> optionalConfigList,
    Optional<List<String>> optionalStringList,
    Optional<Set<Integer>> optionalIntSet,
    Optional<Map<String, Double>> optionalDoubleMap,
    Optional<MyEnum> optionalEnum,

    // Nested Records
    NestedRecord nestedRecord
) {
    public enum MyEnum { V1, V2 }
    public record NestedRecord(String nestedField) {}
}
```

For more detailed examples, please refer to the test cases and related record classes in the `src/test` directory.

## Rationale

Why here as an extension and not part of the core library?  Mostly because Lightbend Config still supports Java 8 as a lower bound, and Java records did not become a core feature until Java 16.  This extension supports 16 as a lower bound and was inspired by [this issue](https://github.com/lightbend/config/issues/769) on the core library, and by the fact that I have already been using most of the implementation code in Akka SDK applications.  
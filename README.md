# config-record-factory

An extension for [Lightbend Config](https://github.com/lightbend/config) to support creating Java Records from `Config` objects.

## Usage

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

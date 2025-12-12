package recordconfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.time.Duration;
import java.util.Set;

public record SetsConfig(
    Set<Integer> empty,
    Set<Integer> ofInt,
    Set<String> ofString,
    Set<Double> ofDouble,
    Set<Long> ofLong,
    Set<Object> ofNull,
    Set<Boolean> ofBoolean,
    Set<Object> ofObject,
    Set<Config> ofConfig,
    Set<ConfigObject> ofConfigObject,
    Set<ConfigValue> ofConfigValue,
    Set<Duration> ofDuration,
    Set<ConfigMemorySize> ofMemorySize,
    Set<StringsConfig> ofStringRecord
) {
}

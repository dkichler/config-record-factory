package recordconfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.util.Optional;
import java.time.Duration;

public record OptionalsConfig(
    Optional<Integer> empty,
    Optional<Integer> ofInt,
    Optional<String> ofString,
    Optional<Double> ofDouble,
    Optional<Long> ofLong,
    Optional<Object> ofNull,
    Optional<Boolean> ofBoolean,
    Optional<Object> ofObject,
    Optional<Config> ofConfig,
    Optional<ConfigObject> ofConfigObject,
    Optional<ConfigValue> ofConfigValue,
    Optional<Duration> ofDuration,
    Optional<ConfigMemorySize> ofMemorySize,
    Optional<StringsConfig> ofStringRecord
) {
}

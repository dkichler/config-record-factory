package recordconfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.util.List;
import java.time.Duration;

public record ArraysConfig(
    List<Integer> empty,
    List<Integer> ofInt,
    List<String> ofString,
    List<Double> ofDouble,
    List<Long> ofLong,
    List<Object> ofNull,
    List<Boolean> ofBoolean,
    List<Object> ofObject,
    List<Config> ofConfig,
    List<ConfigObject> ofConfigObject,
    List<ConfigValue> ofConfigValue,
    List<Duration> ofDuration,
    List<ConfigMemorySize> ofMemorySize,
    List<StringsConfig> ofStringRecord) {
}

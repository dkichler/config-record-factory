package recordconfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import recordconfig.EnumsConfig.Solution;

import java.time.Duration;
import java.util.List;
import java.util.Map;

public record MapsConfig(
    Map<String, String> stringMap,
    Map<String, Integer> intMap,
    Map<String, Double> doubleMap,
    Map<String, Long> longMap,
    Map<String, Boolean> booleanMap,
    Map<String, Duration> durationMap,
    Map<String, com.typesafe.config.ConfigMemorySize> memoryMap,
    Map<String, ConfigObject> objectMap,
    Map<String, Config> configMap,
    Map<String, ConfigValue> configValueMap,
    Map<String, List<Object>> listMap,
    Map<String, SimpleBean> beanMap,
    Map<String, ConfigList> configListMap,
    Map<String, Solution> enumMap
) {
}

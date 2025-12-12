package recordconfig;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;

import java.util.Map;

// test record for various "uncooked" values
public record ValuesConfig(
    Object obj,
    Config config,
    ConfigObject configObj,
    ConfigValue configValue,
    ConfigList list,
    Map<String,Object> unwrappedMap
) {
}

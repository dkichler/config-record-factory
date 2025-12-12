package recordconfig;

import java.util.Map;
import java.util.Optional;

public record MapConfig(
        Map<String, String> mapOfString,
        Map<String, Integer> mapOfInt,
        Map<String, StringsConfig> mapOfRecord
) {
}

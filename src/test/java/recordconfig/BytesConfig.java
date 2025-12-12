package recordconfig;

import com.typesafe.config.ConfigMemorySize;

public record BytesConfig(
    ConfigMemorySize kilobyte,
    ConfigMemorySize kibibyte,
    ConfigMemorySize thousandBytes
) {
}

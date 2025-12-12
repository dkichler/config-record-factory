package recordconfig;

import java.time.Duration;

public record DurationsConfig(
    Duration second,
    Duration secondAsNumber,
    Duration halfSecond
) {
}

package recordconfig;

import java.util.List;

public record ValidationRecordConfig(
    NumbersConfig numbers,
    String propNotListedInConfig,
    int shouldBeInt,
    boolean shouldBeBoolean,
    List<Integer> shouldBeList
) {
}

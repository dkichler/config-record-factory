package recordconfig;


import com.typesafe.config.Optional;

public record ObjectsConfig(ValueObject valueObject) {
    public record ValueObject(
            @Optional String optionalValue,
            String mandatoryValue,
            java.util.Optional<String> Default
    ) {
    }
}

package recordconfig;

import java.net.URI;
import java.util.Map;

public record UnsupportedMapValueConfig(Map<String, URI> unsupportedMapValue) {
}

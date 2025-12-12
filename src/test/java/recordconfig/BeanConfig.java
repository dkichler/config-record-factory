package recordconfig;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public record BeanConfig(
    SimpleBean bean,
    Optional<SimpleBean> optionalBean,
    List<SimpleBean> beanList,
    Set<SimpleBean> beanSet,
    Map<String, SimpleBean> beanMap
) {
}

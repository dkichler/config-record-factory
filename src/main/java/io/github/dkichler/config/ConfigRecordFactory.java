package io.github.dkichler.config;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.impl.ConfigRecordImpl;

/**
 * Factory for automatically creating a Java record from a {@link Config}.
 * See {@link #create(Config,Class)}.
 *
 * @since 0.1.0
 */
public class ConfigRecordFactory {

    /**
     * Creates an instance of a record, initializing its fields from a {@link Config}.
     *
     * Example usage:
     *
     * <pre>
     * Config configSource = ConfigFactory.load().getConfig("foo");
     * FooConfig config = ConfigBeanFactory.create(configSource, FooConfig.class);
     * </pre>
     *
     * Field types can be any of the types you can normally get from a {@link Config},
     * including <code>java.time.Duration</code> or {@link com.typesafe.config.ConfigMemorySize}.
     * class.
     *
     * Fields are mapped to config by converting the config key to
     * camel case.  So the key <code>foo-bar</code> becomes record
     * field <code>fooBar</code>.  Camel case keys are also supported, and in fact
     * given preference where both might exist.
     *
     * @since 0.1.0
     *
     * @param config source of config information
     * @param clazz record class to be instantiated
     * @param <T> the type of the record to be instantiated
     * @return an instance of the record populated with data from the config
     * @throws ConfigRecordException.BadRecord
     *     If something goes wrong attempting to construct the record
     * @throws ConfigException.ValidationFailed
     *     If the config doesn't conform to the bean's implied schema
     * @throws ConfigException
     *     Can throw the same exceptions as the getters on <code>Config</code>
     */
    public static <T extends Record> T create(Config config, Class<T> clazz) {
        return ConfigRecordImpl.createInternal(config, clazz);
    }
}

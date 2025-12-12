package com.typesafe.config.impl;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigFactory;
import com.typesafe.config.ConfigList;
import com.typesafe.config.ConfigMemorySize;
import com.typesafe.config.ConfigObject;
import com.typesafe.config.ConfigValue;
import com.typesafe.config.ConfigValueType;
import io.github.dkichler.config.ConfigRecordException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Parameter;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ConfigRecordImpl {

    public static <T extends Record> T createInternal(Config config, Class<T> recordClass) {
        if (((SimpleConfig) config).root().resolveStatus() != ResolveStatus.RESOLVED)
            throw new ConfigException.NotResolved(
                    "need to Config#resolve() a config before using it to initialize a record, see the API docs for Config#resolve()");

        // catch as many validations up front as possible
        List<ConfigException.ValidationProblem> problems = new ArrayList<>();
        RecordComponent[] recordComponents = recordClass.getRecordComponents();

        Map<String,String> fieldNameToKey = new HashMap<>();
        for (RecordComponent component : recordComponents) {
            String name = component.getName();

            var key = resolveKey(config, name);
            fieldNameToKey.put(name, key);
            ConfigValueType expectedType = getValueTypeOrNull(component.getType());
            if (key == null && !isOptionalProperty(component)) {
                var type = expectedType != null ? expectedType.name().toLowerCase() : component.getType().toString();
                problems.add(new ConfigException.ValidationProblem(name, config.origin(), "No setting at '" + name + "', expecting " + type));
            }
            if (expectedType != null && key != null) {
                Path path = Path.newKey(key);
                AbstractConfigValue configValue = (AbstractConfigValue) config.getValue(key);
                if (configValue != null) {
                    SimpleConfig.checkValid(path, expectedType, configValue, problems);
                }
            }
        }

        if (!problems.isEmpty()) {
            throw new ConfigException.ValidationFailed(problems);
        }

        try {
            Constructor<T> constructor = getCanonicalConstructor(recordClass);
            Parameter[] parameters = constructor.getParameters();
            Object[] args = new Object[parameters.length];

            for (int i = 0; i < parameters.length; i++) {
                Parameter param = parameters[i];
                var key = fieldNameToKey.get(param.getName());
                // null indicates key was not found but is optional, set as null
                args[i] = key == null ? null : getConfigValue(config, param);
            }
            return constructor.newInstance(args);
        } catch (ConfigException e) {
            throw e;
        } catch (Exception e) {
            throw new ConfigRecordException.BadRecord("Failed to map config to record " + recordClass.getName(), e);
        }
    }

    // null if we can't easily say; this is heuristic/best-effort
    private static ConfigValueType getValueTypeOrNull(Class<?> parameterClass) {
        if (parameterClass == Boolean.class || parameterClass == boolean.class) {
            return ConfigValueType.BOOLEAN;
        } else if (parameterClass == Integer.class || parameterClass == int.class) {
            return ConfigValueType.NUMBER;
        } else if (parameterClass == Double.class || parameterClass == double.class) {
            return ConfigValueType.NUMBER;
        } else if (parameterClass == Long.class || parameterClass == long.class) {
            return ConfigValueType.NUMBER;
        } else if (parameterClass == String.class) {
            return ConfigValueType.STRING;
        } else if (parameterClass == Duration.class) {
            return null;
        } else if (parameterClass == ConfigMemorySize.class) {
            return null;
        } else if (parameterClass == List.class) {
            return ConfigValueType.LIST;
        } else if (parameterClass == Map.class) {
            return ConfigValueType.OBJECT;
        } else if (parameterClass == Config.class) {
            return ConfigValueType.OBJECT;
        } else if (parameterClass == ConfigObject.class) {
            return ConfigValueType.OBJECT;
        } else if (parameterClass == ConfigList.class) {
            return ConfigValueType.LIST;
        } else {
            return null;
        }
    }

    // key should be record field name camel case
    private static String resolveKey(Config config, String key) {
        return config.hasPath(key) ? key : config.hasPath(toKebabCase(key)) ? toKebabCase(key) : null;
    }

    private static boolean isOptionalProperty(RecordComponent field) {
        return field.getType() == Optional.class || field.getAnnotationsByType(com.typesafe.config.Optional.class).length > 0;
    }


    @SuppressWarnings("unchecked")
    private static <T extends Record> Constructor<T> getCanonicalConstructor(Class<T> recordClass) {
        Constructor<?>[] constructors = recordClass.getDeclaredConstructors();
        return (Constructor<T>) constructors[0];
    }

    private static Object getConfigValue(Config config, Parameter param) {
        Class<?> type = param.getType();
        String key = param.getName();
        if (!config.hasPath(key)) {
            key = toKebabCase(key);
        }

        if (type == String.class) {
            return config.getString(key);
        } else if (type == int.class || type == Integer.class) {
            return config.getInt(key);
        } else if (type == long.class || type == Long.class) {
            return config.getLong(key);
        } else if (type == double.class || type == Double.class) {
            return config.getDouble(key);
        } else if (type == boolean.class || type == Boolean.class) {
            return config.getBoolean(key);
        } else if (type == Duration.class) {
            return config.getDuration(key);
        } else if (type == ConfigMemorySize.class) {
            return config.getMemorySize(key);
        } else if (type == Optional.class) {
            return getOptionalValue(config, key, param);
        } else if (type == List.class) {
            return getListValue(config, key, (ParameterizedType) param.getParameterizedType());
        } else if (type == Set.class) {
            return getSetValue(config, key, param);
        } else if (type == Map.class) {
            return getMapValue(config, key, param);
        } else if (type == Object.class) {
            return config.getAnyRef(key);
        } else if (type == Config.class) {
            return config.getConfig(key);
        } else if (type == ConfigObject.class) {
            return config.getObject(key);
        } else if (type == ConfigValue.class) {
            return config.getValue(key);
        } else if (type == ConfigList.class) {
            return config.getList(key);
        } else if (type.isEnum()) {
            @SuppressWarnings("unchecked")
            Enum enumValue = config.getEnum((Class<Enum>) type, key);
            return enumValue;
        } else if (hasAtLeastOneBeanProperty(type)) {
            return ConfigBeanImpl.createInternal(config.getConfig(key), type);
        } else if (Record.class.isAssignableFrom(type)) {
            Config nestedConfig = config.getConfig(key);
            return createInternal(nestedConfig, (Class<? extends Record>) type);
        } else {
            throw new ConfigRecordException.BadRecord("Unsupported type for field " + key + ": " + type);
        }

    }

    private static boolean hasAtLeastOneBeanProperty(Class<?> clazz) {
        BeanInfo beanInfo = null;
        try {
            beanInfo = Introspector.getBeanInfo(clazz);
        } catch (IntrospectionException e) {
            return false;
        }

        for (PropertyDescriptor beanProp : beanInfo.getPropertyDescriptors()) {
            if (beanProp.getReadMethod() != null && beanProp.getWriteMethod() != null) {
                return true;
            }
        }

        return false;
    }

    private static Optional<?> getOptionalValue(Config config, String key, Parameter param) {
        Type genericType = param.getParameterizedType();
        if (config.hasPath(key)) {
            if (genericType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) genericType;
                Type[] typeArgs = paramType.getActualTypeArguments();
                if (typeArgs.length > 0) {
                    Class<?> elementType = (Class<?>) typeArgs[0];

                    if (elementType == String.class) {
                        return Optional.of(config.getString(key));
                    } else if (elementType == Integer.class) {
                        return Optional.of(config.getInt(key));
                    } else if (elementType == Long.class) {
                        return Optional.of(config.getLong(key));
                    } else if (elementType == Double.class) {
                        return Optional.of(config.getDouble(key));
                    } else if (elementType == Boolean.class) {
                        return Optional.of(config.getBoolean(key));
                    } else if (elementType == Duration.class) {
                        return Optional.of(config.getDuration(key));
                    } else if (elementType == ConfigMemorySize.class) {
                        return Optional.of(config.getMemorySize(key));
                    } else if (elementType == List.class) {
                        return Optional.of(getListValue(config, key, (ParameterizedType) param.getParameterizedType()));
                    } else if (elementType == Set.class) {
                        return Optional.of(getSetValue(config, key, param));
                    } else if (elementType == Map.class) {
                        return Optional.of(getMapValue(config, key, param));
                    } else if (elementType == Object.class) {
                        return Optional.of(config.getAnyRef(key));
                    } else if (elementType == Config.class) {
                        return Optional.of(config.getConfig(key));
                    } else if (elementType == ConfigObject.class) {
                        return Optional.of(config.getObject(key));
                    } else if (elementType == ConfigValue.class) {
                        return Optional.of(config.getValue(key));
                    } else if (elementType == ConfigList.class) {
                        return Optional.of(config.getList(key));
                    } else if (elementType.isEnum()) {
                        @SuppressWarnings("unchecked")
                        Enum enumValue = config.getEnum((Class<Enum>) elementType, key);
                        return Optional.of(enumValue);
                    } else if (hasAtLeastOneBeanProperty(elementType)) {
                        return Optional.of(ConfigBeanImpl.createInternal(config.getConfig(key), elementType));
                    }else if (Record.class.isAssignableFrom(elementType)) {
                        return Optional.of(createInternal(config.getConfig(key), (Class<? extends Record>) elementType));
                    }
                }
            }
            throw new ConfigRecordException.BadRecord("Unsupported optional type for field " + key + ": " + genericType);
        } else {
            return Optional.empty();
        }
    }

    private static List<?> getListValue(Config config, String key, ParameterizedType type) {
        if (type instanceof ParameterizedType) {
            ParameterizedType paramType = (ParameterizedType) type;
            Type[] typeArgs = paramType.getActualTypeArguments();
            if (typeArgs.length > 0) {
                Class<?> elementType = (Class<?>) typeArgs[0];

                if (elementType == String.class) {
                    return config.getStringList(key);
                } else if (elementType == Integer.class) {
                    return config.getIntList(key);
                } else if (elementType == Long.class) {
                    return config.getLongList(key);
                } else if (elementType == Double.class) {
                    return config.getDoubleList(key);
                } else if (elementType == Boolean.class) {
                    return config.getBooleanList(key);
                } else if (elementType == Duration.class) {
                    return config.getDurationList(key);
                } else if (elementType == ConfigMemorySize.class) {
                    return config.getMemorySizeList(key);
                } else if (elementType == Object.class) {
                    return config.getAnyRefList(key);
                } else if (elementType == Config.class) {
                    return config.getConfigList(key);
                } else if (elementType == ConfigObject.class) {
                    return config.getObjectList(key);
                } else if (elementType == ConfigValue.class) {
                    return config.getList(key);
                } else if (((Class<?>) elementType).isEnum()) {
                    @SuppressWarnings("unchecked")
                    List<Enum> enumValues = config.getEnumList((Class<Enum>) elementType, key);
                    return enumValues;
                } else if (hasAtLeastOneBeanProperty((Class<?>) elementType)) {
                    List<Object> beanList = new ArrayList<Object>();
                    List<? extends Config> configList = config.getConfigList(key);
                    for (Config listMember : configList) {
                        beanList.add(ConfigBeanImpl.createInternal(listMember, (Class<?>) elementType));
                    }
                    return beanList;
                }else if (Record.class.isAssignableFrom(elementType)) {
                    List<? extends Config> configList = config.getConfigList(key);
                    List<Object> result = new ArrayList<>();
                    for (Config itemConfig : configList) {
                        result.add(createInternal(itemConfig, (Class<? extends Record>) elementType));
                    }
                    return result;
                }
            }
        }
        throw new ConfigRecordException.BadRecord("Unsupported list element type for field " + key + ": " + type, null);
    }

    private static Set<?> getSetValue(Config config, String key, Parameter param) {
        List<?> list = getListValue(config, key, (ParameterizedType) param.getParameterizedType());
        return new HashSet<>(list);
    }

    private static Map<String, ?> getMapValue(Config config, String key, Parameter param) {
        Type genericType = param.getParameterizedType();

        if (!(genericType instanceof ParameterizedType)) {
            // No generic type info, return empty map or throw
            throw new IllegalArgumentException("Map must have generic type parameters");
        }

        ParameterizedType paramType = (ParameterizedType) genericType;
        Type[] typeArgs = paramType.getActualTypeArguments();

        if (typeArgs.length != 2) {
            throw new ConfigRecordException.BadRecord("Map must have exactly 2 type parameters");
        }

        Class<?> keyType = (Class<?>) typeArgs[0];
        Type valueType = typeArgs[1];

        // Only support String keys for now
        if (keyType != String.class) {
            throw new ConfigRecordException.BadRecord("Unsupported map key type: " + keyType + ". Only Map<String, ?> is supported");
        }

        // Get the config object at this key
        ConfigObject configObject = config.getObject(key);
        Map<String, Object> result = new HashMap<>();

        for (Map.Entry<String, ConfigValue> entry : configObject.entrySet()) {
            String mapKey = entry.getKey();
            Object mapValue = getMapValueForType(
                    config.getConfig(key),
                    mapKey,
                    valueType
            );
            result.put(mapKey, mapValue);
        }

        return result;
    }

    private static Object getMapValueForType(Config config, String key, Type valueType) {
        if (valueType instanceof Class<?>) {
            Class<?> valueClass = (Class<?>) valueType;

            if (valueClass == String.class) {
                return config.getString(key);
            } else if (valueClass == Integer.class || valueClass == int.class) {
                return config.getInt(key);
            } else if (valueClass == Long.class || valueClass == long.class) {
                return config.getLong(key);
            } else if (valueClass == Double.class || valueClass == double.class) {
                return config.getDouble(key);
            } else if (valueClass == Boolean.class || valueClass == boolean.class) {
                return config.getBoolean(key);
            } else if (valueClass == Duration.class) {
                return config.getDuration(key);
            } else if (valueClass == ConfigMemorySize.class) {
                return config.getMemorySize(key);
            } else if (valueClass == Config.class) {
                return config.getConfig(key);
            } else if (valueClass == ConfigObject.class) {
                return config.getObject(key);
            } else if (valueClass == ConfigValue.class) {
                return config.getValue(key);
            } else if (valueClass == ConfigList.class) {
                return config.getList(key);
            } else if (valueClass.isEnum()) {
                @SuppressWarnings("unchecked")
                Enum enumValue = config.getEnum((Class<Enum>) valueClass, key);
                return enumValue;
            } else if (Record.class.isAssignableFrom(valueClass)) {
                // Map value is a record - recursively parse it
                Config nestedConfig = config.getConfig(key);
                return createInternal(nestedConfig, (Class<? extends Record>) valueClass);
            } else if (hasAtLeastOneBeanProperty(valueClass)) {
                return ConfigBeanImpl.createInternal(config.getConfig(key), valueClass);
            } else if (valueClass == Object.class) {
                return config.getAnyRef(key);
            } else {
                throw new ConfigRecordException.BadRecord("Unsupported map value type: " + valueClass);
            }
        } else if (valueType instanceof ParameterizedType) {
            // Handle nested generics like Map<String, List<SomeRecord>>
            ParameterizedType paramValueType = (ParameterizedType) valueType;
            return getListValue(config, key, paramValueType);
        }

        throw new ConfigRecordException.BadRecord("Unsupported map value type: " + valueType);
    }

    private static String toKebabCase(String camelCase) {
        return camelCase.replaceAll("([a-z0-9])([A-Z])", "$1-$2").toLowerCase();
    }
}

package io.github.dkichler.config;

import com.typesafe.config.ConfigException;
import com.typesafe.config.ConfigOrigin;

public class ConfigRecordException extends ConfigException {

    protected ConfigRecordException(ConfigOrigin origin, String message, Throwable cause) {
        super(origin, message, cause);
    }

    protected ConfigRecordException(String message, Throwable cause) {
        super(message, cause);
    }

    public static class BadRecord extends ConfigRecordException {
        private static final long serialVersionUID = 1L;

        public BadRecord(String message, Throwable cause) {
            super(message, cause);
        }

        public BadRecord(String message) {
            this(message, null);
        }
    }
}

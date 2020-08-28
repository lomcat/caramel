/*
 * Copyright 2018-2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.lomcat.caramel.assist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;

import java.util.concurrent.ConcurrentHashMap;

/**
 * logger wrapper
 *
 * @author Kweny
 * @since 0.0.1
 */
public class CaramelLogger {

    private static final ConcurrentHashMap<Object, CaramelLogger> LOGGER_CACHE = new ConcurrentHashMap<>();

    public static CaramelLogger getLogger(final String name) {
        return LOGGER_CACHE.computeIfAbsent(name, key -> new CaramelLogger(name));
    }

    public static CaramelLogger getLogger(final Class<?> clazz) {
        return LOGGER_CACHE.computeIfAbsent(clazz, key -> new CaramelLogger(clazz));
    }

    public static CaramelLogger getLogger(final String name, final Marker defaultMarker) {
        return LOGGER_CACHE.computeIfAbsent(name, key -> new CaramelLogger(name, defaultMarker));
    }

    public static CaramelLogger getLogger(final Class<?> clazz, final Marker defaultMarker) {
        return LOGGER_CACHE.computeIfAbsent(clazz, key -> new CaramelLogger(clazz, defaultMarker));
    }

    private final Logger logger;
    private final Marker defaultMarker;

    private CaramelLogger(String name) {
        this(name, null);
    }

    private CaramelLogger(Class<?> clazz) {
        this(clazz, null);
    }

    private CaramelLogger(String name, Marker defaultMarker) {
        this.logger = LoggerFactory.getLogger(name);
        this.defaultMarker = defaultMarker;
    }

    private CaramelLogger(Class<?> clazz, Marker defaultMarker) {
        this.logger = LoggerFactory.getLogger(clazz);
        this.defaultMarker = defaultMarker;
    }

    // ----- output methods ----- beginning

    final public String ROOT_LOGGER_NAME = "ROOT";

    public String getName() {
        return logger.getName();
    }

    // trace
    public boolean isTraceEnabled() {
        if (defaultMarker != null) {
            return isTraceEnabled(defaultMarker);
        } else {
            return logger.isTraceEnabled();
        }
    }

    public void trace(String format, Object... arguments) {
        if (defaultMarker != null) {
            trace(defaultMarker, format, arguments);
        } else {
            logger.trace(format, arguments);
        }
    }

    public void trace(Throwable t, String format, Object... arguments) {
        if (this.defaultMarker != null) {
            trace(defaultMarker, t, format, arguments);
        } else {
            if (isTraceEnabled()) {
                String msg = ParameterizedText.format(format, arguments);
                logger.trace(msg, t);
            }
        }
    }

    public boolean isTraceEnabled(Marker marker) {
        return logger.isTraceEnabled(marker);
    }

    public void trace(Marker marker, String format, Object... arguments) {
        logger.trace(marker, format, arguments);
    }

    public void trace(Marker marker, Throwable t, String format, Object... arguments) {
        if (isTraceEnabled(marker)) {
            String msg = ParameterizedText.format(format, arguments);
            logger.trace(marker, msg, t);
        }
    }

    // debug
    public boolean isDebugEnabled() {
        if (defaultMarker != null) {
            return isDebugEnabled(defaultMarker);
        } else {
            return logger.isDebugEnabled();
        }
    }

    public void debug(String format, Object... arguments) {
        if (defaultMarker != null) {
            debug(defaultMarker, format, arguments);
        } else {
            logger.debug(format, arguments);
        }
    }

    public void debug(Throwable t, String format, Object... arguments) {
        if (defaultMarker != null) {
            debug(defaultMarker, t, format, arguments);
        } else {
            if (isDebugEnabled()) {
                String msg = ParameterizedText.format(format, arguments);
                logger.debug( msg, t);
            }
        }
    }

    public boolean isDebugEnabled(Marker marker) {
        return logger.isDebugEnabled(marker);
    }

    public void debug(Marker marker, String format, Object... arguments) {
        logger.debug(marker, format, arguments);
    }

    public void debug(Marker marker, Throwable t, String format, Object... arguments) {
        if (isDebugEnabled(marker)) {
            String msg = ParameterizedText.format(format, arguments);
            logger.debug(marker, msg, t);
        }
    }

    // info
    public boolean isInfoEnabled() {
        if (defaultMarker != null) {
            return isInfoEnabled(defaultMarker);
        } else {
            return logger.isInfoEnabled();
        }
    }

    public void info(String format, Object... arguments) {
        if (defaultMarker != null) {
            info(defaultMarker, format, arguments);
        } else {
            logger.info(format, arguments);
        }
    }

    public void info(Throwable t, String format, Object... arguments) {
        if (defaultMarker != null) {
            info(defaultMarker, t, format, arguments);
        } else {
            if (isInfoEnabled()) {
                String msg = ParameterizedText.format(format, arguments);
                logger.info(msg, t);
            }
        }
    }

    public boolean isInfoEnabled(Marker marker) {
        return logger.isInfoEnabled(marker);
    }

    public void info(Marker marker, String format, Object... arguments) {
        logger.info(marker, format, arguments);
    }

    public void info(Marker marker, Throwable t, String format, Object... arguments) {
        if (isInfoEnabled(marker)) {
            String msg = ParameterizedText.format(format, arguments);
            logger.info(marker, msg, t);
        }
    }

    // warn
    public boolean isWarnEnabled() {
        if (defaultMarker != null) {
            return isWarnEnabled(defaultMarker);
        } else {
            return logger.isWarnEnabled();
        }
    }

    public void warn(String format, Object... arguments) {
        if (defaultMarker != null) {
            warn(defaultMarker, format, arguments);
        } else {
            logger.warn(format, arguments);
        }
    }

    public void warn(Throwable t, String format, Object... arguments) {
        if (defaultMarker != null) {
            warn(defaultMarker, t, format, arguments);
        } else {
            if (isWarnEnabled()) {
                String msg = ParameterizedText.format(format, arguments);
                logger.warn(msg, t);
            }
        }
    }

    public boolean isWarnEnabled(Marker marker) {
        return logger.isWarnEnabled(marker);
    }

    public void warn(Marker marker, String format, Object... arguments) {
        logger.warn(marker, format, arguments);
    }

    public void warn(Marker marker, Throwable t, String format, Object... arguments) {
        if (isWarnEnabled(marker)) {
            String msg = ParameterizedText.format(format, arguments);
            logger.warn(marker, msg, t);
        }
    }

    // error
    public boolean isErrorEnabled() {
        if (defaultMarker != null) {
            return isErrorEnabled(defaultMarker);
        } else {
            return logger.isErrorEnabled();
        }
    }

    public void error(String format, Object... arguments) {
        if (defaultMarker != null) {
            error(defaultMarker, format, arguments);
        } else {
            logger.error(format, arguments);
        }
    }

    public void error(Throwable t, String format, Object... arguments) {
        if (defaultMarker != null) {
            error(defaultMarker, t, format, arguments);
        } else {
            if (isErrorEnabled()) {
                String msg = ParameterizedText.format(format, arguments);
                logger.error(msg, t);
            }
        }
    }

    public boolean isErrorEnabled(Marker marker) {
        return logger.isErrorEnabled(marker);
    }

    public void error(Marker marker, String format, Object... arguments) {
        logger.error(marker, format, arguments);
    }

    public void error(Marker marker, Throwable t, String format, Object... arguments) {
        if (isErrorEnabled(marker)) {
            String msg = ParameterizedText.format(format, arguments);
            logger.error(marker, msg, t);
        }
    }
    // ----- output methods ----- ending
}
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

package com.lomcat.caramel.alpaca.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;

/**
 * @author Kweny
 * @since 0.0.1
 */
public class LoggerConfigurationListener implements ServletContextListener {

    private static final String LOGGER_CONFIG_LOCATION = "loggerConfigLocation";

    @Override
    public void contextInitialized(ServletContextEvent event) {
        ServletContext servletContext = event.getServletContext();
        String loggerConfigLocation = servletContext.getInitParameter(LOGGER_CONFIG_LOCATION);
        try {
            URI loggerConfigURI = LoggerConfigurationListener.class.getResource(loggerConfigLocation).toURI();
            LoggerContext loggerContext = (LoggerContext) LogManager.getContext(false);
            loggerContext.setConfigLocation(loggerConfigURI);
            loggerContext.reconfigure();
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent event) {

    }

    public static void main(String[] args) {
        try (InputStreamReader reader = new InputStreamReader(LoggerConfigurationListener.class.getResourceAsStream("/config/alpaca-druid.conf"))) {
            Properties props = new Properties();
            props.load(reader);
            props.forEach((key, value) -> {
                System.out.println(key + " == " + value);
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
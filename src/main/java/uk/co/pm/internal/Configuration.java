package uk.co.pm.internal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Properties;

/**
 * Simple accessor class for application configuration properties, allowing strongly-typed access to properties by name.
 */
public class Configuration {

    private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

    private final Properties properties;

    /**
     * Creates a new instance, wrapping around the supplied properties
     *
     * @param properties underlying properties object
     */
    public Configuration(Properties properties) {
        this.properties = properties;
    }

    /**
     * Factory method creating a configuration from a file on the local file system.
     *
     * @param path path to file on local file system
     * @return initialised configuration object.
     */
    public static Configuration loadFromFile(String path) {
        logger.info("Loading application configuration from file: {}", path);
        Path filePath = Paths.get(path);

        Properties properties = new Properties();
        try (InputStream is = Files.newInputStream(filePath, StandardOpenOption.READ)) {
            properties.load(is);
        } catch (IOException e) {
            throw new ApplicationInitializationException("Failed to load application configuration", e);
        }
        return new Configuration(properties);
    }

    /**
     * Factory method creating a configuration from a file on the classpath.
     *
     * @param path path to file on local file system
     * @return initialised configuration object.
     */
    public static Configuration loadFromClasspath(String path) {
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        logger.info("Loading application configuration from classpath location: {}", path);

        Properties properties = new Properties();
        try (InputStream is = ApplicationBase.class.getResourceAsStream(path)) {
            properties.load(is);
        } catch (IOException e) {
            throw new ApplicationInitializationException("Failed to load application configuration", e);
        }
        return new Configuration(properties);
    }

    /**
     * Retrieves the value of the named property as a String, returning a default value if the property is found to be null.
     *
     * @param name         property name
     * @param defaultValue default to be returned if the property is null
     * @return property value
     */
    public String getString(String name, String defaultValue) {
        final String value = properties.getProperty(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Retrieves the value of the named property as an int, returning a default value if the property is found to be
     * null or not an integer.
     *
     * @param name         property name
     * @param defaultValue default to be returned if the property is null or not an integer
     * @return property value
     */
    public int getInteger(String name, int defaultValue) {
        final String intValue = properties.getProperty(name);
        if (intValue != null) {
            try {
                return Integer.parseInt(intValue);
            } catch (NumberFormatException n) {
                logger.error("Value of property {} is not an int; returning default value {}", name, defaultValue);
            }
        }
        return defaultValue;
    }
}

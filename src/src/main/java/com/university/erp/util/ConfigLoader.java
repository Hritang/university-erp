package com.university.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class ConfigLoader {

    private String dbUrl;
    private String dbUsername;
    private String dbPassword;

    public ConfigLoader() {

        Properties properties = new Properties();

        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("config.properties")) {

            if (inputStream == null) {
                throw new RuntimeException("⚠ config.properties file not found in resources folder!");
            }

            properties.load(inputStream);

            dbUrl = properties.getProperty("auth.db.url");
            dbUsername = properties.getProperty("auth.db.username");
            dbPassword = properties.getProperty("auth.db.password");

            if (dbUrl == null || dbUsername == null || dbPassword == null) {
                throw new RuntimeException("Missing keys in config.properties. Check file contents.");
            }

        } catch (IOException e) {
            throw new RuntimeException("Failed to load config.properties file.", e);
        }
    }


    public String getDbUrl() {
        return dbUrl;
    }

    public String getDbUsername() {
        return dbUsername;
    }

    public String getDbPassword() {
        return dbPassword;
    }
}
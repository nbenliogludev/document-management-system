package com.nbenliogludev.documentgenerator.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GeneratorConfig {

    private String baseUrl;
    private int count;
    private int delayMs;
    private String authorPrefix;
    private String titlePrefix;
    private String numberPrefix;

    public static GeneratorConfig load(String[] args) {
        GeneratorConfig config = new GeneratorConfig();

        // 1. Load from properties file
        Properties props = new Properties();
        try (InputStream input = GeneratorConfig.class.getClassLoader().getResourceAsStream("generator.properties")) {
            if (input != null) {
                props.load(input);
            } else {
                System.out.println("Warning: generator.properties not found in classpath.");
            }
        } catch (IOException ex) {
            System.err.println("Error loading generator.properties: " + ex.getMessage());
        }

        config.baseUrl = props.getProperty("generator.base-url", "http://localhost:8080");
        config.count = Integer.parseInt(props.getProperty("generator.count", "100"));
        config.delayMs = Integer.parseInt(props.getProperty("generator.delay-ms", "0"));
        config.authorPrefix = props.getProperty("generator.author-prefix", "Generator");
        config.titlePrefix = props.getProperty("generator.title-prefix", "Document");
        config.numberPrefix = props.getProperty("generator.number-prefix", "DOC");

        // 2. Override with CLI arguments (e.g. --count=50)
        for (String arg : args) {
            if (arg.startsWith("--base-url=")) {
                config.baseUrl = arg.substring("--base-url=".length());
            } else if (arg.startsWith("--count=")) {
                config.count = Integer.parseInt(arg.substring("--count=".length()));
            } else if (arg.startsWith("--delay-ms=")) {
                config.delayMs = Integer.parseInt(arg.substring("--delay-ms=".length()));
            } else if (arg.startsWith("--author-prefix=")) {
                config.authorPrefix = arg.substring("--author-prefix=".length());
            } else if (arg.startsWith("--title-prefix=")) {
                config.titlePrefix = arg.substring("--title-prefix=".length());
            } else if (arg.startsWith("--number-prefix=")) {
                config.numberPrefix = arg.substring("--number-prefix=".length());
            }
        }

        return config;
    }

    // Getters
    public String getBaseUrl() {
        return baseUrl;
    }

    public int getCount() {
        return count;
    }

    public int getDelayMs() {
        return delayMs;
    }

    public String getAuthorPrefix() {
        return authorPrefix;
    }

    public String getTitlePrefix() {
        return titlePrefix;
    }

    public String getNumberPrefix() {
        return numberPrefix;
    }
}

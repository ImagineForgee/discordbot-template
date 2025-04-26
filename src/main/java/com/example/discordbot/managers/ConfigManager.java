package com.example.discordbot.managers;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

import java.io.InputStream;
import java.util.Map;

public class ConfigManager {
    private static ConfigManager instance;
    private Map<String, Object> config;

    private ConfigManager() {
        loadConfig();
    }

    public static ConfigManager getInstance() {
        if (instance == null) {
            instance = new ConfigManager();
        }
        return instance;
    }

    private void loadConfig() {
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (input == null) {
                throw new IllegalStateException("config.yml not found in resources folder.");
            }
            LoaderOptions options = new LoaderOptions();
            Yaml yaml = new Yaml(new SafeConstructor(options));
            config = yaml.load(input);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load config.yml", e);
        }
    }

    @SuppressWarnings("unchecked")
    private Object getValue(String path) {
        String[] parts = path.split("\\.");
        Object current = config;
        for (String part : parts) {
            if (!(current instanceof Map)) {
                return null;
            }
            current = ((Map<String, Object>) current).get(part);
            if (current == null) {
                return null;
            }
        }
        return current;
    }

    public String getString(String path) {
        Object value = getValue(path);
        return value != null ? value.toString() : null;
    }

    public int getInt(String path) {
        Object value = getValue(path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return value != null ? Integer.parseInt(value.toString()) : 0;
    }

    public boolean getBoolean(String path) {
        Object value = getValue(path);
        return value != null && Boolean.parseBoolean(value.toString());
    }

    @SuppressWarnings("unchecked")
    public <T> T get(String path, Class<T> type) {
        return (T) getValue(path);
    }

    public boolean contains(String path) {
        return getValue(path) != null;
    }
}

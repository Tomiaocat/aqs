package com.httptool.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 应用设置存储
 */
public class SettingsStorage {
    private static final String SETTINGS_FILE = System.getProperty("user.home") + "/.http-client/settings.json";
    private Map<String, String> settings = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public SettingsStorage() {
        loadSettings();
    }

    /**
     * 获取设置值
     */
    public String getSetting(String key, String defaultValue) {
        return settings.getOrDefault(key, defaultValue);
    }

    /**
     * 保存设置值
     */
    public void setSetting(String key, String value) {
        if (value != null) {
            settings.put(key, value);
        } else {
            settings.remove(key);
        }
        saveToFile();
    }

    /**
     * 获取上次使用的URL
     */
    public String getLastUrl(String defaultUrl) {
        return getSetting("lastUrl", defaultUrl);
    }

    /**
     * 保存上次使用的URL
     */
    public void setLastUrl(String url) {
        setSetting("lastUrl", url);
    }

    private void loadSettings() {
        try {
            File file = new File(SETTINGS_FILE);
            if (file.exists()) {
                settings = mapper.readValue(file, new TypeReference<Map<String, String>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
            settings = new HashMap<>();
        }
    }

    private void saveToFile() {
        try {
            File file = new File(SETTINGS_FILE);
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, settings);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

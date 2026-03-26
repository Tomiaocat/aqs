package com.httptool.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * 密钥存储
 */
public class KeyStorage {
    private static final String KEY_FILE = System.getProperty("user.home") + "/.http-client/keys.json";
    private Map<String, String> keys = new HashMap<>();
    private ObjectMapper mapper = new ObjectMapper();

    public KeyStorage() {
        loadKeys();
    }

    public void saveKey(String name, String key) {
        keys.put(name, key);
        saveToFile();
    }

    public void deleteKey(String name) {
        keys.remove(name);
        saveToFile();
    }

    public String getKey(String name) {
        return keys.get(name);
    }

    public Map<String, String> getAllKeys() {
        return new HashMap<>(keys);
    }

    private void loadKeys() {
        try {
            File file = new File(KEY_FILE);
            if (file.exists()) {
                keys = mapper.readValue(file, new TypeReference<Map<String, String>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
            keys = new HashMap<>();
        }
    }

    private void saveToFile() {
        try {
            File file = new File(KEY_FILE);
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, keys);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

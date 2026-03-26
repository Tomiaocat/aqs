package com.httptool.storage;

import com.httptool.model.RequestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * 请求模板存储
 */
public class TemplateStorage {
    private static final String TEMPLATE_FILE = System.getProperty("user.home") + "/.http-client/templates.json";
    private List<RequestTemplate> templates = new ArrayList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public TemplateStorage() {
        loadTemplates();
    }

    public void saveTemplate(RequestTemplate template) {
        // 检查是否已存在同名模板
        templates.removeIf(t -> t.getName().equals(template.getName()));
        templates.add(template);
        saveToFile();
    }

    public void deleteTemplate(String name) {
        templates.removeIf(t -> t.getName().equals(name));
        saveToFile();
    }

    public List<RequestTemplate> getTemplates() {
        return new ArrayList<>(templates);
    }

    private void loadTemplates() {
        try {
            File file = new File(TEMPLATE_FILE);
            if (file.exists()) {
                templates = mapper.readValue(file, new TypeReference<List<RequestTemplate>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
            templates = new ArrayList<>();
        }
    }

    private void saveToFile() {
        try {
            File file = new File(TEMPLATE_FILE);
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, templates);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

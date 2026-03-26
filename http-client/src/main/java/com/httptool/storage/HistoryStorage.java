package com.httptool.storage;

import com.httptool.model.HttpRequestData;
import com.httptool.model.HttpResponseData;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 请求历史存储
 */
public class HistoryStorage {
    private static final String HISTORY_FILE = System.getProperty("user.home") + "/.http-client/history.json";
    private static final int MAX_HISTORY_SIZE = 100;
    private LinkedList<HttpRequestData> history = new LinkedList<>();
    private ObjectMapper mapper = new ObjectMapper();

    public HistoryStorage() {
        loadHistory();
    }

    public void addHistory(HttpRequestData request, HttpResponseData response) {
        // 保存响应信息到请求对象
        request.setResponseStatus(response.getStatusCode());
        request.setResponseDuration(response.getDuration());

        history.addFirst(request);

        // 限制历史记录数量
        while (history.size() > MAX_HISTORY_SIZE) {
            history.removeLast();
        }

        saveToFile();
    }

    public List<HttpRequestData> getHistory() {
        return new ArrayList<>(history);
    }

    public void clearHistory() {
        history.clear();
        saveToFile();
    }

    private void loadHistory() {
        try {
            File file = new File(HISTORY_FILE);
            if (file.exists()) {
                history = mapper.readValue(file, new TypeReference<LinkedList<HttpRequestData>>() {});
            }
        } catch (IOException e) {
            e.printStackTrace();
            history = new LinkedList<>();
        }
    }

    private void saveToFile() {
        try {
            File file = new File(HISTORY_FILE);
            file.getParentFile().mkdirs();
            mapper.writerWithDefaultPrettyPrinter().writeValue(file, history);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

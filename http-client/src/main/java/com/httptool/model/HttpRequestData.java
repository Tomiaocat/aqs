package com.httptool.model;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP请求数据模型
 */
public class HttpRequestData {
    private String method;
    private String url;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private String key;
    private long timestamp;
    private int responseStatus;
    private long responseDuration;

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getResponseStatus() {
        return responseStatus;
    }

    public void setResponseStatus(int responseStatus) {
        this.responseStatus = responseStatus;
    }

    public long getResponseDuration() {
        return responseDuration;
    }

    public void setResponseDuration(long responseDuration) {
        this.responseDuration = responseDuration;
    }
}

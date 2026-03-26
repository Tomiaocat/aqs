package com.httptool.model;

import java.util.HashMap;
import java.util.Map;

/**
 * HTTP响应数据模型
 */
public class HttpResponseData {
    private int statusCode;
    private Map<String, String> headers = new HashMap<>();
    private String body;
    private String decryptedBody;
    private long duration;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public Map<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(Map<String, String> headers) {
        this.headers = headers;
    }

    public void addHeader(String name, String value) {
        headers.put(name, value);
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDecryptedBody() {
        return decryptedBody;
    }

    public void setDecryptedBody(String decryptedBody) {
        this.decryptedBody = decryptedBody;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }
}

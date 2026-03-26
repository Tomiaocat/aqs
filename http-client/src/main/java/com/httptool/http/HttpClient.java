package com.httptool.http;

import com.httptool.model.HttpResponseData;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.*;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.Map;

/**
 * HTTP客户端封装
 */
public class HttpClient {

    public HttpResponseData sendRequest(String method, String url, Map<String, String> headers, String body) throws IOException {
        long startTime = System.currentTimeMillis();

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpRequestBase request = createRequest(method, url);

            // 设置请求头
            if (headers != null) {
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    request.setHeader(entry.getKey(), entry.getValue());
                }
            }

            // 设置请求体
            if (body != null && !body.isEmpty() && request instanceof HttpEntityEnclosingRequestBase) {
                ((HttpEntityEnclosingRequestBase) request).setEntity(new StringEntity(body, "UTF-8"));
            }

            // 发送请求
            try (CloseableHttpResponse response = httpClient.execute(request)) {
                HttpResponseData responseData = new HttpResponseData();
                responseData.setStatusCode(response.getStatusLine().getStatusCode());
                responseData.setDuration(System.currentTimeMillis() - startTime);

                // 获取响应体
                HttpEntity entity = response.getEntity();
                if (entity != null) {
                    String responseBody = EntityUtils.toString(entity, "UTF-8");
                    responseData.setBody(responseBody);
                }

                // 获取响应头
                org.apache.http.Header[] responseHeaders = response.getAllHeaders();
                for (org.apache.http.Header header : responseHeaders) {
                    responseData.addHeader(header.getName(), header.getValue());
                }

                return responseData;
            }
        }
    }

    private HttpRequestBase createRequest(String method, String url) {
        switch (method.toUpperCase()) {
            case "GET":
                return new HttpGet(url);
            case "POST":
                return new HttpPost(url);
            case "PUT":
                return new HttpPut(url);
            case "PATCH":
                return new HttpPatch(url);
            case "DELETE":
                return new HttpDelete(url);
            default:
                return new HttpPost(url);
        }
    }
}

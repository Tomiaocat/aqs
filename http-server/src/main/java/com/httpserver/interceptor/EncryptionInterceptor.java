package com.httpserver.interceptor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.httpserver.crypto.AesUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.MethodParameter;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.ReadListener;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * 请求解密拦截器 - 解密请求体
 */
@Component
@Order(1)
public class EncryptionInterceptor implements HandlerInterceptor {

    @Value("${api.encrypt.key:default-key}")
    private String encryptKey;

    @Value("${api.encrypt.enabled:true}")
    private boolean encryptionEnabled;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
            Object handler) throws Exception {
        if (!encryptionEnabled) {
            return true;
        }

        // 只处理POST/PUT/PATCH请求且有Body的情况
        String method = request.getMethod();
        if (!(method.equals("POST") || method.equals("PUT") || method.equals("PATCH"))) {
            return true;
        }

        // 读取请求体并解密
        String body = readRequestBody(request);
        if (body != null && !body.trim().isEmpty()) {
            try {
                String decrypted = AesUtil.decryptJsonValues(body, encryptKey);
                // 将解密后的内容设置到request attribute中，供后续使用
                request.setAttribute("decryptedBody", decrypted);
            } catch (Exception e) {
                // 如果解密失败，可能是明文请求，继续处理
                request.setAttribute("decryptedBody", body);
            }
        }
        return true;
    }

    private String readRequestBody(HttpServletRequest request) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
}

/**
 * 响应加密处理器 - 加密响应体
 */
@Component
@ControllerAdvice
class EncryptionResponseAdvice implements ResponseBodyAdvice<Object> {

    @Value("${api.encrypt.key:default-key}")
    private String encryptKey;

    @Value("${api.encrypt.enabled:true}")
    private boolean encryptionEnabled;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public boolean supports(MethodParameter returnType,
            Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查是否是RestController
        return returnType.getContainingClass().isAnnotationPresent(RestController.class);
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType,
            MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType,
            ServerHttpRequest request, ServerHttpResponse response) {
        if (!encryptionEnabled) {
            return body;
        }

        // 如果已经是String类型，直接返回（避免重复加密）
        if (body instanceof String) {
            return body;
        }

        try {
            String json = objectMapper.writeValueAsString(body);
            return AesUtil.encryptJsonValues(json, encryptKey);
        } catch (Exception e) {
            throw new RuntimeException("响应加密失败", e);
        }
    }
}

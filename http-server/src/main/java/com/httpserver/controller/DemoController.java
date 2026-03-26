package com.httpserver.controller;

import com.httpserver.crypto.AesUtil;
import com.httpserver.model.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 示例Controller - 演示加密通信（手动加解密版本）
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    @Value("${api.encrypt.key:default-key}")
    private String encryptKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建用户接口 - 手动解密请求，手动加密响应
     * 接收加密JSON: {"id": "Base64(IV+cipher)", "name": "Base64(IV+cipher)", ...}
     * 返回加密JSON: 同样格式
     */
    @PostMapping("/user")
    public String createUser(@RequestBody String encryptedBody) {
        try {
            // 1. 解密请求体
            System.out.println("收到加密请求: " + encryptedBody);
            String decryptedJson = AesUtil.decryptJsonValues(encryptedBody, encryptKey);
            System.out.println("解密后: " + decryptedJson);

            // 2. 解析JSON为对象
            Map<String, Object> userData = objectMapper.readValue(decryptedJson, Map.class);

            // 3. 业务处理
            User user = new User();
            if (userData.get("id") != null) {
                user.setId(Long.valueOf(userData.get("id").toString()));
            }
            if (userData.get("name") != null) {
                user.setName(userData.get("name").toString());
            }
            if (userData.get("age") != null) {
                user.setAge(Integer.valueOf(userData.get("age").toString()));
            }

            // 4. 构造响应
            ApiResponse<User> response = ApiResponse.success(user);
            String responseJson = objectMapper.writeValueAsString(response);
            System.out.println("响应明文: " + responseJson);

            // 5. 加密响应
            String encryptedResponse = AesUtil.encryptJsonValues(responseJson, encryptKey);
            System.out.println("加密响应: " + encryptedResponse);

            return encryptedResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"code\":500,\"message\":\"解密失败: " + e.getMessage() + "\",\"data\":null}";
        }
    }

    /**
     * 获取用户接口 - 返回加密响应
     */
    @GetMapping("/user/{id}")
    public String getUser(@PathVariable Long id) {
        try {
            // 1. 业务处理
            User user = new User();
            user.setId(id);
            user.setName("张三");
            user.setAge(25);

            // 2. 构造响应
            ApiResponse<User> response = ApiResponse.success(user);
            String responseJson = objectMapper.writeValueAsString(response);

            // 3. 加密响应（仅最外层value）
            String encryptedResponse = AesUtil.encryptJsonValues(responseJson, encryptKey);
            System.out.println("加密响应: " + encryptedResponse);

            return encryptedResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"code\":500,\"message\":\"加密失败: " + e.getMessage() + "\",\"data\":null}";
        }
    }

    /**
     * 通用数据处理接口 - 手动解密请求，手动加密响应
     */
    @PostMapping("/data")
    public String processData(@RequestBody String encryptedBody) {
        try {
            // 1. 解密请求
            System.out.println("收到加密请求: " + encryptedBody);
            String decryptedJson = AesUtil.decryptJsonValues(encryptedBody, encryptKey);
            System.out.println("解密后: " + decryptedJson);

            // 2. 解析数据
            Map<String, Object> data = objectMapper.readValue(decryptedJson, Map.class);

            // 3. 处理业务...

            // 4. 构造并加密响应
            ApiResponse<Map<String, Object>> response = ApiResponse.success(data);
            String responseJson = objectMapper.writeValueAsString(response);
            String encryptedResponse = AesUtil.encryptJsonValues(responseJson, encryptKey);

            return encryptedResponse;

        } catch (Exception e) {
            e.printStackTrace();
            return "{\"code\":500,\"message\":\"处理失败: " + e.getMessage() + "\",\"data\":null}";
        }
    }

    /**
     * 健康检查接口 - 不加密
     */
    @GetMapping("/health")
    public ApiResponse<String> health() {
        return ApiResponse.success("Server is running");
    }

    /**
     * 用户数据类
     */
    public static class User {
        private Long id;
        private String name;
        private int age;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }
    }
}

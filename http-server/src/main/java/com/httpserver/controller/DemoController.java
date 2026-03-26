package com.httpserver.controller;

import com.httpserver.model.ApiResponse;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 示例Controller - 演示加密通信
 */
@RestController
@RequestMapping("/api")
public class DemoController {

    /**
     * 创建用户接口 - 接收加密请求，返回加密响应
     */
    @PostMapping("/user")
    public ApiResponse<User> createUser(@RequestBody Map<String, Object> userData) {
        // 这里的userData已经是解密后的对象
        System.out.println("收到解密后的数据: " + userData);

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

        return ApiResponse.success(user);
    }

    /**
     * 获取用户接口 - 返回加密响应
     */
    @GetMapping("/user/{id}")
    public ApiResponse<User> getUser(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setName("张三");
        user.setAge(25);

        return ApiResponse.success(user);
    }

    /**
     * 通用数据处理接口
     */
    @PostMapping("/data")
    public ApiResponse<Map<String, Object>> processData(@RequestBody Map<String, Object> data) {
        System.out.println("收到解密后的数据: " + data);
        return ApiResponse.success(data);
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

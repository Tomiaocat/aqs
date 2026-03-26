package com.httpserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Spring Boot 服务端启动类
 */
@SpringBootApplication
public class HttpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(HttpServerApplication.class, args);
        System.out.println("========================================");
        System.out.println("HTTP加密通信服务端已启动");
        System.out.println("端口: 8080");
        System.out.println("API地址: http://localhost:8080/api/");
        System.out.println("========================================");
    }
}

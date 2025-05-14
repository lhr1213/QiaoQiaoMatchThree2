package com.qiaoqiao;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

// 为了简化开发，我们暂时排除自动安全配置，使用自定义的安全配置
@SpringBootApplication(exclude = {SecurityAutoConfiguration.class})
public class QiaoQiaoMatchThreeApplication {
    public static void main(String[] args) {
        SpringApplication.run(QiaoQiaoMatchThreeApplication.class, args);
    }
}
package com.ipd.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * IPD研发管理平台 - Spring Boot 主启动类
 */
@SpringBootApplication
@EnableJpaAuditing
public class IpDPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(IpDPlatformApplication.class, args);
    }
}

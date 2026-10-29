package com.trace.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * WeBASE 配置映射类
 */
@Data
@Component
@ConfigurationProperties(prefix = "webase")
public class WebaseConfig {

    // 映射 webase.front.url
    private Front front;

    // 映射 webase.test-accounts
    private List<TestAccount> testAccounts;

    @Data
    public static class Front {
        private String url;
    }

    @Data
    public static class TestAccount {
        private String name;
        private String address;
        private Integer role;
    }
}
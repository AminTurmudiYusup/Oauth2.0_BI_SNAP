package com.example.gateway.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "gateway.scope")
public class ScopeProperties {

    private List<Rule> rules;

    @Data
    public static class Rule {
        private String path;
        private String required;
    }
}

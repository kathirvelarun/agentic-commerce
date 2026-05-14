package com.amex.ace.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class AgentBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(AgentBackendApplication.class, args);
    }
}

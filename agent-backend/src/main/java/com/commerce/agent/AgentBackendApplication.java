package com.commerce.agent;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AgentBackendApplication {

    public static void main(String[] args) {
        // Ensure the key is present before Spring context loads
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isBlank()) {
            System.err.println("""
                ╔══════════════════════════════════════════════════════════╗
                ║  OPENAI_API_KEY environment variable is not set!         ║
                ║                                                          ║
                ║  Start the application with:                             ║
                ║    export OPENAI_API_KEY=sk-proj-...                     ║
                ║    mvn spring-boot:run                                   ║
                ║                                                          ║
                ║  Or inline:                                              ║
                ║    OPENAI_API_KEY=sk-proj-... mvn spring-boot:run        ║
                ╚══════════════════════════════════════════════════════════╝
                """);
            System.exit(1);
        }
        SpringApplication.run(AgentBackendApplication.class, args);
    }
}

package com.railway.helloworld;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.context.annotation.ComponentScan;

import io.github.cdimascio.dotenv.Dotenv;

@SpringBootApplication
@RestController
@ComponentScan(basePackages = {"com.railway.helloworld", "com.railway.config", "com.railway.service"})
public class HelloworldApplication {

    public static void main(String[] args) {
        // Load environment variables from .env
        // Only load .env locally (not in Railway)
        if (System.getenv("RAILWAY_ENVIRONMENT") == null) {
            Dotenv dotenv = Dotenv.load();

            // Load database configuration
            String dbUrl = dotenv.get("DATABASE_URL");
            if (dbUrl != null) {
                System.setProperty("DATABASE_URL", dbUrl);
            }

            String dbUser = dotenv.get("POSTGRES_USER");
            if (dbUser != null) {
                System.setProperty("POSTGRES_USER", dbUser);
            }

            String dbPassword = dotenv.get("POSTGRES_PASSWORD");
            if (dbPassword != null) {
                System.setProperty("POSTGRES_PASSWORD", dbPassword);
            }

            // Load OpenAI configuration
            String openaiKey = dotenv.get("OPENAI_API_KEY");
            if (openaiKey != null) {
                System.setProperty("openai.api.key", openaiKey);
            }
        }

        SpringApplication.run(HelloworldApplication.class, args);
    }

    @GetMapping("/")
    public String hello() {
        return "{\"message\": \"Hello Caden!\"}";
    }

    @GetMapping("/hello-binh")
    public String helloBinh() {
        return "{\"message\": \"Hello Binh!\"}";
    }
}

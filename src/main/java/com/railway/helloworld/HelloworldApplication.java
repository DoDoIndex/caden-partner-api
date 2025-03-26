package com.railway.helloworld;

import io.github.cdimascio.dotenv.Dotenv;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HelloworldApplication {

    public static void main(String[] args) {
        // Check if running locally (in development) or on Railway (in production)
        String environment = System.getenv("RAILWAY_ENVIRONMENT");

        if (environment == null || environment.equals("development")) {
            // Load environment variables from .env file when running locally
            Dotenv dotenv = Dotenv.load();
            System.setProperty("DATABASE_URL", dotenv.get("DATABASE_URL"));
            System.setProperty("POSTGRES_USER", dotenv.get("POSTGRES_USER"));
            System.setProperty("POSTGRES_PASSWORD", dotenv.get("POSTGRES_PASSWORD"));
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

package com.railway.helloworld;

import io.github.cdimascio.dotenv.Dotenv;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class HelloworldApplication {
    // Use @Value to inject environment variables
    @Value("${DATABASE_URL}")
    private String databaseUrl;

    @Value("${POSTGRES_USER}")
    private String postgresUser;

    @Value("${POSTGRES_PASSWORD}")
    private String postgresPassword;

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

        // Start Spring Boot
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

    @GetMapping("/check-db")
    public String checkDatabase() {
        // Check the environment variables injected by Spring
        if (databaseUrl == null || postgresUser == null || postgresPassword == null) {
            return "Database credentials are missing.";
        }

        // Log to check if the database URL, user, and password are correctly loaded
        System.out.println("Using Database URL: " + databaseUrl);
        System.out.println("Using PostgreSQL User: " + postgresUser);

        return "Database is configured correctly!";
    }
}

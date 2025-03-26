package com.railway.helloworld;

import io.github.cdimascio.dotenv.Dotenv;

import java.net.URI;
import java.net.URISyntaxException;

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

        // Convert the DATABASE_URL for Railway to the correct JDBC URL format
        String databaseUrl = System.getenv("DATABASE_URL");

        if (databaseUrl != null && databaseUrl.startsWith("postgresql://")) {
            URI uri;
            try {
                uri = new URI(databaseUrl);
                // Extract the host, port, and database name from the URI
                String host = uri.getHost(); // This is the hostname (e.g., "postgres.railway.internal")
                int port = uri.getPort(); // This is the port (e.g., 5432)
                String dbName = uri.getPath().substring(1); // This is the database name (e.g., "railway")
                // Build the JDBC URL
                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dbName;
                // Set the JDBC URL for Spring Boot to use
                System.setProperty("SPRING_DATASOURCE_URL", jdbcUrl);
            } catch (URISyntaxException e) {
                e.printStackTrace();
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

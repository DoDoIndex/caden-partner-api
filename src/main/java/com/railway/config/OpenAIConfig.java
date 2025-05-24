package com.railway.config;

import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class OpenAIConfig {

    private static final Logger logger = LoggerFactory.getLogger(OpenAIConfig.class);

    @Value("${openai.api.key}")
    private String openaiApiKey;

    @Bean
    public OpenAiService openAiService() {
        logger.debug("Initializing OpenAI service with key length: {}", openaiApiKey != null ? openaiApiKey.length() : 0);
        return new OpenAiService(openaiApiKey);
    }
}

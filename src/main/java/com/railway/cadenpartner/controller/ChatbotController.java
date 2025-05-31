package com.railway.cadenpartner.controller;

import com.railway.cadenpartner.model.ChatRequest;
import com.railway.cadenpartner.model.ChatMessage;
import com.railway.cadenpartner.model.Product;
import com.railway.cadenpartner.service.ChatbotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Collections;

@RestController
@RequestMapping("/api/chat")
public class ChatbotController {

    @Autowired
    private ChatbotService chatbotService;

    @PostMapping("/search")
    public ResponseEntity<List<Product>> searchProducts(@RequestBody ChatRequest request) {
        // Convert single criteria request to the new format
        ChatMessage message = new ChatMessage();
        String searchQuery = String.format("%s %s", request.getSearchValue(), request.getSearchType());
        message.setMessage(searchQuery);

        Map<String, Object> response = chatbotService.processUserMessage(message);
        @SuppressWarnings("unchecked")
        List<Product> products = (List<Product>) response.getOrDefault("products", Collections.emptyList());
        return ResponseEntity.ok(products);
    }

    @PostMapping("/message")
    public ResponseEntity<Map<String, Object>> chat(@RequestBody ChatMessage message) {
        Map<String, Object> response = chatbotService.processUserMessage(message);
        return ResponseEntity.ok(response);
    }
}

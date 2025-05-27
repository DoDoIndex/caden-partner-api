package com.railway.helloworld.service;

import com.railway.helloworld.model.Product;
import com.railway.helloworld.model.ChatRequest;
import com.railway.helloworld.model.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;

@Service
public class ChatbotService {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private OpenAiService openAiService;

    @Value("${api.host}")
    private String apiHost;

    private static final String SYSTEM_PROMPT = """
        You are a helpful tile shopping assistant. Extract search criteria from user messages or handle bookmark/collection actions.
        
        For search queries, output format should be JSON with:
        {
            "action": "search",
            "criteria": [
                {
                    "searchType": one of ["Material", "Color Group", "Size", "Usage", "Trim"],
                    "searchValue": the value to search for
                },
                // ... more criteria if present
            ]
        }
        
        For bookmark actions, output format should be:
        {
            "action": "bookmark",
            "response": "confirmation message about bookmark status",
            "askCollection": true,
            "collectionPrompt": "Would you like to add these tiles to a collection? You can choose an existing collection or create a new one."
        }
        
        For collection actions, output format should be:
        {
            "action": "collection",
            "prompt": "Which collection would you like to add these tiles to? Or type 'new: [collection name]' to create a new collection."
        }
        
        Examples:
        User: "Show me porcelain tiles"
        Output: {
            "action": "search",
            "criteria": [
                {"searchType": "Material", "searchValue": "Porcelain"}
            ]
        }
        
        User: "I need ceramic blue tiles"
        Output: {
            "action": "search",
            "criteria": [
                {"searchType": "Material", "searchValue": "Ceramic"},
                {"searchType": "Color Group", "searchValue": "Blue"}
            ]
        }
        
        User: "Add to bookmark"
        Output: {
            "action": "bookmark",
            "response": "I've added these tiles to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add these tiles to a collection? You can choose an existing collection or create a new one."
        }
        
        If you can't determine the action, respond with: {"action": null, "response": "I'm not sure what you'd like to do. Could you please rephrase that?"}
        """;

    public Map<String, Object> processUserMessage(ChatMessage userMessage) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", SYSTEM_PROMPT));
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("user", userMessage.getMessage()));

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();

            String gptResponse = openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage().getContent();

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> parsedResponse = mapper.readValue(gptResponse, Map.class);

            String action = (String) parsedResponse.get("action");
            if ("search".equals(action)) {
                // Handle multi-criteria search
                List<Map<String, String>> criteria = (List<Map<String, String>>) parsedResponse.get("criteria");
                List<Product> products = searchProductsWithMultipleCriteria(criteria);
                response.put("message", buildResponseMessage(criteria, products.size()));
                response.put("products", products);

            } else if ("bookmark".equals(action)) {
                response.put("message", parsedResponse.get("response"));
                response.put("askCollection", parsedResponse.get("askCollection"));
                response.put("collectionPrompt", parsedResponse.get("collectionPrompt"));

            } else if ("collection".equals(action)) {
                response.put("message", parsedResponse.get("prompt"));
                response.put("action", "collection");

            } else {
                response.put("message", parsedResponse.get("response"));
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.put("message", "I encountered an error processing your request. Could you please try again?");
            response.put("error", true);
        }

        return response;
    }

    private String buildResponseMessage(List<Map<String, String>> criteria, int resultCount) {
        if (resultCount == 0) {
            return String.format("I couldn't find any tiles matching your criteria: %s",
                    formatCriteria(criteria));
        }
        return String.format("I found %d tiles matching your criteria: %s",
                resultCount, formatCriteria(criteria));
    }

    private String formatCriteria(List<Map<String, String>> criteria) {
        return criteria.stream()
                .map(c -> c.get("searchType") + ": " + c.get("searchValue"))
                .collect(Collectors.joining(", "));
    }

    private List<Product> searchProductsWithMultipleCriteria(List<Map<String, String>> criteria) {
        Product[] products = restTemplate.getForObject(apiHost + "/api/catalog", Product[].class);
        if (products == null) {
            return List.of();
        }

        return Arrays.stream(products)
                .filter(product -> matchesAllCriteria(product, criteria))
                .collect(Collectors.toList());
    }

    private boolean matchesAllCriteria(Product product, List<Map<String, String>> criteria) {
        Map<String, Object> details = product.getProductDetails();
        if (details == null) {
            return false;
        }

        return criteria.stream().allMatch(criterion -> {
            String searchType = criterion.get("searchType");
            String searchValue = criterion.get("searchValue").toLowerCase();

            return switch (searchType.toLowerCase()) {
                case "material" ->
                    containsIgnoreCase(String.valueOf(details.get("Material")), searchValue);
                case "color group" ->
                    containsIgnoreCase(String.valueOf(details.get("Color Group")), searchValue);
                case "size" ->
                    containsIgnoreCase(String.valueOf(details.get("Size")), searchValue);
                case "usage" ->
                    containsIgnoreCase(String.valueOf(details.get("Usage")), searchValue);
                case "trim" ->
                    containsIgnoreCase(String.valueOf(details.get("Trim")), searchValue);
                default ->
                    false;
            };
        });
    }

    private boolean containsIgnoreCase(String source, String search) {
        if (source == null || search == null || source.equals("null")) {
            return false;
        }
        return source.toLowerCase().contains(search);
    }
}

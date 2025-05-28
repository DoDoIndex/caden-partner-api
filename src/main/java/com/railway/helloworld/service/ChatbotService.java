package com.railway.helloworld.service;

import com.railway.helloworld.model.Product;
import com.railway.helloworld.model.ChatRequest;
import com.railway.helloworld.model.ChatMessage;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.service.OpenAiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.jdbc.core.JdbcTemplate;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class ChatbotService {

    private static final Logger logger = LoggerFactory.getLogger(ChatbotService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private OpenAiService openAiService;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String SYSTEM_PROMPT = """
        You are a helpful tile shopping assistant. Extract search criteria from user messages or handle bookmark/collection actions.
        
        For greetings (like "hello", "hi", "hey"), output format should be:
        {
            "action": "greeting",
            "response": "Hello! I'm your tile shopping assistant. I can help you with:\n" +
                       "1. Searching for tiles by material, color, size, usage, or trim\n" +
                       "2. Managing your bookmarks\n" +
                       "3. Creating and managing collections\n" +
                       "Just let me know what you're looking for!"
        }
        
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
        
        For combined search and bookmark actions, output format should be:
        {
            "action": "search_and_bookmark",
            "criteria": [
                {
                    "searchType": one of ["Material", "Color Group", "Size", "Usage", "Trim"],
                    "searchValue": the value to search for
                },
                // ... more criteria if present
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add these tiles to a collection? You can choose an existing collection or create a new one."
        }
        
        For combined search, bookmark, and collection actions, output format should be:
        {
            "action": "search_bookmark_collection",
            "criteria": [
                {
                    "searchType": one of ["Material", "Color Group", "Size", "Usage", "Trim"],
                    "searchValue": the value to search for
                },
                // ... more criteria if present
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "collectionName": "name of the collection",
            "collectionResponse": "I've created a new collection and added these tiles to it!"
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
        
        For direct product name queries, output format should be:
        {
            "action": "search_and_bookmark",
            "criteria": [
                {"searchType": "Product Name", "searchValue": "Ares Ivory Matt Polished 35×35 Field"}
            ],
            "bookmarkResponse": "I've added this tile to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add this tile to a collection? You can choose an existing collection or create a new one."
        }
        
        User: "Add Ares Ivory Matt Polished 35×35 Field to bookmark"
        Output: {
            "action": "search_and_bookmark",
            "criteria": [
                {"searchType": "Product Name", "searchValue": "Ares Ivory Matt Polished 35×35 Field"}
            ],
            "bookmarkResponse": "I've added this tile to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add this tile to a collection? You can choose an existing collection or create a new one."
        }
        
        Examples:
        User: "Hello"
        Output: {
            "action": "greeting",
            "response": "Hello! I'm your tile shopping assistant. I can help you with:\n" +
                       "1. Searching for tiles by material, color, size, usage, or trim\n" +
                       "2. Managing your bookmarks\n" +
                       "3. Creating and managing collections\n" +
                       "Just let me know what you're looking for!"
        }
        
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
        
        User: "Show me blue tiles and add to bookmark"
        Output: {
            "action": "search_and_bookmark",
            "criteria": [
                {"searchType": "Color Group", "searchValue": "Blue"}
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add these tiles to a collection? You can choose an existing collection or create a new one."
        }
        
        User: "Add blue tiles to bookmark"
        Output: {
            "action": "search_and_bookmark",
            "criteria": [
                {"searchType": "Color Group", "searchValue": "Blue"}
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add these tiles to a collection? You can choose an existing collection or create a new one."
        }
        
        User: "Find blue tiles and add to bookmark"
        Output: {
            "action": "search_and_bookmark",
            "criteria": [
                {"searchType": "Color Group", "searchValue": "Blue"}
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "askCollection": true,
            "collectionPrompt": "Would you like to add these tiles to a collection? You can choose an existing collection or create a new one."
        }
        
        User: "Add green tiles to bookmark and create collection named 'ABCD'"
        Output: {
            "action": "search_bookmark_collection",
            "criteria": [
                {"searchType": "Color Group", "searchValue": "Green"}
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "collectionName": "ABCD",
            "collectionResponse": "I've created a new collection and added these tiles to it!"
        }
        
        User: "Find me bathroom tiles, bookmark them, and create a collection called 'Bathroom Ideas'"
        Output: {
            "action": "search_bookmark_collection",
            "criteria": [
                {"searchType": "Usage", "searchValue": "Bathroom"}
            ],
            "bookmarkResponse": "I've added these tiles to your bookmarks successfully!",
            "collectionName": "Bathroom Ideas",
            "collectionResponse": "I've created a new collection and added these tiles to it!"
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
        // Initialize default values
        response.put("products", new ArrayList<>());
        response.put("error", false);
        response.put("action", "unknown");

        try {
            if (userMessage == null || userMessage.getMessage() == null) {
                throw new IllegalArgumentException("User message cannot be null");
            }

            List<com.theokanning.openai.completion.chat.ChatMessage> messages = new ArrayList<>();
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("system", SYSTEM_PROMPT));
            messages.add(new com.theokanning.openai.completion.chat.ChatMessage("user", userMessage.getMessage()));

            logger.debug("Sending request to OpenAI with messages: {}", messages);

            ChatCompletionRequest completionRequest = ChatCompletionRequest.builder()
                    .model("gpt-3.5-turbo")
                    .messages(messages)
                    .build();

            String gptResponse = openAiService.createChatCompletion(completionRequest)
                    .getChoices().get(0).getMessage().getContent();

            logger.debug("Received response from OpenAI: {}", gptResponse);

            Map<String, Object> parsedResponse = objectMapper.readValue(gptResponse, Map.class);
            logger.debug("Parsed response: {}", parsedResponse);

            String action = (String) parsedResponse.get("action");
            response.put("action", action != null ? action : "unknown");

            if ("greeting".equals(action)) {
                response.put("message", parsedResponse.get("response"));
            } else if ("search".equals(action)) {
                List<Map<String, String>> criteria = (List<Map<String, String>>) parsedResponse.get("criteria");
                if (criteria == null) {
                    throw new IllegalArgumentException("Search criteria cannot be null");
                }
                List<Product> products = searchProductsWithMultipleCriteria(criteria);
                response.put("message", buildResponseMessage(criteria, products.size()));
                response.put("products", products != null ? products : new ArrayList<>());
            } else if ("search_and_bookmark".equals(action)) {
                List<Map<String, String>> criteria = (List<Map<String, String>>) parsedResponse.get("criteria");
                if (criteria == null) {
                    throw new IllegalArgumentException("Search criteria cannot be null");
                }
                List<Product> products = searchProductsWithMultipleCriteria(criteria);

                // Check if the search is by Product Name
                boolean isProductNameSearch = criteria.size() == 1 && "product name".equalsIgnoreCase(criteria.get(0).get("searchType"));
                String productName = isProductNameSearch ? criteria.get(0).get("searchValue") : null;

                if (isProductNameSearch && (products == null || products.isEmpty())) {
                    // Only show the bookmark message with the product name
                    response.put("message", "I've added " + productName + " to your bookmarks successfully!");
                } else {
                    // Default behavior
                    response.put("message", buildResponseMessage(criteria, products.size()) + "\n" + parsedResponse.get("bookmarkResponse"));
                }
                response.put("products", products != null ? products : new ArrayList<>());
                response.put("askCollection", parsedResponse.get("askCollection"));
                response.put("collectionPrompt", parsedResponse.get("collectionPrompt"));
            } else if ("search_bookmark_collection".equals(action)) {
                List<Map<String, String>> criteria = (List<Map<String, String>>) parsedResponse.get("criteria");
                if (criteria == null) {
                    throw new IllegalArgumentException("Search criteria cannot be null");
                }
                List<Product> products = searchProductsWithMultipleCriteria(criteria);
                String collectionName = (String) parsedResponse.get("collectionName");
                response.put("message", buildResponseMessage(criteria, products.size()) + "\n"
                        + parsedResponse.get("bookmarkResponse") + "\n"
                        + "Collection '" + collectionName + "': " + parsedResponse.get("collectionResponse"));
                response.put("products", products != null ? products : new ArrayList<>());
                response.put("collectionName", collectionName);
            } else if ("bookmark".equals(action)) {
                response.put("message", parsedResponse.get("response"));
                response.put("askCollection", parsedResponse.get("askCollection"));
                response.put("collectionPrompt", parsedResponse.get("collectionPrompt"));
            } else if ("collection".equals(action)) {
                response.put("message", parsedResponse.get("prompt"));
            } else {
                response.put("message", parsedResponse.get("response"));
            }

        } catch (IllegalArgumentException e) {
            logger.error("Invalid input: {}", e.getMessage());
            response.put("message", "Invalid input: " + e.getMessage());
            response.put("error", true);
            response.put("action", "error");
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            logger.error("Failed to parse OpenAI response: {}", e.getMessage());
            response.put("message", "Failed to process the response. Please try again.");
            response.put("error", true);
            response.put("action", "error");
        } catch (Exception e) {
            logger.error("Unexpected error processing message: {}", e.getMessage(), e);
            response.put("message", "I encountered an error processing your request. Could you please try again?");
            response.put("error", true);
            response.put("action", "error");
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
        String sql = "SELECT product_id, product_details FROM tiles";
        try {
            List<Product> products = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Product product = new Product();
                product.setProductId(rs.getInt("product_id"));
                String detailsJson = rs.getString("product_details");
                try {
                    Map<String, Object> details = objectMapper.readValue(detailsJson,
                            new com.fasterxml.jackson.core.type.TypeReference<Map<String, Object>>() {
                    });
                    product.setProductDetails(details);
                } catch (Exception ex) {
                    logger.error("Error parsing product_details JSON for product_id " + rs.getInt("product_id") + ": " + ex.getMessage());
                    product.setProductDetails(null);
                }
                return product;
            });

            return products.stream()
                    .filter(product -> matchesAllCriteria(product, criteria))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            logger.error("Error occurred while fetching tiles: " + e.getMessage());
            return List.of();
        }
    }

    // Check if a product matches all the criteria
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
                case "product name" ->
                    containsIgnoreCase(String.valueOf(details.get("Product Name")), searchValue);
                default ->
                    false;
            };
        });
    }

    // Check if a string contains another string, ignoring case
    private boolean containsIgnoreCase(String source, String search) {
        if (source == null || search == null || source.equals("null")) {
            return false;
        }
        return source.toLowerCase().contains(search);
    }
}

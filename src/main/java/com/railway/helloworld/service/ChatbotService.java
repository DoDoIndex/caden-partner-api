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
You are a helpful tile shopping assistant. Always reply in JSON using one of these actions: greeting, thank_you, search, search_and_bookmark, search_bookmark_collection, bookmark, collection, or null.

**Output formats:**
- greeting: {\"action\": \"greeting\", \"response\": \"...\"}
- thank_you: {\"action\": \"thank_you\", \"response\": \"...\"}
- search: {\"action\": \"search\", \"criteria\": [{\"searchType\": \"...\", \"searchValue\": \"...\"}]}
- search_and_bookmark: {\"action\": \"search_and_bookmark\", \"criteria\": [...], \"bookmarkResponse\": \"...\", \"askCollection\": true, \"collectionPrompt\": \"...\"}
- search_bookmark_collection: {\"action\": \"search_bookmark_collection\", \"criteria\": [...], \"bookmarkResponse\": \"...\", \"collectionName\": \"...\", \"collectionResponse\": \"...\"}
- bookmark: {\"action\": \"bookmark\", \"response\": \"...\", \"askCollection\": true, \"collectionPrompt\": \"...\"}
- collection: {\"action\": \"collection\", \"prompt\": \"...\"}
- If you can't determine the action: {\"action\": null, \"response\": \"I'm not sure what you'd like to do. Could you please rephrase that?\"}

**Instructions:**
- For greetings (e.g., \"hello\", \"hi\", \"hey\"), use the greeting format.
- For thanks (e.g., \"thank you\", \"thanks\"), use the thank_you format.
- For search queries, extract criteria as a list of {\"searchType\", \"searchValue\"}.
- For requests to bookmark or add to collection, use the appropriate format above.
- For direct product name queries, treat as search_and_bookmark with searchType \"Product Name\".
- If a collection name is missing, prompt for it.
- **For collection creation:**  
  - If the user says \"Create collection named 'ABCD'\" or \"Create collection 'ABCD'\", treat both as a request to create a collection.  
  - Respond with: {\"action\": \"collection\", \"prompt\": \"Collection 'ABCD' has been created.\"}
- For requests like \"Add [criteria] to bookmark\" or \"Find [criteria] and add to bookmark\", first search for tiles matching the criteria, then add those tiles to the bookmark. Respond using the search_and_bookmark action, including the search criteria and a confirmation message.
- **For prompts like 'Find [criteria] and create collection (named) "ABCD"', search for tiles, add them to bookmarks, and create collection "ABCD" with those tiles. Respond using the search_bookmark_collection action.**
- Always return valid JSON in a single line (no unescaped newlines).

**Examples:**
User: \"Create collection named 'ABCD'\" → {\"action\": \"collection\", \"prompt\": \"Collection 'ABCD' has been created.\"}
User: \"Create collection 'ABCD'\" → {\"action\": \"collection\", \"prompt\": \"Collection 'ABCD' has been created.\"}
User: \"Show me blue tiles\" → {\"action\": \"search\", \"criteria\": [{\"searchType\": \"Color Group\", \"searchValue\": \"Blue\"}]}
User: \"Add blue tiles to bookmark\" → {\"action\": \"search_and_bookmark\", \"criteria\": [{\"searchType\": \"Color Group\", \"searchValue\": \"Blue\"}], \"bookmarkResponse\": \"I've added these tiles to your bookmarks successfully!\", \"askCollection\": true, \"collectionPrompt\": \"Would you like to add these tiles to a collection? You can choose an existing collection or create a new one.\"}
User: \"Add green tiles to bookmark and create collection named 'ABCD'\" → {\"action\": \"search_bookmark_collection\", \"criteria\": [{\"searchType\": \"Color Group\", \"searchValue\": \"Green\"}], \"bookmarkResponse\": \"I've added these tiles to your bookmarks successfully!\", \"collectionName\": \"ABCD\", \"collectionResponse\": \"I've created a new collection and added these tiles to it!\"}
User: \"Find blue tiles and create collection 'ABCD'\" → {\"action\": \"search_bookmark_collection\", \"criteria\": [{\"searchType\": \"Color Group\", \"searchValue\": \"Blue\"}], \"bookmarkResponse\": \"I've added these tiles to your bookmarks successfully!\", \"collectionName\": \"ABCD\", \"collectionResponse\": \"I've created a new collection and added these tiles to it!\"}
User: \"Add to bookmark\" → {\"action\": \"bookmark\", \"response\": \"I've added these tiles to your bookmarks successfully!\", \"askCollection\": true, \"collectionPrompt\": \"Would you like to add these tiles to a collection? You can choose an existing collection or create a new one.\"}
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

            // Extract only the JSON part from the response
            String jsonPart = extractJson(gptResponse);
            // Replace all newlines with spaces to avoid JSON parsing errors
            jsonPart = jsonPart.replace("\n", " ");
            logger.debug("Extracted JSON for parsing: {}", jsonPart);
            Map<String, Object> parsedResponse = objectMapper.readValue(jsonPart, Map.class);
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
                // For each product, set 'Image' to the first image URL from 'Images', then remove 'Images'
                for (Product product : products) {
                    Map<String, Object> details = product.getProductDetails();
                    if (details != null) {
                        Object imagesObj = details.get("Images");
                        if (imagesObj instanceof String imagesStr && !imagesStr.isEmpty()) {
                            String firstImage = imagesStr.split("\\n")[0];
                            details.put("Image", firstImage);
                        }
                        details.remove("Images");
                    }
                }
                response.put("message", buildResponseMessage(criteria, products.size()));
                response.put("products", products != null ? products : new ArrayList<>());
            } else if ("search_and_bookmark".equals(action)) {
                List<Map<String, String>> criteria = (List<Map<String, String>>) parsedResponse.get("criteria");
                if (criteria == null) {
                    throw new IllegalArgumentException("Search criteria cannot be null");
                }
                List<Product> products = searchProductsWithMultipleCriteria(criteria);
                // For each product, set 'Image' to the first image URL from 'Images', then remove 'Images'
                for (Product product : products) {
                    Map<String, Object> details = product.getProductDetails();
                    if (details != null) {
                        Object imagesObj = details.get("Images");
                        if (imagesObj instanceof String imagesStr && !imagesStr.isEmpty()) {
                            String firstImage = imagesStr.split("\\n")[0];
                            details.put("Image", firstImage);
                        }
                        details.remove("Images");
                    }
                }

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
                // For each product, set 'Image' to the first image URL from 'Images', then remove 'Images'
                for (Product product : products) {
                    Map<String, Object> details = product.getProductDetails();
                    if (details != null) {
                        Object imagesObj = details.get("Images");
                        if (imagesObj instanceof String imagesStr && !imagesStr.isEmpty()) {
                            String firstImage = imagesStr.split("\\n")[0];
                            details.put("Image", firstImage);
                        }
                        details.remove("Images");
                    }
                }
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

    private String extractJson(String response) {
        // Implement the logic to extract only the JSON part from the response
        // This is a placeholder and should be replaced with the actual implementation
        return response;
    }
}

package com.railway.helloworld.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/design")
@CrossOrigin(origins = "*")
public class DesignController {

    @Value("${app.design.api.url}")
    private String designApiUrl;

    private final RestTemplate restTemplate;

    public DesignController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @PostMapping("/replace-tile")
    public ResponseEntity<?> replaceTile(
            @RequestParam("original") MultipartFile original,
            @RequestParam("tile") MultipartFile tile,
            @RequestParam("point_x") String pointX,
            @RequestParam("point_y") String pointY) {
        try {
            // Create headers
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            // Create multipart request body
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("original", new ByteArrayResource(original.getBytes()) {
                @Override
                public String getFilename() {
                    return original.getOriginalFilename();
                }
            });
            body.add("tile", new ByteArrayResource(tile.getBytes()) {
                @Override
                public String getFilename() {
                    return tile.getOriginalFilename();
                }
            });
            body.add("point_x", pointX);
            body.add("point_y", pointY);

            // Create the request entity
            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);

            // Make the request to the design API .
            ResponseEntity<byte[]> response = restTemplate.exchange(
                    designApiUrl + "/api/replace-tile/",
                    HttpMethod.POST,
                    requestEntity,
                    byte[].class
            );

            // Check if the response is an image
            if (response.getHeaders().getContentType() != null
                    && response.getHeaders().getContentType().toString().startsWith("image/")) {
                return ResponseEntity.ok()
                        .contentType(response.getHeaders().getContentType())
                        .body(response.getBody());
            }

            // If not an image, return as JSON
            return ResponseEntity.ok(response.getBody());

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to process the request: " + e.getMessage());
        }
    }
}

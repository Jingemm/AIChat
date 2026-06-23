package com.aichat.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Component
public class LlmUtil {
    @Value("${ai.llm.api-key}")
    private String apiKey;
    @Value("${ai.llm.model}")
    private String model;
    @Value("${ai.llm.url}")
    private String url;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    public String chat(List<Map<String, String>> messages) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.setBearerAuth(apiKey);

            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            ArrayNode msgs = body.putArray("messages");
            for (Map<String, String> msg : messages) {
                ObjectNode m = msgs.addObject();
                m.put("role", msg.get("role"));
                m.put("content", msg.get("content"));
            }

            HttpEntity<String> request = new HttpEntity<>(mapper.writeValueAsString(body), headers);
            ResponseEntity<String> response = restTemplate.postForEntity(url, request, String.class);
            JsonNode root = mapper.readTree(response.getBody());
            return root.path("choices").get(0).path("message").path("content").asText();
        } catch (Exception e) {
            return "大模型暂时不可用，请稍后再试。";
        }
    }
}
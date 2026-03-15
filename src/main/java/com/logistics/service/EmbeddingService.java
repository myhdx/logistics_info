package com.logistics.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${embedding.baseUrl:}")
    private String baseUrl;

    @Value("${embedding.apiKey:}")
    private String apiKey;

    @Value("${embedding.model:}")
    private String model;

    /**
     * 生成文本向量
     */
    public float[] embed(String text) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", text);

            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.set("Authorization", "Bearer " + apiKey);
            headers.set("Content-Type", "application/json");

            org.springframework.http.HttpEntity<Map<String, Object>> request = 
                new org.springframework.http.HttpEntity<>(requestBody, headers);

            String response = restTemplate.postForObject(
                baseUrl + "/embeddings",
                request,
                String.class
            );

            Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
            List<Map<String, Object>> data = (List<Map<String, Object>>) responseMap.get("data");
            if (data != null && !data.isEmpty()) {
                Map<String, Object> embeddingData = data.get(0);
                List<Number> embedding = (List<Number>) embeddingData.get("embedding");
                float[] result = new float[embedding.size()];
                for (int i = 0; i < embedding.size(); i++) {
                    result[i] = embedding.get(i).floatValue();
                }
                return result;
            }
        } catch (Exception e) {
            log.error("生成Embedding失败: {}", text, e);
        }
        
        return null;
    }

    /**
     * 批量生成向量
     */
    public List<float[]> embedBatch(List<String> texts) {
        List<float[]> results = new ArrayList<>();
        for (String text : texts) {
            float[] embedding = embed(text);
            if (embedding != null) {
                results.add(embedding);
            }
        }
        return results;
    }
}
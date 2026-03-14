package com.logistics.service;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.entity.ChatSession;
import com.logistics.entity.KnowledgeChunk;
import com.logistics.mapper.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeChatService {

    private final ChatSessionMapper chatSessionMapper;
    private final KnowledgeService knowledgeService;
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${llm.baseUrl:}")
    private String llmBaseUrl;

    @Value("${llm.apiKey:}")
    private String llmApiKey;

    @Value("${llm.model:}")
    private String llmModel;

    private static final int MAX_HISTORY = 5; // 最近5轮对话

    /**
     * AI 对话（知识库问答）
     */
    public String chat(Long userId, String sessionId, String userMessage) {
        // 1. 获取历史对话
        List<ChatSession> history = getHistory(userId, sessionId);
        
        // 2. 检索相关知识
        List<KnowledgeChunk> relatedChunks = knowledgeService.searchRelated(userMessage, 3);
        String knowledgeContext = relatedChunks.stream()
            .map(KnowledgeChunk::getContent)
            .collect(Collectors.joining("\n\n"));

        // 3. 构建Prompt
        String systemPrompt = buildSystemPrompt(knowledgeContext);
        List<Map<String, String>> messages = buildMessages(history, userMessage, systemPrompt);

        // 4. 调用LLM
        String aiReply;
        try {
            aiReply = callLLM(messages);
        } catch (Exception e) {
            log.error("调用LLM失败", e);
            aiReply = "抱歉，AI服务暂时不可用，请稍后重试。";
        }

        // 5. 保存对话
        saveChat(userId, sessionId, "user", userMessage);
        saveChat(userId, sessionId, "assistant", aiReply);

        return aiReply;
    }

    private String buildSystemPrompt(String knowledgeContext) {
        return """
            你是物流知识问答助手，专门帮助用户解答物流相关问题。
            请根据提供的知识库内容回答用户的问题。
            如果知识库中没有相关信息，请明确告知用户"暂无该信息，可联系管理员"。
            
            知识库内容：
            """ + knowledgeContext;
    }

    private List<Map<String, String>> buildMessages(List<ChatSession> history, String currentMsg, String systemPrompt) {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // System
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", systemPrompt);
        messages.add(system);
        
        // History
        for (ChatSession session : history) {
            Map<String, String> msg = new HashMap<>();
            msg.put("role", session.getRole());
            msg.put("content", session.getContent());
            messages.add(msg);
        }
        
        // Current
        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", currentMsg);
        messages.add(user);
        
        return messages;
    }

    private List<ChatSession> getHistory(Long userId, String sessionId) {
        return chatSessionMapper.selectList(
            new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getUserId, userId)
                .eq(ChatSession::getSessionId, sessionId)
                .orderByAsc(ChatSession::getCreateTime)
                .last("LIMIT " + MAX_HISTORY)
        );
    }

    private void saveChat(Long userId, String sessionId, String role, String content) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setSessionId(sessionId);
        session.setRole(role);
        session.setContent(content);
        session.setCreateTime(LocalDateTime.now());
        chatSessionMapper.insert(session);
    }

    private String callLLM(List<Map<String, String>> messages) throws Exception {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", llmModel);
        requestBody.put("messages", messages);
        requestBody.put("temperature", 0.7);

        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.set("Authorization", "Bearer " + llmApiKey);
        headers.set("Content-Type", "application/json");

        org.springframework.http.HttpEntity<Map<String, Object>> request = 
            new org.springframework.http.HttpEntity<>(requestBody, headers);

        String response = restTemplate.postForObject(
            llmBaseUrl + "/chat/completions",
            request,
            String.class
        );

        Map<String, Object> responseMap = objectMapper.readValue(response, Map.class);
        List<Map<String, Object>> choices = (List<Map<String, Object>>) responseMap.get("choices");
        if (choices != null && !choices.isEmpty()) {
            Map<String, Object> message = (Map<String, Object>) choices.get(0).get("message");
            return (String) message.get("content");
        }
        
        return "抱歉，无法生成回答。";
    }
}
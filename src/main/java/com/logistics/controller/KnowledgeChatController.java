package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.service.KnowledgeChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeChatController {

    private final KnowledgeChatService knowledgeChatService;

    /**
     * AI 对话（知识库问答）
     */
    @PostMapping("/chat")
    public Result<Map<String, String>> chat(@RequestBody Map<String, String> params) {
        Long userId = Long.parseLong(params.get("userId"));
        String sessionId = params.get("sessionId");
        String message = params.get("message");

        if (userId == null || sessionId == null || message == null) {
            return Result.error("参数不完整");
        }

        String reply = knowledgeChatService.chat(userId, sessionId, message);

        Map<String, String> result = Map.of(
            "reply", reply,
            "sessionId", sessionId
        );

        return Result.success(result);
    }
}
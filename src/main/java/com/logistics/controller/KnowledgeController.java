package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.entity.KnowledgeDocument;
import com.logistics.entity.KnowledgeChunk;
import com.logistics.service.KnowledgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
@RequiredArgsConstructor
public class KnowledgeController {

    private final KnowledgeService knowledgeService;

    /**
     * 导入文档（PDF/Word/TXT）
     */
    @PostMapping("/import")
    public Result<KnowledgeDocument> importDocument(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("文件不能为空");
        }

        String fileName = file.getOriginalFilename();
        if (fileName == null || !fileName.matches(".*\\.(pdf|docx|txt)$")) {
            return Result.error("仅支持 PDF/Word/TXT 格式");
        }

        try {
            KnowledgeDocument doc = knowledgeService.importDocument(file);
            return Result.success(doc);
        } catch (Exception e) {
            return Result.error("导入失败: " + e.getMessage());
        }
    }

    /**
     * 向量检索相关段落
     */
    @GetMapping("/search")
    public Result<List<KnowledgeChunk>> search(
            @RequestParam String query,
            @RequestParam(defaultValue = "3") Integer topK) {
        List<KnowledgeChunk> chunks = knowledgeService.searchRelated(query, topK);
        return Result.success(chunks);
    }
}
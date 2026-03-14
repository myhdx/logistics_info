package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.logistics.entity.KnowledgeDocument;
import com.logistics.entity.KnowledgeChunk;
import com.logistics.mapper.KnowledgeDocumentMapper;
import com.logistics.mapper.KnowledgeChunkMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class KnowledgeService {

    private final KnowledgeDocumentMapper documentMapper;
    private final KnowledgeChunkMapper chunkMapper;

    @Value("${file.upload.path:/tmp/logistics-upload}")
    private String uploadPath;

    private static final int CHUNK_SIZE = 500; // 每个段落500字

    /**
     * 导入文档（PDF/Word/TXT）
     */
    public KnowledgeDocument importDocument(MultipartFile file) throws IOException {
        // 1. 保存文件
        String fileName = file.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String newName = UUID.randomUUID().toString() + suffix;
        java.nio.file.Path path = java.nio.file.Paths.get(uploadPath, newName);
        java.nio.file.Files.createDirectories(path.getParent());
        java.nio.file.Files.write(path, file.getBytes());

        // 2. 创建文档记录
        KnowledgeDocument doc = new KnowledgeDocument();
        doc.setTitle(fileName);
        doc.setFileName(newName);
        doc.setFileType(suffix.toLowerCase());
        doc.setFileUrl(path.toString());
        doc.setStatus("processing");
        doc.setChunkCount(0);
        doc.setCreateTime(LocalDateTime.now());
        doc.setUpdateTime(LocalDateTime.now());
        documentMapper.insert(doc);

        // 3. 异步处理文档内容（解析、分段、向量化）
        try {
            processDocument(doc);
            doc.setStatus("done");
        } catch (Exception e) {
            log.error("处理文档失败: docId={}", doc.getId(), e);
            doc.setStatus("error");
        }
        
        documentMapper.updateById(doc);
        return doc;
    }

    private void processDocument(KnowledgeDocument doc) throws IOException {
        String content;
        
        if (".pdf".equals(doc.getFileType())) {
            content = parsePdf(doc.getFileUrl());
        } else if (".docx".equals(doc.getFileType())) {
            content = parseDocx(doc.getFileUrl());
        } else if (".txt".equals(doc.getFileType())) {
            content = java.nio.file.Files.readString(java.nio.file.Paths.get(doc.getFileUrl()));
        } else {
            throw new IOException("不支持的文件类型: " + doc.getFileType());
        }

        // 分段
        List<String> chunks = splitIntoChunks(content);
        
        // 保存段落
        for (String chunk : chunks) {
            KnowledgeChunk knowledgeChunk = new KnowledgeChunk();
            knowledgeChunk.setDocId(doc.getId());
            knowledgeChunk.setContent(chunk);
            knowledgeChunk.setCreateTime(LocalDateTime.now());
            chunkMapper.insert(knowledgeChunk);
        }

        doc.setChunkCount(chunks.size());
    }

    private String parsePdf(String filePath) throws IOException {
        try (PDDocument doc = Loader.loadPDF(java.nio.file.Paths.get(filePath).toFile())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(doc);
        }
    }

    private String parseDocx(String filePath) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (InputStream is = java.nio.file.Files.newInputStream(java.nio.file.Paths.get(filePath));
             XWPFDocument doc = new XWPFDocument(is)) {
            for (XWPFParagraph para : doc.getParagraphs()) {
                sb.append(para.getText()).append("\n");
            }
        }
        return sb.toString();
    }

    private List<String> splitIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        int length = content.length();
        
        for (int i = 0; i < length; i += CHUNK_SIZE) {
            int end = Math.min(i + CHUNK_SIZE, length);
            String chunk = content.substring(i, end).trim();
            if (!chunk.isEmpty()) {
                chunks.add(chunk);
            }
        }
        
        return chunks;
    }

    /**
     * 向量检索相关段落
     */
    public List<KnowledgeChunk> searchRelated(String query, int topK) {
        // TODO: 调用Embedding服务向量化查询，再使用pgvector相似度搜索
        // 简化版：直接返回最近的段落
        return chunkMapper.selectList(
            new LambdaQueryWrapper<KnowledgeChunk>()
                .orderByDesc(KnowledgeChunk::getCreateTime)
                .last("LIMIT " + topK)
        );
    }
}
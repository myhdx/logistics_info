package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.service.LogisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/logistics")
@RequiredArgsConstructor
public class LogisticsController {

    private final LogisticsService logisticsService;

    /**
     * 查询物流轨迹
     */
    @GetMapping("/query")
    public Result<Map<String, Object>> query(
            @RequestParam String logisticsNo,
            @RequestParam(required = false) String logisticsCompany) {
        Map<String, Object> result = logisticsService.queryLogistics(logisticsNo, logisticsCompany);
        return Result.success(result);
    }

    /**
     * 获取发货方联系信息
     */
    @GetMapping("/company")
    public Result<Map<String, Object>> company(@RequestParam String logisticsNo) {
        Map<String, Object> result = logisticsService.getSenderInfo(logisticsNo);
        return Result.success(result);
    }
}
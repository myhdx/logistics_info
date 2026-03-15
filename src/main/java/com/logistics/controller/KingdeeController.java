package com.logistics.controller;

import com.logistics.common.Result;
import com.logistics.service.KingdeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/erp")
@RequiredArgsConstructor
public class KingdeeController {

    private final KingdeeService kingdeeService;

    /**
     * 从金蝶同步发货单到本地
     */
    @PostMapping("/sync")
    public Result<String> sync(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        
        if (startDate == null) {
            startDate = LocalDateTime.now().minusDays(30);
        }
        if (endDate == null) {
            endDate = LocalDateTime.now();
        }

        // 这里调用同步服务（待实现）
        return Result.success("同步任务已启动，请稍后查询");
    }

    /**
     * 直接查询金蝶发货单（实时）
     */
    @GetMapping("/delivery")
    public Result<List<Map<String, Object>>> queryDelivery(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "20") Integer pageSize) {
        
        List<Map<String, Object>> list = kingdeeService.queryDeliveryOrders(startDate, endDate, keyword, page, pageSize);
        return Result.success(list);
    }

    /**
     * 根据单号查询金蝶发货单详情
     */
    @GetMapping("/delivery/{orderNo}")
    public Result<Map<String, Object>> getDelivery(@PathVariable String orderNo) {
        Map<String, Object> order = kingdeeService.getDeliveryOrderByNo(orderNo);
        if (order == null) {
            return Result.error("未找到发货单");
        }
        return Result.success(order);
    }

    /**
     * 查询金蝶物流信息
     */
    @GetMapping("/logistics/{logisticsNo}")
    public Result<Map<String, Object>> getLogistics(@PathVariable String logisticsNo) {
        Map<String, Object> info = kingdeeService.getLogisticsInfo(logisticsNo);
        if (info == null) {
            return Result.error("未找到物流信息");
        }
        return Result.success(info);
    }
}
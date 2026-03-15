package com.logistics.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.common.Result;
import com.logistics.entity.DeliveryOrder;
import com.logistics.service.DeliveryOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/delivery")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryOrderService deliveryOrderService;

    /**
     * 发货单列表（分页+搜索）
     */
    @GetMapping("/list")
    public Result<Page<DeliveryOrder>> list(
            @RequestParam(defaultValue = "1") Integer pageNum,
            @RequestParam(defaultValue = "10") Integer pageSize,
            @RequestParam(required = false) String keyword) {
        Page<DeliveryOrder> page = deliveryOrderService.listPage(pageNum, pageSize, keyword);
        return Result.success(page);
    }

    /**
     * 发货单详情
     */
    @GetMapping("/{id}")
    public Result<DeliveryOrder> detail(@PathVariable Long id) {
        DeliveryOrder order = deliveryOrderService.getById(id);
        if (order == null) {
            return Result.error("发货单不存在");
        }
        return Result.success(order);
    }

    /**
     * 关键字模糊搜索（简化查询）
     */
    @GetMapping("/search")
    public Result<List<DeliveryOrder>> search(@RequestParam String keyword) {
        List<DeliveryOrder> list = deliveryOrderService.search(keyword);
        return Result.success(list);
    }
}
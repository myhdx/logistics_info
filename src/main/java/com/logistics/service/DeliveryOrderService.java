package com.logistics.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.logistics.entity.DeliveryOrder;
import com.logistics.mapper.DeliveryOrderMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DeliveryOrderService {

    private final DeliveryOrderMapper deliveryOrderMapper;

    /**
     * 分页查询发货单列表
     */
    public Page<DeliveryOrder> listPage(Integer pageNum, Integer pageSize, String keyword) {
        Page<DeliveryOrder> page = new Page<>(pageNum, pageSize);
        
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DeliveryOrder::getDeliveryDate);
        
        // 默认查最近30天
        wrapper.ge(DeliveryOrder::getDeliveryDate, LocalDate.now().minusDays(30));
        
        // 关键字搜索（单号、客户名、商品名称）
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w
                .like(DeliveryOrder::getOrderNo, keyword)
                .or().like(DeliveryOrder::getCustomerName, keyword)
                .or().like(DeliveryOrder::getProductName, keyword)
            );
        }
        
        return deliveryOrderMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询发货单详情
     */
    public DeliveryOrder getById(Long id) {
        return deliveryOrderMapper.selectById(id);
    }

    /**
     * 根据单号查询
     */
    public DeliveryOrder getByOrderNo(String orderNo) {
        return deliveryOrderMapper.selectOne(
            new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getOrderNo, orderNo)
        );
    }

    /**
     * 根据物流单号查询
     */
    public DeliveryOrder getByLogisticsNo(String logisticsNo) {
        return deliveryOrderMapper.selectOne(
            new LambdaQueryWrapper<DeliveryOrder>()
                .eq(DeliveryOrder::getLogisticsNo, logisticsNo)
        );
    }

    /**
     * 关键字模糊搜索（简化查询用）
     */
    public List<DeliveryOrder> search(String keyword) {
        LambdaQueryWrapper<DeliveryOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(DeliveryOrder::getDeliveryDate);
        wrapper.ge(DeliveryOrder::getDeliveryDate, LocalDate.now().minusDays(30));
        wrapper.and(w -> w
            .like(DeliveryOrder::getOrderNo, keyword)
            .or().like(DeliveryOrder::getCustomerName, keyword)
            .or().like(DeliveryOrder::getProductName, keyword)
            .or().like(DeliveryOrder::getDeliveryAddress, keyword)
        );
        wrapper.last("LIMIT 20");
        
        return deliveryOrderMapper.selectList(wrapper);
    }
}
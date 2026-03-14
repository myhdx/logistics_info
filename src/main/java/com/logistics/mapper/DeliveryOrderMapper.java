package com.logistics.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.logistics.entity.DeliveryOrder;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface DeliveryOrderMapper extends BaseMapper<DeliveryOrder> {
}
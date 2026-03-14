package com.logistics.service;

import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.logistics.entity.DeliveryOrder;
import com.logistics.entity.LogisticsTrace;
import com.logistics.mapper.LogisticsTraceMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogisticsService {

    private final LogisticsTraceMapper logisticsTraceMapper;
    private final DeliveryOrderService deliveryOrderService;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kuaidi100.customer:}")
    private String customer;

    @Value("${kuaidi100.key:}")
    private String key;

    @Value("${kuaidi100.secret:}")
    private String secret;

    private static final String KUAIDI100_API = "https://api.kuaidi100.com";
    private static final String CACHE_PREFIX = "logistics:";
    private static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 查询物流轨迹
     * 先查缓存，再查快递100 API
     */
    public Map<String, Object> queryLogistics(String logisticsNo, String logisticsCompany) {
        // 1. 先查缓存
        String cacheKey = CACHE_PREFIX + logisticsNo;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (StrUtil.isNotBlank(cached)) {
            try {
                Map<String, Object> result = new HashMap<>();
                result.put("fromCache", true);
                result.put("data", objectMapper.readValue(cached, Object.class));
                return result;
            } catch (Exception e) {
                log.warn("解析缓存物流数据失败", e);
            }
        }

        // 2. 查数据库缓存
        LogisticsTrace trace = logisticsTraceMapper.selectOne(
            new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsNo, logisticsNo)
                .gt(LogisticsTrace::getExpireTime, LocalDateTime.now())
        );
        
        if (trace != null) {
            try {
                Map<String, Object> result = new HashMap<>();
                result.put("fromCache", true);
                result.put("data", objectMapper.readValue(trace.getTraceData(), Object.class));
                // 同步到Redis
                redisTemplate.opsForValue().set(cacheKey, trace.getTraceData(), CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                return result;
            } catch (Exception e) {
                log.warn("解析数据库缓存物流数据失败", e);
            }
        }

        // 3. 调用快递100 API
        return queryFromKuaidi100(logisticsNo, logisticsCompany, cacheKey);
    }

    private Map<String, Object> queryFromKuaidi100(String logisticsNo, String company, String cacheKey) {
        try {
            Map<String, String> params = new HashMap<>();
            params.put("customer", customer);
            params.put("param", String.format(
                "{\"com\":\"%s\",\"num\":\"%s\",\"sign\":\"%s\",\"phone\":\"\"}",
                company, logisticsNo, sign(logisticsNo, company)
            ));

            HttpResponse response = HttpRequest.post(KUAIDI100_API + "/poll/query.do")
                .form(params)
                .execute();

            if (response.isOk()) {
                String body = response.body();
                JsonNode json = objectMapper.readTree(body);
                
                if ("200".equals(json.path("status").asText())) {
                    Object data = objectMapper.readValue(body, Object.class);
                    
                    // 缓存到数据库
                    saveToCache(logisticsNo, body);
                    
                    // 缓存到Redis
                    redisTemplate.opsForValue().set(cacheKey, body, CACHE_EXPIRE_HOURS, TimeUnit.HOURS);
                    
                    Map<String, Object> result = new HashMap<>();
                    result.put("fromCache", false);
                    result.put("data", data);
                    return result;
                }
            }
        } catch (Exception e) {
            log.error("查询快递100失败: logisticsNo={}", logisticsNo, e);
        }

        // 查不到时返回发货方信息
        return getSenderInfo(logisticsNo);
    }

    private void saveToCache(String logisticsNo, String traceData) {
        LogisticsTrace trace = logisticsTraceMapper.selectOne(
            new LambdaQueryWrapper<LogisticsTrace>()
                .eq(LogisticsTrace::getLogisticsNo, logisticsNo)
        );
        
        if (trace == null) {
            trace = new LogisticsTrace();
            trace.setLogisticsNo(logisticsNo);
        }
        
        trace.setTraceData(traceData);
        trace.setQueryTime(LocalDateTime.now());
        trace.setExpireTime(LocalDateTime.now().plusHours(CACHE_EXPIRE_HOURS));
        
        if (trace.getId() == null) {
            logisticsTraceMapper.insert(trace);
        } else {
            logisticsTraceMapper.updateById(trace);
        }
    }

    /**
     * 获取发货方联系信息（查不到物流时使用）
     */
    public Map<String, Object> getSenderInfo(String logisticsNo) {
        DeliveryOrder order = deliveryOrderService.getByLogisticsNo(logisticsNo);
        
        Map<String, Object> result = new HashMap<>();
        
        if (order != null && StrUtil.isNotBlank(order.getSenderName())) {
            result.put("found", true);
            result.put("senderName", order.getSenderName());
            result.put("senderPhone", order.getSenderPhone());
            result.put("senderContact", order.getSenderContact());
            result.put("senderAddress", order.getSenderAddress());
        } else {
            result.put("found", false);
            result.put("message", "未找到发货方信息");
        }
        
        return result;
    }

    private String sign(String num, String company) {
        // 快递100签名算法: md5(customer + key + num + company)
        String str = customer + key + num + company;
        return cn.hutool.crypto.digest.MD5.create().digestHex(str);
    }
}
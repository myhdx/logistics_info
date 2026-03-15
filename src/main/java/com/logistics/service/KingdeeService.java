package com.logistics.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class KingdeeService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${kingdee.api.url:}")
    private String apiUrl;

    @Value("${kingdee.api.appId:}")
    private String appId;

    @Value("${kingdee.api.appSecret:}")
    private String appSecret;

    @Value("${kingdee.api.dcId:}")
    private String dcId;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private String accessToken = null;
    private LocalDateTime tokenExpireTime = null;

    /**
     * 获取访问令牌
     */
    public String getAccessToken() {
        // 检查缓存的token是否有效
        if (accessToken != null && tokenExpireTime != null 
            && LocalDateTime.now().isBefore(tokenExpireTime)) {
            return accessToken;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("appid", appId);
            requestBody.put("appsecret", appSecret);
            requestBody.put("dc", dcId);

            String response = restTemplate.postForObject(
                apiUrl + "/oauth2/token",
                requestBody,
                String.class
            );

            JsonNode json = objectMapper.readTree(response);
            if ("200".equals(json.path("status").asText())) {
                accessToken = json.path("data").path("access_token").asText();
                int expiresIn = json.path("data").path("expires_in").asInt(7200);
                tokenExpireTime = LocalDateTime.now().plusSeconds(expiresIn - 300); // 提前5分钟过期
                return accessToken;
            } else {
                log.error("获取金蝶Token失败: {}", response);
            }
        } catch (Exception e) {
            log.error("调用金蝶API失败", e);
        }

        return null;
    }

    /**
     * 查询发货单列表
     */
    public List<Map<String, Object>> queryDeliveryOrders(LocalDateTime startDate, LocalDateTime endDate, String keyword, int page, int pageSize) {
        String token = getAccessToken();
        if (token == null) {
            return Collections.emptyList();
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("access_token", token);
            requestBody.put("formid", "SAL_OUTSTOCK"); // 发货单表单ID
            requestBody.put("fieldkeys", "FBillNo,FCustomerName,FMaterialName,FQty,FAmount,FDeliveryDate,FDeliveryAddress,FLogisticsNo,FLogisticsCompany,FSenderName,FSenderPhone,FSenderContact,FSenderAddress");
            requestBody.put("filter", buildFilter(startDate, endDate, keyword));
            requestBody.put("page", page);
            requestBody.put("pagesize", pageSize);

            String response = restTemplate.postForObject(
                apiUrl + "/k3cloud/kingdee.bos.webapi.rest.common.cursorquery",
                requestBody,
                String.class
            );

            JsonNode json = objectMapper.readTree(response);
            if ("200".equals(json.path("status").asText())) {
                return parseDeliveryOrders(json.path("data").path("data"));
            }
        } catch (Exception e) {
            log.error("查询发货单失败", e);
        }

        return Collections.emptyList();
    }

    /**
     * 根据单号查询发货单详情
     */
    public Map<String, Object> getDeliveryOrderByNo(String orderNo) {
        String token = getAccessToken();
        if (token == null) {
            return null;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("access_token", token);
            requestBody.put("formid", "SAL_OUTSTOCK");
            requestBody.put("filter", "FBillNo = '" + orderNo + "'");

            String response = restTemplate.postForObject(
                apiUrl + "/k3cloud/kingdee.bos.webapi.rest.common.singleform",
                requestBody,
                String.class
            );

            JsonNode json = objectMapper.readTree(response);
            if ("200".equals(json.path("status").asText())) {
                return parseDeliveryOrderDetail(json.path("data"));
            }
        } catch (Exception e) {
            log.error("查询发货单详情失败: orderNo={}", orderNo, e);
        }

        return null;
    }

    /**
     * 查询物流单信息
     */
    public Map<String, Object> getLogisticsInfo(String logisticsNo) {
        String token = getAccessToken();
        if (token == null) {
            return null;
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("access_token", token);
            requestBody.put("formid", "SAL_OUTSTOCK");
            requestBody.put("filter", "FLogisticsNo = '" + logisticsNo + "'");

            String response = restTemplate.postForObject(
                apiUrl + "/k3cloud/kingdee.bos.webapi.rest.common.singleform",
                requestBody,
                String.class
            );

            JsonNode json = objectMapper.readTree(response);
            if ("200".equals(json.path("status").asText())) {
                return parseLogisticsInfo(json.path("data"));
            }
        } catch (Exception e) {
            log.error("查询物流信息失败: logisticsNo={}", logisticsNo, e);
        }

        return null;
    }

    private String buildFilter(LocalDateTime startDate, LocalDateTime endDate, String keyword) {
        List<String> conditions = new ArrayList<>();
        
        if (startDate != null) {
            conditions.add("FDeliveryDate >= datetime('" + startDate.format(FORMATTER) + "')");
        }
        if (endDate != null) {
            conditions.add("FDeliveryDate <= datetime('" + endDate.format(FORMATTER) + "')");
        }
        if (keyword != null && !keyword.isEmpty()) {
            conditions.add("(FBillNo LIKE '%" + keyword + "%' OR FCustomerName LIKE '%" + keyword + "%' OR FMaterialName LIKE '%" + keyword + "%')");
        }
        
        return conditions.isEmpty() ? "" : String.join(" AND ", conditions);
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> parseDeliveryOrders(JsonNode dataNode) {
        List<Map<String, Object>> result = new ArrayList<>();
        if (dataNode.isArray()) {
            for (JsonNode row : dataNode) {
                result.add(parseDeliveryOrderRow(row));
            }
        }
        return result;
    }

    private Map<String, Object> parseDeliveryOrderRow(JsonNode row) {
        Map<String, Object> map = new HashMap<>();
        // 根据字段顺序映射（与fieldkeys对应）
        map.put("orderNo", safeGet(row, 0));
        map.put("customerName", safeGet(row, 1));
        map.put("productName", safeGet(row, 2));
        map.put("quantity", safeGet(row, 3));
        map.put("amount", safeGet(row, 4));
        map.put("deliveryDate", safeGet(row, 5));
        map.put("deliveryAddress", safeGet(row, 6));
        map.put("logisticsNo", safeGet(row, 7));
        map.put("logisticsCompany", safeGet(row, 8));
        map.put("senderName", safeGet(row, 9));
        map.put("senderPhone", safeGet(row, 10));
        map.put("senderContact", safeGet(row, 11));
        map.put("senderAddress", safeGet(row, 12));
        return map;
    }

    private Map<String, Object> parseDeliveryOrderDetail(JsonNode dataNode) {
        if (dataNode.isArray() && dataNode.size() > 0) {
            return parseDeliveryOrderRow(dataNode.get(0));
        }
        return null;
    }

    private Map<String, Object> parseLogisticsInfo(JsonNode dataNode) {
        if (dataNode.isArray() && dataNode.size() > 0) {
            JsonNode row = dataNode.get(0);
            Map<String, Object> info = new HashMap<>();
            info.put("logisticsNo", safeGet(row, 7));
            info.put("logisticsCompany", safeGet(row, 8));
            info.put("senderName", safeGet(row, 9));
            info.put("senderPhone", safeGet(row, 10));
            info.put("senderContact", safeGet(row, 11));
            info.put("senderAddress", safeGet(row, 12));
            return info;
        }
        return null;
    }

    private String safeGet(JsonNode row, int index) {
        if (row.isArray() && index < row.size()) {
            JsonNode value = row.get(index);
            return value == null || value.isNull() ? null : value.asText();
        }
        return null;
    }
}
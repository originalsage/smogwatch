package com.smogwatch.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smogwatch.dto.LocationDTO;
import com.smogwatch.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class BaiduLocationClient {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${baidu.map.location-url}")
    private String locationUrl;

    @Value("${baidu.map.ak}")
    private String ak;

    public BaiduLocationClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public LocationDTO resolveByIp(String ip) {
        if (ip == null || ip.isBlank()) {
            throw new IllegalArgumentException("缺少客户端 IP，无法调用百度 IP 定位（未传 ip 时会解析为服务器出口 IP）");
        }
        try {
            String queryIp = "&ip=" + ip;
            String url = locationUrl + "?coor=bd09ll&ak=" + ak + queryIp;
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            if (!"0".equals(root.path("status").asText())) {
                throw new ExternalApiException("百度定位失败，status=" + root.path("status").asText());
            }
            JsonNode detail = root.path("content").path("address_detail");
            JsonNode point = root.path("content").path("point");
            LocationDTO dto = new LocationDTO();
            dto.setProvince(detail.path("province").asText(""));
            dto.setCity(detail.path("city").asText(""));
            dto.setDistrict(detail.path("district").asText(""));
            dto.setAdcode(detail.path("adcode").asText(""));
            dto.setLongitude(point.path("x").asText(""));
            dto.setLatitude(point.path("y").asText(""));
            dto.setSource("ip");
            return dto;
        } catch (Exception ex) {
            throw new ExternalApiException("调用百度定位接口失败", ex);
        }
    }
}

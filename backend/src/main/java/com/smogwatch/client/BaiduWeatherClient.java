package com.smogwatch.client;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smogwatch.dto.HourlyForecastDTO;
import com.smogwatch.dto.WeatherDTO;
import com.smogwatch.exception.ExternalApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Component
public class BaiduWeatherClient {

    /** 百度天气接口异常占位值 */
    private static final int INVALID_INT = 999999;
    private static final double INVALID_DOUBLE = 999999d;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${baidu.map.weather-url}")
    private String weatherUrl;

    @Value("${baidu.map.ak}")
    private String ak;

    public BaiduWeatherClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public WeatherDTO queryByDistrictId(String districtId) {
        JsonNode result = fetchResult(districtId);
        return parseNow(result);
    }

    /**
     * 未来 24 小时逐小时预报（百度天气 forecast_hours）
     */
    public List<HourlyForecastDTO> queryHourlyByDistrictId(String districtId) {
        JsonNode result = fetchResult(districtId);
        JsonNode arr = result.path("forecast_hours");
        List<HourlyForecastDTO> list = new ArrayList<>();
        if (!arr.isArray()) {
            return list;
        }
        for (JsonNode item : arr) {
            HourlyForecastDTO row = new HourlyForecastDTO();
            row.setDataTime(item.path("data_time").asText(""));
            row.setTemp(safeInt(item, "temp_fc"));
            row.setHumidity(safeInt(item, "rh"));
            row.setWeatherText(item.path("text").asText("暂无"));
            row.setWindClass(item.path("wind_class").asText("暂无"));
            row.setWindDir(item.path("wind_dir").asText("暂无"));
            row.setPrec1h(safeDouble(item, "prec_1h"));
            row.setClouds(safeInt(item, "clouds"));
            list.add(row);
        }
        return list;
    }

    private JsonNode fetchResult(String districtId) {
        try {
            String url = weatherUrl + "?district_id=" + districtId + "&data_type=all&ak=" + ak;
            String json = restTemplate.getForObject(url, String.class);
            JsonNode root = objectMapper.readTree(json);
            JsonNode result = root.path("result");
            if (result.isMissingNode()) {
                throw new ExternalApiException("百度天气接口无有效返回");
            }
            return result;
        } catch (ExternalApiException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ExternalApiException("调用百度天气接口失败", ex);
        }
    }

    private WeatherDTO parseNow(JsonNode result) {
        JsonNode now = result.path("now");
        JsonNode location = result.path("location");
        WeatherDTO dto = new WeatherDTO();
        dto.setCity(location.path("city").asText(""));
        dto.setTemp(safeInt(now, "temp"));
        dto.setFeelsLike(safeInt(now, "feels_like"));
        dto.setHumidity(safeInt(now, "rh"));
        dto.setWeatherText(now.path("text").asText("暂无"));
        dto.setWindClass(now.path("wind_class").asText("暂无"));
        dto.setWindDir(now.path("wind_dir").asText("暂无"));
        dto.setAqi(safeInt(now, "aqi"));
        dto.setPm25(safeInt(now, "pm25"));
        dto.setObservedAt(now.path("uptime").asText(""));
        return dto;
    }

    private Integer safeInt(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        int parsed = v.asInt(INVALID_INT);
        return parsed == INVALID_INT ? null : parsed;
    }

    private Double safeDouble(JsonNode node, String field) {
        JsonNode v = node.path(field);
        if (v.isMissingNode() || v.isNull()) {
            return null;
        }
        double parsed = v.asDouble(INVALID_DOUBLE);
        return parsed == INVALID_DOUBLE ? null : parsed;
    }
}

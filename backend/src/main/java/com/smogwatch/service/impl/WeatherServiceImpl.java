package com.smogwatch.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.smogwatch.client.BaiduWeatherClient;
import com.smogwatch.dto.HourlyForecastDTO;
import com.smogwatch.dto.WeatherDTO;
import com.smogwatch.entity.WeatherSnapshot;
import com.smogwatch.mapper.WeatherMapper;
import com.smogwatch.service.WeatherService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
public class WeatherServiceImpl implements WeatherService {

    private final BaiduWeatherClient baiduWeatherClient;
    private final WeatherMapper weatherMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final int retryTimes;

    public WeatherServiceImpl(BaiduWeatherClient baiduWeatherClient,
                              WeatherMapper weatherMapper,
                              @Value("${baidu.map.retry-times:2}") int retryTimes) {
        this.baiduWeatherClient = baiduWeatherClient;
        this.weatherMapper = weatherMapper;
        this.retryTimes = retryTimes;
    }

    @Override
    public WeatherDTO fetchCurrentAndSave(String cityOrDistrictId) {
        RuntimeException lastEx = null;
        for (int i = 0; i <= retryTimes; i++) {
            try {
                WeatherDTO dto = baiduWeatherClient.queryByDistrictId(cityOrDistrictId);
                enrichAirQuality(dto);
                saveSnapshot(dto);
                return dto;
            } catch (RuntimeException ex) {
                lastEx = ex;
                try { Thread.sleep(300L * (i + 1)); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
        throw lastEx == null ? new RuntimeException("天气获取失败") : lastEx;
    }

    @Override
    public List<HourlyForecastDTO> hourlyForecast(String districtId) {
        if (districtId == null || districtId.isBlank()) {
            return Collections.emptyList();
        }
        RuntimeException lastEx = null;
        for (int i = 0; i <= retryTimes; i++) {
            try {
                return baiduWeatherClient.queryHourlyByDistrictId(districtId);
            } catch (RuntimeException ex) {
                lastEx = ex;
                try { Thread.sleep(300L * (i + 1)); } catch (InterruptedException ignored) { Thread.currentThread().interrupt(); }
            }
        }
        throw lastEx == null ? new RuntimeException("逐小时预报获取失败") : lastEx;
    }

    @Override
    public List<WeatherDTO> history(String city, String date) {
        List<WeatherSnapshot> snapshots = weatherMapper.findByCityAndDate(city, date);
        List<WeatherDTO> list = new ArrayList<>();
        for (WeatherSnapshot snapshot : snapshots) {
            WeatherDTO dto = new WeatherDTO();
            dto.setCity(snapshot.getCityName());
            dto.setTemp(snapshot.getTemp());
            dto.setFeelsLike(snapshot.getFeelsLike());
            dto.setHumidity(snapshot.getHumidity());
            dto.setWeatherText(snapshot.getWeatherText());
            dto.setWindClass(snapshot.getWindClass());
            dto.setWindDir(snapshot.getWindDir());
            dto.setAqi(snapshot.getAqi());
            dto.setPm25(snapshot.getPm25());
            dto.setObservedAt(snapshot.getObservedAtText());
            enrichAirQuality(dto);
            list.add(dto);
        }
        return list;
    }

    private void saveSnapshot(WeatherDTO dto) {
        WeatherSnapshot entity = new WeatherSnapshot();
        entity.setCityName(dto.getCity());
        entity.setTemp(dto.getTemp());
        entity.setFeelsLike(dto.getFeelsLike());
        entity.setHumidity(dto.getHumidity());
        entity.setWeatherText(dto.getWeatherText());
        entity.setWindClass(dto.getWindClass());
        entity.setWindDir(dto.getWindDir());
        entity.setAqi(dto.getAqi());
        entity.setPm25(dto.getPm25());
        entity.setObservedAtText(dto.getObservedAt());
        try {
            entity.setRawJson(objectMapper.writeValueAsString(dto));
        } catch (JsonProcessingException e) {
            entity.setRawJson("{}");
        }
        weatherMapper.insertWeather(entity);
    }

    private void enrichAirQuality(WeatherDTO dto) {
        if (dto.getAqi() == null) {
            dto.setAirLevel("数据暂无");
            dto.setHealthAdvice("当前接口未返回AQI，建议关注官方提醒并佩戴口罩。");
            return;
        }
        int aqi = dto.getAqi();
        if (aqi <= 50) {
            dto.setAirLevel("优");
            dto.setHealthAdvice("空气质量优秀，适宜户外活动。");
        } else if (aqi <= 100) {
            dto.setAirLevel("良");
            dto.setHealthAdvice("空气质量良好，敏感人群适度防护。");
        } else if (aqi <= 150) {
            dto.setAirLevel("轻度污染");
            dto.setHealthAdvice("建议减少长时间户外活动，佩戴口罩。");
        } else if (aqi <= 200) {
            dto.setAirLevel("中度污染");
            dto.setHealthAdvice("尽量减少外出，必要外出请加强防护。");
        } else {
            dto.setAirLevel("重污染");
            dto.setHealthAdvice("不建议户外活动，请做好室内空气净化。");
        }
    }
}

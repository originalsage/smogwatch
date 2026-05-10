package com.smogwatch.controller;

import com.smogwatch.dto.ApiResponse;
import com.smogwatch.dto.HourlyForecastDTO;
import com.smogwatch.dto.WeatherDTO;
import com.smogwatch.service.WeatherService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/weather")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/current")
    public ApiResponse<WeatherDTO> current(@RequestParam("city") String cityOrDistrictId) {
        return ApiResponse.ok(weatherService.fetchCurrentAndSave(cityOrDistrictId));
    }

    @GetMapping("/history")
    public ApiResponse<List<WeatherDTO>> history(@RequestParam("city") String city,
                                                 @RequestParam("date") String date) {
        return ApiResponse.ok(weatherService.history(city, date));
    }

    /**
     * 未来 24 小时逐小时预报（温度、湿度、天气现象等），用于详情页折线图。
     * 请求参数 city 沿用现有命名，但实际应传区县编码 districtId。
     */
    @GetMapping("/forecast")
    public ApiResponse<List<HourlyForecastDTO>> forecast(@RequestParam("city") String districtId) {
        return ApiResponse.ok(weatherService.hourlyForecast(districtId));
    }
}

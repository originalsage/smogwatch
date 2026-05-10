package com.smogwatch.service;

import com.smogwatch.dto.HourlyForecastDTO;
import com.smogwatch.dto.WeatherDTO;

import java.util.List;

public interface WeatherService {
    WeatherDTO fetchCurrentAndSave(String cityOrDistrictId);
    List<WeatherDTO> history(String city, String date);
    List<HourlyForecastDTO> hourlyForecast(String districtId);
}

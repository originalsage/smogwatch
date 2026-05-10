package com.smogwatch.mapper;

import com.smogwatch.entity.WeatherSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeatherMapper {
    int insertWeather(WeatherSnapshot snapshot);
    List<WeatherSnapshot> findByCityAndDate(@Param("cityName") String cityName, @Param("date") String date);
}

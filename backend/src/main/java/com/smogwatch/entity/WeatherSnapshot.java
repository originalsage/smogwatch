package com.smogwatch.entity;

import java.time.LocalDateTime;

public class WeatherSnapshot {
    private Long id;
    private String cityName;
    private Integer temp;
    private Integer feelsLike;
    private Integer humidity;
    private String weatherText;
    private String windClass;
    private String windDir;
    private Integer aqi;
    private Integer pm25;
    private String observedAtText;
    private String rawJson;
    private LocalDateTime observedAt;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCityName() { return cityName; }
    public void setCityName(String cityName) { this.cityName = cityName; }
    public Integer getTemp() { return temp; }
    public void setTemp(Integer temp) { this.temp = temp; }
    public Integer getFeelsLike() { return feelsLike; }
    public void setFeelsLike(Integer feelsLike) { this.feelsLike = feelsLike; }
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    public String getWeatherText() { return weatherText; }
    public void setWeatherText(String weatherText) { this.weatherText = weatherText; }
    public String getWindClass() { return windClass; }
    public void setWindClass(String windClass) { this.windClass = windClass; }
    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }
    public Integer getAqi() { return aqi; }
    public void setAqi(Integer aqi) { this.aqi = aqi; }
    public Integer getPm25() { return pm25; }
    public void setPm25(Integer pm25) { this.pm25 = pm25; }
    public String getObservedAtText() { return observedAtText; }
    public void setObservedAtText(String observedAtText) { this.observedAtText = observedAtText; }
    public String getRawJson() { return rawJson; }
    public void setRawJson(String rawJson) { this.rawJson = rawJson; }
    public LocalDateTime getObservedAt() { return observedAt; }
    public void setObservedAt(LocalDateTime observedAt) { this.observedAt = observedAt; }
}

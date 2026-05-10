package com.smogwatch.dto;

public class WeatherDTO {
    private String city;
    private Integer temp;
    private Integer feelsLike;
    private Integer humidity;
    private String weatherText;
    private String windClass;
    private String windDir;
    private Integer aqi;
    private Integer pm25;
    private String airLevel;
    private String healthAdvice;
    private String observedAt;

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
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
    public String getAirLevel() { return airLevel; }
    public void setAirLevel(String airLevel) { this.airLevel = airLevel; }
    public String getHealthAdvice() { return healthAdvice; }
    public void setHealthAdvice(String healthAdvice) { this.healthAdvice = healthAdvice; }
    public String getObservedAt() { return observedAt; }
    public void setObservedAt(String observedAt) { this.observedAt = observedAt; }
}

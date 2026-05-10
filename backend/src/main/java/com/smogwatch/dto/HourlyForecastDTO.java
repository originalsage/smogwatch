package com.smogwatch.dto;

/**
 * 百度天气 forecast_hours 单条数据。
 * 字段对照参考百度地图开放平台 - 国内天气查询服务。
 */
public class HourlyForecastDTO {
    private String dataTime;
    private Integer temp;
    private Integer humidity;
    private String weatherText;
    private String windClass;
    private String windDir;
    private Double prec1h;
    private Integer clouds;

    public String getDataTime() { return dataTime; }
    public void setDataTime(String dataTime) { this.dataTime = dataTime; }
    public Integer getTemp() { return temp; }
    public void setTemp(Integer temp) { this.temp = temp; }
    public Integer getHumidity() { return humidity; }
    public void setHumidity(Integer humidity) { this.humidity = humidity; }
    public String getWeatherText() { return weatherText; }
    public void setWeatherText(String weatherText) { this.weatherText = weatherText; }
    public String getWindClass() { return windClass; }
    public void setWindClass(String windClass) { this.windClass = windClass; }
    public String getWindDir() { return windDir; }
    public void setWindDir(String windDir) { this.windDir = windDir; }
    public Double getPrec1h() { return prec1h; }
    public void setPrec1h(Double prec1h) { this.prec1h = prec1h; }
    public Integer getClouds() { return clouds; }
    public void setClouds(Integer clouds) { this.clouds = clouds; }
}

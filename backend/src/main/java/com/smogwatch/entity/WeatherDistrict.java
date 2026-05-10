package com.smogwatch.entity;

public class WeatherDistrict {
    private Long id;
    private String districtId;
    private String province;
    private String city;
    private String cityGeocode;
    private String district;
    private String districtGeocode;
    private Double lon;
    private Double lat;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getCityGeocode() { return cityGeocode; }
    public void setCityGeocode(String cityGeocode) { this.cityGeocode = cityGeocode; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getDistrictGeocode() { return districtGeocode; }
    public void setDistrictGeocode(String districtGeocode) { this.districtGeocode = districtGeocode; }
    public Double getLon() { return lon; }
    public void setLon(Double lon) { this.lon = lon; }
    public Double getLat() { return lat; }
    public void setLat(Double lat) { this.lat = lat; }
}

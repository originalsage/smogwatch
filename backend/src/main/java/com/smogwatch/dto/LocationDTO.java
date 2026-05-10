package com.smogwatch.dto;

public class LocationDTO {
    private String province;
    private String city;
    private String district;
    private String adcode;
    private String longitude;
    private String latitude;
    private String source;

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getAdcode() { return adcode; }
    public void setAdcode(String adcode) { this.adcode = adcode; }
    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }
    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}

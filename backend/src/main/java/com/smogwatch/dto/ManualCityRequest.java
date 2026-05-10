package com.smogwatch.dto;

import jakarta.validation.constraints.NotBlank;

public class ManualCityRequest {
    @NotBlank(message = "province不能为空")
    private String province;
    @NotBlank(message = "city不能为空")
    private String city;
    @NotBlank(message = "district不能为空")
    private String district;
    @NotBlank(message = "cityGeocode不能为空")
    private String cityGeocode;
    @NotBlank(message = "districtId不能为空")
    private String districtId;

    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getDistrict() { return district; }
    public void setDistrict(String district) { this.district = district; }
    public String getCityGeocode() { return cityGeocode; }
    public void setCityGeocode(String cityGeocode) { this.cityGeocode = cityGeocode; }
    public String getDistrictId() { return districtId; }
    public void setDistrictId(String districtId) { this.districtId = districtId; }
}

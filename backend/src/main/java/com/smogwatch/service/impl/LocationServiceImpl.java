package com.smogwatch.service.impl;

import com.smogwatch.client.BaiduLocationClient;
import com.smogwatch.dto.LocationDTO;
import com.smogwatch.dto.ManualCityRequest;
import com.smogwatch.entity.CityLocationLog;
import com.smogwatch.mapper.LocationMapper;
import com.smogwatch.service.LocationService;
import org.springframework.stereotype.Service;

@Service
public class LocationServiceImpl implements LocationService {

    private final BaiduLocationClient baiduLocationClient;
    private final LocationMapper locationMapper;

    public LocationServiceImpl(BaiduLocationClient baiduLocationClient, LocationMapper locationMapper) {
        this.baiduLocationClient = baiduLocationClient;
        this.locationMapper = locationMapper;
    }

    @Override
    public LocationDTO resolveAndSave(String ip) {
        LocationDTO dto = baiduLocationClient.resolveByIp(ip);
        saveLog(dto);
        return dto;
    }

    @Override
    public LocationDTO saveManualCity(ManualCityRequest request) {
        LocationDTO dto = new LocationDTO();
        dto.setProvince(request.getProvince());
        dto.setCity(request.getCity());
        dto.setDistrict(request.getDistrict());
        dto.setAdcode(request.getDistrictId());
        dto.setSource("manual");
        saveLog(dto);
        return dto;
    }

    private void saveLog(LocationDTO dto) {
        CityLocationLog log = new CityLocationLog();
        log.setSource(dto.getSource());
        log.setProvince(dto.getProvince());
        log.setCity(dto.getCity());
        log.setDistrict(dto.getDistrict());
        log.setAdcode(dto.getAdcode());
        log.setLongitude(dto.getLongitude());
        log.setLatitude(dto.getLatitude());
        locationMapper.insertLocation(log);
    }
}

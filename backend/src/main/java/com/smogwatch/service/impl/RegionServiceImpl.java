package com.smogwatch.service.impl;

import com.smogwatch.dto.CityOptionDTO;
import com.smogwatch.dto.DistrictOptionDTO;
import com.smogwatch.mapper.WeatherDistrictMapper;
import com.smogwatch.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RegionServiceImpl implements RegionService {
    private final WeatherDistrictMapper weatherDistrictMapper;

    public RegionServiceImpl(WeatherDistrictMapper weatherDistrictMapper) {
        this.weatherDistrictMapper = weatherDistrictMapper;
    }

    @Override
    public List<String> provinces() {
        return weatherDistrictMapper.findProvinces();
    }

    @Override
    public List<CityOptionDTO> cities(String province) {
        return weatherDistrictMapper.findCitiesByProvince(province);
    }

    @Override
    public List<DistrictOptionDTO> districts(String province, String city, String cityGeocode) {
        return weatherDistrictMapper.findDistricts(province, city, cityGeocode);
    }
}

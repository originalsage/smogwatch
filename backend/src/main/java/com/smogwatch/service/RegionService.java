package com.smogwatch.service;

import com.smogwatch.dto.CityOptionDTO;
import com.smogwatch.dto.DistrictOptionDTO;

import java.util.List;

public interface RegionService {
    List<String> provinces();
    List<CityOptionDTO> cities(String province);
    List<DistrictOptionDTO> districts(String province, String city, String cityGeocode);
}

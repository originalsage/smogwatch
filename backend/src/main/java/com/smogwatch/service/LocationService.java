package com.smogwatch.service;

import com.smogwatch.dto.LocationDTO;
import com.smogwatch.dto.ManualCityRequest;

public interface LocationService {
    LocationDTO resolveAndSave(String ip);
    LocationDTO saveManualCity(ManualCityRequest request);
}

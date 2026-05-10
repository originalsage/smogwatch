package com.smogwatch.controller;

import com.smogwatch.dto.ApiResponse;
import com.smogwatch.dto.CityOptionDTO;
import com.smogwatch.dto.DistrictOptionDTO;
import com.smogwatch.service.RegionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/regions")
public class RegionController {
    private final RegionService regionService;

    public RegionController(RegionService regionService) {
        this.regionService = regionService;
    }

    @GetMapping("/provinces")
    public ApiResponse<List<String>> provinces() {
        return ApiResponse.ok(regionService.provinces());
    }

    @GetMapping("/cities")
    public ApiResponse<List<CityOptionDTO>> cities(@RequestParam("province") String province) {
        return ApiResponse.ok(regionService.cities(province));
    }

    @GetMapping("/districts")
    public ApiResponse<List<DistrictOptionDTO>> districts(@RequestParam("province") String province,
                                                          @RequestParam("city") String city,
                                                          @RequestParam("cityGeocode") String cityGeocode) {
        return ApiResponse.ok(regionService.districts(province, city, cityGeocode));
    }
}

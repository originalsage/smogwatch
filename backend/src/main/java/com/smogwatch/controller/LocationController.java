package com.smogwatch.controller;

import com.smogwatch.dto.ApiResponse;
import com.smogwatch.dto.LocationDTO;
import com.smogwatch.dto.ManualCityRequest;
import com.smogwatch.service.LocationService;
import com.smogwatch.util.ClientIpResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/location")
public class LocationController {

    private final LocationService locationService;

    public LocationController(LocationService locationService) {
        this.locationService = locationService;
    }

    @PostMapping("/resolve")
    public ApiResponse<LocationDTO> resolveByIp(HttpServletRequest request) {
        String ip = ClientIpResolver.resolve(request);
        if (ip == null) {
            return ApiResponse.fail("无法解析客户端公网 IP。请确认入口网关已注入 X-Forwarded-For / X-Real-IP / Forwarded，"
                    + "或使用 /api/location/manual 提交城市。");
        }
        return ApiResponse.ok(locationService.resolveAndSave(ip));
    }

    @PostMapping("/manual")
    public ApiResponse<LocationDTO> manual(@Valid @RequestBody ManualCityRequest request) {
        return ApiResponse.ok(locationService.saveManualCity(request));
    }
}

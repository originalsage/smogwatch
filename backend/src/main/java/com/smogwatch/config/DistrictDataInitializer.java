package com.smogwatch.config;

import com.smogwatch.entity.WeatherDistrict;
import com.smogwatch.mapper.WeatherDistrictMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class DistrictDataInitializer implements CommandLineRunner {

    private final WeatherDistrictMapper weatherDistrictMapper;
    private final ResourceLoader resourceLoader;

    @Value("${app.district.csv-path:classpath:weather_district_id.csv}")
    private String csvPath;

    public DistrictDataInitializer(WeatherDistrictMapper weatherDistrictMapper, ResourceLoader resourceLoader) {
        this.weatherDistrictMapper = weatherDistrictMapper;
        this.resourceLoader = resourceLoader;
    }

    @Override
    public void run(String... args) throws Exception {
        int count;
        try {
            count = weatherDistrictMapper.countAll();
        } catch (DataAccessException ex) {
            // During tests or first boot before SQL import, table may not exist yet.
            return;
        }
        if (count > 0) {
            return;
        }
        Resource resource = resourceLoader.getResource(csvPath);
        if (!resource.exists()) {
            return;
        }
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8))) {
            String line = reader.readLine();
            while ((line = reader.readLine()) != null) {
                String[] arr = line.split(",");
                if (arr.length < 8) {
                    continue;
                }
                WeatherDistrict district = new WeatherDistrict();
                district.setDistrictId(arr[0].trim());
                district.setProvince(arr[1].trim());
                district.setCity(arr[2].trim());
                district.setCityGeocode(arr[3].trim());
                district.setDistrict(arr[4].trim());
                district.setDistrictGeocode(arr[5].trim());
                district.setLon(parseDouble(arr[6]));
                district.setLat(parseDouble(arr[7]));
                weatherDistrictMapper.insert(district);
            }
        }
    }

    private Double parseDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (Exception ex) {
            return null;
        }
    }
}

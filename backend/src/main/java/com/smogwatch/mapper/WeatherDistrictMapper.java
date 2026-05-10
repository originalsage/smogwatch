package com.smogwatch.mapper;

import com.smogwatch.dto.CityOptionDTO;
import com.smogwatch.dto.DistrictOptionDTO;
import com.smogwatch.entity.WeatherDistrict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface WeatherDistrictMapper {
    int countAll();
    int insert(WeatherDistrict district);
    List<String> findProvinces();
    List<CityOptionDTO> findCitiesByProvince(@Param("province") String province);
    List<DistrictOptionDTO> findDistricts(@Param("province") String province,
                                          @Param("city") String city,
                                          @Param("cityGeocode") String cityGeocode);
    WeatherDistrict findByDistrictId(@Param("districtId") String districtId);
}

package com.smogwatch.mapper;

import com.smogwatch.entity.CityLocationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LocationMapper {
    int insertLocation(CityLocationLog locationLog);
}

package com.meteo.observation.service;

import com.meteo.observation.document.AirQuality;

import java.util.List;

public interface AirQualityService {

    List<AirQuality> findAll();

    List<AirQuality> findByCityId(Long cityId);

    AirQuality findById(String id);

    AirQuality create(AirQuality airQuality);

    AirQuality update(String id, AirQuality airQuality);

    void delete(String id);
}

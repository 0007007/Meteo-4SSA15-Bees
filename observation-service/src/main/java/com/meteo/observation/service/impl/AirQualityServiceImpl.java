package com.meteo.observation.service.impl;

import com.meteo.observation.document.AirQuality;
import com.meteo.observation.exception.ResourceNotFoundException;
import com.meteo.observation.repository.AirQualityRepository;
import com.meteo.observation.service.AirQualityService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AirQualityServiceImpl implements AirQualityService {

    private final AirQualityRepository airQualityRepository;

    public AirQualityServiceImpl(AirQualityRepository airQualityRepository) {
        this.airQualityRepository = airQualityRepository;
    }

    @Override
    public List<AirQuality> findAll() {
        return airQualityRepository.findAll();
    }

    @Override
    public List<AirQuality> findByCityId(Long cityId) {
        return airQualityRepository.findByCityId(cityId);
    }

    @Override
    public AirQuality findById(String id) {
        return airQualityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("AirQuality", id));
    }

    @Override
    public AirQuality create(AirQuality airQuality) {
        airQuality.setId(null);
        return airQualityRepository.save(airQuality);
    }

    @Override
    public AirQuality update(String id, AirQuality airQuality) {
        AirQuality existing = findById(id);
        existing.setCityId(airQuality.getCityId());
        existing.setMeasuredAt(airQuality.getMeasuredAt());
        existing.setAqi(airQuality.getAqi());
        existing.setPm25(airQuality.getPm25());
        existing.setPm10(airQuality.getPm10());
        existing.setO3(airQuality.getO3());
        existing.setNo2(airQuality.getNo2());
        existing.setLevel(airQuality.getLevel());
        return airQualityRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        AirQuality existing = findById(id);
        airQualityRepository.delete(existing);
    }
}

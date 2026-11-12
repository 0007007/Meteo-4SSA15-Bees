package com.meteo.station.service.impl;

import com.meteo.station.entity.WeatherStation;
import com.meteo.station.exception.ResourceNotFoundException;
import com.meteo.station.repository.SensorRepository;
import com.meteo.station.repository.WeatherStationRepository;
import com.meteo.station.service.WeatherStationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class WeatherStationServiceImpl implements WeatherStationService {

    private final WeatherStationRepository weatherStationRepository;
    private final SensorRepository sensorRepository;

    public WeatherStationServiceImpl(WeatherStationRepository weatherStationRepository, SensorRepository sensorRepository) {
        this.weatherStationRepository = weatherStationRepository;
        this.sensorRepository = sensorRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeatherStation> findAll() {
        return weatherStationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<WeatherStation> findByCityId(Long cityId) {
        return weatherStationRepository.findByCityId(cityId);
    }

    @Override
    @Transactional(readOnly = true)
    public WeatherStation findById(Long id) {
        return weatherStationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("WeatherStation", id));
    }

    @Override
    public WeatherStation create(WeatherStation weatherStation) {
        weatherStation.setId(null);
        return weatherStationRepository.save(weatherStation);
    }

    @Override
    public WeatherStation update(Long id, WeatherStation weatherStation) {
        WeatherStation existing = findById(id);
        existing.setCode(weatherStation.getCode());
        existing.setName(weatherStation.getName());
        existing.setLatitude(weatherStation.getLatitude());
        existing.setLongitude(weatherStation.getLongitude());
        existing.setAltitude(weatherStation.getAltitude());
        existing.setInstallationDate(weatherStation.getInstallationDate());
        existing.setStatus(weatherStation.getStatus());
        existing.setCityId(weatherStation.getCityId());
        return weatherStationRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        WeatherStation existing = findById(id);
        if (sensorRepository.existsByStationId(id)) {
            throw new IllegalStateException("WeatherStation " + id + " still has sensors: delete them first");
        }
        weatherStationRepository.delete(existing);
    }
}

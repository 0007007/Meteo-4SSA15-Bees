package com.meteo.station.service.impl;

import com.meteo.station.entity.Sensor;
import com.meteo.station.entity.WeatherStation;
import com.meteo.station.exception.ResourceNotFoundException;
import com.meteo.station.repository.SensorRepository;
import com.meteo.station.repository.WeatherStationRepository;
import com.meteo.station.service.SensorService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SensorServiceImpl implements SensorService {

    private final SensorRepository sensorRepository;
    private final WeatherStationRepository weatherStationRepository;

    public SensorServiceImpl(SensorRepository sensorRepository, WeatherStationRepository weatherStationRepository) {
        this.sensorRepository = sensorRepository;
        this.weatherStationRepository = weatherStationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> findAll() {
        return sensorRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Sensor> findByStationId(Long stationId) {
        return sensorRepository.findByStationId(stationId);
    }

    @Override
    @Transactional(readOnly = true)
    public Sensor findById(Long id) {
        return sensorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sensor", id));
    }

    @Override
    public Sensor create(Sensor sensor) {
        sensor.setId(null);
        sensor.setStation(resolveWeatherStation(sensor.getStation()));
        return sensorRepository.save(sensor);
    }

    @Override
    public Sensor update(Long id, Sensor sensor) {
        Sensor existing = findById(id);
        existing.setSerialNumber(sensor.getSerialNumber());
        existing.setType(sensor.getType());
        existing.setUnit(sensor.getUnit());
        existing.setLastCalibrationDate(sensor.getLastCalibrationDate());
        existing.setActive(sensor.getActive());
        existing.setStation(resolveWeatherStation(sensor.getStation()));
        return sensorRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Sensor existing = findById(id);
        sensorRepository.delete(existing);
    }

    private WeatherStation resolveWeatherStation(WeatherStation station) {
        if (station == null || station.getId() == null) {
            throw new IllegalArgumentException("station.id is required");
        }
        return weatherStationRepository.findById(station.getId())
                .orElseThrow(() -> new ResourceNotFoundException("WeatherStation", station.getId()));
    }
}

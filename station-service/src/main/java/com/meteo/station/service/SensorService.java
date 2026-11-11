package com.meteo.station.service;

import com.meteo.station.entity.Sensor;

import java.util.List;

public interface SensorService {

    List<Sensor> findAll();

    List<Sensor> findByStationId(Long stationId);

    Sensor findById(Long id);

    Sensor create(Sensor sensor);

    Sensor update(Long id, Sensor sensor);

    void delete(Long id);
}

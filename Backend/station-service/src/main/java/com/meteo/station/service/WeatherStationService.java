package com.meteo.station.service;

import com.meteo.station.entity.WeatherStation;

import java.util.List;

public interface WeatherStationService {

    List<WeatherStation> findAll();

    List<WeatherStation> findByCityId(Long cityId);

    WeatherStation findById(Long id);

    WeatherStation create(WeatherStation weatherStation);

    WeatherStation update(Long id, WeatherStation weatherStation);

    void delete(Long id);
}

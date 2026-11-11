package com.meteo.forecast.service;

import com.meteo.forecast.document.Forecast;

import java.util.List;

public interface ForecastService {

    List<Forecast> findAll();

    List<Forecast> findByCityId(Long cityId);

    Forecast findById(String id);

    Forecast create(Forecast forecast);

    Forecast update(String id, Forecast forecast);

    void delete(String id);
}

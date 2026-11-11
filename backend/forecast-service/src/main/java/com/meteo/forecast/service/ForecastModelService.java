package com.meteo.forecast.service;

import com.meteo.forecast.document.ForecastModel;

import java.util.List;

public interface ForecastModelService {

    List<ForecastModel> findAll();

    ForecastModel findById(String id);

    ForecastModel create(ForecastModel forecastModel);

    ForecastModel update(String id, ForecastModel forecastModel);

    void delete(String id);
}

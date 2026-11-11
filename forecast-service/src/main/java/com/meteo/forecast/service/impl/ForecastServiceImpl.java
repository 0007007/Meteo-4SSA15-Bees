package com.meteo.forecast.service.impl;

import com.meteo.forecast.document.Forecast;
import com.meteo.forecast.exception.ResourceNotFoundException;
import com.meteo.forecast.repository.ForecastModelRepository;
import com.meteo.forecast.repository.ForecastRepository;
import com.meteo.forecast.service.ForecastService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ForecastServiceImpl implements ForecastService {

    private final ForecastRepository forecastRepository;
    private final ForecastModelRepository forecastModelRepository;

    public ForecastServiceImpl(ForecastRepository forecastRepository, ForecastModelRepository forecastModelRepository) {
        this.forecastRepository = forecastRepository;
        this.forecastModelRepository = forecastModelRepository;
    }

    @Override
    public List<Forecast> findAll() {
        return forecastRepository.findAll();
    }

    @Override
    public List<Forecast> findByCityId(Long cityId) {
        return forecastRepository.findByCityId(cityId);
    }

    @Override
    public Forecast findById(String id) {
        return forecastRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Forecast", id));
    }

    @Override
    public Forecast create(Forecast forecast) {
        forecast.setId(null);
        checkForecastModelExists(forecast.getModelId());
        forecast.setCreatedAt(LocalDateTime.now());
        return forecastRepository.save(forecast);
    }

    @Override
    public Forecast update(String id, Forecast forecast) {
        Forecast existing = findById(id);
        checkForecastModelExists(forecast.getModelId());
        existing.setCityId(forecast.getCityId());
        existing.setForecastDate(forecast.getForecastDate());
        existing.setMinTemperature(forecast.getMinTemperature());
        existing.setMaxTemperature(forecast.getMaxTemperature());
        existing.setCondition(forecast.getCondition());
        existing.setPrecipitationProbability(forecast.getPrecipitationProbability());
        existing.setWindSpeed(forecast.getWindSpeed());
        existing.setModelId(forecast.getModelId());
        return forecastRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        Forecast existing = findById(id);
        forecastRepository.delete(existing);
    }

    private void checkForecastModelExists(String modelId) {
        if (modelId == null || !forecastModelRepository.existsById(modelId)) {
            throw new ResourceNotFoundException("ForecastModel", modelId);
        }
    }
}

package com.meteo.forecast.service.impl;

import com.meteo.forecast.document.ForecastModel;
import com.meteo.forecast.exception.ResourceNotFoundException;
import com.meteo.forecast.repository.ForecastModelRepository;
import com.meteo.forecast.repository.ForecastRepository;
import com.meteo.forecast.service.ForecastModelService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ForecastModelServiceImpl implements ForecastModelService {

    private final ForecastModelRepository forecastModelRepository;
    private final ForecastRepository forecastRepository;

    public ForecastModelServiceImpl(ForecastModelRepository forecastModelRepository, ForecastRepository forecastRepository) {
        this.forecastModelRepository = forecastModelRepository;
        this.forecastRepository = forecastRepository;
    }

    @Override
    public List<ForecastModel> findAll() {
        return forecastModelRepository.findAll();
    }

    @Override
    public ForecastModel findById(String id) {
        return forecastModelRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("ForecastModel", id));
    }

    @Override
    public ForecastModel create(ForecastModel forecastModel) {
        forecastModel.setId(null);
        return forecastModelRepository.save(forecastModel);
    }

    @Override
    public ForecastModel update(String id, ForecastModel forecastModel) {
        ForecastModel existing = findById(id);
        existing.setName(forecastModel.getName());
        existing.setProvider(forecastModel.getProvider());
        existing.setVersion(forecastModel.getVersion());
        existing.setResolutionKm(forecastModel.getResolutionKm());
        existing.setHorizonDays(forecastModel.getHorizonDays());
        existing.setDescription(forecastModel.getDescription());
        return forecastModelRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        ForecastModel existing = findById(id);
        if (forecastRepository.existsByModelId(id)) {
            throw new IllegalStateException("ForecastModel " + id + " still has forecasts: delete them first");
        }
        forecastModelRepository.delete(existing);
    }
}

package com.meteo.observation.service.impl;

import com.meteo.observation.document.Observation;
import com.meteo.observation.exception.ResourceNotFoundException;
import com.meteo.observation.repository.ObservationRepository;
import com.meteo.observation.service.ObservationService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ObservationServiceImpl implements ObservationService {

    private final ObservationRepository observationRepository;

    public ObservationServiceImpl(ObservationRepository observationRepository) {
        this.observationRepository = observationRepository;
    }

    @Override
    public List<Observation> findAll() {
        return observationRepository.findAll();
    }

    @Override
    public List<Observation> findByStationId(Long stationId) {
        return observationRepository.findByStationId(stationId);
    }

    @Override
    public Observation findById(String id) {
        return observationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Observation", id));
    }

    @Override
    public Observation create(Observation observation) {
        observation.setId(null);
        return observationRepository.save(observation);
    }

    @Override
    public Observation update(String id, Observation observation) {
        Observation existing = findById(id);
        existing.setStationId(observation.getStationId());
        existing.setObservedAt(observation.getObservedAt());
        existing.setTemperature(observation.getTemperature());
        existing.setHumidity(observation.getHumidity());
        existing.setPressure(observation.getPressure());
        existing.setWindSpeed(observation.getWindSpeed());
        existing.setWindDirection(observation.getWindDirection());
        existing.setPrecipitation(observation.getPrecipitation());
        return observationRepository.save(existing);
    }

    @Override
    public void delete(String id) {
        Observation existing = findById(id);
        observationRepository.delete(existing);
    }
}

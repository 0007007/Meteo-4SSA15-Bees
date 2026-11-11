package com.meteo.observation.service;

import com.meteo.observation.document.Observation;

import java.util.List;

public interface ObservationService {

    List<Observation> findAll();

    List<Observation> findByStationId(Long stationId);

    Observation findById(String id);

    Observation create(Observation observation);

    Observation update(String id, Observation observation);

    void delete(String id);
}

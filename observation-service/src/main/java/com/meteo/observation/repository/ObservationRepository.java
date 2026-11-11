package com.meteo.observation.repository;

import com.meteo.observation.document.Observation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ObservationRepository extends MongoRepository<Observation, String> {
    List<Observation> findByStationId(Long stationId);
}

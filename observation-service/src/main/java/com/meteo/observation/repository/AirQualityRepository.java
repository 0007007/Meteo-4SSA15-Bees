package com.meteo.observation.repository;

import com.meteo.observation.document.AirQuality;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AirQualityRepository extends MongoRepository<AirQuality, String> {
    List<AirQuality> findByCityId(Long cityId);
}

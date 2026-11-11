package com.meteo.forecast.repository;

import com.meteo.forecast.document.Forecast;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ForecastRepository extends MongoRepository<Forecast, String> {
    List<Forecast> findByCityId(Long cityId);

    boolean existsByModelId(String modelId);
}

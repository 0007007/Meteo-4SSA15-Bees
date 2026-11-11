package com.meteo.forecast.repository;

import com.meteo.forecast.document.ForecastModel;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ForecastModelRepository extends MongoRepository<ForecastModel, String> {
}

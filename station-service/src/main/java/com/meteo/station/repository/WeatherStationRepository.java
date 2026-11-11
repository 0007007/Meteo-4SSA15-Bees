package com.meteo.station.repository;

import com.meteo.station.entity.WeatherStation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WeatherStationRepository extends JpaRepository<WeatherStation, Long> {
    List<WeatherStation> findByCityId(Long cityId);
}

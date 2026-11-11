package com.meteo.station.repository;

import com.meteo.station.entity.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SensorRepository extends JpaRepository<Sensor, Long> {
    List<Sensor> findByStationId(Long stationId);

    boolean existsByStationId(Long stationId);
}

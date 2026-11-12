package com.meteo.station.controller;

import com.meteo.station.entity.Sensor;
import com.meteo.station.service.SensorService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/sensors")
public class SensorController {

    private final SensorService sensorService;

    public SensorController(SensorService sensorService) {
        this.sensorService = sensorService;
    }

    @GetMapping
    public List<Sensor> getAll(@RequestParam(name = "stationId", required = false) Long stationId) {
        return stationId == null ? sensorService.findAll() : sensorService.findByStationId(stationId);
    }

    @GetMapping("/{id}")
    public Sensor getById(@PathVariable("id") Long id) {
        return sensorService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Sensor create(@Valid @RequestBody Sensor sensor) {
        return sensorService.create(sensor);
    }

    @PutMapping("/{id}")
    public Sensor update(@PathVariable("id") Long id, @Valid @RequestBody Sensor sensor) {
        return sensorService.update(id, sensor);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        sensorService.delete(id);
    }
}

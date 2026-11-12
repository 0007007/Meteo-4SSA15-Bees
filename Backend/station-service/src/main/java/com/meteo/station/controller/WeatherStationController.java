package com.meteo.station.controller;

import com.meteo.station.entity.WeatherStation;
import com.meteo.station.service.WeatherStationService;
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
@RequestMapping("/api/stations")
public class WeatherStationController {

    private final WeatherStationService weatherStationService;

    public WeatherStationController(WeatherStationService weatherStationService) {
        this.weatherStationService = weatherStationService;
    }

    @GetMapping
    public List<WeatherStation> getAll(@RequestParam(name = "cityId", required = false) Long cityId) {
        return cityId == null ? weatherStationService.findAll() : weatherStationService.findByCityId(cityId);
    }

    @GetMapping("/{id}")
    public WeatherStation getById(@PathVariable("id") Long id) {
        return weatherStationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WeatherStation create(@Valid @RequestBody WeatherStation weatherStation) {
        return weatherStationService.create(weatherStation);
    }

    @PutMapping("/{id}")
    public WeatherStation update(@PathVariable("id") Long id, @Valid @RequestBody WeatherStation weatherStation) {
        return weatherStationService.update(id, weatherStation);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        weatherStationService.delete(id);
    }
}

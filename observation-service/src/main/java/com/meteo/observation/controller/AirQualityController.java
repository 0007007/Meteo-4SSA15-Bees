package com.meteo.observation.controller;

import com.meteo.observation.document.AirQuality;
import com.meteo.observation.service.AirQualityService;
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
@RequestMapping("/api/air-quality")
public class AirQualityController {

    private final AirQualityService airQualityService;

    public AirQualityController(AirQualityService airQualityService) {
        this.airQualityService = airQualityService;
    }

    @GetMapping
    public List<AirQuality> getAll(@RequestParam(name = "cityId", required = false) Long cityId) {
        return cityId == null ? airQualityService.findAll() : airQualityService.findByCityId(cityId);
    }

    @GetMapping("/{id}")
    public AirQuality getById(@PathVariable("id") String id) {
        return airQualityService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AirQuality create(@Valid @RequestBody AirQuality airQuality) {
        return airQualityService.create(airQuality);
    }

    @PutMapping("/{id}")
    public AirQuality update(@PathVariable("id") String id, @Valid @RequestBody AirQuality airQuality) {
        return airQualityService.update(id, airQuality);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        airQualityService.delete(id);
    }
}

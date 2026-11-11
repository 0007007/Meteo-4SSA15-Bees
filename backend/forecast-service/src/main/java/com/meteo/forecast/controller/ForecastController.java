package com.meteo.forecast.controller;

import com.meteo.forecast.document.Forecast;
import com.meteo.forecast.service.ForecastService;
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
@RequestMapping("/api/forecasts")
public class ForecastController {

    private final ForecastService forecastService;

    public ForecastController(ForecastService forecastService) {
        this.forecastService = forecastService;
    }

    @GetMapping
    public List<Forecast> getAll(@RequestParam(name = "cityId", required = false) Long cityId) {
        return cityId == null ? forecastService.findAll() : forecastService.findByCityId(cityId);
    }

    @GetMapping("/{id}")
    public Forecast getById(@PathVariable("id") String id) {
        return forecastService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Forecast create(@Valid @RequestBody Forecast forecast) {
        return forecastService.create(forecast);
    }

    @PutMapping("/{id}")
    public Forecast update(@PathVariable("id") String id, @Valid @RequestBody Forecast forecast) {
        return forecastService.update(id, forecast);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        forecastService.delete(id);
    }
}

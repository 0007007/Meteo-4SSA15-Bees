package com.meteo.forecast.controller;

import com.meteo.forecast.document.ForecastModel;
import com.meteo.forecast.service.ForecastModelService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/forecast-models")
public class ForecastModelController {

    private final ForecastModelService forecastModelService;

    public ForecastModelController(ForecastModelService forecastModelService) {
        this.forecastModelService = forecastModelService;
    }

    @GetMapping
    public List<ForecastModel> getAll() {
        return forecastModelService.findAll();
    }

    @GetMapping("/{id}")
    public ForecastModel getById(@PathVariable("id") String id) {
        return forecastModelService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ForecastModel create(@Valid @RequestBody ForecastModel forecastModel) {
        return forecastModelService.create(forecastModel);
    }

    @PutMapping("/{id}")
    public ForecastModel update(@PathVariable("id") String id, @Valid @RequestBody ForecastModel forecastModel) {
        return forecastModelService.update(id, forecastModel);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") String id) {
        forecastModelService.delete(id);
    }
}

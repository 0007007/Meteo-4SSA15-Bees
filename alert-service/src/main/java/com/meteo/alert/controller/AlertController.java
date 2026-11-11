package com.meteo.alert.controller;

import com.meteo.alert.entity.Alert;
import com.meteo.alert.service.AlertService;
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
@RequestMapping("/api/alerts")
public class AlertController {

    private final AlertService alertService;

    public AlertController(AlertService alertService) {
        this.alertService = alertService;
    }

    @GetMapping
    public List<Alert> getAll(@RequestParam(name = "cityId", required = false) Long cityId) {
        return cityId == null ? alertService.findAll() : alertService.findByCityId(cityId);
    }

    @GetMapping("/{id}")
    public Alert getById(@PathVariable("id") Long id) {
        return alertService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public Alert create(@Valid @RequestBody Alert alert) {
        return alertService.create(alert);
    }

    @PutMapping("/{id}")
    public Alert update(@PathVariable("id") Long id, @Valid @RequestBody Alert alert) {
        return alertService.update(id, alert);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        alertService.delete(id);
    }
}

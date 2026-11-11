package com.meteo.alert.service;

import com.meteo.alert.entity.Alert;

import java.util.List;

public interface AlertService {

    List<Alert> findAll();

    List<Alert> findByCityId(Long cityId);

    Alert findById(Long id);

    Alert create(Alert alert);

    Alert update(Long id, Alert alert);

    void delete(Long id);
}

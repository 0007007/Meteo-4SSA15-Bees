package com.meteo.alert.service.impl;

import com.meteo.alert.entity.Alert;
import com.meteo.alert.exception.ResourceNotFoundException;
import com.meteo.alert.repository.AlertRepository;
import com.meteo.alert.repository.NotificationRepository;
import com.meteo.alert.service.AlertService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AlertServiceImpl implements AlertService {

    private final AlertRepository alertRepository;
    private final NotificationRepository notificationRepository;

    public AlertServiceImpl(AlertRepository alertRepository, NotificationRepository notificationRepository) {
        this.alertRepository = alertRepository;
        this.notificationRepository = notificationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> findAll() {
        return alertRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Alert> findByCityId(Long cityId) {
        return alertRepository.findByCityId(cityId);
    }

    @Override
    @Transactional(readOnly = true)
    public Alert findById(Long id) {
        return alertRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alert", id));
    }

    @Override
    public Alert create(Alert alert) {
        alert.setId(null);
        return alertRepository.save(alert);
    }

    @Override
    public Alert update(Long id, Alert alert) {
        Alert existing = findById(id);
        existing.setTitle(alert.getTitle());
        existing.setDescription(alert.getDescription());
        existing.setType(alert.getType());
        existing.setSeverity(alert.getSeverity());
        existing.setCityId(alert.getCityId());
        existing.setStartTime(alert.getStartTime());
        existing.setEndTime(alert.getEndTime());
        existing.setActive(alert.getActive());
        return alertRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Alert existing = findById(id);
        if (notificationRepository.existsByAlertId(id)) {
            throw new IllegalStateException("Alert " + id + " still has notifications: delete them first");
        }
        alertRepository.delete(existing);
    }
}

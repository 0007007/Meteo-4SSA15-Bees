package com.meteo.alert.service.impl;

import com.meteo.alert.entity.Alert;
import com.meteo.alert.entity.Notification;
import com.meteo.alert.exception.ResourceNotFoundException;
import com.meteo.alert.repository.AlertRepository;
import com.meteo.alert.repository.NotificationRepository;
import com.meteo.alert.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class NotificationServiceImpl implements NotificationService {

    private final NotificationRepository notificationRepository;
    private final AlertRepository alertRepository;

    public NotificationServiceImpl(NotificationRepository notificationRepository, AlertRepository alertRepository) {
        this.notificationRepository = notificationRepository;
        this.alertRepository = alertRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findAll() {
        return notificationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public Notification findById(Long id) {
        return notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", id));
    }

    @Override
    public Notification create(Notification notification) {
        notification.setId(null);
        notification.setAlert(resolveAlert(notification.getAlert()));
        return notificationRepository.save(notification);
    }

    @Override
    public Notification update(Long id, Notification notification) {
        Notification existing = findById(id);
        existing.setUserId(notification.getUserId());
        existing.setChannel(notification.getChannel());
        existing.setMessage(notification.getMessage());
        existing.setStatus(notification.getStatus());
        existing.setAlert(resolveAlert(notification.getAlert()));
        return notificationRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        Notification existing = findById(id);
        notificationRepository.delete(existing);
    }

    private Alert resolveAlert(Alert alert) {
        if (alert == null || alert.getId() == null) {
            throw new IllegalArgumentException("alert.id is required");
        }
        return alertRepository.findById(alert.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Alert", alert.getId()));
    }
}

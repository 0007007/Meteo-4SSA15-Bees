package com.meteo.alert.service;

import com.meteo.alert.entity.Notification;

import java.util.List;

public interface NotificationService {

    List<Notification> findAll();

    List<Notification> findByUserId(Long userId);

    Notification findById(Long id);

    Notification create(Notification notification);

    Notification update(Long id, Notification notification);

    void delete(Long id);
}

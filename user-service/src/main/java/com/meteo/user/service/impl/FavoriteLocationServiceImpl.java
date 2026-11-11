package com.meteo.user.service.impl;

import com.meteo.user.entity.FavoriteLocation;
import com.meteo.user.entity.User;
import com.meteo.user.exception.ResourceNotFoundException;
import com.meteo.user.repository.FavoriteLocationRepository;
import com.meteo.user.repository.UserRepository;
import com.meteo.user.service.FavoriteLocationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FavoriteLocationServiceImpl implements FavoriteLocationService {

    private final FavoriteLocationRepository favoriteLocationRepository;
    private final UserRepository userRepository;

    public FavoriteLocationServiceImpl(FavoriteLocationRepository favoriteLocationRepository, UserRepository userRepository) {
        this.favoriteLocationRepository = favoriteLocationRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteLocation> findAll() {
        return favoriteLocationRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FavoriteLocation> findByUserId(Long userId) {
        return favoriteLocationRepository.findByUserId(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public FavoriteLocation findById(Long id) {
        return favoriteLocationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("FavoriteLocation", id));
    }

    @Override
    public FavoriteLocation create(FavoriteLocation favoriteLocation) {
        favoriteLocation.setId(null);
        favoriteLocation.setUser(resolveUser(favoriteLocation.getUser()));
        return favoriteLocationRepository.save(favoriteLocation);
    }

    @Override
    public FavoriteLocation update(Long id, FavoriteLocation favoriteLocation) {
        FavoriteLocation existing = findById(id);
        existing.setCityId(favoriteLocation.getCityId());
        existing.setLabel(favoriteLocation.getLabel());
        existing.setAlertsEnabled(favoriteLocation.getAlertsEnabled());
        existing.setUser(resolveUser(favoriteLocation.getUser()));
        return favoriteLocationRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        FavoriteLocation existing = findById(id);
        favoriteLocationRepository.delete(existing);
    }

    private User resolveUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("user.id is required");
        }
        return userRepository.findById(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("User", user.getId()));
    }
}

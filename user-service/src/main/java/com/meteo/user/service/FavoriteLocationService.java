package com.meteo.user.service;

import com.meteo.user.entity.FavoriteLocation;

import java.util.List;

public interface FavoriteLocationService {

    List<FavoriteLocation> findAll();

    List<FavoriteLocation> findByUserId(Long userId);

    FavoriteLocation findById(Long id);

    FavoriteLocation create(FavoriteLocation favoriteLocation);

    FavoriteLocation update(Long id, FavoriteLocation favoriteLocation);

    void delete(Long id);
}

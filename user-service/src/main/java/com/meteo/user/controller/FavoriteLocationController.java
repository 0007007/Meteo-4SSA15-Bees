package com.meteo.user.controller;

import com.meteo.user.entity.FavoriteLocation;
import com.meteo.user.service.FavoriteLocationService;
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
@RequestMapping("/api/favorites")
public class FavoriteLocationController {

    private final FavoriteLocationService favoriteLocationService;

    public FavoriteLocationController(FavoriteLocationService favoriteLocationService) {
        this.favoriteLocationService = favoriteLocationService;
    }

    @GetMapping
    public List<FavoriteLocation> getAll(@RequestParam(name = "userId", required = false) Long userId) {
        return userId == null ? favoriteLocationService.findAll() : favoriteLocationService.findByUserId(userId);
    }

    @GetMapping("/{id}")
    public FavoriteLocation getById(@PathVariable("id") Long id) {
        return favoriteLocationService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public FavoriteLocation create(@Valid @RequestBody FavoriteLocation favoriteLocation) {
        return favoriteLocationService.create(favoriteLocation);
    }

    @PutMapping("/{id}")
    public FavoriteLocation update(@PathVariable("id") Long id, @Valid @RequestBody FavoriteLocation favoriteLocation) {
        return favoriteLocationService.update(id, favoriteLocation);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("id") Long id) {
        favoriteLocationService.delete(id);
    }
}

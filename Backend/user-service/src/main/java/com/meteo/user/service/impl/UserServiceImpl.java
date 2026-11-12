package com.meteo.user.service.impl;

import com.meteo.user.entity.User;
import com.meteo.user.exception.ResourceNotFoundException;
import com.meteo.user.repository.FavoriteLocationRepository;
import com.meteo.user.repository.UserRepository;
import com.meteo.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final FavoriteLocationRepository favoriteLocationRepository;

    public UserServiceImpl(UserRepository userRepository, FavoriteLocationRepository favoriteLocationRepository) {
        this.userRepository = userRepository;
        this.favoriteLocationRepository = favoriteLocationRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Override
    public User create(User user) {
        user.setId(null);
        return userRepository.save(user);
    }

    @Override
    public User update(Long id, User user) {
        User existing = findById(id);
        existing.setFirstName(user.getFirstName());
        existing.setLastName(user.getLastName());
        existing.setEmail(user.getEmail());
        existing.setPhone(user.getPhone());
        existing.setPreferredUnit(user.getPreferredUnit());
        existing.setRole(user.getRole());
        return userRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        User existing = findById(id);
        if (favoriteLocationRepository.existsByUserId(id)) {
            throw new IllegalStateException("User " + id + " still has favorite locations: delete them first");
        }
        userRepository.delete(existing);
    }
}

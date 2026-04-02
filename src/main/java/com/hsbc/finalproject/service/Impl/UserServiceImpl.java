package com.hsbc.finalproject.service.Impl;

import com.hsbc.finalproject.model.User;
import com.hsbc.finalproject.repository.UserRepository;
import com.hsbc.finalproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    UserRepository userRepository;

    @Override
    public List<User> showAll() {
        return userRepository.findAll();
    }

    @Override
    public java.util.Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }
}

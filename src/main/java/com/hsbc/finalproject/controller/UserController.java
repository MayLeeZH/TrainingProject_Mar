package com.hsbc.finalproject.controller;

import com.hsbc.finalproject.common.ApiResponse;
import com.hsbc.finalproject.model.User;
import com.hsbc.finalproject.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserService userService;

    @GetMapping("/{id}")
    public ApiResponse<User> getUserById(@PathVariable Long id) {
        Optional<User> user = userService.getUserById(id);
        if (user.isPresent()) {
            return new ApiResponse<>(200, "success", user.get());
        } else {
            return new ApiResponse<>(404, "User not found", null);
        }
    }
}

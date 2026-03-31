package com.hsbc.finalproject.service;

import com.hsbc.finalproject.model.User;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

public interface UserService {
    public List<User> showAll();

}

package com.seminarhall.service;

import com.seminarhall.model.User;
import com.seminarhall.model.UserRole;

public interface UserService {
    User authenticate(String email, String password) throws ServiceException;
    User register(String fullName, String email, String phone, String password, UserRole role) throws ServiceException;
    User getUserById(String userId) throws ServiceException;
    boolean isEmailRegistered(String email) throws ServiceException;
    void updateProfile(User user) throws ServiceException;
}

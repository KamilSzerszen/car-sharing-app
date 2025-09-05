package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.model.User;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request);

    User getCurrentUser();
}

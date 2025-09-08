package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.dto.UserRoleUpdateRequestDto;
import org.example.carsharingapp.dto.UserUpdateProfileInfoRequestDto;
import org.example.carsharingapp.model.User;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request);

    User getCurrentUser();

    UserResponseDto updateRoleById(Long id, UserRoleUpdateRequestDto requestDto);

    UserResponseDto getUserProfileInfo();

    UserResponseDto updateUserProfileInfo(UserUpdateProfileInfoRequestDto requestDto);
}

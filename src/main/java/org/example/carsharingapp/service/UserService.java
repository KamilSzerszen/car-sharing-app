package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.*;
import org.example.carsharingapp.model.User;
import org.springframework.data.domain.Page;

import java.awt.print.Pageable;

public interface UserService {

    UserResponseDto register(UserRegistrationRequestDto request);

    User getCurrentUser();

    UserResponseDto updateRoleById(Long id, UserRoleUpdateRequestDto requestDto);

    UserResponseDto getUserProfileInfo();

    UserResponseDto updateUserProfileInfo(UserUpdateProfileInfoRequestDto requestDto);

}

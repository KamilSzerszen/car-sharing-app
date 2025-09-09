package org.example.carsharingapp.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.dto.UserRoleUpdateRequestDto;
import org.example.carsharingapp.dto.UserUpdateProfileInfoRequestDto;
import org.example.carsharingapp.security.annotation.IsCustomer;
import org.example.carsharingapp.security.annotation.IsManager;
import org.example.carsharingapp.service.UserService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User Controller", description = "Managing authentication and user registration")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @IsManager
    @PutMapping("/{id}/role")
    @Operation(summary = "Update roles", description = "Update user role")
    public UserResponseDto updateUserRole(
            @PathVariable Long id,
            @RequestBody @Valid UserRoleUpdateRequestDto userRoleUpdateRequestDto) {
        return userService.updateRoleById(id, userRoleUpdateRequestDto);
    }

    @IsCustomer
    @GetMapping("/me")
    @Operation(summary = "Profile info", description = "Get user profile info")
    public UserResponseDto getUserProfileInfo() {
        return userService.getUserProfileInfo();
    }

    @IsCustomer
    @PutMapping("/me")
    @Operation(summary = "Update profile", description = "Update profile info")
    public UserResponseDto updateUserProfileInfo(
            @RequestBody @Valid UserUpdateProfileInfoRequestDto requestDto
    ) {
        return userService.updateUserProfileInfo(requestDto);
    }

}

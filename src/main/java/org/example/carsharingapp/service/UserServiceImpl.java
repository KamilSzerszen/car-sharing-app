package org.example.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.exception.RegistrationException;
import org.example.carsharingapp.mapper.UserMapper;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.example.carsharingapp.model.User;
import org.example.carsharingapp.repository.RoleRepository;
import org.example.carsharingapp.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Set;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService{

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public UserResponseDto register(UserRegistrationRequestDto request) {
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new RegistrationException("Email already exist");
        }

        Role cutomerRole = roleRepository.findByName(RoleName.ROLE_CUSTOMER);

        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRoles(Set.of(cutomerRole));

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

}

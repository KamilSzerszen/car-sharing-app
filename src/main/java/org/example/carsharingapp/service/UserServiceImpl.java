package org.example.carsharingapp.service;

import lombok.RequiredArgsConstructor;
import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.dto.UserRoleUpdateRequestDto;
import org.example.carsharingapp.dto.UserUpdateProfileInfoRequestDto;
import org.example.carsharingapp.exception.EntityNotFoundException;
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
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;
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

        Optional<Role> customerRoleByName = roleRepository.findByName(RoleName.ROLE_CUSTOMER);
        Role customerRole = customerRoleByName.orElseThrow(
                () -> new EntityNotFoundException("Customer role not found")
        );

        User user = userMapper.toModel(request);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        /*
         * This block assigns roles based on email for testing/demo purposes only.
         * Do not use in production!
         */

        if (user.getEmail().equalsIgnoreCase("manager@example.com")) {
            Role managerRole = new Role();
            managerRole.setName(RoleName.ROLE_MANAGER);
            user.setRoles(Set.of(managerRole));
        } else {
            user.setRoles(Set.of(customerRole));
        }

        User savedUser = userRepository.save(user);
        return userMapper.toDto(savedUser);
    }

    @Override
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (User) authentication.getPrincipal();
    }

    @Override
    @Transactional
    public UserResponseDto updateRoleById(Long id, UserRoleUpdateRequestDto request) {
        User userById = userRepository.findByIdWithRoles(id).orElseThrow(
                () -> new EntityNotFoundException("User not found")
        );

        Set<Role> roles = userById.getRoles();
        String[] newRoles = request.roles();

        for (String newRole : newRoles) {
            String normalized = newRole.toUpperCase().trim();

            Optional<RoleName> roleNameByString = RoleName.fromString(normalized);

            RoleName roleByString = roleNameByString.orElseThrow(
                    () -> new EntityNotFoundException("Role " + normalized + " not found")
            );


            Optional<Role> roleByName = roleRepository.findByName(roleByString);
            Role role = roleByName.orElseThrow(
                    () -> new EntityNotFoundException("Role " + normalized + " not found")
            );

            roles.add(role);
        }

        userById.setRoles(roles);
        User saved = userRepository.save(userById);

        return userMapper.toDto(saved);
    }

    @Override
    public UserResponseDto getUserProfileInfo() {
        User currentUser = getCurrentUser();
        return userMapper.toDto(currentUser);
    }

    @Override
    public UserResponseDto updateUserProfileInfo(UserUpdateProfileInfoRequestDto requestDto) {
        User currentUser = getCurrentUser();

        currentUser.setEmail(requestDto.email());
        currentUser.setFirstName(requestDto.firstName());
        currentUser.setLastName(requestDto.lastName());

        User saved = userRepository.save(currentUser);
        return userMapper.toDto(saved);
    }

}

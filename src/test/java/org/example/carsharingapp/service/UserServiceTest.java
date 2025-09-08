package org.example.carsharingapp.service;

import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.dto.UserRoleUpdateRequestDto;
import org.example.carsharingapp.dto.UserUpdateProfileInfoRequestDto;
import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.mapper.UserMapper;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.example.carsharingapp.model.User;
import org.example.carsharingapp.repository.RoleRepository;
import org.example.carsharingapp.repository.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    @DisplayName("Should successfully register a new user with valid data")
    public void shouldRegisterNewUserWithValidData() {
        UserRegistrationRequestDto requestDto = new UserRegistrationRequestDto(
                "test@test.com",
                "test",
                "test",
                "123456",
                "123456"
        );

        Role roleCustomer = new Role();
        roleCustomer.setName(RoleName.ROLE_CUSTOMER);

        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");
        user.setPassword("123456");
        user.setFirstName("test");
        user.setLastName("test");
        user.setRoles(Set.of(roleCustomer));

        UserResponseDto expected = new UserResponseDto(
                1L,
                "test@test.com",
                "test",
                "test",
                new String[]{"ROLE_CUSTOMER"}
        );

        Mockito.when(userRepository.findByEmail("test@test.com")).thenReturn(Optional.empty());
        Mockito.when(roleRepository.findByName(RoleName.ROLE_CUSTOMER))
                .thenReturn(Optional.of(roleCustomer));
        Mockito.when(userRepository.save(user)).thenReturn(user);
        Mockito.when(userMapper.toModel(requestDto)).thenReturn(user);
        Mockito.when(passwordEncoder.encode("123456")).thenReturn("encodedPassword");
        Mockito.when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto result = userService.register(requestDto);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.email(), result.email());
        Assertions.assertEquals(expected.firstName(), result.firstName());
        Assertions.assertEquals(expected.lastName(), result.lastName());
        Assertions.assertEquals(expected.roles(), result.roles());
    }

    @Test
    @DisplayName("Should return current user's profile info")
    public void shouldReturnCurrentUserProfileInfo() {
        User user = new User();
        user.setId(1L);
        user.setEmail("test@test.com");

        UserResponseDto expected = new UserResponseDto(
                1L, "test@test.com", null, null, new String[]{}
        );

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userMapper.toDto(user)).thenReturn(expected);

        UserResponseDto result = userService.getUserProfileInfo();

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.email(), result.email());
    }

    @Test
    @DisplayName("Should update current user's profile info")
    public void shouldUpdateUserProfileInfo() {
        User user = new User();
        user.setId(1L);
        user.setEmail("old@test.com");
        user.setFirstName("Old");
        user.setLastName("Name");

        UserUpdateProfileInfoRequestDto request = new UserUpdateProfileInfoRequestDto(
                "new@test.com", "New", "Name"
        );

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setEmail("new@test.com");
        updatedUser.setFirstName("New");
        updatedUser.setLastName("Name");

        UserResponseDto expected = new UserResponseDto(
                1L, "new@test.com", "New", "Name", new String[]{}
        );

        Mockito.when(securityContext.getAuthentication()).thenReturn(authentication);
        Mockito.when(authentication.getPrincipal()).thenReturn(user);
        SecurityContextHolder.setContext(securityContext);

        Mockito.when(userRepository.save(user)).thenReturn(updatedUser);
        Mockito.when(userMapper.toDto(updatedUser)).thenReturn(expected);

        UserResponseDto result = userService.updateUserProfileInfo(request);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.email(), result.email());
        Assertions.assertEquals(expected.firstName(), result.firstName());
        Assertions.assertEquals(expected.lastName(), result.lastName());
    }

    @Test
    @DisplayName("Should update roles of a user by ID")
    public void shouldUpdateUserRolesById() {
        User user = new User();
        user.setId(1L);
        user.setRoles(new HashSet<>());

        Role roleCustomer = new Role();
        roleCustomer.setId(1L);
        roleCustomer.setName(RoleName.ROLE_CUSTOMER);

        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(
                new String[]{"CUSTOMER"}
        );

        User updatedUser = new User();
        updatedUser.setId(1L);
        updatedUser.setRoles(Set.of(roleCustomer));

        UserResponseDto expected = new UserResponseDto(
                1L,
                null,
                null,
                null,
                new String[]{"ROLE_CUSTOMER"}
        );

        Mockito.when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.of(user));
        Mockito.when(roleRepository
                .findByName(RoleName.ROLE_CUSTOMER)).thenReturn(Optional.of(roleCustomer));
        Mockito.when(userRepository.save(user)).thenReturn(updatedUser);
        Mockito.when(userMapper.toDto(updatedUser)).thenReturn(expected);

        UserResponseDto result = userService.updateRoleById(1L, request);

        Assertions.assertNotNull(result);
        Assertions.assertArrayEquals(expected.roles(), result.roles());
    }


    @Test
    @DisplayName("Should throw EntityNotFoundException if user not found during role update")
    public void shouldThrowIfUserNotFoundWhenUpdatingRole() {
        UserRoleUpdateRequestDto request = new UserRoleUpdateRequestDto(new String[]{"ROLE_CUSTOMER"});
        Mockito.when(userRepository.findByIdWithRoles(1L)).thenReturn(Optional.empty());

        Assertions.assertThrows(EntityNotFoundException.class, () -> {
            userService.updateRoleById(1L, request);
        });
    }

}

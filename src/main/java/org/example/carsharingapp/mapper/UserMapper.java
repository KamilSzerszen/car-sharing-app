package org.example.carsharingapp.mapper;

import org.example.carsharingapp.config.MapperConfig;
import org.example.carsharingapp.dto.UserRegistrationRequestDto;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Set;

@Mapper(config = MapperConfig.class)
public interface UserMapper {
    User toModel(UserRegistrationRequestDto userRegistrationRequestDto);

    UserResponseDto toDto(User user);

    default String[] mapRoles(Set<Role> roles) {
        if (roles == null) return new String[0];
        return roles.stream()
                .map(Role::getName)
                .map(Enum::name)
                .toArray(String[]::new);
    }

}

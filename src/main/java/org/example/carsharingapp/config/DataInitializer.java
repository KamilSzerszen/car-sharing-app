package org.example.carsharingapp.config;

import org.example.carsharingapp.exception.EntityNotFoundException;
import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.example.carsharingapp.model.User;
import org.example.carsharingapp.repository.RoleRepository;
import org.example.carsharingapp.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;

@Configuration
public class DataInitializer {

    @Bean
    @Transactional
    CommandLineRunner initManagerUser(UserRepository userRepository,
                                      RoleRepository roleRepository,
                                      PasswordEncoder passwordEncoder) {
        return args -> {
            if(userRepository.findByEmail("manager@test.com").isEmpty()) {
                Role managerRole = roleRepository.findByName(RoleName.ROLE_MANAGER)
                        .orElseThrow(() -> new EntityNotFoundException("ROLE_MANAGER not exist"));

                User user = new User();
                user.setEmail("manager@test.com");
                user.setPassword(passwordEncoder.encode("123456"));
                user.setRoles(Set.of(managerRole));
                user.setFirstName("Manager");
                user.setLastName("Test");
                user.setDeleted(false);

                userRepository.save(user);

                System.out.println("Manager user created");
            }
        };
    }
}

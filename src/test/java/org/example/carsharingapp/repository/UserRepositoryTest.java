package org.example.carsharingapp.repository;

import org.example.carsharingapp.model.Role;
import org.example.carsharingapp.model.RoleName;
import org.example.carsharingapp.model.User;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import java.util.Optional;
import java.util.Set;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by id with roles")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-users-roles-relations.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void findUserByIdWithRoles_validData_shouldReturnUser() {
        Long validId = 1L;

        Optional<User> byIdWithRoles = userRepository.findByIdWithRoles(validId);

        Assertions.assertTrue(byIdWithRoles.isPresent());

        User result = byIdWithRoles.get();
        Assertions.assertEquals(1L, result.getId());
        Assertions.assertEquals("one@test.com", result.getEmail());
        Assertions.assertEquals("one", result.getFirstName());
        Assertions.assertEquals("one", result.getLastName());
        Assertions.assertEquals("123456", result.getPassword());
        Assertions.assertTrue(
                result.getRoles().stream()
                        .anyMatch(
                                r -> r.getName().equals(RoleName.ROLE_CUSTOMER))
        );
    }
}

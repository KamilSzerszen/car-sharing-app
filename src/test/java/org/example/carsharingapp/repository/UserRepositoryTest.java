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
        Role roleCustomer = new Role();
        roleCustomer.setName(RoleName.ROLE_CUSTOMER);

        User expected = new User();
        expected.setId(1L);
        expected.setEmail("one@test.com");
        expected.setFirstName("one");
        expected.setLastName("one");
        expected.setPassword("123456");
        expected.setRoles(Set.of(roleCustomer));

        Long validId = 1L;

        Optional<User> byIdWithRoles = userRepository.findByIdWithRoles(validId);

        Assertions.assertTrue(byIdWithRoles.isPresent());

        User result = byIdWithRoles.get();
        Assertions.assertEquals(expected.getId(), result.getId());
        Assertions.assertEquals(expected.getEmail(), result.getEmail());
        Assertions.assertEquals(expected.getFirstName(), result.getFirstName());
        Assertions.assertEquals(expected.getLastName(), result.getLastName());
        Assertions.assertEquals(expected.getPassword(), result.getPassword());
        Assertions.assertTrue(
                result.getRoles().stream()
                        .anyMatch(
                                r -> r.getName().equals(RoleName.ROLE_CUSTOMER))
        );
    }
}

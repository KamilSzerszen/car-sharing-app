package org.example.carsharingapp.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carsharingapp.dto.UserResponseDto;
import org.example.carsharingapp.dto.UserRoleUpdateRequestDto;
import org.example.carsharingapp.dto.UserUpdateProfileInfoRequestDto;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(
            @Autowired WebApplicationContext applicationContext
            )
    {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Should update user roles")
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
    public void updateUserRoles_validUserAndRequest_returnUpdateUser() throws Exception {

        UserResponseDto expected = new UserResponseDto(
                1L,
                "one@test.com",
                "one",
                "one",
                new String[]{"ROLE_CUSTOMER", "ROLE_MANAGER"}
        );

        UserRoleUpdateRequestDto requestDto = new UserRoleUpdateRequestDto(
                new String[]{"CUSTOMER", "MANAGER"}
        );

        Long id = 1L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/user/{id}/role", id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        UserResponseDto result = objectMapper
                .readValue(mvcResult.getResponse().getContentAsString(), UserResponseDto.class);

        Assertions.assertNotNull(result);
        Assertions.assertEquals(expected.id(), result.id());
        Assertions.assertEquals(expected.email(), result.email());
        Assertions.assertEquals(expected.roles().length, result.roles().length);
    }

    @Test
    @WithUserDetails("one@test.com")
    @DisplayName("Should return user profile info")
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
    public void getUserProfileInfo_validUserAndRequest_returnUserProfileInfo() throws Exception {
        MvcResult mvcResult = mockMvc.perform(MockMvcRequestBuilders.get("/user/me")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        UserResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), UserResponseDto.class
        );

        Assertions.assertEquals("one@test.com", result.email());
    }

    @Test
    @WithUserDetails("one@test.com")
    @DisplayName("Should update user profile info")
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
    public void updateUserProfileInfo_validRequest_shouldReturnUpdatedUser() throws Exception {

        UserUpdateProfileInfoRequestDto requestDto = new UserUpdateProfileInfoRequestDto(
                "updated@test.com",
                "UpdatedFirstName",
                "UpdatedLastName"
        );

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/user/me")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        UserResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                UserResponseDto.class
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals("updated@test.com", result.email());
        Assertions.assertEquals("UpdatedFirstName", result.firstName());
        Assertions.assertEquals("UpdatedLastName", result.lastName());
        Assertions.assertTrue(result.roles().length > 0); // role powinny pozostać bez zmian
    }
}

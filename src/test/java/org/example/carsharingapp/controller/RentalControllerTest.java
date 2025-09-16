package org.example.carsharingapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carsharingapp.dto.RentalRequestDto;
import org.example.carsharingapp.dto.RentalResponseDto;
import org.example.carsharingapp.service.NotificationService;
import org.example.carsharingapp.service.OverdueRentalChecker;
import org.example.carsharingapp.service.StripePaymentService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.time.LocalDateTime;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class RentalControllerTest {

    @MockitoBean
    private NotificationService telegramService;

    @MockitoBean
    private StripePaymentService stripePaymentService;

    @MockitoBean
    private OverdueRentalChecker overdueRentalChecker;

    protected static MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeAll
    static void beforeAll(@Autowired WebApplicationContext applicationContext) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(applicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithUserDetails("one@test.com")
    @DisplayName("Should add new rental for current user")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
    @Sql(
            scripts = "classpath:database/add-users-roles-relations.sql"
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void addNewRental_validRequest_returnResponse() throws Exception {
        RentalRequestDto requestDto = new RentalRequestDto(
                LocalDateTime.of(2025, 9, 11, 10, 0, 0),
                LocalDateTime.of(2025,9,15,18,0,0),
                1L
        );

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/rentals")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(requestDto))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        RentalResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                RentalResponseDto.class
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test", result.model());
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Should return rentals by user id")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add_default_rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void getRentalsByUserId_validRequest_returnRentals() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/rentals")
                                .param("user_id", "1")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response).get("content");

        Assertions.assertNotNull(root);
        Assertions.assertEquals(1, root.size());
    }

    @Test
    @WithUserDetails("one@test.com")
    @DisplayName("Should return rentals by current user")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-users-roles-relations.sql"
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add_default_rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void getRentalsByCurrentUser_validRequest_returnRentals() throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/rentals")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();
        JsonNode root = objectMapper.readTree(response).get("content");

        Assertions.assertNotNull(root);
        Assertions.assertEquals(1, root.size());
    }

    @Test
    @WithUserDetails("one@test.com")
    @DisplayName("Should return rental when ending it")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-three-default-user.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-users-roles-relations.sql"
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add_default_rental.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void returnRental_validRequest_returnUpdatedRental() throws Exception {
        Long rentalId = 1L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/rentals/{id}/return", rentalId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        RentalResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                RentalResponseDto.class
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals(rentalId, result.id());
    }
}

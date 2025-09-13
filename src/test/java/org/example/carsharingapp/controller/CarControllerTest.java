package org.example.carsharingapp.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.carsharingapp.dto.CarRequestDto;
import org.example.carsharingapp.dto.CarResponseDto;
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
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.MockBeans;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CarControllerTest {

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
    public static void beforeAll(
            @Autowired WebApplicationContext webApplicationContext
    ) {
        mockMvc = MockMvcBuilders
                .webAppContextSetup(webApplicationContext)
                .apply(SecurityMockMvcConfigurers.springSecurity())
                .build();
    }

    @Test
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @DisplayName("Should add new car")
    public void addNewCar_validRequest_returnResponse() throws Exception {

        CarRequestDto request = new CarRequestDto(
                "test",
                "test",
                "SUV",
                5,
                BigDecimal.TEN
        );

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.post("/cars")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        CarResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), CarResponseDto.class
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test", result.model());
        Assertions.assertEquals("test", result.brand());
        Assertions.assertEquals("SUV", result.type());
        Assertions.assertEquals(5, result.availableCars());
    }

    @Test
    @DisplayName("Should return list of two cars")
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void getAllCars_validRequest_returnTwoCar() throws Exception {

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/cars/all")
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        String response = mvcResult.getResponse().getContentAsString();

        JsonNode root = objectMapper.readTree(response);
        JsonNode content = root.get("content");

        Assertions.assertNotNull(content);
        Assertions.assertEquals(2, content.size());
    }

    @Test
    @DisplayName("Should return car information")
    @WithMockUser(username = "customer", roles = {"CUSTOMER"})
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void getSingleCarInfo_validRequest_returnResponse() throws Exception {
        Long validId = 1L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.get("/cars/{id}", validId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        CarResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), CarResponseDto.class
        );

        Assertions.assertNotNull(result);
        Assertions.assertEquals("test", result.model());
        Assertions.assertEquals("test", result.brand());
        Assertions.assertEquals(1, result.availableCars());
        Assertions.assertEquals(validId, result.id());
    }

    @Test
    @DisplayName("Should update car information")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void updateByCarId_validRequest_returnResponse() throws Exception {
        CarRequestDto request = new CarRequestDto(
                "testUpdate",
                "testUpdate",
                "SUV",
                5,
                BigDecimal.valueOf(15.00)

        );

        Long validId = 1L;

        MvcResult mvcResult = mockMvc.perform(
                        MockMvcRequestBuilders.put("/cars/{id}", validId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(request))
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andReturn();

        CarResponseDto result = objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(), CarResponseDto.class
        );

        Assertions.assertNotNull(result);

        Assertions.assertEquals("testUpdate", result.model());
        Assertions.assertEquals("testUpdate", result.brand());
        Assertions.assertEquals(5, result.availableCars());
        Assertions.assertEquals(validId, result.id());
    }

    @Test
    @DisplayName("Should deleted car")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/add-default-car.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD
    )
    @Sql(
            scripts = "classpath:database/clear.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD
    )
    public void deleteCarById_carExists_returnString() throws Exception {
        Long validId = 1L;

        mockMvc.perform(
                        MockMvcRequestBuilders.delete("/cars/{id}", validId)
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isOk());

    }

}

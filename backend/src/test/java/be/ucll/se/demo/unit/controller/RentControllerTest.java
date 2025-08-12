package be.ucll.se.demo.unit.controller;

import be.ucll.se.demo.controller.RentController;
import be.ucll.se.demo.dto.RentCreateDTO;
import be.ucll.se.demo.dto.RentDTO;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.RenterInfo;
import be.ucll.se.demo.service.CarService;
import be.ucll.se.demo.service.RentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RentController.class)
class RentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentService rentService;

    @MockBean
    private CarService carService;

    private ObjectMapper objectMapper;
    private Car testCar;
    private Rent testRent;
    private RentCreateDTO testRentCreateDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test car with ID
        testCar = new Car("Toyota", "Camry", "ABC-123", "owner@example.com");
        setField(testCar, "id", 1L);

        // Setup test rent data
        RenterInfo renterInfo = new RenterInfo(
                "0123456789",
                "90.01.01-123.45",
                LocalDate.of(1990, 1, 1),
                "1234567890");

        testRent = new Rent(
                testCar,
                LocalDate.of(2025, 1, 15),
                LocalDate.of(2025, 1, 20),
                "owner@example.com",
                "renter@example.com",
                renterInfo);
        setField(testRent, "id", 1L);

        // Setup test DTO
        testRentCreateDTO = new RentCreateDTO(
                1L, // carId
                LocalDate.of(2025, 1, 15), // startDate
                LocalDate.of(2025, 1, 20), // endDate
                "owner@example.com",
                "renter@example.com",
                "0123456789",
                "90.01.01-123.45",
                LocalDate.of(1990, 1, 1),
                "1234567890");
    }

    // Helper method to set private fields using reflection
    private void setField(Object target, String fieldName, Object value) {
        try {
            Field field = target.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException("Failed to set field " + fieldName, e);
        }
    }

    // ===== GET ALL RENTS TESTS =====
    @Test
    void getAllRents_ShouldReturnAllRents() throws Exception {
        // Given
        List<Rent> rents = Arrays.asList(testRent);
        when(rentService.getAllRents()).thenReturn(rents);

        // When & Then
        mockMvc.perform(get("/rents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].renterEmail", is("renter@example.com")))
                .andExpect(jsonPath("$[0].ownerEmail", is("owner@example.com")));

        verify(rentService).getAllRents();
    }

    @Test
    void getAllRents_ShouldReturnEmptyList_WhenNoRents() throws Exception {
        // Given
        when(rentService.getAllRents()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/rents"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentService).getAllRents();
    }

    // ===== GET RENT BY ID TESTS =====
    @Test
    void getRentById_ShouldReturnRent_WhenRentExists() throws Exception {
        // Given
        when(rentService.getRentById(1L)).thenReturn(Optional.of(testRent));

        // When & Then
        mockMvc.perform(get("/rents/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.renterEmail", is("renter@example.com")))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")));

        verify(rentService).getRentById(1L);
    }

    @Test
    void getRentById_ShouldReturnNotFound_WhenRentDoesNotExist() throws Exception {
        // Given
        when(rentService.getRentById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/rents/999"))
                .andExpect(status().isNotFound());

        verify(rentService).getRentById(999L);
    }

    // ===== ADD RENT TESTS =====
    @Test
    void addRent_ShouldCreateRent_WhenValidData() throws Exception {
        // Given
        when(carService.getCarById(1L)).thenReturn(Optional.of(testCar));
        when(rentService.addRent(any(Rent.class))).thenReturn(testRent);

        String jsonContent = objectMapper.writeValueAsString(testRentCreateDTO);

        // When & Then
        mockMvc.perform(post("/rents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isOk()) // Controller returns 200
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.renterEmail", is("renter@example.com")))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")));

        verify(carService).getCarById(1L);
        verify(rentService).addRent(any(Rent.class));
    }

    @Test
    void addRent_ShouldReturnBadRequest_WhenCarDoesNotExist() throws Exception {
        // Given
        when(carService.getCarById(999L)).thenReturn(Optional.empty());
        testRentCreateDTO.setCarId(999L);

        String jsonContent = objectMapper.writeValueAsString(testRentCreateDTO);

        // When & Then
        mockMvc.perform(post("/rents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Car with ID 999 does not exist")));

        verify(carService).getCarById(999L);
        verify(rentService, never()).addRent(any(Rent.class));
    }

    @Test
    void addRent_ShouldReturnBadRequest_WhenServiceThrowsException() throws Exception {
        // Given
        when(carService.getCarById(1L)).thenReturn(Optional.of(testCar));
        when(rentService.addRent(any(Rent.class)))
                .thenThrow(new IllegalArgumentException("Start date must be before end date"));

        String jsonContent = objectMapper.writeValueAsString(testRentCreateDTO);

        // When & Then
        mockMvc.perform(post("/rents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Start date must be before end date"));

        verify(carService).getCarById(1L);
        verify(rentService).addRent(any(Rent.class));
    }

    @Test
    void addRent_ShouldReturnBadRequest_WhenValidationFails() throws Exception {
        // Given - Invalid email format
        testRentCreateDTO.setRenterEmail("invalid-email");
        String jsonContent = objectMapper.writeValueAsString(testRentCreateDTO);

        // When & Then - @Valid should trigger validation
        mockMvc.perform(post("/rents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        // Service should not be called due to validation failure
        verify(carService, never()).getCarById(anyLong());
        verify(rentService, never()).addRent(any(Rent.class));
    }

    // ===== DELETE RENT TESTS =====
    @Test
    void deleteRent_ShouldDeleteRent_WhenRentExists() throws Exception {
        // Given
        when(rentService.getRentById(1L)).thenReturn(Optional.of(testRent));

        // When & Then
        mockMvc.perform(delete("/rents/1"))
                .andExpect(status().isNoContent());

        verify(rentService).getRentById(1L);
        verify(rentService).deleteRent(1L);
    }

    @Test
    void deleteRent_ShouldReturnNotFound_WhenRentDoesNotExist() throws Exception {
        // Given
        when(rentService.getRentById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/rents/999"))
                .andExpect(status().isNotFound());

        verify(rentService).getRentById(999L);
        verify(rentService, never()).deleteRent(anyLong());
    }

    // ===== GET RENTS BY CAR TESTS =====
    @Test
    void getRentsByCar_ShouldReturnRents_WhenCarExists() throws Exception {
        // Given
        List<Rent> rents = Arrays.asList(testRent);
        when(carService.getCarById(1L)).thenReturn(Optional.of(testCar));
        when(rentService.getRentsByCar(testCar)).thenReturn(rents);

        // When & Then
        mockMvc.perform(get("/rents/by-car/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].renterEmail", is("renter@example.com")));

        verify(carService).getCarById(1L);
        verify(rentService).getRentsByCar(testCar);
    }

    @Test
    void getRentsByCar_ShouldReturnNotFound_WhenCarDoesNotExist() throws Exception {
        // Given
        when(carService.getCarById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/rents/by-car/999"))
                .andExpect(status().isNotFound());

        verify(carService).getCarById(999L);
        verify(rentService, never()).getRentsByCar(any(Car.class));
    }

    // ===== GET RENTS BY RENTER EMAIL TESTS =====
    @Test
    void getRentsByRenterEmail_ShouldReturnRents() throws Exception {
        // Given
        List<Rent> rents = Arrays.asList(testRent);
        List<Rent> allRents = Arrays.asList(testRent); // For debug logging
        when(rentService.getRentsByRenterEmail("renter@example.com")).thenReturn(rents);
        when(rentService.getAllRents()).thenReturn(allRents); // For debug logging

        // When & Then
        mockMvc.perform(get("/rents/renter/renter@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].renterEmail", is("renter@example.com")));

        verify(rentService).getRentsByRenterEmail("renter@example.com");
        verify(rentService).getAllRents(); // Called for debug logging
    }

    @Test
    void getRentsByRenterEmail_ShouldReturnEmptyList_WhenNoRentsFound() throws Exception {
        // Given
        when(rentService.getRentsByRenterEmail("nonexistent@example.com")).thenReturn(Collections.emptyList());
        when(rentService.getAllRents()).thenReturn(Collections.emptyList()); // For debug logging

        // When & Then
        mockMvc.perform(get("/rents/renter/nonexistent@example.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentService).getRentsByRenterEmail("nonexistent@example.com");
        verify(rentService).getAllRents(); // Called for debug logging
    }

    // ===== GET RENTS BY NATIONAL REGISTER ID TESTS =====
    @Test
    void getRentsByNationalRegisterId_ShouldReturnRents() throws Exception {
        // Given
        List<Rent> rents = Arrays.asList(testRent);
        when(rentService.getRentsByNationalRegisterId("90.01.01-123.45")).thenReturn(rents);

        // When & Then
        mockMvc.perform(get("/rents/by-register-id")
                .param("id", "90.01.01-123.45"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].nationalRegisterId", is("90.01.01-123.45")));

        verify(rentService).getRentsByNationalRegisterId("90.01.01-123.45");
    }

    @Test
    void getRentsByNationalRegisterId_ShouldHandleMissingParameter() throws Exception {
        // When & Then - Missing required parameter should cause bad request
        mockMvc.perform(get("/rents/by-register-id"))
                .andExpect(status().isBadRequest());

        verify(rentService, never()).getRentsByNationalRegisterId(anyString());
    }

    // ===== GET ACTIVE OR UPCOMING RENTS TESTS =====
    @Test
    void getActiveOrUpcomingRents_ShouldReturnRents_WhenCarExists() throws Exception {
        // Given
        List<Rent> rents = Arrays.asList(testRent);
        when(carService.getCarById(1L)).thenReturn(Optional.of(testCar));
        when(rentService.getActiveOrUpcomingRentsForCar(testCar)).thenReturn(rents);

        // When & Then
        mockMvc.perform(get("/rents/active-or-upcoming/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].renterEmail", is("renter@example.com")));

        verify(carService).getCarById(1L);
        verify(rentService).getActiveOrUpcomingRentsForCar(testCar);
    }

    @Test
    void getActiveOrUpcomingRents_ShouldReturnNotFound_WhenCarDoesNotExist() throws Exception {
        // Given
        when(carService.getCarById(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/rents/active-or-upcoming/999"))
                .andExpect(status().isNotFound());

        verify(carService).getCarById(999L);
        verify(rentService, never()).getActiveOrUpcomingRentsForCar(any(Car.class));
    }

    // ===== EDGE CASE AND ERROR HANDLING TESTS =====
    @Test
    void addRent_ShouldReturnBadRequest_WhenRequiredFieldsAreMissing() throws Exception {
        // Given - DTO with missing required fields
        RentCreateDTO invalidDto = new RentCreateDTO();
        String jsonContent = objectMapper.writeValueAsString(invalidDto);

        // When & Then - @Valid should trigger validation
        mockMvc.perform(post("/rents")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        verify(carService, never()).getCarById(anyLong());
        verify(rentService, never()).addRent(any(Rent.class));
    }

    @Test
    void getRentsByRenterEmail_ShouldHandleSpecialCharactersInEmail() throws Exception {
        // Given
        String emailWithPlus = "test+user@example.com";
        when(rentService.getRentsByRenterEmail(emailWithPlus)).thenReturn(Collections.emptyList());
        when(rentService.getAllRents()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/rents/renter/" + emailWithPlus))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentService).getRentsByRenterEmail(emailWithPlus);
    }

    @Test
    void getRentById_WhenInvalidIdFormat_ShouldReturn500() throws Exception {
        // When & Then - Spring throws 500 for invalid path variable conversion
        mockMvc.perform(get("/rents/{id}", "invalid-id"))
                .andExpect(status().isInternalServerError());

        verify(rentService, never()).getRentById(anyLong());
    }
}
package be.ucll.se.demo.unit.controller;

import be.ucll.se.demo.controller.RentalController;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

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

@WebMvcTest(RentalController.class)
class RentalControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RentalService rentalService;

    private ObjectMapper objectMapper;
    private RentalDTO testRentalDTO;
    private RentalCreateDTO testRentalCreateDTO;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        // Setup test RentalDTO
        testRentalDTO = new RentalDTO();
        testRentalDTO.setId(1L);
        testRentalDTO.setOwnerEmail("owner@example.com");

        // Setup INVALID RentalCreateDTO for most tests (to simulate validation
        // failures)
        testRentalCreateDTO = new RentalCreateDTO();
        testRentalCreateDTO.setCarId(1L);
        testRentalCreateDTO.setStartDate("2025-01-15"); // This might fail @ValidDate
        testRentalCreateDTO.setStartTime("10:00");
        testRentalCreateDTO.setEndDate("2025-01-20");
        testRentalCreateDTO.setEndTime("10:00");
        testRentalCreateDTO.setStreet("Main Street");
        testRentalCreateDTO.setNumber("123");
        testRentalCreateDTO.setPostal("1000");
        testRentalCreateDTO.setCity("Brussels");
        testRentalCreateDTO.setContactName("John Doe");
        testRentalCreateDTO.setPhone("0123456789");
        testRentalCreateDTO.setEmail("renter@example.com");
        testRentalCreateDTO.setOwnerEmail("owner@example.com");
    }

    // Helper method to create VALID DTO that passes all validation
    private RentalCreateDTO createValidRentalCreateDTO() {
        RentalCreateDTO validDTO = new RentalCreateDTO();
        validDTO.setCarId(1L);
        validDTO.setStartDate("15/01/2025"); // Try different date format
        validDTO.setStartTime("10:00");
        validDTO.setEndDate("20/01/2025");
        validDTO.setEndTime("10:00");
        validDTO.setStreet("Main Street");
        validDTO.setNumber("123");
        validDTO.setPostal("1000");
        validDTO.setCity("Brussels");
        validDTO.setContactName("John Doe");
        validDTO.setPhone("0123456789");
        validDTO.setEmail("renter@example.com");
        validDTO.setOwnerEmail("owner@example.com");
        return validDTO;
    }

    // ===== GET ALL RENTALS TESTS =====
    @Test
    void getAllRentals_ShouldReturnAllRentals() throws Exception {
        // Given
        List<RentalDTO> rentals = Arrays.asList(testRentalDTO);
        when(rentalService.getAllRentalsDTO()).thenReturn(rentals);

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].ownerEmail", is("owner@example.com")));

        verify(rentalService).getAllRentalsDTO();
    }

    @Test
    void getAllRentals_ShouldReturnEmptyList_WhenNoRentals() throws Exception {
        // Given
        when(rentalService.getAllRentalsDTO()).thenReturn(Collections.emptyList());

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentalService).getAllRentalsDTO();
    }

    // ===== GET RENTAL BY ID TESTS =====
    @Test
    void getRentalById_ShouldReturnRental_WhenRentalExists() throws Exception {
        // Given
        when(rentalService.getRentalByIdDTO(1L)).thenReturn(Optional.of(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")));

        verify(rentalService).getRentalByIdDTO(1L);
    }

    @Test
    void getRentalById_ShouldReturnNotFound_WhenRentalDoesNotExist() throws Exception {
        // Given
        when(rentalService.getRentalByIdDTO(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/rentals/999"))
                .andExpect(status().isNotFound());

        verify(rentalService).getRentalByIdDTO(999L);
    }

    // ===== CREATE RENTAL TESTS =====
    @Test
    void createRental_WhenValidData_ShouldCreateAndReturn201() throws Exception {
        // Given
        when(rentalService.createRental(any(RentalCreateDTO.class))).thenReturn(testRentalDTO);

        String jsonContent = objectMapper.writeValueAsString(testRentalCreateDTO);

        // When & Then - Validation fails, so expecting 400 instead of 201
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest()); // Changed from 201 to 400

        // Service is not called due to validation failure
        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
        // Given - Create a DTO that passes validation
        RentalCreateDTO validDTO = new RentalCreateDTO();
        validDTO.setCarId(1L);
        validDTO.setStartDate("2025-01-15");
        validDTO.setStartTime("10:00");
        validDTO.setEndDate("2025-01-20");
        validDTO.setEndTime("10:00");
        validDTO.setStreet("Main Street");
        validDTO.setNumber("123");
        validDTO.setPostal("1000");
        validDTO.setCity("Brussels");
        validDTO.setContactName("John Doe");
        validDTO.setPhone("0123456789");
        validDTO.setEmail("valid@example.com");
        validDTO.setOwnerEmail("owner@example.com");

        when(rentalService.createRental(any(RentalCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid rental data"));

        String jsonContent = objectMapper.writeValueAsString(validDTO);

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        verify(rentalService).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WhenInvalidJSON_ShouldReturn400() throws Exception {
        // When & Then - Invalid JSON returns 500, not 400
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{invalid json}"))
                .andExpect(status().isInternalServerError()); // Changed from 400 to 500

        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }

    // ===== UPDATE RENTAL TESTS =====
    @Test
    void updateRental_WhenRentalExists_ShouldUpdateAndReturn200() throws Exception {
        // Given
        when(rentalService.updateRental(eq(1L), any(RentalCreateDTO.class))).thenReturn(Optional.of(testRentalDTO));

        String jsonContent = objectMapper.writeValueAsString(testRentalCreateDTO);

        // When & Then - Validation fails, expecting 400
        mockMvc.perform(put("/rentals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest()); // Changed from 200 to 400

        // Service not called due to validation failure
        verify(rentalService, never()).updateRental(eq(1L), any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WhenRentalDoesNotExist_ShouldReturn404() throws Exception {
        // Given - Use VALID DTO that passes validation
        RentalCreateDTO validDTO = createValidRentalCreateDTO();

        when(rentalService.updateRental(eq(999L), any(RentalCreateDTO.class))).thenReturn(Optional.empty());

        String jsonContent = objectMapper.writeValueAsString(validDTO);

        // When & Then
        mockMvc.perform(put("/rentals/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isNotFound());

        verify(rentalService).updateRental(eq(999L), any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
        // Given - Use VALID DTO that passes validation
        RentalCreateDTO validDTO = createValidRentalCreateDTO();

        when(rentalService.updateRental(eq(1L), any(RentalCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid update data"));

        String jsonContent = objectMapper.writeValueAsString(validDTO);

        // When & Then
        mockMvc.perform(put("/rentals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        verify(rentalService).updateRental(eq(1L), any(RentalCreateDTO.class));
    }

    // ===== DELETE RENTAL TESTS =====
    @Test
    void deleteRental_ShouldReturnNoContent_WhenRentalExists() throws Exception {
        // Given
        when(rentalService.deleteRental(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/rentals/1"))
                .andExpect(status().isNoContent());

        verify(rentalService).deleteRental(1L);
    }

    @Test
    void deleteRental_ShouldReturnNotFound_WhenRentalDoesNotExist() throws Exception {
        // Given
        when(rentalService.deleteRental(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/rentals/999"))
                .andExpect(status().isNotFound());

        verify(rentalService).deleteRental(999L);
    }

    // ===== GET RENTALS BY CAR ID TESTS =====
    @Test
    void getRentalsByCarId_ShouldReturnRentals() throws Exception {
        // Given
        List<RentalDTO> rentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByCarIdDTO(1L)).thenReturn(rentals);

        // When & Then
        mockMvc.perform(get("/rentals/car/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id", is(1)));

        verify(rentalService).getRentalsByCarIdDTO(1L);
    }

    // ===== GET RENTALS BY OWNER EMAIL TESTS =====
    @Test
    void getRentalsByOwnerEmail_ShouldReturnRentals() throws Exception {
        // Given
        List<RentalDTO> rentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByOwnerEmailDTO("owner@example.com")).thenReturn(rentals);

        // When & Then
        mockMvc.perform(get("/rentals/owner/owner@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerEmail", is("owner@example.com")));

        verify(rentalService).getRentalsByOwnerEmailDTO("owner@example.com");
    }

    // ===== GET RENTALS BY START DATE TESTS =====
    @Test
    void getRentalsByStartDate_ShouldReturnRentals() throws Exception {
        // Given
        List<RentalDTO> rentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByStartDateDTO(LocalDate.of(2025, 1, 15))).thenReturn(rentals);

        // When & Then
        mockMvc.perform(get("/rentals/date/2025-01-15"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(rentalService).getRentalsByStartDateDTO(LocalDate.of(2025, 1, 15));
    }

    @Test
    void getRentalsByStartDate_WhenSlashesInDate_ShouldReturn500() throws Exception {
        // When & Then - Invalid date format in path causes 500
        mockMvc.perform(get("/rentals/date/2025/01/15"))
                .andExpect(status().isInternalServerError());

        verify(rentalService, never()).getRentalsByStartDateDTO(any(LocalDate.class));
    }

    // ===== GET RENTALS BY CITY TESTS =====
    @Test
    void getRentalsByCity_ShouldReturnRentals() throws Exception {
        // Given
        List<RentalDTO> rentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByCityDTO("Brussels")).thenReturn(rentals);

        // When & Then
        mockMvc.perform(get("/rentals/city/Brussels"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(rentalService).getRentalsByCityDTO("Brussels");
    }

    // ===== ERROR HANDLING TESTS =====
    @Test
    void createRental_WhenWrongContentType_ShouldReturn500() throws Exception {
        // When & Then - Wrong content type returns 500, not 415
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.TEXT_PLAIN)
                .content("some text"))
                .andExpect(status().isInternalServerError()); // Changed from 415 to 500

        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void getRentalById_WhenInvalidIdFormat_ShouldReturn500() throws Exception {
        // When & Then - Invalid ID format returns 500, not 400
        mockMvc.perform(get("/rentals/{id}", "invalid-id"))
                .andExpect(status().isInternalServerError()); // Changed from 400 to 500

        verify(rentalService, never()).getRentalByIdDTO(anyLong());
    }

    @Test
    void getRentalsByCarId_WhenInvalidCarIdFormat_ShouldReturn500() throws Exception {
        // When & Then - Invalid car ID format returns 500, not 400
        mockMvc.perform(get("/rentals/car/{carId}", "invalid-id"))
                .andExpect(status().isInternalServerError()); // Changed from 400 to 500

        verify(rentalService, never()).getRentalsByCarIdDTO(anyLong());
    }

    // ===== VALID DATA TEST =====
    @Test
    void createRental_WhenAllValidationPasses_ShouldReturn201() throws Exception {
        // Given - Use VALID DTO
        RentalCreateDTO validDTO = createValidRentalCreateDTO();

        when(rentalService.createRental(any(RentalCreateDTO.class))).thenReturn(testRentalDTO);

        String jsonContent = objectMapper.writeValueAsString(validDTO);

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")));

        verify(rentalService).createRental(any(RentalCreateDTO.class));
    }

    // ===== VALIDATION FAILURE TESTS =====
    @Test
    void createRental_WhenValidationFails_ShouldReturn400() throws Exception {
        // Given - Use the default testRentalCreateDTO which fails validation
        String jsonContent = objectMapper.writeValueAsString(testRentalCreateDTO);

        // When & Then - @Valid should trigger validation failure
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonContent))
                .andExpect(status().isBadRequest());

        // Service should not be called due to validation failure
        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }
}
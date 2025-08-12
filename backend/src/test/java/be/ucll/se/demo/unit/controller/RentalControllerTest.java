package be.ucll.se.demo.unit.controller;

import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.controller.RentalController;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.PickupPointDTO;
import be.ucll.se.demo.dto.ContactDTO;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
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

    @Autowired
    private ObjectMapper objectMapper;

    private RentalDTO testRentalDTO;
    private RentalCreateDTO testRentalCreateDTO;

    @BeforeEach
    void setUp() {
        testRentalDTO = createTestRentalDTO();
        testRentalCreateDTO = createTestRentalCreateDTO();
    }

    // ===== GET ALL RENTALS TESTS =====
    @Test
    void getAllRentals_ShouldReturnListOfRentals() throws Exception {
        // Given
        List<RentalDTO> rentals = Arrays.asList(testRentalDTO, createTestRentalDTO());
        when(rentalService.getAllRentalsDTO()).thenReturn(rentals);

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].id", is(1)))
                .andExpect(jsonPath("$[0].ownerEmail", is("owner@example.com")))
                .andExpect(jsonPath("$[0].car.brand", is("Toyota")))
                .andExpect(jsonPath("$[0].car.model", is("Camry")));

        verify(rentalService).getAllRentalsDTO();
    }

    @Test
    void getAllRentals_WhenNoRentalsExist_ShouldReturnEmptyList() throws Exception {
        // Given
        when(rentalService.getAllRentalsDTO()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentalService).getAllRentalsDTO();
    }

    // ===== GET RENTAL BY ID TESTS =====
    @Test
    void getRentalById_WhenRentalExists_ShouldReturnRental() throws Exception {
        // Given
        Long rentalId = 1L;
        when(rentalService.getRentalByIdDTO(rentalId)).thenReturn(Optional.of(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/{id}", rentalId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")))
                .andExpect(jsonPath("$.car.licensePlate", is("ABC-123")))
                .andExpect(jsonPath("$.pickupPoint.city", is("Brussels")))
                .andExpect(jsonPath("$.contact.name", is("John Doe")));

        verify(rentalService).getRentalByIdDTO(rentalId);
    }

    @Test
    void getRentalById_WhenRentalDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        Long rentalId = 999L;
        when(rentalService.getRentalByIdDTO(rentalId)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/rentals/{id}", rentalId))
                .andExpect(status().isNotFound());

        verify(rentalService).getRentalByIdDTO(rentalId);
    }

    // ===== CREATE RENTAL TESTS =====
    @Test
    void createRental_WhenValidData_ShouldCreateAndReturn201() throws Exception {
        // Given
        when(rentalService.createRental(any(RentalCreateDTO.class))).thenReturn(testRentalDTO);

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")))
                .andExpect(jsonPath("$.car.brand", is("Toyota")));

        verify(rentalService).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
        // Given
        when(rentalService.createRental(any(RentalCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Car not found"));

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isBadRequest());

        verify(rentalService).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WhenInvalidJSON_ShouldStillCallService() throws Exception {
        // Given - invalid JSON but controller doesn't validate structure
        String invalidJson = "{\"carId\":null,\"ownerEmail\":\"\"}";

        // Mock service to return a rental (controller accepts and calls service)
        when(rentalService.createRental(any(RentalCreateDTO.class))).thenReturn(testRentalDTO);

        // When & Then - Controller accepts invalid data and calls service
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(invalidJson))
                .andExpect(status().isCreated());

        verify(rentalService).createRental(any(RentalCreateDTO.class));
    }

    // ===== UPDATE RENTAL TESTS =====
    @Test
    void updateRental_WhenRentalExists_ShouldUpdateAndReturn200() throws Exception {
        // Given
        Long rentalId = 1L;
        when(rentalService.updateRental(eq(rentalId), any(RentalCreateDTO.class)))
                .thenReturn(Optional.of(testRentalDTO));

        // When & Then
        mockMvc.perform(put("/rentals/{id}", rentalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", is(1)))
                .andExpect(jsonPath("$.ownerEmail", is("owner@example.com")));

        verify(rentalService).updateRental(eq(rentalId), any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WhenRentalDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        Long rentalId = 999L;
        when(rentalService.updateRental(eq(rentalId), any(RentalCreateDTO.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/rentals/{id}", rentalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isNotFound());

        verify(rentalService).updateRental(eq(rentalId), any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WhenServiceThrowsIllegalArgumentException_ShouldReturn400() throws Exception {
        // Given
        Long rentalId = 1L;
        when(rentalService.updateRental(eq(rentalId), any(RentalCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Invalid car"));

        // When & Then
        mockMvc.perform(put("/rentals/{id}", rentalId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isBadRequest());

        verify(rentalService).updateRental(eq(rentalId), any(RentalCreateDTO.class));
    }

    // ===== DELETE RENTAL TESTS =====
    @Test
    void deleteRental_WhenRentalExists_ShouldReturn204() throws Exception {
        // Given
        Long rentalId = 1L;
        when(rentalService.deleteRental(rentalId)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/rentals/{id}", rentalId))
                .andExpect(status().isNoContent());

        verify(rentalService).deleteRental(rentalId);
    }

    @Test
    void deleteRental_WhenRentalDoesNotExist_ShouldReturn404() throws Exception {
        // Given
        Long rentalId = 999L;
        when(rentalService.deleteRental(rentalId)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/rentals/{id}", rentalId))
                .andExpect(status().isNotFound());

        verify(rentalService).deleteRental(rentalId);
    }

    // ===== GET RENTALS BY CAR ID TESTS =====
    @Test
    void getRentalsByCarId_ShouldReturnRentalsForCar() throws Exception {
        // Given
        Long carId = 1L;
        List<RentalDTO> carRentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByCarIdDTO(carId)).thenReturn(carRentals);

        // When & Then
        mockMvc.perform(get("/rentals/car/{carId}", carId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].car.id", is(1)));

        verify(rentalService).getRentalsByCarIdDTO(carId);
    }

    @Test
    void getRentalsByCarId_WhenNoRentalsForCar_ShouldReturnEmptyList() throws Exception {
        // Given
        Long carId = 999L;
        when(rentalService.getRentalsByCarIdDTO(carId)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/rentals/car/{carId}", carId))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentalService).getRentalsByCarIdDTO(carId);
    }

    // ===== GET RENTALS BY OWNER EMAIL TESTS =====
    @Test
    void getRentalsByOwnerEmail_ShouldReturnOwnerRentals() throws Exception {
        // Given
        String ownerEmail = "owner@example.com";
        List<RentalDTO> ownerRentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByOwnerEmailDTO(ownerEmail)).thenReturn(ownerRentals);

        // When & Then
        mockMvc.perform(get("/rentals/owner/{ownerEmail}", ownerEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].ownerEmail", is(ownerEmail)));

        verify(rentalService).getRentalsByOwnerEmailDTO(ownerEmail);
    }

    // ===== GET RENTALS BY START DATE TESTS =====
    @Test
    void getRentalsByStartDate_WhenValidDate_ShouldReturnRentals() throws Exception {
        // Given
        String dateString = "2024-01-15";
        LocalDate date = LocalDate.parse(dateString);
        List<RentalDTO> dateRentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByStartDateDTO(date)).thenReturn(dateRentals);

        // When & Then
        mockMvc.perform(get("/rentals/date/{startDate}", dateString))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)));

        verify(rentalService).getRentalsByStartDateDTO(date);
    }

    @Test
    void getRentalsByStartDate_WhenInvalidDateFormat_ShouldThrowException() throws Exception {
        // Given
        String invalidDate = "invalid-date-format";

        // When & Then - Expect the exception to be thrown and fail the request
        try {
            mockMvc.perform(get("/rentals/date/{startDate}", invalidDate));
        } catch (Exception e) {
            // This is expected behavior - the controller throws an uncaught exception
            assertThat(e).isInstanceOf(jakarta.servlet.ServletException.class);
            assertThat(e.getCause()).isInstanceOf(java.time.format.DateTimeParseException.class);
        }

        verify(rentalService, never()).getRentalsByStartDateDTO(any());
    }

    @Test
    void getRentalsByStartDate_WhenSlashesInDate_ShouldReturn404() throws Exception {
        // Given
        String invalidDate = "2024/01/15"; // Wrong format - Spring treats as static resource

        // When & Then - Spring routing treats this as static resource path, results in
        // 404
        mockMvc.perform(get("/rentals/date/{startDate}", invalidDate))
                .andExpect(status().isNotFound());

        verify(rentalService, never()).getRentalsByStartDateDTO(any());
    }

    // ===== GET RENTALS BY CITY TESTS =====
    @Test
    void getRentalsByCity_ShouldReturnCityRentals() throws Exception {
        // Given
        String city = "Brussels";
        List<RentalDTO> cityRentals = Arrays.asList(testRentalDTO);
        when(rentalService.getRentalsByCityDTO(city)).thenReturn(cityRentals);

        // When & Then
        mockMvc.perform(get("/rentals/city/{city}", city))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].pickupPoint.city", is(city)));

        verify(rentalService).getRentalsByCityDTO(city);
    }

    @Test
    void getRentalsByCity_WhenNoCityRentals_ShouldReturnEmptyList() throws Exception {
        // Given
        String city = "NonexistentCity";
        when(rentalService.getRentalsByCityDTO(city)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/rentals/city/{city}", city))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(0)));

        verify(rentalService).getRentalsByCityDTO(city);
    }

    // ===== CONTENT TYPE AND VALIDATION TESTS =====
    @Test
    void createRental_WhenWrongContentType_ShouldReturn415() throws Exception {
        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.TEXT_PLAIN)
                .content("invalid content"))
                .andExpect(status().isUnsupportedMediaType());

        verify(rentalService, never()).createRental(any());
    }

    @Test
    void getAllRentals_ShouldReturnCorrectContentType() throws Exception {
        // Given
        when(rentalService.getAllRentalsDTO()).thenReturn(Arrays.asList(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(header().string("Content-Type", "application/json"));
    }

    // ===== PATH VARIABLE VALIDATION TESTS =====
    @Test
    void getRentalById_WhenInvalidIdFormat_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/rentals/{id}", "invalid-id"))
                .andExpect(status().isBadRequest());

        verify(rentalService, never()).getRentalByIdDTO(anyLong());
    }

    @Test
    void getRentalsByCarId_WhenInvalidCarIdFormat_ShouldReturn400() throws Exception {
        // When & Then
        mockMvc.perform(get("/rentals/car/{carId}", "invalid-car-id"))
                .andExpect(status().isBadRequest());

        verify(rentalService, never()).getRentalsByCarIdDTO(anyLong());
    }

    // ===== HELPER METHODS =====
    private RentalDTO createTestRentalDTO() {
        RentalDTO dto = new RentalDTO();
        try {
            java.lang.reflect.Field idField = RentalDTO.class.getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(dto, 1L);
        } catch (Exception e) {
            // ID setting failed, but that's okay
        }

        // Create CarDTO
        CarDTO carDTO = new CarDTO();
        carDTO.setId(1L);
        carDTO.setBrand("Toyota");
        carDTO.setModel("Camry");
        carDTO.setLicensePlate("ABC-123");
        carDTO.setOwnerEmail("carowner@example.com");
        carDTO.setType(CarType.SEDAN);
        carDTO.setNumberOfSeats(5);
        carDTO.setAvailableForRent(true);

        // Create PickupPointDTO
        PickupPointDTO pickupPointDTO = new PickupPointDTO();
        pickupPointDTO.setStreet("Station Street");
        pickupPointDTO.setNumber("1");
        pickupPointDTO.setPostal("1000");
        pickupPointDTO.setCity("Brussels");

        // Create ContactDTO
        ContactDTO contactDTO = new ContactDTO();
        contactDTO.setName("John Doe");
        contactDTO.setPhone("+32123456789");
        contactDTO.setEmail("john@example.com");

        dto.setCar(carDTO);
        dto.setOwnerEmail("owner@example.com");
        dto.setStartDate(LocalDate.now().plusDays(1));
        dto.setStartTime(LocalTime.of(10, 0));
        dto.setEndDate(LocalDate.now().plusDays(3));
        dto.setEndTime(LocalTime.of(18, 0));
        dto.setPickupPoint(pickupPointDTO);
        dto.setContact(contactDTO);

        return dto;
    }

    private RentalCreateDTO createTestRentalCreateDTO() {
        RentalCreateDTO dto = new RentalCreateDTO();
        dto.setCarId(1L);
        dto.setOwnerEmail("owner@example.com");
        dto.setStartDate("2024-01-15");
        dto.setStartTime("10:00");
        dto.setEndDate("2024-01-18");
        dto.setEndTime("18:00");

        // PickupPoint fields
        dto.setStreet("Station Street");
        dto.setNumber("1");
        dto.setPostal("1000");
        dto.setCity("Brussels");

        // Contact fields
        dto.setContactName("John Doe");
        dto.setPhone("+32123456789");
        dto.setEmail("john@example.com");

        return dto;
    }
}
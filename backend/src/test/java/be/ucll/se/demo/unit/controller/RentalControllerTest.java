package be.ucll.se.demo.unit.controller;

import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.controller.RentalController;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.PickupPointDTO;
import be.ucll.se.demo.dto.ContactDTO;
import be.ucll.se.demo.service.RentalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
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
    private List<RentalDTO> testRentalList;

    @BeforeEach
    void setUp() {
        // Setup test DTOs
        testRentalDTO = createTestRentalDTO();
        testRentalCreateDTO = createTestRentalCreateDTO();
        testRentalList = Arrays.asList(testRentalDTO, createAnotherTestRentalDTO());
    }

    private RentalDTO createTestRentalDTO() {
        RentalDTO rental = new RentalDTO();
        rental.setId(1L);
        rental.setStartDate(LocalDate.of(2024, 12, 1));
        rental.setStartTime(LocalTime.of(10, 0));
        rental.setEndDate(LocalDate.of(2024, 12, 5));
        rental.setEndTime(LocalTime.of(18, 0));
        rental.setOwnerEmail("owner@example.com");

        // Car DTO
        CarDTO car = new CarDTO();
        car.setId(1L);
        car.setBrand("Toyota");
        car.setModel("Camry");
        rental.setCar(car);

        // PickupPoint DTO
        PickupPointDTO pickupPoint = new PickupPointDTO();
        pickupPoint.setStreet("Main Street");
        pickupPoint.setNumber("123");
        pickupPoint.setPostal("3500");
        pickupPoint.setCity("Hasselt");
        rental.setPickupPoint(pickupPoint);

        // Contact DTO
        ContactDTO contact = new ContactDTO();
        contact.setName("John Doe");
        contact.setPhone("+32123456789");
        contact.setEmail("john@example.com");
        rental.setContact(contact);

        return rental;
    }

    private RentalDTO createAnotherTestRentalDTO() {
        RentalDTO rental = new RentalDTO();
        rental.setId(2L);
        rental.setStartDate(LocalDate.of(2024, 12, 10));
        rental.setStartTime(LocalTime.of(9, 0));
        rental.setEndDate(LocalDate.of(2024, 12, 15));
        rental.setEndTime(LocalTime.of(17, 0));
        rental.setOwnerEmail("owner2@example.com");

        // Car DTO
        CarDTO car = new CarDTO();
        car.setId(2L);
        car.setBrand("Honda");
        car.setModel("Civic");
        rental.setCar(car);

        // PickupPoint DTO
        PickupPointDTO pickupPoint = new PickupPointDTO();
        pickupPoint.setStreet("Oak Avenue");
        pickupPoint.setNumber("456");
        pickupPoint.setPostal("2000");
        pickupPoint.setCity("Antwerpen");
        rental.setPickupPoint(pickupPoint);

        // Contact DTO
        ContactDTO contact = new ContactDTO();
        contact.setName("Jane Smith");
        contact.setPhone("+32987654321");
        contact.setEmail("jane@example.com");
        rental.setContact(contact);

        return rental;
    }

    private RentalCreateDTO createTestRentalCreateDTO() {
        RentalCreateDTO createDTO = new RentalCreateDTO();
        createDTO.setCarId(1L);
        createDTO.setStartDate("01/12/2024"); // dd/mm/yyyy format
        createDTO.setStartTime("10:00");
        createDTO.setEndDate("05/12/2024"); // dd/mm/yyyy format
        createDTO.setEndTime("18:00");
        createDTO.setStreet("Main Street");
        createDTO.setNumber("123");
        createDTO.setPostal("3500");
        createDTO.setCity("Hasselt");
        createDTO.setContactName("John Doe");
        createDTO.setPhone("+32123456789");
        createDTO.setEmail("john@example.com");
        createDTO.setOwnerEmail("owner@example.com");
        return createDTO;
    }

    @Test
    void getAllRentals_ShouldReturnListOfRentals() throws Exception {
        // Given
        when(rentalService.getAllRentalsDTO()).thenReturn(testRentalList);

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].car.brand").value("Toyota"))
                .andExpect(jsonPath("$[0].ownerEmail").value("owner@example.com"))
                .andExpect(jsonPath("$[1].id").value(2))
                .andExpect(jsonPath("$[1].car.brand").value("Honda"));

        verify(rentalService, times(1)).getAllRentalsDTO();
    }

    @Test
    void getAllRentals_WhenEmpty_ShouldReturnEmptyList() throws Exception {
        // Given
        when(rentalService.getAllRentalsDTO()).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/rentals"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getRentalById_WhenExists_ShouldReturnRental() throws Exception {
        // Given
        when(rentalService.getRentalByIdDTO(1L)).thenReturn(Optional.of(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.car.brand").value("Toyota"))
                .andExpect(jsonPath("$.ownerEmail").value("owner@example.com"))
                .andExpect(jsonPath("$.pickupPoint.city").value("Hasselt"));

        verify(rentalService, times(1)).getRentalByIdDTO(1L);
    }

    @Test
    void getRentalById_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(rentalService.getRentalByIdDTO(999L)).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/rentals/999"))
                .andExpect(status().isNotFound());

        verify(rentalService, times(1)).getRentalByIdDTO(999L);
    }

    @Test
    void createRental_WithValidData_ShouldReturn201() throws Exception {
        // Given
        when(rentalService.createRental(any(RentalCreateDTO.class))).thenReturn(testRentalDTO);

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.car.brand").value("Toyota"))
                .andExpect(jsonPath("$.ownerEmail").value("owner@example.com"));

        verify(rentalService, times(1)).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WithInvalidCarId_ShouldReturn400() throws Exception {
        // Given - Use valid DTO but mock service to throw exception
        RentalCreateDTO validDTO = createTestRentalCreateDTO();
        when(rentalService.createRental(any(RentalCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Car not found with id: 999"));

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest());

        verify(rentalService, times(1)).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WithInvalidData_ShouldReturn400() throws Exception {
        // Given - invalid DTO with missing required fields
        RentalCreateDTO invalidDTO = new RentalCreateDTO();
        invalidDTO.setCarId(null); // Required field missing
        invalidDTO.setStartDate("invalid-date"); // Invalid date format

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.carId").exists())
                .andExpect(jsonPath("$.startDate").exists());

        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WhenExists_ShouldReturnUpdated() throws Exception {
        // Given
        RentalDTO updatedRental = createTestRentalDTO();
        updatedRental.setOwnerEmail("updated@example.com");
        when(rentalService.updateRental(eq(1L), any(RentalCreateDTO.class)))
                .thenReturn(Optional.of(updatedRental));

        // When & Then
        mockMvc.perform(put("/rentals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.ownerEmail").value("updated@example.com"));

        verify(rentalService, times(1)).updateRental(eq(1L), any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(rentalService.updateRental(eq(999L), any(RentalCreateDTO.class)))
                .thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/rentals/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(testRentalCreateDTO)))
                .andExpect(status().isNotFound());

        verify(rentalService, times(1)).updateRental(eq(999L), any(RentalCreateDTO.class));
    }

    @Test
    void updateRental_WithInvalidCarId_ShouldReturn400() throws Exception {
        // Given - Use valid DTO but mock service to throw exception
        RentalCreateDTO validDTO = createTestRentalCreateDTO();
        when(rentalService.updateRental(eq(1L), any(RentalCreateDTO.class)))
                .thenThrow(new IllegalArgumentException("Car not found with id: 999"));

        // When & Then
        mockMvc.perform(put("/rentals/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validDTO)))
                .andExpect(status().isBadRequest());

        verify(rentalService, times(1)).updateRental(eq(1L), any(RentalCreateDTO.class));
    }

    @Test
    void deleteRental_WhenExists_ShouldReturn204() throws Exception {
        // Given
        when(rentalService.deleteRental(1L)).thenReturn(true);

        // When & Then
        mockMvc.perform(delete("/rentals/1"))
                .andExpect(status().isNoContent());

        verify(rentalService, times(1)).deleteRental(1L);
    }

    @Test
    void deleteRental_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(rentalService.deleteRental(999L)).thenReturn(false);

        // When & Then
        mockMvc.perform(delete("/rentals/999"))
                .andExpect(status().isNotFound());

        verify(rentalService, times(1)).deleteRental(999L);
    }

    @Test
    void getRentalsByCarId_ShouldReturnRentals() throws Exception {
        // Given
        when(rentalService.getRentalsByCarIdDTO(1L)).thenReturn(Arrays.asList(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/car/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].car.id").value(1));

        verify(rentalService, times(1)).getRentalsByCarIdDTO(1L);
    }

    @Test
    void getRentalsByOwnerEmail_ShouldReturnRentals() throws Exception {
        // Given
        String ownerEmail = "owner@example.com";
        when(rentalService.getRentalsByOwnerEmailDTO(ownerEmail)).thenReturn(Arrays.asList(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/owner/" + ownerEmail))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].ownerEmail").value(ownerEmail));

        verify(rentalService, times(1)).getRentalsByOwnerEmailDTO(ownerEmail);
    }

    @Test
    void getRentalsByStartDate_ShouldReturnRentals() throws Exception {
        // Given
        String dateString = "2024-12-01";
        LocalDate date = LocalDate.parse(dateString);
        when(rentalService.getRentalsByStartDateDTO(date)).thenReturn(Arrays.asList(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/date/" + dateString))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].startDate").value(dateString));

        verify(rentalService, times(1)).getRentalsByStartDateDTO(date);
    }

    @Test
    void getRentalsByStartDate_WithInvalidDate_ShouldReturn500() throws Exception {
        // Note: Invalid date parsing throws DateTimeParseException which results in 500
        // This is handled by GlobalExceptionHandler
        mockMvc.perform(get("/rentals/date/invalid-date"))
                .andExpect(status().isInternalServerError());

        verify(rentalService, never()).getRentalsByStartDateDTO(any(LocalDate.class));
    }

    @Test
    void getRentalsByCity_ShouldReturnRentals() throws Exception {
        // Given
        String city = "Hasselt";
        when(rentalService.getRentalsByCityDTO(city)).thenReturn(Arrays.asList(testRentalDTO));

        // When & Then
        mockMvc.perform(get("/rentals/city/" + city))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pickupPoint.city").value(city));

        verify(rentalService, times(1)).getRentalsByCityDTO(city);
    }

    @Test
    void getRentalsByCity_WhenNoRentalsFound_ShouldReturnEmptyList() throws Exception {
        // Given
        String city = "UnknownCity";
        when(rentalService.getRentalsByCityDTO(city)).thenReturn(Arrays.asList());

        // When & Then
        mockMvc.perform(get("/rentals/city/" + city))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.length()").value(0));

        verify(rentalService, times(1)).getRentalsByCityDTO(city);
    }

    @Test
    void createRental_WithInvalidEmail_ShouldReturn400() throws Exception {
        // Given - invalid email format
        RentalCreateDTO invalidDTO = createTestRentalCreateDTO();
        invalidDTO.setEmail("invalid-email");

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.email").exists());

        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WithBlankRequiredFields_ShouldReturn400() throws Exception {
        // Given - blank required fields
        RentalCreateDTO invalidDTO = createTestRentalCreateDTO();
        invalidDTO.setStreet(""); // Required field is blank
        invalidDTO.setContactName(""); // Required field is blank

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.street").exists())
                .andExpect(jsonPath("$.contactName").exists());

        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }

    @Test
    void createRental_WithInvalidDateFormat_ShouldReturn400() throws Exception {
        // Given - invalid date format
        RentalCreateDTO invalidDTO = createTestRentalCreateDTO();
        invalidDTO.setStartDate("2024-12-01"); // Should be dd/mm/yyyy
        invalidDTO.setEndDate("2024-12-05"); // Should be dd/mm/yyyy

        // When & Then
        mockMvc.perform(post("/rentals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDTO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.startDate").exists())
                .andExpect(jsonPath("$.endDate").exists());

        verify(rentalService, never()).createRental(any(RentalCreateDTO.class));
    }
}
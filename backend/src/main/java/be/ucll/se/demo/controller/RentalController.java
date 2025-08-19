package be.ucll.se.demo.controller;

import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.service.RentalService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/rentals")
public class RentalController {

    private final RentalService rentalService;

    public RentalController(RentalService rentalService) {
        this.rentalService = rentalService;
    }

    @GetMapping
    public List<RentalDTO> getAllRentals() {
        return rentalService.getAllRentalsDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentalDTO> getRentalById(@PathVariable Long id) {
        return rentalService.getRentalByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<RentalDTO> createRental(@Valid @RequestBody RentalCreateDTO createDTO) {
        try {
            RentalDTO savedRental = rentalService.createRental(createDTO);
            return ResponseEntity.status(201).body(savedRental);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<RentalDTO> updateRental(@PathVariable Long id,
            @Valid @RequestBody RentalCreateDTO updateDTO) {
        try {
            return rentalService.updateRental(id, updateDTO)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRental(@PathVariable Long id) {
        if (rentalService.deleteRental(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/car/{carId}")
    public List<RentalDTO> getRentalsByCarId(@PathVariable Long carId) {
        return rentalService.getRentalsByCarIdDTO(carId);
    }

    @GetMapping("/owner/{ownerEmail}")
    public List<RentalDTO> getRentalsByOwnerEmail(@PathVariable String ownerEmail) {
        return rentalService.getRentalsByOwnerEmailDTO(ownerEmail);
    }

    @GetMapping("/date/{startDate}")
    public List<RentalDTO> getRentalsByStartDate(@PathVariable String startDate) {
        LocalDate date = LocalDate.parse(startDate);
        return rentalService.getRentalsByStartDateDTO(date);
    }

    @GetMapping("/city/{city}")
    public List<RentalDTO> getRentalsByCity(@PathVariable String city) {
        return rentalService.getRentalsByCityDTO(city);
    }
}
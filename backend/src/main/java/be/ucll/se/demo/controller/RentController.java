package be.ucll.se.demo.controller;

import be.ucll.se.demo.dto.RentCreateDTO;
import be.ucll.se.demo.dto.RentDTO;
import be.ucll.se.demo.mapper.RentMapper;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.service.CarService;
import be.ucll.se.demo.service.RentService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/rents")
public class RentController {

    private final RentService rentService;
    private final CarService carService;

    public RentController(RentService rentService, CarService carService) {
        this.rentService = rentService;
        this.carService = carService;
    }

    @GetMapping
    public List<RentDTO> getAllRents() {
        return rentService.getAllRents().stream()
                .map(RentMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RentDTO> getRentById(@PathVariable Long id) {
        return rentService.getRentById(id)
                .map(RentMapper::toDto)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> addRent(@Valid @RequestBody RentCreateDTO rentCreateDTO) {
        try {
            Optional<Car> carOpt = carService.getCarById(rentCreateDTO.getCarId());
            if (carOpt.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Car with ID " + rentCreateDTO.getCarId() + " does not exist.");
            }

            Rent rent = RentMapper.toEntity(rentCreateDTO, carOpt.get());

            Rent savedRent = rentService.addRent(rent);

            RentDTO response = RentMapper.toDto(savedRent);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRent(@PathVariable Long id) {
        Optional<Rent> rent = rentService.getRentById(id);
        if (rent.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        rentService.deleteRent(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/by-car/{carId}")
    public ResponseEntity<List<RentDTO>> getRentsByCar(@PathVariable Long carId) {
        return carService.getCarById(carId)
                .map(car -> {
                    List<RentDTO> rents = rentService.getRentsByCar(car).stream()
                            .map(RentMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(rents);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/renter/{email}")
    public List<RentDTO> getRentsByRenterEmail(@PathVariable String email) {
        System.out.println("🔍 Searching for rents with renterEmail: '" + email + "'");

        List<Rent> rents = rentService.getRentsByRenterEmail(email);
        System.out.println("📊 Found " + rents.size() + " rents for email: " + email);

        List<Rent> allRents = rentService.getAllRents();
        System.out.println("🗄️ All renter emails in database:");
        for (Rent rent : allRents) {
            System.out.println("  - ID: " + rent.getId() + ", renterEmail: '" + rent.getRenterEmail() + "'");
        }

        List<RentDTO> result = rents.stream()
                .map(RentMapper::toDto)
                .collect(Collectors.toList());

        System.out.println("✅ Returning " + result.size() + " RentDTOs");
        return result;
    }

    @GetMapping("/by-register-id")
    public List<RentDTO> getRentsByNationalRegisterId(@RequestParam String id) {
        return rentService.getRentsByNationalRegisterId(id).stream()
                .map(RentMapper::toDto)
                .collect(Collectors.toList());
    }

    @GetMapping("/active-or-upcoming/{carId}")
    public ResponseEntity<List<RentDTO>> getActiveOrUpcomingRents(@PathVariable Long carId) {
        return carService.getCarById(carId)
                .map(car -> {
                    List<RentDTO> rents = rentService.getActiveOrUpcomingRentsForCar(car).stream()
                            .map(RentMapper::toDto)
                            .collect(Collectors.toList());
                    return ResponseEntity.ok(rents);
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
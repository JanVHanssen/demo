package be.ucll.se.demo.controller;

import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.service.CarService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/cars")
public class CarController {

    private final CarService carService;

    public CarController(CarService carService) {
        this.carService = carService;
    }

    // Basis CRUD operaties
    @GetMapping
    public List<CarDTO> getAllCars() {
        return carService.getAllCarsDTO();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CarDTO> getCarById(@PathVariable Long id) {
        return carService.getCarByIdDTO(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CarDTO> createCar(@Valid @RequestBody CarCreateDTO createDTO) {
        CarDTO savedCar = carService.createCar(createDTO);
        return ResponseEntity.status(201).body(savedCar);
    }

    @PutMapping("/{id}")
    public ResponseEntity<CarDTO> updateCar(@PathVariable Long id, @Valid @RequestBody CarCreateDTO updateDTO) {
        return carService.updateCar(id, updateDTO)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (carService.deleteCar(id)) {
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    // Extra query endpoints
    @GetMapping("/available")
    public List<CarDTO> getAvailableCars() {
        return carService.getAvailableCarsDTO();
    }

    @GetMapping("/owner/{email}")
    public List<CarDTO> getCarsByOwnerEmail(@PathVariable String email) {
        return carService.getCarsByOwnerEmailDTO(email);
    }

    @GetMapping("/type/{type}")
    public ResponseEntity<List<CarDTO>> getCarsByType(@PathVariable String type) {
        try {
            List<CarDTO> cars = carService.getCarsByTypeDTO(type);
            return ResponseEntity.ok(cars);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/license/{licensePlate}")
    public ResponseEntity<CarDTO> getCarByLicensePlate(@PathVariable String licensePlate) {
        return carService.getCarByLicensePlateDTO(licensePlate)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
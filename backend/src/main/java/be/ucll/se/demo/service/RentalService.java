package be.ucll.se.demo.service;

import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.mapper.RentalMapper;
import be.ucll.se.demo.model.Rental;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.repository.RentalRepository;
import be.ucll.se.demo.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class RentalService {

    private final RentalRepository rentalRepository;
    private final CarRepository carRepository;
    private final RentalMapper rentalMapper;

    public RentalService(RentalRepository rentalRepository,
            CarRepository carRepository,
            RentalMapper rentalMapper) {
        this.rentalRepository = rentalRepository;
        this.carRepository = carRepository;
        this.rentalMapper = rentalMapper;
    }

    // DTO methods
    public List<RentalDTO> getAllRentalsDTO() {
        return rentalRepository.findAll()
                .stream()
                .map(rentalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<RentalDTO> getRentalByIdDTO(Long id) {
        return rentalRepository.findById(id)
                .map(rentalMapper::toDTO);
    }

    public RentalDTO createRental(RentalCreateDTO createDTO) {
        // Validate car exists
        Car car = carRepository.findById(createDTO.getCarId())
                .orElseThrow(() -> new IllegalArgumentException("Car not found with id: " + createDTO.getCarId()));

        // Convert to entity and save
        Rental rental = rentalMapper.toEntity(createDTO, car);
        Rental savedRental = rentalRepository.save(rental);

        return rentalMapper.toDTO(savedRental);
    }

    public Optional<RentalDTO> updateRental(Long id, RentalCreateDTO updateDTO) {
        return rentalRepository.findById(id)
                .map(existingRental -> {
                    // Validate car exists
                    Car car = carRepository.findById(updateDTO.getCarId())
                            .orElseThrow(() -> new IllegalArgumentException(
                                    "Car not found with id: " + updateDTO.getCarId()));

                    // Update existing rental
                    Rental updatedRental = rentalMapper.toEntity(updateDTO, car);
                    updatedRental.setId(existingRental.getId()); // Keep existing ID

                    Rental saved = rentalRepository.save(updatedRental);
                    return rentalMapper.toDTO(saved);
                });
    }

    public boolean deleteRental(Long id) {
        if (rentalRepository.existsById(id)) {
            rentalRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // Legacy methods (behouden voor compatibiliteit)
    public List<Rental> getAllRentals() {
        return rentalRepository.findAll();
    }

    public Optional<Rental> getRentalById(Long id) {
        return rentalRepository.findById(id);
    }

    public Rental addRental(Rental rental) {
        return rentalRepository.save(rental);
    }

    // Extra methodes met DTO support
    public List<RentalDTO> getRentalsByCarIdDTO(Long carId) {
        return rentalRepository.findByCarId(carId)
                .stream()
                .map(rentalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RentalDTO> getRentalsByOwnerEmailDTO(String ownerEmail) {
        return rentalRepository.findByOwnerEmail(ownerEmail)
                .stream()
                .map(rentalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RentalDTO> getRentalsByStartDateDTO(LocalDate startDate) {
        return rentalRepository.findByStartDate(startDate)
                .stream()
                .map(rentalMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<RentalDTO> getRentalsByCityDTO(String city) {
        return rentalRepository.findByPickupPoint_City(city)
                .stream()
                .map(rentalMapper::toDTO)
                .collect(Collectors.toList());
    }

    // Legacy extra methods
    public List<Rental> getRentalsByCarId(Long carId) {
        return rentalRepository.findByCarId(carId);
    }

    public List<Rental> getRentalsByOwnerEmail(String ownerEmail) {
        return rentalRepository.findByOwnerEmail(ownerEmail);
    }

    public List<Rental> getRentalsByStartDate(LocalDate startDate) {
        return rentalRepository.findByStartDate(startDate);
    }

    public List<Rental> getRentalsByCity(String city) {
        return rentalRepository.findByPickupPoint_City(city);
    }
}
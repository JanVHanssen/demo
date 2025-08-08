package be.ucll.se.demo.service;

import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.mapper.CarMapper;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.CarType;
import be.ucll.se.demo.repository.CarRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CarService {

    private final CarRepository carRepository;
    private final CarMapper carMapper;

    public CarService(CarRepository carRepository, CarMapper carMapper) {
        this.carRepository = carRepository;
        this.carMapper = carMapper;
    }

    // DTO-based methods
    public List<CarDTO> getAllCarsDTO() {
        return carRepository.findAll()
                .stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    public Optional<CarDTO> getCarByIdDTO(Long id) {
        return carRepository.findById(id)
                .map(carMapper::toDTO);
    }

    public CarDTO createCar(CarCreateDTO createDTO) {
        Car car = carMapper.toEntity(createDTO);
        Car savedCar = carRepository.save(car);
        return carMapper.toDTO(savedCar);
    }

    public Optional<CarDTO> updateCar(Long id, CarCreateDTO updateDTO) {
        return carRepository.findById(id)
                .map(existingCar -> {
                    carMapper.updateEntityFromDTO(existingCar, updateDTO);
                    Car savedCar = carRepository.save(existingCar);
                    return carMapper.toDTO(savedCar);
                });
    }

    public boolean deleteCar(Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
            return true;
        }
        return false;
    }

    // DTO-based query methods
    public Optional<CarDTO> getCarByLicensePlateDTO(String licensePlate) {
        return carRepository.findByLicensePlate(licensePlate)
                .map(carMapper::toDTO);
    }

    public List<CarDTO> getCarsByOwnerEmailDTO(String ownerEmail) {
        return carRepository.findByOwnerEmail(ownerEmail)
                .stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CarDTO> getAvailableCarsDTO() {
        return carRepository.findByAvailableForRentTrue()
                .stream()
                .map(carMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<CarDTO> getCarsByTypeDTO(String type) {
        try {
            CarType carType = CarType.valueOf(type.toUpperCase());
            return carRepository.findByType(carType)
                    .stream()
                    .map(carMapper::toDTO)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid car type: " + type);
        }
    }

    // Legacy methods (behouden voor compatibiliteit)
    public List<Car> getAllCars() {
        return carRepository.findAll();
    }

    public Optional<Car> getCarById(Long id) {
        return carRepository.findById(id);
    }

    public Car addCar(CarCreateDTO dto) {
        Car car = carMapper.toEntity(dto);
        return carRepository.save(car);
    }

    public Optional<Car> getCarByLicensePlate(String licensePlate) {
        return carRepository.findByLicensePlate(licensePlate);
    }

    public List<Car> getCarsByOwnerEmail(String ownerEmail) {
        return carRepository.findByOwnerEmail(ownerEmail);
    }

    public List<Car> getAvailableCars() {
        return carRepository.findByAvailableForRentTrue();
    }

    public List<Car> getCarsByType(String type) {
        try {
            CarType carType = CarType.valueOf(type.toUpperCase());
            return carRepository.findByType(carType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid car type: " + type);
        }
    }
}
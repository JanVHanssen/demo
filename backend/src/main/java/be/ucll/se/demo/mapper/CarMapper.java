package be.ucll.se.demo.mapper;

import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.CarCreateDTO;
import be.ucll.se.demo.model.Car;
import org.springframework.stereotype.Component;

@Component
public class CarMapper {

    // Entity naar DTO
    public CarDTO toDTO(Car car) {
        if (car == null) {
            return null;
        }

        CarDTO dto = new CarDTO();
        dto.setId(car.getId());
        dto.setBrand(car.getBrand());
        dto.setModel(car.getModel());
        dto.setLicensePlate(car.getLicensePlate());
        dto.setOwnerEmail(car.getOwnerEmail());
        dto.setType(car.getType());
        dto.setNumberOfSeats(car.getNumberOfSeats());
        dto.setNumberOfChildSeats(car.getNumberOfChildSeats());
        dto.setFoldingRearSeat(car.isFoldingRearSeat());
        dto.setTowBar(car.isTowBar());
        dto.setAvailableForRent(car.isAvailableForRent());

        return dto;
    }

    // CreateDTO naar Entity (voor nieuwe car)
    public Car toEntity(CarCreateDTO createDTO) {
        if (createDTO == null) {
            return null;
        }

        Car car = new Car();
        updateEntityFromDTO(car, createDTO);
        return car;
    }

    // Update bestaande entity met DTO data
    public void updateEntityFromDTO(Car car, CarCreateDTO createDTO) {
        if (car == null || createDTO == null) {
            return;
        }

        car.setBrand(createDTO.getBrand());
        car.setModel(createDTO.getModel());
        car.setLicensePlate(createDTO.getLicensePlate());
        car.setOwnerEmail(createDTO.getOwnerEmail());
        car.setType(createDTO.getType());
        car.setNumberOfSeats(createDTO.getNumberOfSeats());
        car.setNumberOfChildSeats(createDTO.getNumberOfChildSeats());
        car.setFoldingRearSeat(createDTO.isFoldingRearSeat());
        car.setTowBar(createDTO.isTowBar());
        car.setAvailableForRent(createDTO.isAvailableForRent());
    }
}
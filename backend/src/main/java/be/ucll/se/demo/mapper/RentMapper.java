
package be.ucll.se.demo.mapper;

import be.ucll.se.demo.dto.RentCreateDTO;
import be.ucll.se.demo.dto.RentDTO;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.Rent;
import be.ucll.se.demo.model.RenterInfo;

public class RentMapper {

    public static Rent toEntity(RentCreateDTO dto, Car car) {

        RenterInfo renterInfo = new RenterInfo(
                dto.getPhoneNumber(),
                dto.getNationalRegisterId(),
                dto.getBirthDate(),
                dto.getDrivingLicenseNumber());

        return new Rent(
                car,
                dto.getStartDate(),
                dto.getEndDate(),
                dto.getOwnerEmail(),
                dto.getRenterEmail(),
                renterInfo);
    }

    public static RentDTO toDto(Rent rent) {
        return new RentDTO(
                rent.getId(),
                rent.getCar().getId(),
                rent.getStartDate(),
                rent.getEndDate(),
                rent.getOwnerEmail(),
                rent.getRenterEmail(),
                rent.getRenterInfo().getPhoneNumber(),
                rent.getRenterInfo().getNationalRegisterId(),
                rent.getRenterInfo().getBirthDate(),
                rent.getRenterInfo().getDrivingLicenseNumber());
    }
}
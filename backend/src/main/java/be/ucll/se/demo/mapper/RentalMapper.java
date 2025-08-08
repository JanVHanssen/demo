package be.ucll.se.demo.mapper;

import be.ucll.se.demo.dto.RentalDTO;
import be.ucll.se.demo.dto.RentalCreateDTO;
import be.ucll.se.demo.dto.CarDTO;
import be.ucll.se.demo.dto.PickupPointDTO;
import be.ucll.se.demo.dto.ContactDTO;
import be.ucll.se.demo.model.Rental;
import be.ucll.se.demo.model.Car;
import be.ucll.se.demo.model.PickupPoint;
import be.ucll.se.demo.model.Contact;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Component
public class RentalMapper {

    private final CarMapper carMapper;

    public RentalMapper(CarMapper carMapper) {
        this.carMapper = carMapper;
    }

    // Entity naar DTO
    public RentalDTO toDTO(Rental rental) {
        if (rental == null) {
            return null;
        }

        RentalDTO dto = new RentalDTO();
        dto.setId(rental.getId());
        dto.setCar(carMapper.toDTO(rental.getCar()));
        dto.setStartDate(rental.getStartDate());
        dto.setStartTime(rental.getStartTime());
        dto.setEndDate(rental.getEndDate());
        dto.setEndTime(rental.getEndTime());
        dto.setPickupPoint(toPickupPointDTO(rental.getPickupPoint()));
        dto.setContact(toContactDTO(rental.getContact()));
        dto.setOwnerEmail(rental.getOwnerEmail());

        return dto;
    }

    // CreateDTO naar Entity
    public Rental toEntity(RentalCreateDTO createDTO, Car car) {
        if (createDTO == null) {
            return null;
        }

        Rental rental = new Rental();
        rental.setCar(car);

        // Parse dates and times
        rental.setStartDate(LocalDate.parse(createDTO.getStartDate()));
        rental.setStartTime(LocalTime.parse(createDTO.getStartTime()));
        rental.setEndDate(LocalDate.parse(createDTO.getEndDate()));
        rental.setEndTime(LocalTime.parse(createDTO.getEndTime()));

        // Create PickupPoint
        PickupPoint pickupPoint = new PickupPoint();
        pickupPoint.setStreet(createDTO.getStreet());
        pickupPoint.setNumber(createDTO.getNumber());
        pickupPoint.setPostal(createDTO.getPostal());
        pickupPoint.setCity(createDTO.getCity());
        rental.setPickupPoint(pickupPoint);

        // Create Contact
        Contact contact = new Contact();
        contact.setName(createDTO.getContactName());
        contact.setPhoneNumber(createDTO.getPhone());
        contact.setEmail(createDTO.getEmail());
        rental.setContact(contact);

        rental.setOwnerEmail(createDTO.getOwnerEmail());

        return rental;
    }

    // Helper methods voor embedded objects
    private PickupPointDTO toPickupPointDTO(PickupPoint pickupPoint) {
        if (pickupPoint == null) {
            return null;
        }

        PickupPointDTO dto = new PickupPointDTO();
        dto.setStreet(pickupPoint.getStreet());
        dto.setNumber(pickupPoint.getNumber());
        dto.setPostal(pickupPoint.getPostal());
        dto.setCity(pickupPoint.getCity());

        return dto;
    }

    private ContactDTO toContactDTO(Contact contact) {
        if (contact == null) {
            return null;
        }

        ContactDTO dto = new ContactDTO();
        dto.setName(contact.getName());
        dto.setPhone(contact.getPhone());
        dto.setEmail(contact.getEmail());

        return dto;
    }
}
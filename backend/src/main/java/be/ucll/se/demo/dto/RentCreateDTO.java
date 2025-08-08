package be.ucll.se.demo.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public class RentCreateDTO {
    @NotNull(message = "Car ID is required")
    private Long carId;

    @NotNull(message = "Start date is required")
    private LocalDate startDate;

    @NotNull(message = "End date is required")
    private LocalDate endDate;

    @NotBlank(message = "Owner email is required")
    @Email(message = "Owner email is invalid, it has to be of the following format xxx@yyy.zzz")
    private String ownerEmail;

    @NotBlank(message = "Renter email is required")
    @Email(message = "Renter email is invalid, it has to be of the following format xxx@yyy.zzz")
    private String renterEmail;

    @NotBlank(message = "Phone number is required")
    private String phoneNumber;

    @NotBlank(message = "Identification number of national register is required")
    @Pattern(regexp = "^\\d{2}\\.\\d{2}\\.\\d{2}-\\d{3}\\.\\d{2}$", message = "Identification number of national register is invalid, it has to be of the following format yy.mm.dd-xxx.zz")
    private String nationalRegisterId;

    @NotNull(message = "Birth date is required")
    private LocalDate birthDate;

    @NotBlank(message = "Driving license number is required")
    @Pattern(regexp = "^\\d{10}$", message = "Driving license number is invalid, it has to be of the following format 0000000000 (where each 0 is a number between 0 and 9)")
    private String drivingLicenseNumber;

    // Getters en setters
    public Long getCarId() {
        return carId;
    }

    public void setCarId(Long carId) {
        this.carId = carId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public String getRenterEmail() {
        return renterEmail;
    }

    public void setRenterEmail(String renterEmail) {
        this.renterEmail = renterEmail;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getNationalRegisterId() {
        return nationalRegisterId;
    }

    public void setNationalRegisterId(String nationalRegisterId) {
        this.nationalRegisterId = nationalRegisterId;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(LocalDate birthDate) {
        this.birthDate = birthDate;
    }

    public String getDrivingLicenseNumber() {
        return drivingLicenseNumber;
    }

    public void setDrivingLicenseNumber(String drivingLicenseNumber) {
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    // Constructors
    public RentCreateDTO() {
    }

    public RentCreateDTO(Long carId, LocalDate startDate, LocalDate endDate, String ownerEmail,
            String renterEmail, String phoneNumber, String nationalRegisterId,
            LocalDate birthDate, String drivingLicenseNumber) {
        this.carId = carId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ownerEmail = ownerEmail;
        this.renterEmail = renterEmail;
        this.phoneNumber = phoneNumber;
        this.nationalRegisterId = nationalRegisterId;
        this.birthDate = birthDate;
        this.drivingLicenseNumber = drivingLicenseNumber;
    }
}

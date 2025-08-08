package be.ucll.se.demo.dto;

import java.time.LocalDate;

public class RentDTO {
    private Long id;
    private Long carId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String ownerEmail;

    private String renterEmail;
    private String phoneNumber;
    private String nationalRegisterId;
    private LocalDate birthDate;
    private String drivingLicenseNumber;

    // Constructor
    public RentDTO(Long id, Long carId, LocalDate startDate, LocalDate endDate, String ownerEmail,
            String renterEmail, String phoneNumber, String nationalRegisterId,
            LocalDate birthDate, String drivingLicenseNumber) {
        this.id = id;
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

    // Getters en setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
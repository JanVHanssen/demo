
package be.ucll.se.demo.model;

import jakarta.persistence.Embeddable;
import java.time.LocalDate;

@Embeddable
public class RenterInfo {

    private String phoneNumber;
    private String nationalRegisterId;
    private LocalDate birthDate;
    private String drivingLicenseNumber;

    // Default constructor
    public RenterInfo() {
    }

    // Constructor zonder email parameter
    public RenterInfo(String phoneNumber, String nationalRegisterId,
            LocalDate birthDate, String drivingLicenseNumber) {
        this.phoneNumber = phoneNumber;
        this.nationalRegisterId = nationalRegisterId;
        this.birthDate = birthDate;
        this.drivingLicenseNumber = drivingLicenseNumber;
    }

    // Getters and setters
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
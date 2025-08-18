
package be.ucll.se.demo.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class Rent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "car_id", nullable = false)
    private Car car;

    private LocalDate startDate;
    private LocalDate endDate;

    private String ownerEmail;

    private String renterEmail;

    @Embedded
    private RenterInfo renterInfo;

    public Rent() {
    }

    // Constructor met renterEmail parameter
    public Rent(Car car, LocalDate startDate, LocalDate endDate,
            String ownerEmail, String renterEmail, RenterInfo renterInfo) {
        this.car = car;
        this.startDate = startDate;
        this.endDate = endDate;
        this.ownerEmail = ownerEmail;
        this.renterEmail = renterEmail;
        this.renterInfo = renterInfo;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Car getCar() {
        return car;
    }

    public void setCar(Car car) {
        this.car = car;
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

    public RenterInfo getRenterInfo() {
        return renterInfo;
    }

    public void setRenterInfo(RenterInfo renterInfo) {
        this.renterInfo = renterInfo;
    }
}
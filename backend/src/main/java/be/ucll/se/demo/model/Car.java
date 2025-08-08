package be.ucll.se.demo.model;

import jakarta.persistence.*;
import java.util.List;

@Entity
public class Car {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String brand;
    private String model;
    private String licensePlate;
    private String ownerEmail;

    @Enumerated(EnumType.STRING)
    private CarType type;

    private int numberOfSeats;
    private int numberOfChildSeats;

    private boolean foldingRearSeat;
    private boolean towBar;

    private boolean availableForRent;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rental> rentals;

    @OneToMany(mappedBy = "car", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Rent> rents;

    // Constructors
    public Car() {
    }

    public Car(String brand, String model, String licensePlate, String ownerEmail) {
        this.brand = brand;
        this.model = model;
        this.licensePlate = licensePlate;
        this.ownerEmail = ownerEmail;
    }

    // Getters and Setters

    public Long getId() {
        return id;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getLicensePlate() {
        return licensePlate;
    }

    public void setLicensePlate(String licensePlate) {
        this.licensePlate = licensePlate;
    }

    public String getOwnerEmail() {
        return ownerEmail;
    }

    public void setOwnerEmail(String ownerEmail) {
        this.ownerEmail = ownerEmail;
    }

    public CarType getType() {
        return type;
    }

    public void setType(CarType type) {
        this.type = type;
    }

    public int getNumberOfSeats() {
        return numberOfSeats;
    }

    public void setNumberOfSeats(int numberOfSeats) {
        this.numberOfSeats = numberOfSeats;
    }

    public int getNumberOfChildSeats() {
        return numberOfChildSeats;
    }

    public void setNumberOfChildSeats(int numberOfChildSeats) {
        this.numberOfChildSeats = numberOfChildSeats;
    }

    public boolean isFoldingRearSeat() {
        return foldingRearSeat;
    }

    public void setFoldingRearSeat(boolean foldingRearSeat) {
        this.foldingRearSeat = foldingRearSeat;
    }

    public boolean isTowBar() {
        return towBar;
    }

    public void setTowBar(boolean towBar) {
        this.towBar = towBar;
    }

    public boolean isAvailableForRent() {
        return availableForRent;
    }

    public void setAvailableForRent(boolean availableForRent) {
        this.availableForRent = availableForRent;
    }

    public List<Rental> getRentals() {
        return rentals;
    }

    public void setRentals(List<Rental> rentals) {
        this.rentals = rentals;
    }

    public List<Rent> getRents() {
        return rents;
    }

    public void setRents(List<Rent> rents) {
        this.rents = rents;
    }
}

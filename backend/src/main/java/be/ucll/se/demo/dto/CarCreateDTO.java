package be.ucll.se.demo.dto;

import be.ucll.se.demo.model.CarType;

import jakarta.validation.constraints.*;

public class CarCreateDTO {

    @NotBlank(message = "Brand is required")
    private String brand;

    @NotBlank(message = "Model is required")
    private String model;

    @NotBlank(message = "License plate is required")
    @Pattern(regexp = "^[A-Z0-9-]{2,10}$", message = "License plate must be 2-10 characters (letters, numbers, hyphens only)")
    private String licensePlate;

    @Email(message = "Invalid email format")
    private String ownerEmail;

    @NotNull(message = "Type is required")
    private CarType type;

    @NotNull(message = "Number of seats is required")
    @Min(value = 1, message = "Number of seats must be at least 1")
    private Integer numberOfSeats;

    @Min(value = 0, message = "Number of child seats cannot be negative")
    private int numberOfChildSeats;

    private boolean foldingRearSeat;
    private boolean towBar;
    private boolean availableForRent;

    // Getters & Setters

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
}

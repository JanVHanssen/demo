Feature: Car Booking Flow
  As a renter I want to be able to search, select and book cars
  So that I can have a vehicle for my trip

  Background:
    Given the following users exist:
      | username | email               | role   |
      | renter1  | renter@example.com  | RENTER |
      | owner1   | owner@example.com   | OWNER  |
    And the following cars exist:
      | brand  | model | licensePlate | ownerEmail        | type  | seats | available |
      | Toyota | Camry | ABC-123      | owner@example.com | SEDAN | 5     | true      |
      | BMW    | X5    | XYZ-789      | owner@example.com | SUV   | 7     | true      |

  Scenario: Successful end-to-end car booking
    Given I am logged in as "renter@example.com"
    When I search for cars from "2024-12-01" to "2024-12-03"
    Then I should see 2 available cars
    When I select the "Toyota Camry" with license plate "ABC-123"
    And I choose dates from "2024-12-01" to "2024-12-03"
    And I enter my renter information:
      | phoneNumber     | nationalRegisterId | birthDate  | drivingLicenseNumber |
      | +32-123-456-789 | 90.01.01-123.45    | 1990-01-01 | 1234567890           |
    And I confirm the booking
    Then the booking should be created successfully
    And I should receive a booking confirmation email
    And the car owner should receive a new booking notification

  Scenario: Search for cars with no results
    Given I am logged in as "renter@example.com"
    When I search for cars from "2025-01-01" to "2025-01-03"
    Then I should see 0 available cars
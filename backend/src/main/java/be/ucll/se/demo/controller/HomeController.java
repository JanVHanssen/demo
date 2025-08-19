package be.ucll.se.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "Car Sharing Platform Backend is running! 🚗";
    }

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}
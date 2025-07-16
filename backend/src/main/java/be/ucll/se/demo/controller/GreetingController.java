package be.ucll.se.demo.controller;

import be.ucll.se.demo.model.Greeting;
import be.ucll.se.demo.service.GreetingService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hello")
public class GreetingController {
    private final GreetingService service;

    public GreetingController(GreetingService service) {
        this.service = service;
    }

    @GetMapping
    public Greeting getGreeting() {
        System.out.println("HELLO endpoint aangeroepen");
        return service.getGreetingById(1L);
    }

}
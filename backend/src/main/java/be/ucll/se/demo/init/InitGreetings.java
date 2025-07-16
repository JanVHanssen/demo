package be.ucll.se.demo.init;  

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import be.ucll.se.demo.model.Greeting;
import be.ucll.se.demo.repository.GreetingRepository;

@Component
public class InitGreetings {

    @Autowired
    private GreetingRepository greetingRepository;

    @PostConstruct
    public void insertGreeting() {
        if (greetingRepository.findById(1L).isEmpty()) {
            Greeting greeting = new Greeting("Hello World!");
            greeting.setId(1L);
            greetingRepository.save(greeting);
            System.out.println("Greeting initialized: " + greeting.getMessage());
        }
    }
}
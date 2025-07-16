package be.ucll.se.demo.service;

import be.ucll.se.demo.model.Greeting;
import be.ucll.se.demo.repository.GreetingRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class GreetingService {

    private final GreetingRepository repository;

    public GreetingService(GreetingRepository repository) {
        this.repository = repository;
    }

    public Greeting getGreetingById(long id) {
        Optional<Greeting> greeting = repository.findById(id);
        return greeting.orElse(null);
    }
}

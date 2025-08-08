package be.ucll.se.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = { "be.ucll.se.demo", "be.ucll.se.demo.config" })
public class JanvhanssenBackendApplication {
    public static void main(String[] args) {
        SpringApplication.run(JanvhanssenBackendApplication.class, args);
    }
}

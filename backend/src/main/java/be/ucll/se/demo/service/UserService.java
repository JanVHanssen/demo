// UserService.java - FIXED: Include email parameter
package be.ucll.se.demo.service;

import java.util.Base64;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import be.ucll.se.demo.model.User;
import be.ucll.se.demo.repository.UserRepository;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public boolean register(String username, String email, String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return false; // gebruiker bestaat al
        }

        if (userRepository.findByEmail(email).isPresent()) {
            return false; // email bestaat al
        }

        String hashedPassword = hashPassword(password);
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(hashedPassword);
        userRepository.save(user);
        return true;
    }

    public User login(String username, String password) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent() && verifyPassword(password, userOpt.get().getPassword())) {
            return userOpt.get();
        }
        return null;
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    private String hashPassword(String password) {
        return Base64.getEncoder().encodeToString(password.getBytes());
    }

    private boolean verifyPassword(String raw, String hashed) {
        return hashPassword(raw).equals(hashed);
    }
}
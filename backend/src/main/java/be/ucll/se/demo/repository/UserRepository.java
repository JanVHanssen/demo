package be.ucll.se.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import be.ucll.se.demo.model.User;
import be.ucll.se.demo.model.RoleName;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    // Role-based queries
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName")
    List<User> findByRole(@Param("roleName") RoleName roleName);

    @Query("SELECT u FROM User u WHERE u.enabled = :enabled")
    List<User> findByEnabled(@Param("enabled") boolean enabled);

    // Check if user has specific role
    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u JOIN u.roles r WHERE u.email = :email AND r.name = :roleName")
    boolean userHasRole(@Param("email") String email, @Param("roleName") RoleName roleName);

    // Find enabled users with specific role
    @Query("SELECT u FROM User u JOIN u.roles r WHERE r.name = :roleName AND u.enabled = true")
    List<User> findEnabledUsersByRole(@Param("roleName") RoleName roleName);
}
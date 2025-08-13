package be.ucll.se.demo.repository;

import be.ucll.se.demo.model.Role;
import be.ucll.se.demo.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {

    Optional<Role> findByName(RoleName name);

    boolean existsByName(RoleName name);
}
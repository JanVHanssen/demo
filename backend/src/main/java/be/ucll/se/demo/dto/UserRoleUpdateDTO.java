package be.ucll.se.demo.dto;

import be.ucll.se.demo.model.RoleName;
import jakarta.validation.constraints.NotEmpty;
import java.util.Set;

public class UserRoleUpdateDTO {

    @NotEmpty(message = "At least one role must be assigned")
    private Set<RoleName> roles;

    // Getters & Setters
    public Set<RoleName> getRoles() {
        return roles;
    }

    public void setRoles(Set<RoleName> roles) {
        this.roles = roles;
    }
}

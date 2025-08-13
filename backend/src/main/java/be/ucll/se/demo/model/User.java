package be.ucll.se.demo.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.micrometer.common.lang.NonNull;
import jakarta.persistence.*;

import java.util.*;

@Entity
@Table(name = "app_user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(unique = true)
    private String userId;

    @Column(name = "user_name", unique = true)
    @NonNull
    private String username;

    @NonNull
    @Column(name = "email", unique = true)
    private String email;

    @NonNull
    @JsonIgnore
    private String password;

    // NEW: Role support
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))
    private Set<Role> roles = new HashSet<>();

    // NEW: Account status
    @Column(nullable = false)
    private boolean enabled = true;

    public User() {
    }

    public User(String userId, @NonNull String username, @NonNull String email, @NonNull String password) {
        this.userId = userId;
        this.username = username;
        this.email = email;
        this.password = password;
    }

    // NEW: Constructor with role
    public User(@NonNull String username, @NonNull String email, @NonNull String password, RoleName roleName) {
        this.username = username;
        this.email = email;
        this.password = password;
        // Role will be added via service
    }

    // Existing getters/setters
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    // NEW: Role management methods
    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public void addRole(Role role) {
        this.roles.add(role);
    }

    public void removeRole(Role role) {
        this.roles.remove(role);
    }

    public boolean hasRole(RoleName roleName) {
        return roles.stream()
                .anyMatch(role -> role.getName() == roleName);
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public String toString() {
        return "User{" +
                "userId='" + userId + '\'' +
                ", username='" + username + '\'' +
                ", email='" + email + '\'' +
                ", roles=" + roles.size() + " roles" +
                ", enabled=" + enabled +
                '}';
    }
}
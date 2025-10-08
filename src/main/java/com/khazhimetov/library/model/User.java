package com.khazhimetov.library.model;

import java.time.LocalDateTime;

/**
 * Represents a user of the library system.
 * Includes personal details, role, account status, and audit timestamps.
 */
public class User {
    /** Unique identifier of the user */
    private Long id;
    /** Email address of the user (used for login) */
    private String email;
    /** Encrypted password of the user */
    private String password;
    /** User's first name */
    private String firstName;
    /** User's last name */
    private String lastName;
    /** Role of the user (e.g., ADMIN, LIBRARIAN, READER) */
    private UserRole role;
    /** Indicates whether the user's account is active */
    private boolean isActive;
    /** Timestamp of when the user was created */
    private LocalDateTime createdAt;
    /** Timestamp of the last update to the user data */
    private LocalDateTime updatedAt;

    public User() {}

    /**
     * Full constructor for user entity.
     *
     * @param id         unique user ID
     * @param email      user email
     * @param password   encrypted password
     * @param firstName  user's first name
     * @param lastName   user's last name
     * @param role       user role
     * @param isActive   whether the account is active
     * @param createdAt  creation timestamp
     * @param updatedAt  last update timestamp
     */
    public User(Long id, String email, String password, String firstName,
                String lastName, UserRole role, boolean isActive,
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.email = email;
        this.password = password;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.isActive = isActive;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public UserRole getRole() { return role; }
    public void setRole(UserRole userRole) { this.role = userRole; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    /**
     * @return full name composed of first and last name
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
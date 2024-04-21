package entities;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import dto.Notification;
import jakarta.persistence.*;
@Entity
@Table(name="Users")
@NamedQuery(name = "User.findUserByToken", query = "SELECT DISTINCT u FROM UserEntity u WHERE u.token = :token")
@NamedQuery(name = "User.findUserByUsername", query = "SELECT u FROM UserEntity u WHERE u.username = :username")
@NamedQuery(name = "User.updateToken", query = "UPDATE UserEntity u SET u.token = :token WHERE u.username = :username")
@NamedQuery(name = "User.searchUser", query = "SELECT u FROM UserEntity u WHERE u.name LIKE CONCAT(:searchTerm, '%')")
@NamedQuery(name = "User.findUserByConfirmationToken", query = "SELECT u FROM UserEntity u WHERE u.confirmationToken = :confirmationToken")
@NamedQuery(name = "User.countTotalUsers", query = "SELECT COUNT(u) FROM UserEntity u")
@NamedQuery(name = "User.countConfirmedUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed = true")
@NamedQuery(name = "User.countUnconfirmedUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.confirmed = false")
@NamedQuery(name = "User.countActiveUsers", query = "SELECT COUNT(u) FROM UserEntity u WHERE u.active = true")
@NamedQuery(name = "User.countActiveUsersByRegistrationDate", query = "SELECT u.registrationDate, COUNT(u) FROM UserEntity u WHERE u.active = true GROUP BY u.registrationDate")




public class UserEntity implements Serializable{
    @Id
    @Column (name="id", nullable = false, unique = true, updatable = false)
    String username;
    @Column (name="name", nullable = false, unique = false)
    String name;
    @Column (name="email", nullable = false, unique = true)
    String email;
    @Column (name="password", nullable = false, unique = false)
    String password;
    @Column (name="contactNumber", nullable = false, unique = false)
    String contactNumber;
    @Column (name="userPhoto", nullable = true, unique = false)
    String userPhoto;
    @Column (name="token", nullable = true, unique = true)
    String token;
    @Column (name="role", nullable = true, unique = false)
    String role;
    @Column (name="active", nullable = false, unique = false)
    boolean active;

    @Column (name="confirmed", nullable = false, unique = false,updatable = true)
    boolean confirmed;

    @Column(name="confirmationToken", nullable = true, unique = true, updatable = true)
    String confirmationToken;

    @Column(name = "registrationDate", nullable = true, unique = false, updatable = true)
    LocalDate registrationDate;

    public LocalDate getRegistrationDate() {
        return registrationDate;
    }

    public void setRegistrationDate(LocalDate registrationDate) {
        this.registrationDate = registrationDate;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public void setConfirmed(boolean confirmed) {
        this.confirmed = confirmed;
    }


    public String getConfirmationToken() {
        return confirmationToken;
    }

    public void setConfirmationToken(String confirmationToken) {
        this.confirmationToken = confirmationToken;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getUserPhoto() {
        return userPhoto;
    }

    public void setUserPhoto(String userPhoto) {
        this.userPhoto = userPhoto;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
    public String getRole() {
        return role;
    }
    public void setRole(String role) {
        this.role = role;
    }
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

}


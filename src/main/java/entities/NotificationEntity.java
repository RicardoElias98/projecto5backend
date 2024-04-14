package entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "Notification")
@NamedQuery( name = "Notification.findByUser", query = "SELECT n FROM NotificationEntity n WHERE n.user = :user")
@NamedQuery(name = "Notification.findUncheckedByUser", query = "SELECT n FROM NotificationEntity n WHERE n.user = :user AND n.checked = false"
)

public class NotificationEntity implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name = "notification_datetime", nullable = false, unique = false, updatable = false)
    private LocalDateTime notificationDateTime;

    @Column(name = "checked", nullable = false, unique = false, updatable = true)
    private boolean checked;

    @Column(name = "text", nullable = false, unique = false, updatable = false)
    private String text;

    @ManyToOne
    @JoinColumn(name = "user", nullable = false, unique = false, updatable = false)
    private UserEntity user;

    public int getId() {
        return id;
    }

    public UserEntity getUser() {
        return user;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getNotificationDateTime() {
        return notificationDateTime;
    }

    public boolean isChecked() {
        return checked;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNotificationDateTime(LocalDateTime messageDateTime) {
        this.notificationDateTime = messageDateTime;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}

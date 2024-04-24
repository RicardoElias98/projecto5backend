package dto;

import entities.UserEntity;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

@XmlRootElement
public class Notification {

    private int id;

    private LocalDateTime notificationDateTime;

    private boolean checked;

    private String text;

    private UserEntity user;

    public Notification() {
    }



    @XmlElement
    public UserEntity getUser() {
        return user;
    }

    @XmlElement
    public int getId() {
        return id;
    }

    @XmlElement
    public LocalDateTime getNotificationDateTime() {
        return notificationDateTime;
    }

    @XmlElement
    public boolean isChecked() {
        return checked;
    }

    @XmlElement
    public String getText() {
        return text;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setNotificationDateTime(LocalDateTime notificationDateTime) {
        this.notificationDateTime = notificationDateTime;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }
}

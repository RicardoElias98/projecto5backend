package entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="Mensage")
public class MensageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="id", nullable = false, unique = true, updatable = false)
    private int id;

    @Column(name="text", nullable = true, unique = false, updatable = false)
    private String text;

    @Column(name = "message_datetime", nullable = false, unique = false, updatable = false)
    private LocalDateTime messageDateTime;

    public int getId() {
        return id;
    }

    public String getText() {
        return text;
    }

    public LocalDateTime getMessageDateTime() {
        return messageDateTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setText(String text) {
        this.text = text;
    }

    public void setMessageDateTime(LocalDateTime messageDateTime) {
        this.messageDateTime = messageDateTime;
    }
}

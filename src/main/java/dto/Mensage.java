package dto;

import entities.UserEntity;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.time.LocalDateTime;

@XmlRootElement
public class Mensage {

    private int id;

    private String text;

    private LocalDateTime messageDateTime;

    private String receptor;

    public Mensage () {

    }

    @XmlElement
    public String getReceptor() {
        return receptor;
    }



    @XmlElement
    public int getId() {
        return id;
    }
    @XmlElement
    public String getText() {
        return text;
    }
    @XmlElement
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

    public void setReceptor(String receptor) {
        this.receptor = receptor;
    }
}

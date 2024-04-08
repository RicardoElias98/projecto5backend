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

    private String sender;

    private boolean checked = false;

    public Mensage () {

    }

    @XmlElement
    public boolean isChecked() {
        return checked;
    }



    @XmlElement
    public String getReceptor() {
        return receptor;
    }

    @XmlElement
    public String getSender() {
        return sender;
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

    public void setSender(String sender) {
        this.sender = sender;
    }

    public void setChecked(boolean checked) {
        this.checked = checked;
    }
}

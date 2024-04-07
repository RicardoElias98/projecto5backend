package bean;

import dao.MensageDao;
import dao.UserDao;
import dto.Mensage;
import entities.MensageEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

import java.time.LocalDateTime;
@Singleton
public class MensageBean {

    public MensageBean () {}


    @EJB
    MensageDao mensageDao;
    @EJB
    UserDao userDao;


    public MensageBean(MensageDao mensageDao) {
        this.mensageDao = mensageDao;
    }

    public Mensage createMensage (String text, UserEntity receptor, LocalDateTime time) {
        MensageEntity msgEntity = new MensageEntity();
        System.out.println("1" + time);
        msgEntity.setText(text);
        msgEntity.setMessageDateTime(time);
        msgEntity.setReceptor(receptor);
        MensageEntity msgFinal = mensageDao.createMsg(msgEntity);
        return convertMsgToDto(msgFinal);
    }

    public Mensage convertMsgToDto (MensageEntity entity) {
        Mensage dto = new Mensage();
        dto.setText(entity.getText());
        dto.setMessageDateTime(entity.getMessageDateTime());
        dto.setReceptor(entity.getReceptor().getUsername());
        return dto;
    }
}

package bean;

import dao.MensageDao;
import dao.UserDao;
import dto.Mensage;
import dto.User;
import entities.MensageEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    public Mensage createMensage (String text, UserEntity receptor, LocalDateTime time, UserEntity senderEntity) {
        MensageEntity msgEntity = new MensageEntity();
        System.out.println("1" + time);
        msgEntity.setText(text);
        msgEntity.setMessageDateTime(time);
        msgEntity.setReceptor(receptor);
        msgEntity.setSender(senderEntity);
        MensageEntity msgFinal = mensageDao.createMsg(msgEntity);
        return convertMsgToDto(msgFinal);
    }

    public Mensage convertMsgToDto (MensageEntity entity) {
        Mensage dto = new Mensage();
        dto.setText(entity.getText());
        dto.setMessageDateTime(entity.getMessageDateTime());
        dto.setReceptor(entity.getReceptor().getUsername());
        dto.setSender(entity.getSender().getUsername());
        return dto;
    }

    public List<Mensage> getTradedMsgs (UserEntity sender, UserEntity receptor) {
        List <Mensage> msgs = new ArrayList<>();
        List <MensageEntity> msgsEntity= mensageDao.findMsgBySenderAndReceptor(sender, receptor);
        for (MensageEntity m : msgsEntity) {
            msgs.add(convertMsgToDto(m));
        }
        return msgs;
    }
}

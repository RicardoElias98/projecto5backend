package bean;

import dao.NotificationDao;
import dao.UserDao;
import dto.Notification;
import dto.User;
import entities.NotificationEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Singleton
public class NotificationBean {

    public NotificationBean() {
    }

    @EJB
    UserDao userDao;

    @EJB
    NotificationDao notificationDao;

    public List <Notification> getAllNotification (UserEntity user) {
        List<NotificationEntity> entityList =  notificationDao.findNotificationsByUser(user);
        List <Notification> dtoList = new ArrayList<>();
        for (NotificationEntity entity : entityList) {
            Notification dto = convertNotificationEntityToDto(entity);
            dtoList.add(dto);
        }
        return dtoList;
    }

    public Notification convertNotificationEntityToDto(NotificationEntity entity) {
        Notification dto = new Notification();
        dto.setUser(entity.getUser());
        dto.setText(entity.getText());
        dto.setChecked(entity.isChecked());
        dto.setNotificationDateTime(entity.getNotificationDateTime());
        dto.setId(entity.getId());
        return dto;
    }

    public NotificationEntity convertNotificationDtoToEntity (Notification dto) {
        NotificationEntity entity = new NotificationEntity();
        entity.setUser(dto.getUser());
        entity.setText(dto.getText());
        entity.setChecked(dto.isChecked());
        entity.setNotificationDateTime(dto.getNotificationDateTime());
        return entity;
    }


    public Notification createNotificationMsg (String usernameSender, LocalDateTime time, String usernameReceptor) {
        Notification notification = new Notification();
        notification.setNotificationDateTime(time);
        notification.setChecked(false);
        notification.setText("Hey, you received a message from " + usernameSender + " on this date: " + time.getDayOfMonth() + "/" + time.getMonthValue() + "/" + time.getYear() + " at this time: " + time.getHour() + "h" + time.getMinute()+"m");
        UserEntity userEntity = userDao.findUserByUsername(usernameReceptor);
        notification.setUser(userEntity);
        NotificationEntity entity = convertNotificationDtoToEntity(notification);
        notificationDao.persist(entity);
        return notification;
    }

    public void checkNotifications(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        List <NotificationEntity> entityList = notificationDao.findNotificationsByUser(userEntity);
        for (NotificationEntity ntf : entityList) {
            ntf.setChecked(true);
            notificationDao.merge(ntf);
        }
    }

    public List <Notification> getNotCheckedNotif (String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        List <NotificationEntity> entityList = notificationDao.findUncheckedNotificationsByUser(userEntity);
        List <Notification> dtoList = new ArrayList<>();
        for (NotificationEntity entity : entityList) {
            Notification dto = convertNotificationEntityToDto(entity);
            dtoList.add(dto);
        }
        return dtoList;
    }


}

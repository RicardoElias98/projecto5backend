package bean;

import bean.UserBean;
import dao.NotificationDao;
import dao.UserDao;
import dto.PasswordDto;
import dto.User;
import entities.UserEntity;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import utilities.EncryptHelper;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import dao.NotificationDao;
import dao.UserDao;
import dto.Notification;
import entities.NotificationEntity;
import entities.UserEntity;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class NotificationBeanTest {

    @Mock
    UserDao userDao;

    @Mock
    NotificationDao notificationDao;

    @InjectMocks
    NotificationBean notificationBean;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConvertNotificationEntityToDto() {
        NotificationEntity entity = new NotificationEntity();
        entity.setUser(new UserEntity());
        entity.setText("Test Notification");
        entity.setChecked(true);
        entity.setNotificationDateTime(LocalDateTime.now());

        Notification notification = notificationBean.convertNotificationEntityToDto(entity);

        assertEquals(entity.getUser(), notification.getUser());
        assertEquals(entity.getText(), notification.getText());
        assertEquals(entity.isChecked(), notification.isChecked());
        assertEquals(entity.getNotificationDateTime(), notification.getNotificationDateTime());
    }

    @Test
    public void testGetAllNotification() {

        UserEntity user = new UserEntity();

        // list ficticia de NotificationEntity
        List <NotificationEntity> entityList = new ArrayList<>();
        entityList.add(new NotificationEntity());
        entityList.add(new NotificationEntity());
        entityList.add(new NotificationEntity());
        entityList.add(new NotificationEntity());

        when(notificationDao.findNotificationsByUser(user)).thenReturn(entityList);

        List  <Notification> result = notificationBean.getAllNotification(user);

        //lista vazia?
        assertNotNull(result);

        //tamanho da lista de retorno é igual ao tamanho da lista de entidades simuladas?
        assertEquals(entityList.size(), result.size());

        //cada elemento na lista de retorno é uma instância de Notification?
        for (Notification notification : result) {
            assertTrue(notification instanceof Notification);
        }
    }

    @Test
    public void testCheckNotifications() {
        // token do usuário
        String token = "user_token";


        UserEntity userEntity = new UserEntity();


        // lista fictícia de NotificationEntity
        List         <NotificationEntity> entityList = new ArrayList<>();
        NotificationEntity entity1 = new NotificationEntity();
        NotificationEntity entity2 = new NotificationEntity();
        NotificationEntity entity3 = new NotificationEntity();
        entityList.add(entity1);
        entityList.add(entity2);
        entityList.add(entity3);

        //devolver o user correspondente ao token
        when(userDao.findUserByToken(token)).thenReturn(userEntity);

        // devolver a lista de entidades
        when(notificationDao.findNotificationsByUser(userEntity)).thenReturn(entityList);

        // Chama o método que você quer testar
        notificationBean.checkNotifications(token);

        // check para cada notificação na lista?
        for (NotificationEntity ntf : entityList) {
            assertTrue(ntf.isChecked());
        }
    }

    @Test
    public void testCreateNotificationMsg() {
        //dados
        String usernameSender = "sender_username";
        String usernameReceptor = "receptor_username";
        LocalDateTime time = LocalDateTime.now();


        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(usernameSender);
        when(userDao.findUserByUsername(usernameReceptor)).thenReturn(userEntity);


        Notification result = notificationBean.createNotificationMsg(usernameSender, time, usernameReceptor);


        verify(notificationDao).persist(any(NotificationEntity.class));


        assertNotNull(result);


        assertEquals(usernameSender, result.getUser().getUsername());
        assertEquals(time, result.getNotificationDateTime());
        assertFalse(result.isChecked());
    }

}







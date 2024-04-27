package bean;

import bean.UserBean;
import dao.*;
import dto.Mensage;
import dto.PasswordDto;
import dto.User;
import entities.MensageEntity;
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
public class MensageBeanTest {

    @Mock
    UserDao userDao;

    @Mock
    MensageDao mensageDao;

    @InjectMocks
    MensageBean mensageBean;

    @BeforeEach
    public void init() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testCreateMensage() {

        String text = "Hello, this is a test message.";
        LocalDateTime time = LocalDateTime.now();
        UserEntity receptor = new UserEntity();
        UserEntity senderEntity = new UserEntity();
        receptor.setUsername("Ricardo");
        senderEntity.setUsername("Beatriz");


        MensageEntity msgEntity = new MensageEntity();
        msgEntity.setReceptor(receptor);
        msgEntity.setSender(senderEntity);
        msgEntity.setText(text);
        msgEntity.setMessageDateTime(time);
        when(mensageDao.createMsg(any(MensageEntity.class))).thenReturn(msgEntity);


        Mensage result = mensageBean.createMensage(text, receptor, time, senderEntity);


        verify(mensageDao).createMsg(any(MensageEntity.class));


        assertNotNull(result);


        assertEquals(text, result.getText());
        assertEquals(time, result.getMessageDateTime());
        assertEquals(receptor.getUsername(), result.getReceptor());
        assertEquals(senderEntity.getUsername(), result.getSender());
        assertFalse(result.isChecked());
    }


}

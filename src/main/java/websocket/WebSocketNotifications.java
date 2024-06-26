package websocket;
import bean.UserBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Mensage;
import dto.Notification;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import utilities.InstantAdapter;
import utilities.LocalDateAdapter;
import utilities.LocalDateTimeAdapter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/notification/{token}")
public class WebSocketNotifications {

    private UserBean userbean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();

    public void send(Notification notiAgain, String notification) {

        UserEntity user = notiAgain.getUser();
        String username = user.getUsername();
        String tokenUser = userbean.getUserByUsername(username).getToken();
        Session receiverSession = sessions.get(tokenUser);
        if (receiverSession!= null) {
            try {
                receiverSession.getBasicRemote().sendObject(notification);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            } catch (EncodeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocketNotification session is opened for client with token: " + token);
        sessions.put(token, session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("WebsocketNotification session is closed with CloseCode: " +
                reason.getCloseCode() + ": " + reason.getReasonPhrase());
        for (String key : sessions.keySet()) {
            if (sessions.get(key) == session) sessions.remove(key);
        }
    }

    @OnMessage
    public void toDoOnMessage(String notification) throws NamingException {
        InitialContext ctx = new InitialContext();
        userbean = (UserBean) ctx.lookup("java:module/UserBean");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                .registerTypeAdapter(Instant.class, new InstantAdapter())
                .create();
        Notification notiAgain = gson.fromJson(notification, Notification.class);
        send(notiAgain,notification);


    }

}

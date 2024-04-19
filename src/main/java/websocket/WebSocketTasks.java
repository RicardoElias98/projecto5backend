package websocket;

import bean.UserBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Mensage;
import dto.Notification;
import dto.Task;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import utilities.LocalDateTimeAdapter;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/tasks/{token}")
public class WebSocketTasks {

    private UserBean userbean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();

    public void send(String task) {
        for (Session session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendObject(task);
                } catch (IOException | EncodeException e) {
                    System.out.println("Error to sendind message " + e.getMessage());
                }
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
    public void toDoOnMessage(String task) throws NamingException {
        /*InitialContext ctx = new InitialContext();
        userbean = (UserBean) ctx.lookup("java:module/UserBean");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        Task taskAgain = gson.fromJson(task, Task.class);*/
        send(task);

    }

}

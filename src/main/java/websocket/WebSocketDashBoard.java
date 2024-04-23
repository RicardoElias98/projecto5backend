package websocket;

import bean.UserBean;
import jakarta.ejb.Singleton;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import javax.naming.NamingException;
import java.io.IOException;
import java.util.HashMap;

@Singleton
@ServerEndpoint("/dashboard/{token}")
public class WebSocketDashBoard {

    private UserBean userbean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();

    public void send(String dashboardNews) {
        for (Session session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.getBasicRemote().sendText(dashboardNews);
                } catch (IOException e) {
                    System.out.println("Error to sendind message " + e.getMessage());
                }
            }
        }
    }

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocketDashBoard session is opened for client with token: " + token);
        sessions.put(token, session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("WebSocketDashBoard session is closed with CloseCode: " +
                reason.getCloseCode() + ": " + reason.getReasonPhrase());
        for (String key : sessions.keySet()) {
            if (sessions.get(key) == session) sessions.remove(key);
        }
    }

    @OnMessage
    public void toDoOnMessage(String dashboardNews) throws NamingException {
        /*InitialContext ctx = new InitialContext();
        userbean = (UserBean) ctx.lookup("java:module/UserBean");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        Task taskAgain = gson.fromJson(task, Task.class);*/
        send(dashboardNews);

    }

}

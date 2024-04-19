package websocket;

import bean.UserBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Mensage;
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
@ServerEndpoint("/message/{token}")
public class WebSocketMessages {

    private UserBean userbean;

    HashMap<String, Session> sessions = new HashMap<String, Session>();

    public void send( Mensage msgAgain, String msg) {
        String usernameReceptor = msgAgain.getReceptor();
        String usernameSender = msgAgain.getSender();
        String token = userbean.getUserByUsername(usernameReceptor).getToken();
        String tokenSender = userbean.getUserByUsername(usernameSender).getToken();
        Session receiverSession = sessions.get(token);
        Session senderSession = sessions.get(tokenSender);
        System.out.println("A new message is received: " + msgAgain.getText());
        if (receiverSession != null && senderSession != null) {
            try {
                receiverSession.getBasicRemote().sendObject(msg);
                senderSession.getBasicRemote().sendObject(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            } catch (EncodeException e) {
                throw new RuntimeException(e);
            }
        } else if (receiverSession == null && senderSession != null) {
            try {
                senderSession.getBasicRemote().sendObject(msg);
            } catch (IOException e) {
                System.out.println("Something went wrong!");
            } catch (EncodeException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @OnOpen
    public void toDoOnOpen(Session session, @PathParam("token") String token) {
        System.out.println("A new WebSocket session is opened for client with token: " + token);
        sessions.put(token, session);
    }

    @OnClose
    public void toDoOnClose(Session session, CloseReason reason) {
        System.out.println("Websocket session is closed with CloseCode: " +
                reason.getCloseCode() + ": " + reason.getReasonPhrase());
        for (String key : sessions.keySet()) {
            if (sessions.get(key) == session) sessions.remove(key);
        }
    }

    @OnMessage
    public void toDoOnMessage(String msg) throws NamingException {
        InitialContext ctx = new InitialContext();
        userbean = (UserBean) ctx.lookup("java:module/UserBean");
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();
        Mensage msgAgain = gson.fromJson(msg, Mensage.class);
        send(msgAgain,msg);

    }
}


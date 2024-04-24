package service;

import bean.MensageBean;
import bean.NotificationBean;
import bean.UserBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Mensage;
import dto.Notification;
import dto.User;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import utilities.InstantAdapter;
import utilities.LocalDateAdapter;
import utilities.LocalDateTimeAdapter;
import websocket.WebSocketMessages;
import websocket.WebSocketNotifications;

import javax.naming.NamingException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Path("/msg")
public class MensageService {

    @Inject
    MensageBean mensageBean;

    @Inject
    UserBean userBean;

    @Inject
    NotificationBean notificationBean;

    @EJB
    WebSocketMessages webSocketMessages;

    @EJB
    WebSocketNotifications webSocketNotifications;


    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMsg(@HeaderParam("token") String token, Mensage msg) throws NamingException {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.tokenExists(token);

            if (!user) {
                return Response.status(403).entity("User with this token is not found").build();
            } else {

                String receptorUsername = msg.getReceptor();
                String mensage = msg.getText();
                String senderUsername = msg.getSender();
                LocalDateTime time = msg.getMessageDateTime();
                User userdto = userBean.getUserByUsername(receptorUsername);
                UserEntity userEntity = userBean.convertToEntity(userdto);
                User senderDto = userBean.getUserByUsername(senderUsername);
                UserEntity senderEntity = userBean.convertToEntity(senderDto);
                mensageBean.createMensage(mensage, userEntity, time, senderEntity);
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .create();
                String jsonMsg = gson.toJson(msg);
                System.out.println(jsonMsg);
                webSocketMessages.toDoOnMessage(jsonMsg);
                Gson gson2 = new GsonBuilder()
                        .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                        .registerTypeAdapter(Instant.class, new InstantAdapter())
                        .create();
                Notification notification = notificationBean.createNotificationMsg(senderUsername, msg.getMessageDateTime(), receptorUsername);
                System.out.println("notification" + notification.getText() + notification.getNotificationDateTime() + notification.getId() + notification.getUser());
                String jsonNoti = gson2.toJson(notification);
                System.out.println("Passed");
                webSocketNotifications.toDoOnMessage(jsonNoti);
                userBean.setTokenTimer(token);
                return Response.status(201).entity("A new msg is created").build();
            }
        }
    }

    @GET
    @Path("/tradedMsgs")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response tradedMsgs(@HeaderParam("token") String token, @HeaderParam("sender") String sender, @HeaderParam("receptor") String receptor) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.tokenExists(token);
            if (!user) {
                return Response.status(403).entity("User with this token is not found").build();
            } else {
                User senderDto = userBean.getUserByUsername(sender);
                UserEntity senderEntity = userBean.convertToEntity(senderDto);
                User receptorDto = userBean.getUserByUsername(receptor);
                UserEntity receptorEntity = userBean.convertToEntity(receptorDto);
                List<Mensage> msgs = mensageBean.getTradedMsgs(senderEntity, receptorEntity);
                userBean.setTokenTimer(token);
                return Response.status(200).entity(msgs).build();
            }
        }
    }

    @PUT
    @Path("/udpateChecked/{checked}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateChecked(@HeaderParam("token") String token, @HeaderParam("msgId") int msgId ,@PathParam("checked")  Boolean checked) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.tokenExists(token);
            if (!user) {
                return Response.status(403).entity("User with this token is not found").build();
            } else {
                mensageBean.updateChecked(msgId, checked);
                userBean.setTokenTimer(token);
                return Response.status(200).entity("Msg is checked").build();
            }
        }
    }



}

package service;

import bean.NotificationBean;
import bean.UserBean;
import dto.Notification;
import dto.User;
import entities.UserEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.*;

import java.util.List;


@Path("/notif")
public class NotificationService {

    @Inject
    UserBean userBean;

    @Inject
    NotificationBean notificationBean;

    @GET
    @Path("/notifications")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("token") String token, @HeaderParam("username") String username) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else {
            User userdto = userBean.getUserByUsername(username);
            UserEntity userEntity = userBean.convertToEntity(userdto);
            List<Notification> listNotif = notificationBean.getAllNotification(userEntity);
            return Response.status(200).entity(listNotif).build();
        }
    }

    @GET
    @Path("/notificationsNotChecked")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNotifications(@HeaderParam("token") String token) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else {
            List<Notification> listNotif = notificationBean.getNotCheckedNotif(token);
            return Response.status(200).entity(listNotif).build();
        }
    }
    @PUT
    @Path("/checkNotification")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkNotification(@HeaderParam("token") String token) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else {
            notificationBean.checkNotifications(token);
            return Response.status(200).entity("Notifications checked").build();
        }
    }

}

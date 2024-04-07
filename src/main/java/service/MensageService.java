package service;

import bean.MensageBean;
import bean.UserBean;
import dto.Mensage;
import dto.User;
import entities.UserEntity;
import jakarta.inject.Inject;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Path("/msg")
public class MensageService {

    @Inject
    MensageBean mensageBean;

    @Inject
    UserBean userBean;

    @POST
    @Path("/create")
    @Produces(MediaType.APPLICATION_JSON)
    public Response createMsg(@HeaderParam("token") String token, Mensage msg) {
        boolean user = userBean.tokenExists(token);

        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else  {
           String receptorUsername = msg.getReceptor();
           String mensage = msg.getText();
           LocalDateTime time = msg.getMessageDateTime();
            User userdto = userBean.getUserByUsername(receptorUsername);
            UserEntity userEntity = userBean.convertToEntity(userdto);
            mensageBean.createMensage(mensage,userEntity, time);
            return Response.status(201).entity("A new msg is created").build();
        }
    }

}

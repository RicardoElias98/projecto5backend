package service;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import bean.LogBean;
import bean.TaskBean;
import bean.UserBean;
import dto.*;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import utilities.EmailSender;
import utilities.EncryptHelper;
import websocket.WebSocketDashBoard;
import websocket.WebSocketMessages;

import javax.naming.NamingException;
import org.apache.logging.log4j.LogManager;


@Path("/user")
public class UserService {
    @Context
    private HttpServletRequest request;
    @Inject
    UserBean userBean;
    @Inject
    EncryptHelper encryptHelper;

    @Inject
    LogBean logBean;

    @Inject
    EmailSender emailSender;

    @Inject
    TaskBean taskBean;

    @EJB
    WebSocketDashBoard webSocketDashBoard;


    @GET
    @Path("/dashBoardInfo")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dashBoardInfo (@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.tokenExists(token);
            if (!user) {
                return Response.status(403).entity("User with this token is not found").build();
            } else {
                ArrayList<Long> dbInfo = new ArrayList<>();
                /* Info dos users confirmados [0] */
                long confirmedUsers = userBean.getConfirmedUsers();
                dbInfo.add(confirmedUsers);
                /* Info dos users não confirmados [1]*/
                long notConfirmedUsers = userBean.getNotConfirmedUsers();
                dbInfo.add(notConfirmedUsers);
                /* Info do nº de tasks por status */
                long taskByStatus10 = taskBean.getTasksByStatus(10);
                long taskByStatus20 = taskBean.getTasksByStatus(20);
                long taskByStatus30 = taskBean.getTasksByStatus(30);
                dbInfo.add(taskByStatus10); /* [2] */
                dbInfo.add(taskByStatus20); /* [3] */
                dbInfo.add(taskByStatus30); /* [4] */
                userBean.setTokenTimer(token);
                return Response.status(200).entity(dbInfo).build();
            }
        }
    }

    @GET
    @Path("/dashBoardInfoMediaTasksByUser")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response dashBoardInfoMedia (@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.tokenExists(token);
            if (!user) {
                return Response.status(403).entity("User with this token is not found").build();
            } else {
                /* Info do nº médio de tasks por user */
                double medTasksByUser = userBean.getMedTaskByUser();
                userBean.setTokenTimer(token);
                return Response.status(200).entity(medTasksByUser).build();
            }
        }
    }


    @PUT
    @Path("/tokenConfirmationAndChangePassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response tokenConfirmation(@HeaderParam("tokenConfirmation") String tokenConfirmation, @HeaderParam("password") String password, @HeaderParam("passwordConfirmation") String passwordConfirmation) throws NamingException {
            if (userBean.confirmToken(tokenConfirmation)) {
                if (!password.equals(passwordConfirmation)){
                    return Response.status(400).entity("Passwords are not the same").build();
                } else {
                    userBean.updateFirstPass(tokenConfirmation,password);
                    webSocketDashBoard.toDoOnMessage("news");
                    return Response.status(200).entity("User is confirmated and first password is updated").build();
                }
            } else  {
                return Response.status(403).entity("User with this token confirmation is not found").build();
            }
        }


    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUsers(@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else {
                List<UserEntity> users = userBean.getUsers();
            userBean.setTokenTimer(token);
            return Response.status(200).entity(users).build();
        }
    }}

    @GET
    @Path("/allActiveUsers")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getActiveUsers(@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.tokenExists(token);
            if (!user) {
                return Response.status(403).entity("User with this token is not found").build();
            } else {
                List usersByDate = userBean.getActiveUsersByDate();
                userBean.setTokenTimer(token);
                return Response.status(200).entity(usersByDate).build();
            }
        }
    }

    @POST
    @Path("/emailRecoveryPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response emailRecoveryPassword (@HeaderParam("email") String email) {
       String username =  userBean.usernameByEmail(email);
       User user = userBean.getUserByUsername(username);
        System.out.println(user.getUsername() + user.getConfirmationToken() + user.getEmail());
        emailSender.sendRecoveryPassword("testeAor@hotmail.com",user.getConfirmationToken());
        return Response.status(200).entity("Email sended").build();
    }

    @PUT
    @Path("/recoveryPassword")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response recoveryPassord (@HeaderParam("confirmationToken") String confirmationToken, @HeaderParam("password") String password, @HeaderParam("confirmPassword") String confirmPassword) {
        if (userBean.confirmToken(confirmationToken)) {
            if (password.equals(confirmPassword)) {
                if (!userBean.recoveryPassword(confirmationToken, password)) {
                    return Response.status(401).entity("Invalid information").build();
                } else {
                    return Response.status(200).entity("Password is updated").build();
                }
            } else {return Response.status(400).entity("Passwords are not the same").build();}
        }  else {
            return Response.status(403).entity("Forbidden").build();
        }
    }


    @POST
    @Path("/register")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addUser(User a) throws NamingException {
        boolean valid = userBean.isUserValid(a);
        if (!valid) {
            return Response.status(400).entity("All elements are required").build();
        }
        boolean user = userBean.userNameExists(a.getUsername());
        if (user) {
            return Response.status(409).entity("User with this username is already exists").build();
        } else {
            if (a.getRole() == null || a.getRole().isEmpty()) {
                a.setRole("developer");
            }
            User userWithConfirmationToken = userBean.addUser(a);
            emailSender.sendConfirmationEmail("testeAor@hotmail.com",userWithConfirmationToken.getConfirmationToken());
            webSocketDashBoard.toDoOnMessage("news");
            return Response.status(201).entity(userWithConfirmationToken).build();
        }
    }

    @GET
    @Path("/photo")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getPhoto(@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
        boolean user = userBean.userExists(token);
        boolean authorized = userBean.isUserAuthorized(token);
        if (!user) {
            return Response.status(404).entity("User with this username is not found").build();
        } else if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        }
        User user1 = userBean.getUser(token);
        if (user1.getUserPhoto() == null) {
            return Response.status(400).entity("User with no photo").build();
        }
        return Response.status(200).entity(user1.getUserPhoto()).build();
    } }

    @GET
    @Path("/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean exists = userBean.findOtherUserByUsername(username);
            if (!exists) {
                return Response.status(404).entity("User with this username is not found").build();
            } else if (userBean.getUser(token).getRole().equals("developer") && !userBean.getUser(token).getUsername().equals(username)) {
                return Response.status(403).entity("Forbidden").build();
            }
            User user = userBean.getUserByUsername(username);
            UserDto userDto = userBean.convertUsertoUserDto(user);
            userBean.setTokenTimer(token);
            return Response.status(200).entity(userDto).build();
        }
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateUser(@HeaderParam("token") String token, User a) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean user = userBean.userNameExists(a.getUsername());
            boolean valid = userBean.isUserValid(a);
            if (!user) {
                return Response.status(404).entity("User with this username is not found").build();
            } else if (!valid) {
                return Response.status(406).entity("All elements are required").build();
            }
            if (!userBean.getUser(token).getRole().equals("Owner") || a.getUsername().equals(userBean.getUser(token).getUsername()) && (a.getRole() == null)) {
                a.setRole(userBean.getUser(token).getRole());
                a.setPassword(userBean.getUser(token).getPassword());
                boolean updated = userBean.updateUser(token, a);
                if (!updated) {
                    return Response.status(400).entity("Failed. User not updated").build();
                }
                userBean.setTokenTimer(token);
                logBean.logUserInfo(token,"User updted to: " + a.getName() + " " + a.getEmail() + " " +  a.getUserPhoto() + " " +  a.getRole() + " " +  a.getContactNumber(),1 );
                return Response.status(200).entity("User updated").build();

            } else if (userBean.getUser(token).getRole().equals("Owner") && a.getRole() != null) {
                boolean updated = userBean.ownerupdateUser(token, a);

                if (!updated) {
                    return Response.status(400).entity("Failed. User not updated").build();
                }
                userBean.setTokenTimer(token);
                logBean.logUserInfo(token,"User updted to: " + a.getName() + " " + a.getEmail() + " " +  a.getUserPhoto() + " " +  a.getRole() + " " +  a.getContactNumber(),1 );
                return Response.status(200).entity("User updated").build();
            }
            return Response.status(403).entity("Forbidden").build();
        }
    }

    @PUT
    @Path("/updatePassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response updatePassword(@HeaderParam("token") String token, PasswordDto password) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            boolean valid = userBean.isPasswordValid(password);
            if (!authorized) {
                return Response.status(403).entity("Forbidden").build();
            } else if (!valid) {
                return Response.status(406).entity("Password is not valid").build();
            } else {
                boolean updated = userBean.updatePassword(token, password);
                if (!updated) {
                    return Response.status(400).entity("Failed. Password not updated").build();
                }
                userBean.setTokenTimer(token);
                return Response.status(200).entity("Password updated").build();
            }
        }
    }

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam("username") String username, @HeaderParam("password") String password) {

        if (username == null || password == null) {
            return Response.status(400).entity("Username and password headers are required").build();
        }

        User user = userBean.getUserByUsername(username);

        if (!user.isActive()) {
            return Response.status(403).entity("User is not active").build();
        }  else if (!user.isConfirmed()) {
            return Response.status(403).entity("User is not confirmed").build();
        } else {
            String token = userBean.login(username, password);
            if (token == null) {
                return Response.status(404).entity("User with this username and password is not found").build();
            } else {
                logBean.logUserInfo(token,"User logged with username: " + username,1 );
                return Response.status(200).entity(token).build();
            }
        }
    }

    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(405).entity("Forbidden").build();
        } else {
            userBean.logout(token);
            return Response.status(200).entity("Logged out").build();
        }
    }

    @DELETE
    @Path("/delete/{username}")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response deleteUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserOwner(token);
            if (!authorized) {
                return Response.status(403).entity("Forbidden").build();
            } else {
                if (userBean.deleteUser(token, username)) {
                    userBean.setTokenTimer(token);
                   //logBean.logUserInfo(token,"User deleted: " + username,1 );
                    return Response.status(200).entity("User deleted").build();
                } else {
                    return Response.status(400).entity("User not deleted").build();
                }
            }
        }
    }

    @GET
    @Path("/myUserDto")
    @Produces(MediaType.APPLICATION_JSON)
    public Response myProfile(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(403).entity("Forbidden").build();
        } else {
            User user = userBean.getUser(token);
            UserDto userDto = userBean.convertUsertoUserDto(user);
            return Response.status(200).entity(userDto).build();
        }
    }

    @POST
    @Path("/restore/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreUser(@HeaderParam("token") String token, @PathParam("username") String username) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserOwner(token);
            if (!authorized) {
                return Response.status(405).entity("Forbidden").build();
            } else {
                if (userBean.restoreUser(username)) {
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity("User restored").build();
                } else {
                    return Response.status(400).entity("User not restored").build();
                }
            }
        }
    }

    @GET
    @Path("/search/{user}")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response searchUser(@HeaderParam("token") String token, @PathParam("user") String name) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(403).entity("Forbidden").build();
            } else {
                List<UserDto> searchResult = userBean.getUserBySearch(name);
                userBean.setTokenTimer(token);
                return Response.status(200).entity(searchResult).build();
            }
        }
    }
}


package service;

import bean.LogBean;
import bean.TaskBean;
import bean.UserBean;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dto.Category;
import dto.Task;
import dto.TaskCreator;
import dto.User;
import entities.TaskEntity;
import entities.UserEntity;
import entities.CategoryEntity;
import jakarta.ejb.EJB;
import jakarta.inject.Inject;
import jakarta.json.Json;
import jakarta.json.JsonObject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.HeaderParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.io.StringReader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.ws.rs.*;
import utilities.LocalDateAdapter;
import utilities.LocalDateTimeAdapter;
import websocket.WebSocketDashBoard;
import websocket.WebSocketTasks;

import javax.naming.NamingException;

@Path("/task")
public class TaskService {
    @Inject
    TaskBean taskBean;
    @Inject
    UserBean userBean;

    @Inject
    LogBean logBean;

    @EJB
    WebSocketTasks webSocketTasks;

    @EJB
    WebSocketDashBoard webSocketDashBoard;

    @GET
    @Path("/listDesCcategory")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response listDesCcategory (@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(401).entity("Invalid Token").build();
        } else {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else {
            List<Object[]> listDes = taskBean.getListDescCate();
            userBean.setTokenTimer(token);
            return Response.status(200).entity(listDes).build();
        }
    } }

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isUserValid(@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            ArrayList<Task> taskList = new ArrayList<>();
            for (TaskEntity taskEntity : taskBean.getTasks()) {
                    taskList.add(taskBean.convertToDto(taskEntity));
            }
            taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
            userBean.setTokenTimer(token);
            return Response.status(200).entity(taskList).build();
        }
    } }

    @GET
    @Path("/byUser/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByUser(@HeaderParam("token") String token,@PathParam("username") String username) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            User user = userBean.getUserByUsername(username);
            ArrayList<Task> taskList = new ArrayList<>();
            for (TaskEntity taskEntity : taskBean.getTasksByUser(userBean.convertToEntity(user))) {
                if(taskEntity.isActive()) {
                    taskList.add(taskBean.convertToDto(taskEntity));
                }
            }
            taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
            userBean.setTokenTimer(token);
            return Response.status(200).entity(taskList).build();
        }
    }}

    @GET
    @Path("/byCategory/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategory(@HeaderParam("token") String token, @PathParam("category") String category) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            ArrayList<Task> taskList = new ArrayList<>();
            for (TaskEntity taskEntity : taskBean.getTasks()) {
                if(taskEntity.isActive()) {
                    if(taskEntity.getCategory().getName().equals(category)) {
                        taskList.add(taskBean.convertToDto(taskEntity));
                    }
                }
            }
            taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
            userBean.setTokenTimer(token);
            return Response.status(200).entity(taskList).build();
        }
    } }

    @GET
    @Path("/byCategoryAndUser/{category}/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategoryAndUser (@HeaderParam("token") String token, @PathParam("category") String category, @PathParam("username") String username) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                User user = userBean.getUserByUsername(username);
                List<Task> taskList = taskBean.getTasksByCategoryAndUser(userBean.convertToEntity(user), category);
                taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
                userBean.setTokenTimer(token);
                return Response.status(200).entity(taskList).build();
            }
        }
    }

    @DELETE
    @Path("/deleteAll/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAllTasks(@HeaderParam("token") String token, @PathParam("username") String username) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserOwner(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                User user = userBean.getUserByUsername(username);
                boolean removed = taskBean.deleteAllTasksByUser(userBean.convertToEntity(user));
                if (!removed) {
                    return Response.status(400).entity("Failed. Tasks not removed").build();
                } else {
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity("Tasks removed").build();
                }
            }
        }
    }
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(Task task, @HeaderParam("token") String token) throws NamingException {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean valid = taskBean.isTaskValid(task);
                boolean categoryExists = taskBean.categoryExists(task.getCategory());
                if (!valid) {
                    return Response.status(400).entity("All elements are required").build();
                } else if (!categoryExists) {
                    return Response.status(400).entity("Category does not exist").build();
                }
                User user = userBean.getUser(token);
                taskBean.setInitialId(task);
                UserEntity userEntity = userBean.convertToEntity(user);
                TaskEntity taskEntity = taskBean.createTaskEntity(task, userEntity);
                taskBean.addTask(taskEntity);

                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                        .create();
                String jsonTask = gson.toJson(task);
                System.out.println(jsonTask);
                webSocketTasks.toDoOnMessage(jsonTask);
                webSocketDashBoard.toDoOnMessage("news");
                logBean.logUserInfo(token,"Task added with this id: " + taskEntity.getId(), 1);
                userBean.setTokenTimer(token);
                return Response.status(201).entity(taskBean.convertToDto(taskEntity)).build();
            }
        }
    }

    @PUT
    @Path("/restore/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreTask(@HeaderParam("token") String token, @PathParam("id") String id) throws NamingException {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean restored = taskBean.restoreTask(id);
                if (!restored) {
                    return Response.status(400).entity("Failed. Task not restored").build();
                } else {
                    webSocketDashBoard.toDoOnMessage("news");
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity("Task restored").build();
                }
            }
        }
    }
    @POST
    @Path("/createCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategory(Category category, @HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserOwner(token);
            User user = userBean.getUser(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean available = taskBean.categoryExists(category.getName());
                if (available) {
                    return Response.status(409).entity("Name not available").build();
                }
                Category dto = taskBean.createCategory(category.getName(), user.getUsername());
                userBean.setTokenTimer(token);
                return Response.status(201).entity(dto).build();
            }
        }
    }

    @PUT
    @Path("/updateCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(Category category, @HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserOwner(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean notavailable = taskBean.categoryExists(category.getName());
                if (notavailable) {
                    return Response.status(409).entity("Category name is not available").build();
                }
            }
            CategoryEntity categoryEntity = taskBean.findCategoryById(category.getId());
            categoryEntity.setName(category.getName());
            if (taskBean.updateCategory(categoryEntity)) {
                userBean.setTokenTimer(token);
                return Response.status(200).entity("Category updated").build();
            } else {
                return Response.status(400).entity("Failed. Category not updated").build();
            }
        }
    }

    @DELETE
    @Path("/deleteCategory/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeCategory(@HeaderParam("token") String token, @PathParam("name") String name) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserOwner(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean exists = taskBean.categoryExists(name);
                if (!exists) {
                    return Response.status(404).entity("Category does not exist").build();
                }
                boolean removed = taskBean.removeCategory(name);
                if (!removed) {
                    return Response.status(409).entity("Failed. Category not removed. update all tasks before deleting the category").build();
                } else {
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity("Category removed").build();
                }
            }
        }
    }

    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(Task task, @HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            User user = userBean.getUser(token);
            TaskEntity taskEntity = taskBean.convertToEntity(task);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean valid = taskBean.isTaskValid(task);
                boolean categoryExists = taskBean.categoryExists(task.getCategory());
                if (!valid) {
                    return Response.status(406).entity("All elements are required").build();
                } else if (!categoryExists) {
                    return Response.status(404).entity("Category does not exist").build();
                } else if (!user.getUsername().equals(taskEntity.getUser().getUsername()) && user.getRole().equals("developer")) {
                    return Response.status(403).entity("Forbidden").build();
                }
                String category = task.getCategory();
                CategoryEntity categoryEntity = taskBean.findCategoryByName(category);
                taskEntity.setCategory(categoryEntity);
                boolean updated = taskBean.updateTask(taskEntity);
                if (!updated) {
                    return Response.status(400).entity("Failed. Task not updated").build();
                } else {
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity(taskBean.convertToDto(taskEntity)).build();
                }
            }
        }
    }


    @PUT
    @Path("/changeStatus/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeStatus(@HeaderParam("token") String token, @PathParam("id") String id,  String status) throws NamingException {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                JsonObject jsonObject = Json.createReader(new StringReader(status)).readObject();
                int newActiveStatus = jsonObject.getInt("status");
                Task taskdto = taskBean.changeStatus(id, newActiveStatus);
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                        .create();
                String jsonTask = gson.toJson(taskdto);
                System.out.println(jsonTask);
                webSocketTasks.toDoOnMessage(jsonTask);
                webSocketDashBoard.toDoOnMessage("news");
                userBean.setTokenTimer(token);
                return Response.status(200).entity("Status changed").build();
            }
        }
    }

    @DELETE
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeTask(@HeaderParam("token") String token, @PathParam("id") String id) throws NamingException {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                boolean removed = taskBean.removeTask(id);
                if (!removed) {
                    return Response.status(400).entity("Failed. Task not removed").build();
                } else {
                    webSocketDashBoard.toDoOnMessage("news");
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity("Task removed").build();
                }
            }
        }
    }
    @PUT
    @Path("/block/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response blockTask(@HeaderParam("token") String token, @PathParam("id") String id) throws NamingException {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            User user = userBean.getUser(token);
            String role = user.getRole();
            System.out.println("role a entrar " + role);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                Task blocked = taskBean.blockTask(id, role);
                if (blocked == null) {
                    return Response.status(400).entity("Failed. Task not blocked").build();
                } else {
                    Gson gson = new GsonBuilder()
                            .registerTypeAdapter(LocalDate.class, new LocalDateAdapter())
                            .create();
                    String jsonTask = gson.toJson(blocked);
                    System.out.println(jsonTask);
                    webSocketTasks.toDoOnMessage(jsonTask);
                    webSocketDashBoard.toDoOnMessage("news");
                    userBean.setTokenTimer(token);
                    return Response.status(200).entity("Task blocked").build();
                }
            }
        }
    }

    @GET
    @Path("/allCategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                ArrayList<Category> categoryList = new ArrayList<>();
                for (CategoryEntity categoryEntity : taskBean.getAllCategories()) {
                    categoryList.add(taskBean.convertCatToDto(categoryEntity));
                }
                userBean.setTokenTimer(token);
                return Response.status(200).entity(categoryList).build();
            }
        }
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskById(@HeaderParam("token") String token, @PathParam("id") String id) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                Task task = taskBean.findTaskById(id);
                userBean.setTokenTimer(token);
                return Response.status(200).entity(task).build();
            }
        }
    }

    @GET
    @Path("/creator/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreatorByName(@HeaderParam("token") String token, @PathParam("id") String id) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {
                TaskCreator creator = taskBean.findUserById(id);
                userBean.setTokenTimer(token);
                return Response.status(200).entity(creator).build();
            }
        }
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlltasksByStatus(@HeaderParam("token") String token, @HeaderParam("status") int status) {
        if (!userBean.isTokenValid(token)) {
            userBean.logout(token);
            return Response.status(403).entity("Invalid Token").build();
        } else {
            boolean authorized = userBean.isUserAuthorized(token);
            if (!authorized) {
                return Response.status(401).entity("Unauthorized").build();
            } else {

                List<Task> taskList = taskBean.tasksByStatus(status);
                taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
                userBean.setTokenTimer(token);
                return Response.status(200).entity(taskList).build();
            }
        }
    }

    @GET
    @Path("/taskDoneByDate")
    @Consumes (MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response gettaskDoneByDate (@HeaderParam("token") String token) {
        boolean user = userBean.tokenExists(token);
        if (!user) {
            return Response.status(403).entity("User with this token is not found").build();
        } else {
            List tasksListByDate = taskBean.getTasksDoneByDate();
            userBean.setTokenTimer(token);
            return Response.status(200).entity(tasksListByDate).build();
        }
    }


}

package service;

import bean.TaskBean;
import bean.UserBean;
import dto.Category;
import dto.Task;
import dto.TaskCreator;
import dto.User;
import entities.TaskEntity;
import entities.UserEntity;
import entities.CategoryEntity;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import jakarta.ws.rs.*;
@Path("/task")
public class TaskService {
    @Inject
    TaskBean taskBean;
    @Inject
    UserBean userBean;

    @GET
    @Path("/all")
    @Produces(MediaType.APPLICATION_JSON)
    public Response isUserValid(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            ArrayList<Task> taskList = new ArrayList<>();
            for (TaskEntity taskEntity : taskBean.getTasks()) {
                    taskList.add(taskBean.convertToDto(taskEntity));
            }
            taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
            return Response.status(200).entity(taskList).build();
        }
    }
    @GET
    @Path("/byUser/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByUser(@HeaderParam("token") String token,@PathParam("username") String username) {
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
            return Response.status(200).entity(taskList).build();
        }
    }@GET
    @Path("/byCategory/{category}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategory(@HeaderParam("token") String token, @PathParam("category") String category) {
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
            return Response.status(200).entity(taskList).build();
        }
    }

    @GET
    @Path("/byCategoryAndUser/{category}/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTasksByCategoryAndUser (@HeaderParam("token") String token, @PathParam("category") String category, @PathParam("username") String username) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            User user = userBean.getUserByUsername(username);
            List<Task> taskList = taskBean.getTasksByCategoryAndUser(userBean.convertToEntity(user), category);
            taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
            return Response.status(200).entity(taskList).build();
        }
    }


    @DELETE
    @Path("/deleteAll/{username}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeAllTasks(@HeaderParam("token") String token, @PathParam("username") String username) {
        boolean authorized = userBean.isUserOwner(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            User user = userBean.getUserByUsername(username);
            boolean removed = taskBean.deleteAllTasksByUser(userBean.convertToEntity(user));
            if (!removed) {
                return Response.status(400).entity("Failed. Tasks not removed").build();
            } else {
                return Response.status(200).entity("Tasks removed").build();
            }
        }
    }
    @POST
    @Path("/add")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response addTask(Task task, @HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean valid = taskBean.isTaskValid(task);
            boolean categoryExists = taskBean.categoryExists(task.getCategory());
            if (!valid) {
                return Response.status(400).entity("All elements are required").build();
            }else if (!categoryExists){
                return Response.status(400).entity("Category does not exist").build();
            }
            User user = userBean.getUser(token);
            taskBean.setInitialId(task);
            UserEntity userEntity = userBean.convertToEntity(user);
            TaskEntity taskEntity = taskBean.createTaskEntity(task,userEntity);
            taskBean.addTask(taskEntity);
            return Response.status(201).entity(taskBean.convertToDto(taskEntity)).build();
        }
    }
    @PUT
    @Path("/restore/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response restoreTask(@HeaderParam("token") String token, @PathParam("id") String id) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean restored = taskBean.restoreTask(id);
            if (!restored) {
                return Response.status(400).entity("Failed. Task not restored").build();
            } else {
                return Response.status(200).entity("Task restored").build();
            }
        }
    }
    @POST
    @Path("/createCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response createCategory(Category category, @HeaderParam("token") String token) {
        boolean authorized = userBean.isUserOwner(token);
           User user = userBean.getUser(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean available = taskBean.categoryExists(category.getName());
            if (available) {
                return Response.status(409).entity("Name not available").build();
            }
            taskBean.createCategory(category.getName(), user.getUsername());
            return Response.status(201).entity("Category created").build();
        }
    }
    @PUT
    @Path("/updateCategory")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateCategory(Category category, @HeaderParam("token") String token) {
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
            return Response.status(200).entity("Category updated").build();
        }else{
            return Response.status(400).entity("Failed. Category not updated").build();
        }
    }

    @DELETE
    @Path("/deleteCategory/{name}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeCategory(@HeaderParam("token") String token, @PathParam("name") String name) {
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
                return Response.status(200).entity("Category removed").build();
            }
        }
    }
    @PUT
    @Path("/update")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response updateTask(Task task, @HeaderParam("token") String token) {
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
            }else if (!categoryExists){
                return Response.status(404).entity("Category does not exist").build();
            }else if(!user.getUsername().equals(taskEntity.getUser().getUsername()) && user.getRole().equals("developer")){
                return Response.status(403).entity("Forbidden").build();
            }
            String category = task.getCategory();
            CategoryEntity categoryEntity = taskBean.findCategoryByName(category);
            taskEntity.setCategory(categoryEntity);
            boolean updated = taskBean.updateTask(taskEntity);
            if(!updated){
                return Response.status(400).entity("Failed. Task not updated").build();
            } else{
                return Response.status(200).entity(taskBean.convertToDto(taskEntity)).build();
            }
        }
    }


    @PUT
    @Path("/changeStatus/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeStatus(@HeaderParam("token") String token, @PathParam("id") String id,  String status) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            JsonObject jsonObject = Json.createReader(new StringReader(status)).readObject();
            int newActiveStatus = jsonObject.getInt("status");
            boolean changed = taskBean.changeStatus(id, newActiveStatus);
            if (!changed) {
                return Response.status(400).entity("Failed. Status not changed").build();
            } else {
                return Response.status(200).entity("Status changed").build();
            }
        }
    }
    @DELETE
    @Path("/delete/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response removeTask(@HeaderParam("token") String token, @PathParam("id") String id) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean removed = taskBean.removeTask(id);
            if (!removed) {
                return Response.status(400).entity("Failed. Task not removed").build();
            } else {
                return Response.status(200).entity("Task removed").build();
            }
        }
    }
    @PUT
    @Path("/block/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response blockTask(@HeaderParam("token") String token, @PathParam("id") String id) {
        boolean authorized = userBean.isUserAuthorized(token);
        User user = userBean.getUser(token);
        String role = user.getRole();
        System.out.println("role a entrar " + role);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            boolean blocked = taskBean.blockTask(id, role);
            if (!blocked) {
                return Response.status(400).entity("Failed. Task not blocked").build();
            } else {
                return Response.status(200).entity("Task blocked").build();
            }
        }
    }
    @GET
    @Path("/allCategories")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAllCategories(@HeaderParam("token") String token) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            ArrayList<Category> categoryList = new ArrayList<>();
            for (CategoryEntity categoryEntity : taskBean.getAllCategories()) {
                categoryList.add(taskBean.convertCatToDto(categoryEntity));
            }
            return Response.status(200).entity(categoryList).build();
        }
    }
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTaskById(@HeaderParam("token") String token, @PathParam("id") String id) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            Task task = taskBean.findTaskById(id);
            return Response.status(200).entity(task).build();
        }
    }
    @GET
    @Path("/creator/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getCreatorByName(@HeaderParam("token") String token, @PathParam("id") String id) {
        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {
            TaskCreator creator = taskBean.findUserById(id);
            return Response.status(200).entity(creator).build();
        }
    }

    @GET
    @Path("/status")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getAlltasksByStatus(@HeaderParam("token") String token, @HeaderParam("status") int status) {

        boolean authorized = userBean.isUserAuthorized(token);
        if (!authorized) {
            return Response.status(401).entity("Unauthorized").build();
        } else {

            List<Task> taskList = taskBean.tasksByStatus(status);
            taskList.sort(Comparator.comparing(Task::getPriority, Comparator.reverseOrder()).thenComparing(Comparator.comparing(Task::getStartDate).thenComparing(Task::getEndDate)));
            return Response.status(200).entity(taskList).build();
        }
    }


}

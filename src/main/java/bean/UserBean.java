package bean;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import dao.MensageDao;
import dao.NotificationDao;
import dao.UserDao;
import dto.*;
import entities.NotificationEntity;
import entities.TaskEntity;
import entities.UserEntity;
import jakarta.ejb.EJB;
import jakarta.ejb.Singleton;
import utilities.EncryptHelper;
import java.time.temporal.ChronoUnit;

@Singleton
public class UserBean {
    public UserBean() {
    }

    @EJB
    UserDao userDao;
    @EJB
    TaskBean taskDao;

    @EJB
    NotificationDao notificationDao;
    @EJB
    EncryptHelper EncryptHelper;

    int tokenTimer = 300;

    public User addUser(User a) {

        a.setPassword(EncryptHelper.encryptPassword(a.getPassword()));
        a.setConfirmed(false);
        String confirmationToken = generateToken();
        a.setConfirmationToken(confirmationToken);
        a.setRegistrationDate(LocalDate.now());
        System.out.println("confirmation Token " + a.getConfirmationToken() );
        UserEntity userEntity = convertToEntity(a);
        userDao.persist(userEntity);
        return a;
    }

    public User getUser(String token) {
        UserEntity userEntity = userDao.findUserByToken(token);
        return convertToDto(userEntity);
    }

    public User findUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        return convertToDto(userEntity);
    }


    public List<UserEntity> getUsers() {
        List<UserEntity> users = userDao.findAll();
        return users;
    }

    public boolean blockUser(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            a.setActive(false);
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean removeUser(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            userDao.remove(a);
            return true;
        }
        return false;
    }

    public boolean ownerupdateUser(String token, User user) {
        UserEntity a = userDao.findUserByUsername(user.getUsername());
        UserEntity responsible = userDao.findUserByToken(token);
        if (a != null && responsible.getRole().equals("Owner")) {
            a.setName(user.getName());
            a.setEmail(user.getEmail());
            a.setContactNumber(user.getContactNumber());
            a.setUserPhoto(user.getUserPhoto());
            a.setRole(user.getRole());
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean updateUser(String token, User user) {
        UserEntity a = userDao.findUserByUsername(user.getUsername());
        if (a != null) {
            a.setUsername(user.getUsername());
            a.setName(user.getName());
            a.setEmail(user.getEmail());
            a.setPassword(user.getPassword());
            a.setContactNumber(user.getContactNumber());
            a.setUserPhoto(user.getUserPhoto());
            a.setRole(user.getRole());
            a.setActive(user.isActive());
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean updatePassword(String token, PasswordDto password) {
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            if (a.getPassword().equals(EncryptHelper.encryptPassword(password.getPassword()))) {
                a.setPassword(EncryptHelper.encryptPassword(password.getNewPassword()));
                userDao.updateUser(a);
                return true;
            }
        }
        return false;
    }

    public boolean isPasswordValid(PasswordDto password) {
        if (password.getPassword().isBlank() || password.getNewPassword().isBlank()) {
            return false;
        } else if (password.getPassword() == null || password.getNewPassword() == null) {
            return false;
        }
        return true;
    }

    public boolean findOtherUserByUsername(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        return a != null;
    }

    public String login(String username, String password) {
        UserEntity user = userDao.findUserByUsername(username);
        String password1 = EncryptHelper.encryptPassword(password);
        System.out.println("Pass1 --- " + user.getPassword());
        System.out.println("Pass2 --- " + password1);
        if (user != null && user.isActive()) {
            String token;
            if (user.getPassword().equals(password1)) {
                do {
                    token = generateToken();
                } while (tokenExists(token));
            } else {
                return null;
            }
            user.setToken(token);
            userDao.updateToken(user);
            user.setTokenExpiration(Instant.now().plus(tokenTimer,ChronoUnit.SECONDS));
            return token;
        }
        return null;
    }

    public void setTokenTimer (String token) {
        UserEntity user = userDao.findUserByToken(token);
        user.setTokenExpiration(Instant.now().plus(tokenTimer,ChronoUnit.SECONDS));
    }

    public boolean userExists(String token) {
        ;
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            return true;
        }
        return false;
    }

    public boolean userNameExists(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            return true;
        }
        return false;
    }

    public String usernameByEmail (String email) {
        String username = userDao.findUsernameByEmail(email);
        return username;
    }
    public boolean recoveryPassword (String confirmationToken, String password) {
        UserEntity userEntity = userDao.findUserByConfirmationToken(confirmationToken);
        if (userEntity!= null) {
            String pass = EncryptHelper.encryptPassword(password);
            userEntity.setPassword(pass);
            return true;
        } else return false;
    }

    public boolean isUserAuthorized(String token) {
        UserEntity a = userDao.findUserByToken(token);
        if (a != null) {
            return true;
        }
        return false;
    }

    public boolean isTokenValid (String token) {
        UserEntity a = userDao.findUserByToken(token);
        Instant expiration = a.getTokenExpiration();
        if (expiration.isAfter(Instant.now())) {
            return true;
        } return false;
    }

    public boolean isUserValid(User user) {
        if (user.getUsername().isBlank() || user.getName().isBlank() || user.getEmail().isBlank() || user.getContactNumber().isBlank() || user.getUserPhoto().isBlank()) {
            return false;
        } else if (user.getUsername() == null || user.getName() == null || user.getEmail() == null || user.getContactNumber() == null || user.getUserPhoto() == null) {
            return false;
        }
        return true;
    }

    public User getUserByUsername(String username) {
        UserEntity userEntity = userDao.findUserByUsername(username);
        return convertToDto(userEntity);
    }

    public UserEntity convertToEntity(User user) {
        UserEntity userEntity = new UserEntity();
        userEntity.setUsername(user.getUsername());
        userEntity.setName(user.getName());
        userEntity.setEmail(user.getEmail());
        userEntity.setPassword(user.getPassword());
        userEntity.setContactNumber(user.getContactNumber());
        userEntity.setUserPhoto(user.getUserPhoto());
        userEntity.setToken(user.getToken());
        userEntity.setRole(user.getRole());
        userEntity.setActive(user.isActive());
        userEntity.setConfirmationToken(user.getConfirmationToken());
        userEntity.setRegistrationDate(user.getRegistrationDate());
        return userEntity;
    }

    public List<Notification> entityToDtoNotification (List<NotificationEntity> entityList){
        List<Notification> notificationsList = new ArrayList<>();
        for (NotificationEntity n : entityList) {
            Notification dto = new Notification();
            dto.setId(n.getId());
            dto.setChecked(n.isChecked());
            dto.setText(n.getText());
            dto.setNotificationDateTime(n.getNotificationDateTime());
            notificationsList.add(dto);
        }
        return notificationsList;
    }

    public List<NotificationEntity> dtoToEntityNotification(List<Notification> dtoList) {
        List<NotificationEntity> entityList = new ArrayList<>();
        for (Notification dto : dtoList) {
            NotificationEntity entity = new NotificationEntity();
            entity.setId(dto.getId());
            entity.setChecked(dto.isChecked());
            entity.setText(dto.getText());
            entity.setNotificationDateTime(dto.getNotificationDateTime());
            entityList.add(entity);
        }
        return entityList;
    }

    public User convertToDto(UserEntity userEntity) {
        User user = new User();
        user.setUsername(userEntity.getUsername());
        user.setName(userEntity.getName());
        user.setEmail(userEntity.getEmail());
        user.setPassword(userEntity.getPassword());
        user.setContactNumber(userEntity.getContactNumber());
        user.setUserPhoto(userEntity.getUserPhoto());
        user.setToken(userEntity.getToken());
        user.setRole(userEntity.getRole());
        user.setActive(userEntity.isActive());
        user.setConfirmed(userEntity.isConfirmed());
        user.setRegistrationDate(userEntity.getRegistrationDate());
        user.setConfirmationToken(userEntity.getConfirmationToken());
        return user;
    }

    public boolean tokenExists(String token) {
        UserEntity a = userDao.findUserByToken(token);
        return a != null;
    }

    public String generateToken() {
        String token = "";
        for (int i = 0; i < 10; i++) {
            token += (char) (Math.random() * 26 + 'a');
        }
        return token;
    }

    public boolean deleteUser(String token, String username) {
        if (username.equals("admin") || username.equals("deleted")) {
            return false;
        }

        UserEntity user = userDao.findUserByUsername(username);
        UserEntity responsible = userDao.findUserByToken(token);
        if (user.isActive() && responsible.getRole().equals("Owner") && !user.getUsername().equals(responsible.getUsername())) {
            user.setActive(false);
            user.setToken(null);
            userDao.updateUser(user);
            return true;
        }
        if (responsible.getRole().equals("Owner") && !user.isActive()) {
            if (doesUserHaveTasks(username)) {
                List<TaskEntity> tasks = taskDao.getTasksByUser(user);
                UserEntity deletedUser = userDao.findUserByUsername("deleted");
                for (TaskEntity task : tasks) {
                    task.setUser(deletedUser);
                    taskDao.updateTask(task);
                }
            }
            userDao.remove(user);
            return true;
        }
        return false;
    }

    public void logout(String token) {
        UserEntity user = userDao.findUserByToken(token);
        user.setToken(null);
        user.setTokenExpiration(null);
        userDao.updateToken(user);
    }

    public UserDto convertUsertoUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());
        userDto.setContactNumber(user.getContactNumber());
        userDto.setRole(user.getRole());
        userDto.setUserPhoto(user.getUserPhoto());
        userDto.setUsername(user.getUsername());
        return userDto;
    }

    public boolean isUserOwner(String token) {
        UserEntity a = userDao.findUserByToken(token);
        if (a.getRole().equals("Owner")) {
            return true;
        }
        return false;
    }

    public boolean restoreUser(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        if (a != null) {
            a.setActive(true);
            userDao.updateUser(a);
            return true;
        }
        return false;
    }

    public boolean doesUserHaveTasks(String username) {
        UserEntity a = userDao.findUserByUsername(username);
        List<TaskEntity> tasks = taskDao.getTasksByUser(a);
        if (tasks.size() > 0) {
            return true;
        } else {
            return false;
        }
    }

    public void createDefaultUsers() {
        if (userDao.findUserByUsername("admin") == null) {
            UserEntity userEntity = new UserEntity();
            userEntity.setUsername("admin");
            userEntity.setName("admin");
            userEntity.setEmail("coiso@cenas.com");
            userEntity.setPassword(EncryptHelper.encryptPassword("admin"));
            userEntity.setContactNumber("123456789");
            userEntity.setUserPhoto("https://cdn-icons-png.freepik.com/512/10015/10015419.png");
            userEntity.setRole("Owner");
            userEntity.setActive(true);
            userEntity.setConfirmed(true);
            userEntity.setRegistrationDate(LocalDate.of(2024, 4, 20));
            userDao.persist(userEntity);
        }
        if (userDao.findUserByUsername("deleted") == null) {

            UserEntity userEntity1 = new UserEntity();
            userEntity1.setUsername("deleted");
            userEntity1.setName("Deleted");
            userEntity1.setEmail("ThrowFeces@ppl.com");
            userEntity1.setPassword(EncryptHelper.encryptPassword("deleted"));
            userEntity1.setContactNumber("123456789");
            userEntity1.setUserPhoto("https://www.pngitem.com/pimgs/m/146-1468479_my-profile-icon-blank-profile-picture-circle-hd.png");
            userEntity1.setRole("developer");
            userEntity1.setActive(true);
            userEntity1.setConfirmed(true);
            userEntity1.setRegistrationDate(LocalDate.of(2024, 4, 20));
            userDao.persist(userEntity1);
        }
    }

    public List<UserDto> getUserBySearch(String name) {
        List<UserEntity> entity = userDao.searchUsersByUsername(name);
        List<UserDto> dto = new ArrayList<>();
        for (UserEntity entity1 : entity) {
            dto.add(convertUsertoUserDto(convertToDto(entity1)));
        }
        return dto;
    }

    public boolean confirmToken(String tokenConfirmation) {
        UserEntity entity = userDao.findUserByConfirmationToken(tokenConfirmation);
        if (tokenConfirmation.equals(entity.getConfirmationToken())) {
            entity.setConfirmed(true);
            userDao.merge(entity);
            return true;
        } else {
            return false;
        }
    }

    public void updateFirstPass (String tokenConfirmation, String password) {
        UserEntity entity = userDao.findUserByConfirmationToken(tokenConfirmation);
        String pass = EncryptHelper.encryptPassword(password);
        entity.setPassword(pass);
        userDao.merge(entity);
    }

   public long  getConfirmedUsers () {
        long confirmedUsers = userDao.countConfirmedUsers();
        return confirmedUsers;
   }

   public long getNotConfirmedUsers() {
        long notConfirmedUsers = userDao.countUnconfirmedUsers();
        return notConfirmedUsers;
   }

   public double getMedTaskByUser() {
        double medTasksByUser = userDao.getAverageTasksPerUser();
        return medTasksByUser;
   }

   public long getActiveUsers () {
        long activeUsers = userDao.countActiveUsers();
        return activeUsers;
   }
    public List getActiveUsersByDate() {
        List<Map.Entry<LocalDate, Long>> list = userDao.countActiveUsersByRegistrationDate();
        List<Map.Entry<LocalDate, Long>> resultList = new ArrayList<>();
        long runningTotal = 0;

        for (Map.Entry<LocalDate, Long> entry : list) {
            runningTotal += entry.getValue();
            resultList.add(new AbstractMap.SimpleEntry<>(entry.getKey(), runningTotal));
        }

        return resultList;
    }




}






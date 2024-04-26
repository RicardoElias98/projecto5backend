package dao;

import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;

import java.time.LocalDate;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Stateless
public class UserDao extends AbstractDao<UserEntity>{
    @PersistenceContext
    private EntityManager em;
    private static final long serialVersionUID = 1L;
    public UserDao() {
        super(UserEntity.class);
    }
    public UserEntity findUserByToken(String token) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByToken").setParameter("token", token)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }
    public UserEntity findUserByUsername(String username) {
        try {
            return (UserEntity) em.createNamedQuery("User.findUserByUsername").setParameter("username", username)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }
    }
    public void updateToken(UserEntity userEntity) {
        em.createNamedQuery("User.updateToken").setParameter("token", userEntity.getToken()).setParameter("username",userEntity.getName()).executeUpdate();
    }


    public void updateUser(UserEntity userEntity) {
        em.merge(userEntity);
    }

    public List<UserEntity> searchUsersByUsername(String searchTerm) {
        try {
            return em.createNamedQuery("User.searchUser").setParameter("searchTerm", searchTerm).getResultList();
        } catch (NoResultException e) {
            return new ArrayList<>();
        }
    }

    public UserEntity findUserByConfirmationToken(String confirmationToken) {
        try {
            UserEntity user = em.createNamedQuery("User.findUserByConfirmationToken", UserEntity.class)
                    .setParameter("confirmationToken", confirmationToken)
                    .getSingleResult();
            return user;
        } catch (NoResultException e) {
            return null;
        }
    }

    public long countConfirmedUsers() {
        Query query = em.createNamedQuery("User.countConfirmedUsers");
        return (long) query.getSingleResult();
    }

    public long countUnconfirmedUsers() {
        Query query = em.createNamedQuery("User.countUnconfirmedUsers");
        return (long) query.getSingleResult();
    }

    public double getAverageTasksPerUser() {
        Query query = em.createNamedQuery("Task.averageTasksPerUser");
        Long result = (Long) query.getSingleResult();
        return result.doubleValue();
    }

    public long countActiveUsers() {
        Query query = em.createNamedQuery("User.countActiveUsers");
        return (long) query.getSingleResult();
    }

    public List<Map.Entry<LocalDate, Long>> countActiveUsersByRegistrationDate() {
        List<Object[]> results = em.createNamedQuery("User.countActiveUsersByRegistrationDate")
                .getResultList();

        List<Map.Entry<LocalDate, Long>> resultList = new ArrayList<>();
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            Long count = (Long) result[1];
            resultList.add(new AbstractMap.SimpleEntry<>(date, count));
        }

        return resultList;
    }

    public String findUsernameByEmail(String email) {
        try {
            return em.createNamedQuery("User.findUsernameByEmail", String.class)
                    .setParameter("email", email)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

}

package dao;

import entities.CategoryEntity;
import entities.TaskEntity;
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
public class TaskDao extends AbstractDao<TaskEntity>{
    @PersistenceContext
    private EntityManager em;
    public TaskDao() {
        super(TaskEntity.class);
    }
    private static final long serialVersionUID = 1L;

    public TaskEntity findTaskById(int id) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
                    .getSingleResult();

        } catch (NoResultException e) {
            return null;
        }

    }

    public ArrayList<TaskEntity> findTaskByUserAndCategory(UserEntity userEntity, String categoryName) {
        try {

            CategoryEntity categoryEntity = em.createQuery("SELECT c FROM CategoryEntity c WHERE c.name = :name", CategoryEntity.class)
                    .setParameter("name", categoryName)
                    .getSingleResult();


            ArrayList<TaskEntity> taskEntities = (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTaskByUserAndCategory")
                    .setParameter("user", userEntity)
                    .setParameter("category", categoryEntity)
                    .getResultList();
            return taskEntities;
        } catch (Exception e) {
            e.printStackTrace();
            return new ArrayList<>();
        }
    }

    public ArrayList<TaskEntity> findTaskByUser(UserEntity userEntity) {
        try {
            ArrayList<TaskEntity> taskEntityEntities = (ArrayList<TaskEntity>) em.createNamedQuery("Task.findTaskByUser").setParameter("user", userEntity).getResultList();
            return taskEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public TaskEntity createTask(TaskEntity taskEntity) {
        em.persist(taskEntity);
        return taskEntity;
    }
    public String findCreatorByName(String name){
        try {
            return (String) em.createNamedQuery("Category.findCreatorByName").setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<TaskEntity> findTasksByUser(UserEntity userEntity) {
        try {
            List<TaskEntity> taskEntityEntities = (List<TaskEntity>) em.createNamedQuery("Task.findTaskByUser").setParameter("user", userEntity).getResultList();
            return taskEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public CategoryEntity findCategoryByName(String name){
        System.out.println("nome da categoria: " + name);
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryByName").setParameter("name", name).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void removeCategory(CategoryEntity categoryEntity) {
        em.remove(categoryEntity);
    }
    public void createCategory(CategoryEntity categoryEntity) {
        em.persist(categoryEntity);
    }
    public void updateCategory(CategoryEntity categoryEntity) {
        em.merge(categoryEntity);
    }
    public CategoryEntity findCategoryById(int id) {
        try {
            return (CategoryEntity) em.createNamedQuery("Category.findCategoryById").setParameter("id", id).getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public void updateTask(TaskEntity taskEntity) {
        em.merge(taskEntity);
    }
    public TaskEntity findTaskById(String id) {
        try {
            return (TaskEntity) em.createNamedQuery("Task.findTaskById").setParameter("id", id)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }
    public List<TaskEntity> findTasksByCategory(String category) {
        try {
           return  (List<TaskEntity>) em.createNamedQuery("Task.findTaskByCategory").setParameter("category", category).getResultList();

        } catch (Exception e) {
            return null;
        }
    }
    public List<TaskEntity> findBlockedTasks() {
        try {
            List<TaskEntity> taskEntityEntities = (List<TaskEntity>) em.createNamedQuery("Task.findBlockedTasks").getResultList();
            return taskEntityEntities;
        } catch (Exception e) {
            return null;
        }
    }
    public List<CategoryEntity> findAllCategories() {
        try {
            List<CategoryEntity> categoryEntities = (List<CategoryEntity>) em.createNamedQuery("Category.findAll").getResultList();
            return categoryEntities;
        } catch (Exception e) {
            return null;
        }
    }

    public List <TaskEntity> findTaskByStatus (int status) {
        try {
            List<TaskEntity> taskEntities = (List<TaskEntity>) em.createNamedQuery("Task.findTaskByStatus").setParameter("status", status).getResultList();
            return taskEntities;
        } catch (Exception e) {
            return null;
        }
    }

    public long countTasksByStatus(int status) {
        Query query = em.createNamedQuery("Task.countTasksByStatus");
        query.setParameter("status", status);
        return (long) query.getSingleResult();
    }

    public List<Object[]> countTasksByCategory() {
        Query query = em.createNamedQuery("Task.countTasksByCategory");
        return query.getResultList();
    }

    public List<Map.Entry<LocalDate, Long>> countTasksByRealFinalDate() {
        List<Object[]> results = em.createNamedQuery("Task.countTasksByRealFinalDate")
                .getResultList();

        List<Map.Entry<LocalDate, Long>> resultList = new ArrayList<>();
        for (Object[] result : results) {
            LocalDate date = (LocalDate) result[0];
            Long count = (Long) result[1];
            resultList.add(new AbstractMap.SimpleEntry<>(date, count));
        }

        return resultList;
    }




}

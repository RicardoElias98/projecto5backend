package dao;

import entities.NotificationEntity;
import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

import java.util.List;

@Stateless
public class NotificationDao extends AbstractDao<NotificationEntity> {

    @PersistenceContext
    private EntityManager em;
    private static final long serialVersionUID = 1L;

    public NotificationDao() {
        super(NotificationEntity.class);
    }

    public List<NotificationEntity> findNotificationsByUser(UserEntity user) {
        TypedQuery<NotificationEntity> query = em.createNamedQuery(
                "Notification.findByUser", NotificationEntity.class);
        query.setParameter("user", user);
        return query.getResultList();
    }

    public List<NotificationEntity> findUncheckedNotificationsByUser(UserEntity user) {
        return em.createNamedQuery("Notification.findUncheckedByUser", NotificationEntity.class)
                .setParameter("user", user)
                .getResultList();
    }


}

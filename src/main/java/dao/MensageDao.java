package dao;

import entities.MensageEntity;
import entities.TaskEntity;
import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

@Stateless
public class MensageDao extends AbstractDao<MensageEntity> {

    @PersistenceContext
    private EntityManager em;
    private static final long serialVersionUID = 1L;

    public MensageDao() {
        super(MensageEntity.class);
    }

    public MensageEntity createMsg(MensageEntity msg) {
        em.persist(msg);
        return msg;
    }



}

package dao;

import entities.MensageEntity;
import entities.TaskEntity;
import entities.UserEntity;
import jakarta.ejb.Stateless;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import java.util.List;

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

    public List<MensageEntity> findMsgBySenderAndReceptor(UserEntity sender, UserEntity receptor) {
        return em.createNamedQuery("Mensage.findMsgBySenderAndReceptor", MensageEntity.class)
                .setParameter("sender", sender)
                .setParameter("receptor", receptor)
                .getResultList();
    }



}

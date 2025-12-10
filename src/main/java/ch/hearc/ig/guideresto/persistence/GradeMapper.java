package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour `Grade` (table NOTES).
 */
public class GradeMapper extends AbstractMapper<Grade> {
    private final EntityManager em;
    public GradeMapper(EntityManager em) {
       this.em = em;
    }

    @Override
    public Grade findById(int id) {
        try {
            TypedQuery<Grade> q = em.createNamedQuery("Grade.findById", Grade.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public Set<Grade> findAll() {
        TypedQuery<Grade> q = em.createNamedQuery("Grade.findAll", Grade.class);
        List<Grade> list = q.getResultList();
        return new HashSet<>(list);
    }

    public Set<Grade> findByEvaluationId(int evaluationId) {
        TypedQuery<Grade> q = em.createQuery("SELECT g FROM Grade g WHERE g.evaluation.id = :evaluationId", Grade.class);
        q.setParameter("evaluationId", evaluationId);
        List<Grade> list = q.getResultList();
        return new HashSet<>(list);
    }

    @Override
    public Grade create(Grade grade) {
        em.persist(grade);
        return grade;
    }

    /**
     * @throws OptimisticLockException Si la note a été modifiée par un autre utilisateur
     */
    @Override
    public Grade update(Grade grade) {
        try {
            return em.merge(grade);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour de la note ID={}: {}",
                grade.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * @throws OptimisticLockException Si la note a été modifiée par un autre utilisateur
     */
    @Override
    public boolean delete(Grade grade) {
        try {
            Grade managed = em.find(Grade.class, grade.getId());
            if (managed != null) {
                em.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression de la note ID={}: {}",
                grade.getId(), e.getMessage());
            throw e;
        }
    }

}


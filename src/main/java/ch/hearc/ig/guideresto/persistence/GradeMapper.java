package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Grade;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour `Grade` (table NOTES).
 */
public class GradeMapper {

    private final EntityManager em;

    public GradeMapper(EntityManager em) {
        this.em = em;
    }

    public Grade findById(int id) {
        try {
            TypedQuery<Grade> q = em.createNamedQuery("Grade.findById", Grade.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

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

    public Grade create(Grade grade) {
        em.persist(grade);
        return grade;
    }

    public Grade update(Grade grade) {
        return em.merge(grade);
    }

    public boolean delete(Grade grade) {
        Grade managed = em.find(Grade.class, grade.getId());
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }

}

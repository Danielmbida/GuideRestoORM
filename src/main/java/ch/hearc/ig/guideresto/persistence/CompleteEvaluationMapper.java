package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour `CompleteEvaluation`. Utilise les NamedQueries définies sur l'entité.
 * Ne gère pas les transactions.
 */
public class CompleteEvaluationMapper {

    private final EntityManager em;

    public CompleteEvaluationMapper(EntityManager em) {
        this.em = em;
    }

    public CompleteEvaluation findById(int id) {
        // Utilisation de em.find qui est la manière la plus simple pour récupérer une entité par sa PK
        return em.find(CompleteEvaluation.class, id);
    }

    public Set<CompleteEvaluation> findAll() {
        try {
            TypedQuery<CompleteEvaluation> q = em.createQuery("SELECT ce FROM CompleteEvaluation ce", CompleteEvaluation.class);
            List<CompleteEvaluation> list = q.getResultList();
            return new HashSet<>(list);
        } catch (NoResultException ex) {
            return new HashSet<>();
        }
    }

    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        TypedQuery<CompleteEvaluation> q = em.createNamedQuery("CompleteEvaluation.getCompleteEvaluationsOfARestaurant", CompleteEvaluation.class);
        q.setParameter("restaurantId", restaurantId);
        return new HashSet<>(q.getResultList());
    }

    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        em.persist(evaluation);
        return evaluation;
    }

    public CompleteEvaluation update(CompleteEvaluation evaluation) {
        return em.merge(evaluation);
    }

    public boolean delete(CompleteEvaluation evaluation) {
        CompleteEvaluation managed = em.find(CompleteEvaluation.class, evaluation.getId());
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }

    public boolean deleteById(int id) {
        CompleteEvaluation managed = em.find(CompleteEvaluation.class, id);
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }
}

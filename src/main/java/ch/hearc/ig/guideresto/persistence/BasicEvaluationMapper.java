package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour BasicEvaluation. N'assure pas la gestion des transactions.
 * Utilise les NamedQueries présentes dans Evaluation et BasicEvaluation.
 */
public class BasicEvaluationMapper {

    private final EntityManager em;

    public BasicEvaluationMapper(EntityManager em) {
        this.em = em;
    }

    public BasicEvaluation findById(int id) {
        try {
            TypedQuery<Evaluation> q = em.createNamedQuery("Evaluation.findById", Evaluation.class);
            q.setParameter("id", id);
            Evaluation e = q.getSingleResult();
            if (e instanceof BasicEvaluation) {
                return (BasicEvaluation) e;
            }
            return null;
        } catch (NoResultException ex) {
            return null;
        }
    }

    public Set<BasicEvaluation> findAll() {
        TypedQuery<Evaluation> q = em.createNamedQuery("Evaluation.findAll", Evaluation.class);
        List<Evaluation> results = q.getResultList();
        Set<BasicEvaluation> set = new HashSet<>();
        for (Evaluation e : results) {
            if (e instanceof BasicEvaluation) {
                set.add((BasicEvaluation) e);
            }
        }
        return set;
    }

    public Set<BasicEvaluation> findByRestaurantId(int restaurantId) {
        TypedQuery<BasicEvaluation> q = em.createQuery(
                "SELECT be FROM BasicEvaluation be WHERE be.restaurant.id = :restaurantId", BasicEvaluation.class);
        q.setParameter("restaurantId", restaurantId);
        return new HashSet<>(q.getResultList());
    }

    public int getLikesCountForRestaurant(int restaurantId) {
        try {
            TypedQuery<Long> q = em.createNamedQuery("BasicEvaluation.getRestaurantAmountLikes", Long.class);
            q.setParameter("restaurantId", restaurantId);
            Long count = q.getSingleResult();
            return count == null ? 0 : count.intValue();
        } catch (NoResultException ex) {
            return 0;
        }
    }

    public int getDislikesCountForRestaurant(int restaurantId) {
        try {
            TypedQuery<Long> q = em.createNamedQuery("BasicEvaluation.getRestaurantAmountDislikes", Long.class);
            q.setParameter("restaurantId", restaurantId);
            Long count = q.getSingleResult();
            return count == null ? 0 : count.intValue();
        } catch (NoResultException ex) {
            return 0;
        }
    }

    public BasicEvaluation create(BasicEvaluation evaluation) {
        em.persist(evaluation);
        return evaluation;
    }

    public BasicEvaluation update(BasicEvaluation evaluation) {
        return em.merge(evaluation);
    }

    public boolean delete(BasicEvaluation evaluation) {
        BasicEvaluation managed = em.find(BasicEvaluation.class, evaluation.getId());
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }
}

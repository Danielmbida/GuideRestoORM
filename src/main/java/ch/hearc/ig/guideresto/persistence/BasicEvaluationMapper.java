package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour BasicEvaluation. N'assure pas la gestion des transactions.
 * Utilise les NamedQueries présentes dans Evaluation et BasicEvaluation.
 */
public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

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

    @Override
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

    @Override
    public BasicEvaluation create(BasicEvaluation evaluation) {
        em.persist(evaluation);
        return evaluation;
    }

    /**
     * @throws OptimisticLockException Si l'évaluation a été modifiée par un autre utilisateur
     */
    @Override
    public BasicEvaluation update(BasicEvaluation evaluation) {
        try {
            return em.merge(evaluation);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour de l'évaluation basique ID={}: {}",
                evaluation.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * @throws OptimisticLockException Si l'évaluation a été modifiée par un autre utilisateur
     */
    @Override
    public boolean delete(BasicEvaluation evaluation) {
        try {
            BasicEvaluation managed = em.find(BasicEvaluation.class, evaluation.getId());
            if (managed != null) {
                em.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression de l'évaluation basique ID={}: {}",
                evaluation.getId(), e.getMessage());
            throw e;
        }
    }
}

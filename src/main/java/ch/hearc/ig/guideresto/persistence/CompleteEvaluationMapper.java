package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour `CompleteEvaluation`. Utilise les NamedQueries définies sur l'entité.
 * Ne gère pas les transactions.
 */
public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    private final EntityManager em;

    public CompleteEvaluationMapper(EntityManager em) {
        this.em = em;
    }

    public CompleteEvaluation findById(int id) {
        // Utilisation de em.find qui est la manière la plus simple pour récupérer une entité par sa PK
        return em.find(CompleteEvaluation.class, id);
    }

    @Override
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

    @Override
    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        em.persist(evaluation);
        return evaluation;
    }

    /**
     * @throws OptimisticLockException Si l'évaluation a été modifiée par un autre utilisateur
     */
    @Override
    public CompleteEvaluation update(CompleteEvaluation evaluation) {
        try {
            return em.merge(evaluation);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour de l'évaluation complète ID={}: {}",
                evaluation.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * @throws OptimisticLockException Si l'évaluation a été modifiée par un autre utilisateur
     */
    @Override
    public boolean delete(CompleteEvaluation evaluation) {
        try {
            CompleteEvaluation managed = em.find(CompleteEvaluation.class, evaluation.getId());
            if (managed != null) {
                em.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression de l'évaluation complète ID={}: {}",
                evaluation.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * @throws OptimisticLockException Si l'évaluation a été modifiée par un autre utilisateur
     */
    public boolean deleteById(int id) {
        try {
            CompleteEvaluation managed = em.find(CompleteEvaluation.class, id);
            if (managed != null) {
                em.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression de l'évaluation complète ID={}: {}",
                id, e.getMessage());
            throw e;
        }
    }
}

// java
package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.EvaluationCriteria;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.Set;

/**
 * Mapper JPA pour EvaluationCriteria.
 * Utilise les NamedQueries définies sur l'entité et n'assure pas la gestion des transactions.
 */
public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    private final EntityManager entityManager;

    public EvaluationCriteriaMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public EvaluationCriteria findById(int id) {
        try {
            TypedQuery<EvaluationCriteria> q = entityManager.createNamedQuery("EvaluationCriteria.findById", EvaluationCriteria.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public Set<EvaluationCriteria> findAll() {
        TypedQuery<EvaluationCriteria> q = entityManager.createNamedQuery("EvaluationCriteria.findAll", EvaluationCriteria.class);
        return new HashSet<>(q.getResultList());
    }

    public EvaluationCriteria findByName(String name) {
        try {
            TypedQuery<EvaluationCriteria> q = entityManager.createNamedQuery("EvaluationCriteria.findByName", EvaluationCriteria.class);
            q.setParameter("name", name);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    @Override
    public EvaluationCriteria create(EvaluationCriteria criteria) {
        entityManager.persist(criteria);
        return criteria;
    }

    /**
     * @throws OptimisticLockException Si le critère a été modifié par un autre utilisateur
     */
    @Override
    public EvaluationCriteria update(EvaluationCriteria criteria) {
        try {
            return entityManager.merge(criteria);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour du critère d'évaluation ID={}: {}",
                criteria.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * @throws OptimisticLockException Si le critère a été modifié par un autre utilisateur
     */
    @Override
    public boolean delete(EvaluationCriteria criteria) {
        try {
            EvaluationCriteria managed = entityManager.find(EvaluationCriteria.class, criteria.getId());
            if (managed != null) {
                entityManager.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression du critère d'évaluation ID={}: {}",
                criteria.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * @throws OptimisticLockException Si le critère a été modifié par un autre utilisateur
     */
    public boolean deleteById(int id) {
        try {
            EvaluationCriteria managed = entityManager.find(EvaluationCriteria.class, id);
            if (managed != null) {
                entityManager.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression du critère d'évaluation ID={}: {}",
                id, e.getMessage());
            throw e;
        }
    }

}

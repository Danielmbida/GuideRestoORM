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

    /**
     * Constructeur du mapper.
     *
     * @param entityManager EntityManager utilisé pour les opérations JPA (ne gère pas les transactions)
     */
    public EvaluationCriteriaMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Recherche un critère d'évaluation par son identifiant métier.
     *
     * Utilise {@code EntityManager.find} pour retrouver l'entité gérée.
     *
     * @param id identifiant métier du critère
     * @return l'entité {@link EvaluationCriteria} si trouvée, sinon {@code null}
     */
    @Override
    public EvaluationCriteria findById(int id) {
        return entityManager.find(EvaluationCriteria.class, id);
    }

    /**
     * Récupère tous les critères d'évaluation en base.
     *
     * Utilise la NamedQuery {@code EvaluationCriteria.findAll} définie dans l'entité.
     *
     * @return un {@link Set} contenant toutes les instances d'{@link EvaluationCriteria} (ou vide si aucune)
     */
    @Override
    public Set<EvaluationCriteria> findAll() {
        TypedQuery<EvaluationCriteria> q = entityManager.createNamedQuery("EvaluationCriteria.findAll", EvaluationCriteria.class);
        return new HashSet<>(q.getResultList());
    }

    /**
     * Recherche un critère d'évaluation par son nom.
     *
     * Utilise la NamedQuery {@code EvaluationCriteria.findByName}.
     * Si aucune entité ne correspond, retourne {@code null}.
     *
     * @param name nom du critère à rechercher (comparaison insensible à la casse selon la NamedQuery)
     * @return l'entité {@link EvaluationCriteria} correspondante ou {@code null} si introuvable
     */
    public EvaluationCriteria findByName(String name) {
        try {
            TypedQuery<EvaluationCriteria> q = entityManager.createNamedQuery("EvaluationCriteria.findByName", EvaluationCriteria.class);
            q.setParameter("name", name);
            return q.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Persiste un nouveau critère d'évaluation en base.
     *
     * Remarque : la gestion transactionnelle (begin/commit/rollback) doit être effectuée par le code appelant
     * (le mapper n'ouvre ni ne commit/rollback de transactions).
     *
     * @param criteria instance à persister
     * @return la même instance persistée
     */
    @Override
    public EvaluationCriteria create(EvaluationCriteria criteria) {
        entityManager.persist(criteria);
        return criteria;
    }

    /**
     * Met à jour un critère d'évaluation existant en effectuant un {@code merge}.
     *
     * Peut lever une {@link OptimisticLockException} si un conflit de version est détecté.
     *
     * @param criteria instance à mettre à jour
     * @return l'entité résultante du {@code merge}
     * @throws OptimisticLockException si conflit de version détecté
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
     * Supprime un critère d'évaluation de la base.
     *
     * Cherche d'abord l'entité gérée via {@code EntityManager.find} puis appelle {@code remove} si elle existe.
     *
     * @param criteria instance à supprimer (doit contenir un identifiant)
     * @return {@code true} si l'entité a été trouvée et supprimée, {@code false} si elle n'existait pas
     * @throws OptimisticLockException en cas de conflit de version lors de la suppression
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
     * Supprime un critère d'évaluation identifié par son id.
     *
     * Utile lorsque l'on ne dispose que de l'identifiant. Ne gère pas la transaction.
     *
     * @param id identifiant métier du critère à supprimer
     * @return {@code true} si l'entité a été trouvée et supprimée, {@code false} sinon
     * @throws OptimisticLockException en cas de conflit de version
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

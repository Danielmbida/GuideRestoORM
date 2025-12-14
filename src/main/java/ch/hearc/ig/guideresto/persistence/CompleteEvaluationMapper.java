package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;
import ch.hearc.ig.guideresto.business.Evaluation;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Mapper JPA pour `CompleteEvaluation`. Utilise les NamedQueries définies sur l'entité.
 * Ne gère pas les transactions : le service appelant doit ouvrir/committer/rollback la transaction.
 */
public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    private final EntityManager em;

    /**
     * Constructeur du mapper.
     *
     * @param em EntityManager utilisé pour toutes les opérations JPA (ne gère pas la transaction)
     */
    public CompleteEvaluationMapper(EntityManager em) {
        this.em = em;
    }

    /**
     * Recherche une `CompleteEvaluation` par son identifiant métier.
     *
     * Utilise `EntityManager.find` et renvoie l'entité gérée ou null si absente.
     *
     * @param id identifiant métier de l'évaluation
     * @return l'entité `CompleteEvaluation` trouvée ou null si introuvable
     */
    @Override
    public CompleteEvaluation findById(int id) {
        return em.find(CompleteEvaluation.class, id);
    }

    /**
     * Récupère toutes les évaluations complètes présentes en base.
     *
     * Utilise la NamedQuery "Evaluation.findAll" (retourne toutes les evaluations) puis filtre
     * celles qui sont des `CompleteEvaluation`.
     *
     * Attention : la méthode ne gère pas la transaction.
     *
     * @return un Set contenant toutes les `CompleteEvaluation` (vide si aucune trouvée)
     */
    @Override
    public Set<CompleteEvaluation> findAll() {
        try {
            TypedQuery<Evaluation> q = em.createNamedQuery("Evaluation.findAll", Evaluation.class);
            List<Evaluation> results = q.getResultList();
            Set<CompleteEvaluation> set = new HashSet<>();
            for (Evaluation e : results) {
                if (e instanceof CompleteEvaluation) {
                    set.add((CompleteEvaluation) e);
                }
            }
            return new HashSet<>(set);
        } catch (NoResultException ex) {
            return new HashSet<>();
        }
    }

    /**
     * Récupère les `CompleteEvaluation` associées à un restaurant donné.
     *
     * Utilise la NamedQuery "CompleteEvaluation.getCompleteEvaluationsOfARestaurant".
     * Renvoie les résultats dans un `LinkedHashSet` pour conserver l'ordre retourné par la requête
     * (la NamedQuery ordonne par date de visite décroissante).
     *
     * @param restaurantId identifiant métier du restaurant
     * @return ensemble ordonné des `CompleteEvaluation` associés au restaurant (vide si aucune trouvée)
     */
    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        TypedQuery<CompleteEvaluation> q = em.createNamedQuery("CompleteEvaluation.getCompleteEvaluationsOfARestaurant", CompleteEvaluation.class);
        q.setParameter("restaurantId", restaurantId);
        return new LinkedHashSet<>(q.getResultList());
    }

    /**
     * Persiste une nouvelle `CompleteEvaluation` en base.
     *
     * Remarque : la gestion transactionnelle (begin/commit/rollback) doit être réalisée par l'appelant.
     *
     * @param evaluation instance à persister
     * @return la même instance persistée
     */
    @Override
    public CompleteEvaluation create(CompleteEvaluation evaluation) {
        em.persist(evaluation);
        return evaluation;
    }

    /**
     * Met à jour une `CompleteEvaluation` existante (merge).
     *
     * Peut lancer une `OptimisticLockException` si la version en base a changé.
     * La gestion de la transaction doit être faite par le service appelant.
     *
     * @param evaluation instance à mettre à jour
     * @return l'entité résultante du merge
     * @throws OptimisticLockException si conflit de version détecté
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
     * Supprime une `CompleteEvaluation` de la base.
     *
     * Cherche d'abord l'entité gérée via `em.find` puis appelle `em.remove` si trouvée.
     * La méthode ne gère pas la transaction.
     *
     * @param evaluation instance à supprimer (doit contenir un id)
     * @return true si l'entité a été trouvée et supprimée, false si elle n'existait pas
     * @throws OptimisticLockException en cas de conflit de version lors de la suppression
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
     * Supprime une `CompleteEvaluation` identifiée par son id.
     *
     * Pratique pour les appels qui ne disposent que de l'identifiant. Ne gère pas la transaction.
     *
     * @param id identifiant métier de l'évaluation à supprimer
     * @return true si l'entité a été trouvée et supprimée, false sinon
     * @throws OptimisticLockException en cas de conflit de version
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

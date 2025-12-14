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

    /**
     * Constructeur du mapper.
     *
     * @param em EntityManager utilisé pour toutes les opérations JPA (ne gère pas la transaction)
     */
    public BasicEvaluationMapper(EntityManager em) {
        this.em = em;
    }

    /**
     * Recherche une BasicEvaluation par son identifiant métier.
     *
     * Le mapper tente de retrouver une instance d'Evaluation et vérifie ensuite
     * si elle est bien de type BasicEvaluation. Si ce n'est pas le cas, retourne null.
     *
     * @param id identifiant de l'évaluation recherchée
     * @return l'instance BasicEvaluation si trouvée et du bon type, sinon null
     */
    @Override
    public BasicEvaluation findById(int id) {
        Evaluation e = em.find(Evaluation.class, id);
        if (e instanceof BasicEvaluation) {
            return (BasicEvaluation) e;
        }
        return null;
    }

    /**
     * Récupère toutes les BasicEvaluation présentes en base.
     *
     * Utilise la NamedQuery "Evaluation.findAll" pour obtenir toutes les evaluations,
     * puis filtre celles qui sont des BasicEvaluation.
     *
     * @return un Set contenant toutes les BasicEvaluation (vide si aucune trouvée)
     */
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

    /**
     * Récupère les BasicEvaluation associées à un restaurant donné.
     *
     * @param restaurantId identifiant du restaurant
     * @return un Set de BasicEvaluation liés au restaurant (vide si aucune trouvée)
     */
    public Set<BasicEvaluation> findByRestaurantId(int restaurantId) {
        TypedQuery<BasicEvaluation> q = em.createQuery(
                "SELECT be FROM BasicEvaluation be WHERE be.restaurant.id = :restaurantId", BasicEvaluation.class);
        q.setParameter("restaurantId", restaurantId);
        return new HashSet<>(q.getResultList());
    }

    /**
     * Compte le nombre de "likes" pour un restaurant donné.
     *
     * Utilise la NamedQuery "BasicEvaluation.getRestaurantAmountLikes".
     * Si aucune valeur n'est retournée, la méthode renvoie 0.
     *
     * @param restaurantId identifiant du restaurant
     * @return nombre de likes (0 si aucune donnée)
     */
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

    /**
     * Compte le nombre de "dislikes" pour un restaurant donné.
     *
     * Utilise la NamedQuery "BasicEvaluation.getRestaurantAmountDislikes".
     * Si aucune valeur n'est retournée, la méthode renvoie 0.
     *
     * @param restaurantId identifiant du restaurant
     * @return nombre de dislikes (0 si aucune donnée)
     */
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

    /**
     * Persiste une nouvelle BasicEvaluation en base.
     *
     * Attention : la gestion des transactions doit être faite par l'appelant (le mapper
     * n'ouvre/commit/rollback aucune transaction).
     *
     * @param evaluation instance à persister
     * @return la même instance persistée
     */
    @Override
    public BasicEvaluation create(BasicEvaluation evaluation) {
        em.persist(evaluation);
        return evaluation;
    }

    /**
     * Met à jour une BasicEvaluation existante en effectuant un merge.
     *
     * Peut lancer une OptimisticLockException si la version en base a changé.
     *
     * @param evaluation instance à merger
     * @return l'entité résultante du merge
     * @throws OptimisticLockException si conflit de version détecté
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
     * Supprime une BasicEvaluation de la base.
     *
     * Cherche l'entité gérée via em.find puis appelle em.remove si elle existe.
     *
     * @param evaluation instance à supprimer (doit contenir un id)
     * @return true si l'entité a été trouvée et supprimée, false si elle n'existait pas
     * @throws OptimisticLockException si conflit de version détecté lors de la suppression
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

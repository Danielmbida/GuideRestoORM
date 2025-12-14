package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    private final EntityManager entityManager;

    /**
     * Constructeur du mapper.
     *
     * @param entityManager EntityManager utilisé pour les opérations JPA (le mapper ne gère pas les transactions)
     */
    public RestaurantTypeMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Recherche un type gastronomique par son identifiant (NUMERO).
     *
     * Utilise {@link EntityManager#find} et retourne null si l'entité n'existe pas.
     *
     * @param id Identifiant unique du type gastronomique à rechercher.
     * @return L'objet {@link RestaurantType} correspondant, ou null s'il n'existe pas.
     */
    @Override
    public RestaurantType findById(int id) {
        return entityManager.find(RestaurantType.class, id);
    }

    /**
     * Recherche un type par son libellé (égalité insensible à la casse).
     *
     * Utilise la NamedQuery {@code RestaurantType.findByLabel}.
     *
     * @param namePart libellé recherché
     * @return le {@link RestaurantType} correspondant ou {@code null} si aucune correspondance
     */
    public RestaurantType findByLabel(String namePart) {
        try {
            TypedQuery<RestaurantType> q = entityManager.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class);
            q.setParameter("label", namePart);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }


    /**
     * Récupère tous les types gastronomiques existants.
     *
     * Utilise la NamedQuery {@code RestaurantType.findAll}.
     *
     * @return Un {@link Set} contenant tous les objets {@link RestaurantType} (vide si aucun)
     */
    public Set<RestaurantType> findAll() {
        TypedQuery<RestaurantType> q = entityManager.createNamedQuery("RestaurantType.findAll", RestaurantType.class);
        List<RestaurantType> list = q.getResultList();
        return new HashSet<>(list);
    }

    /**
     * Persiste un nouveau type gastronomique.
     *
     * Attention : la gestion transactionnelle (begin/commit/rollback) doit être faite par l'appelant.
     *
     * @param object L'objet {@link RestaurantType} à insérer.
     * @return Le {@link RestaurantType} inséré (référence fournie en paramètre)
     */
    public RestaurantType create(RestaurantType object) {
        entityManager.persist(object);
        return object;
    }

    /**
     * Met à jour un type gastronomique existant via {@link EntityManager#merge(Object)}.
     *
     * @param object L'objet {@link RestaurantType} contenant les nouvelles données
     * @return Le {@link RestaurantType} mis à jour
     * @throws OptimisticLockException si un conflit de version est détecté
     */
    public RestaurantType update(RestaurantType object) {
        try {
            return entityManager.merge(object);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour du type gastronomique ID={}: {}",
                object.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime un type gastronomique de la base.
     *
     * Cherche l'entité gérée et appelle {@link EntityManager#remove(Object)} si elle existe.
     *
     * @param object L'objet {@link RestaurantType} à supprimer.
     * @return true si la suppression a réussi, false si l'entité n'existait pas
     * @throws OptimisticLockException si un conflit de version est détecté lors de la suppression
     */
    @Override
    public boolean delete(RestaurantType object) {
        try {
            RestaurantType managed = entityManager.find(RestaurantType.class, object.getId());
            if (managed != null) {
                entityManager.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression du type gastronomique ID={}: {}",
                object.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime un type gastronomique identifié par son id.
     *
     * @param id Identifiant métier du type à supprimer
     * @return true si la suppression a eu lieu, false sinon
     * @throws OptimisticLockException si un conflit de version est détecté
     */
    public boolean deleteById(int id) {
        try {
            RestaurantType managed = entityManager.find(RestaurantType.class, id);
            if (managed != null) {
                entityManager.remove(managed);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression du type gastronomique ID={}: {}",
                id, e.getMessage());
            throw e;
        }
    }
}

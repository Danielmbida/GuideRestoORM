package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RestaurantTypeMapper {

    private final EntityManager em;

    public RestaurantTypeMapper(EntityManager em) {
        this.em = em;
    }

    /**
     * Recherche un type gastronomique par son identifiant (NUMERO).
     *
     * @param id Identifiant unique du type gastronomique à rechercher.
     * @return L'objet RestaurantType correspondant, ou null s'il n'existe pas.
     */
    public RestaurantType findById(int id) {
        try {
            TypedQuery<RestaurantType> q = em.createNamedQuery("RestaurantType.findById", RestaurantType.class);
            q.setParameter("id", id);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }

    public RestaurantType findByLabel(String namePart) {
        try {
            TypedQuery<RestaurantType> q = em.createNamedQuery("RestaurantType.findByLabel", RestaurantType.class);
            q.setParameter("label", namePart);
            return q.getSingleResult();
        } catch (NoResultException ex) {
            return null;
        }
    }


    /**
     * Recherche un type gastronomique par son libellé.
     *
     * @param libelle Libellé du type gastronomique à rechercher.
     * @return L'objet {@link RestaurantType} correspondant, ou null s'il n'existe pas.
     */
    public RestaurantType findByType(String libelle) {
        // réutilise findByLabel
        return findByLabel(libelle);
    }


    /**
     * Récupère tous les types gastronomiques existants dans la base de données.
     *
     * @return Un Set contenant tous les objets RestaurantType.
     */
    public Set<RestaurantType> findAll() {
        TypedQuery<RestaurantType> q = em.createNamedQuery("RestaurantType.findAll", RestaurantType.class);
        List<RestaurantType> list = q.getResultList();
        return new HashSet<>(list);
    }

    /**
     * Crée un nouveau type gastronomique dans la base de données.
     *
     * @param object L'objet RestaurantType à insérer.
     * @return Le RestaurantType nouvellement créé (récupéré depuis la base), ou null si l'insertion échoue.
     */
    public RestaurantType create(RestaurantType object) {
        em.persist(object);
        return object;
    }

    /**
     * Met à jour un type gastronomique existant dans la base de données.
     *
     * @param object L'objet RestaurantType contenant les nouvelles informations.
     * @return true si la mise à jour a réussi, false sinon.
     */
    public RestaurantType update(RestaurantType object) {
        return em.merge(object);
    }

    /**
     * Supprime un type gastronomique de la base de données.
     *
     * @param object L'objet RestaurantType à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    public boolean delete(RestaurantType object) {
        RestaurantType managed = em.find(RestaurantType.class, object.getId());
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }

    public boolean deleteById(int id) {
        RestaurantType managed = em.find(RestaurantType.class, id);
        if (managed != null) {
            em.remove(managed);
            return true;
        }
        return false;
    }
}

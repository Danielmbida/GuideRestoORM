package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Restaurant;
import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.persistence.OptimisticLockException;
import jakarta.persistence.TypedQuery;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {
    private final EntityManager entityManager;

    public RestaurantMapper(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    /**
     * Recherche un restaurant dans la base de données par son identifiant.
     *
     * @param id Identifiant unique du restaurant à rechercher.
     * @return L'objet Restaurant correspondant, ou null s'il n'existe pas.
     */
    @Override
    public Restaurant findById(int id) {
        return entityManager.find(Restaurant.class, id);
    }

    /**
     * Recherche un restaurant dans la base de données par son nom exact.
     *
     * @param name Nom du restaurant à rechercher.
     * @return L'objet Restaurant correspondant, ou null s'il n'existe pas.
     */
    public Restaurant findByName(String name) {
        try {
            TypedQuery<Restaurant> query = entityManager.createNamedQuery("Restaurant.getRestaurantByName", Restaurant.class);
            query.setParameter("name", name);
            return query.getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    /**
     * Recherche des restaurants dont le nom contient la chaîne spécifiée (recherche partielle).
     *
     * @param namePart Partie du nom à rechercher.
     * @return Un ensemble de restaurants correspondant au critère.
     */
    public Set<Restaurant> findByNameLike(String namePart) {
        TypedQuery<Restaurant> query = entityManager.createNamedQuery("Restaurant.getRestaurantByNameLike", Restaurant.class);
        query.setParameter("namePattern", "%" + namePart + "%");
        return new LinkedHashSet<>(query.getResultList());
    }

    /**
     * Recherche des restaurants situés dans une ville donnée.
     *
     * @param cityName Nom de la ville.
     * @return Un ensemble de restaurants dans cette ville.
     */
    public Set<Restaurant> findByCityNameLike(String cityName) {
        TypedQuery<Restaurant> query = entityManager.createNamedQuery("Restaurant.getRestaurantByCityNameLike", Restaurant.class);
        query.setParameter("cityName", "%" + cityName + "%");
        return new LinkedHashSet<>(query.getResultList());
    }

    /**
     * Récupère l'ensemble des restaurants enregistrés dans la base de données.
     *
     * @return Un Set contenant tous les objets Restaurant.
     */
    @Override
    public Set<Restaurant> findAll() {
        TypedQuery<Restaurant> query = entityManager.createNamedQuery("Restaurant.findAll", Restaurant.class);
        return new HashSet<>(query.getResultList());
    }


    /**
     * Recherche tous les restaurants appartenant à un type gastronomique donné (par ID de type).
     *
     * @param typeLabel Identifiant unique du type gastronomique.
     * @return Un ensemble d'objets Restaurant correspndant au type spécifié.
     */
    public Set<Restaurant> findByTypeLabel(String typeLabel) {
        TypedQuery<Restaurant> query = entityManager.createNamedQuery("Restaurant.getRestaur" +
                "antsByTypeLabel", Restaurant.class);
        query.setParameter("typeLabel", typeLabel);
        return new HashSet<>(query.getResultList());
    }

    /**
     * Crée un nouveau restaurant dans la base de données.
     * @param restaurant L'objet Restaurant à insérer.
     * @return Le Restaurant nouvellement créé (avec son ID généré).
     */
    @Override
    public Restaurant create(Restaurant restaurant) {
        entityManager.persist(restaurant);
        return restaurant;
    }

    /**
     * Met à jour les informations d'un restaurant existant dans la base de données.
     * @param restaurant L'objet Restaurant contenant les nouvelles informations.
     * @return Le restaurant mis à jour.
     * @throws OptimisticLockException Si le restaurant a été modifié par un autre utilisateur
     */
    @Override
    public Restaurant update(Restaurant restaurant) {
        try {
            return entityManager.merge(restaurant);
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la mise à jour du restaurant ID={}: {}",
                restaurant.getId(), e.getMessage());
            throw e;
        }
    }

    /**
     * Supprime un restaurant de la base de données.
     * Les évaluations associées sont supprimées en cascade grâce aux relations JPA.
       *
     * @param restaurant L'objet Restaurant à supprimer.
     * @return true si la suppression a réussi, false sinon.
     * @throws OptimisticLockException Si le restaurant a été modifié par un autre utilisateur
     */
    @Override
    public boolean delete(Restaurant restaurant) {
        try {
            // Si l'entité n'est pas managée, il faut la récupérer d'abord
            Restaurant managedRestaurant = entityManager.find(Restaurant.class, restaurant.getId());
            if (managedRestaurant != null) {
                entityManager.remove(managedRestaurant);
                return true;
            }
            return false;
        } catch (OptimisticLockException e) {
            logger.error("Conflit de version détecté lors de la suppression du restaurant ID={}: {}",
                restaurant.getId(), e.getMessage());
            throw e;
        }
    }

}
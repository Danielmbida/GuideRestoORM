package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;

import java.util.Set;

/**
 * Service métier principal pour la gestion des restaurants.
 * <p>
 * Cette classe agit comme une couche intermédiaire entre JPA/Hibernate
 * et la logique applicative. Elle fournit des méthodes permettant de
 * manipuler les entités {@link Restaurant}, {@link City} et {@link RestaurantType}.
 * </p>
 * <p>
 * Le service suit le pattern Singleton afin de garantir une instance unique et
 * réutilisable dans toute l'application.
 * </p>
 */
public class RestaurantService extends AbstractService {

    // Instance unique du service (Singleton)
    private static RestaurantService restaurantService = null;

    /**
     * Constructeur privé (pattern Singleton).
     * Initialise l'EntityManager via AbstractService.
     */
    private RestaurantService() {
        super();
    }

    /**
     * Retourne l'instance unique du service (pattern Singleton).
     *
     * @return Instance unique de {@link RestaurantService}.
     */
    public static RestaurantService getInstance() {
        if (restaurantService == null) {
            restaurantService = new RestaurantService();
        }
        return restaurantService;
    }

    // ---------------------------------------------------------------
    // ------------------- Méthodes de récupération -------------------
    // ---------------------------------------------------------------

    /**
     * Récupère l'ensemble des restaurants enregistrés dans la base.
     *
     * @return Un ensemble d'objets {@link Restaurant}.
     */
    public Set<Restaurant> getRestaurants() {
        return Set.copyOf(entityManager.createQuery(
                "SELECT r FROM Restaurant r",
                Restaurant.class)
                .getResultList());
    }

    /**
     * Recherche un restaurant par son nom exact.
     *
     * @param name Nom exact du restaurant à rechercher.
     * @return L'objet {@link Restaurant} correspondant, ou {@code null} s'il n'existe pas.
     */
    public Restaurant getRestaurantByName(String name) {
        try {
            return entityManager.createQuery(
                    "SELECT r FROM Restaurant r WHERE r.name = :name",
                    Restaurant.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Recherche des restaurants dont le nom contient une sous-chaîne donnée
     * (recherche "LIKE").
     *
     * @param name Sous-chaîne à rechercher dans le nom des restaurants.
     * @return Un ensemble de {@link Restaurant} dont le nom correspond partiellement.
     */
    public Set<Restaurant> getRestaurantByNameLike(String name) {
        return Set.copyOf(entityManager.createQuery(
                "SELECT r FROM Restaurant r WHERE r.name LIKE :name",
                Restaurant.class)
                .setParameter("name", "%" + name + "%")
                .getResultList());
    }

    /**
     * Récupère la liste de toutes les villes associées à au moins un restaurant.
     *
     * @return Un ensemble d'objets {@link City}.
     */
    public Set<City> getVillesRestaurants() {
        return Set.copyOf(entityManager.createQuery(
                "SELECT c FROM City c",
                City.class)
                .getResultList());
    }

    /**
     * Recherche tous les restaurants appartenant à une ville donnée (par son nom).
     *
     * @param cityName Nom de la ville.
     * @return Un ensemble d'objets {@link Restaurant} situés dans la ville spécifiée.
     */
    public Set<Restaurant> getRestaurantsByCityName(String cityName) {
        return Set.copyOf(entityManager.createQuery(
                "SELECT r FROM Restaurant r WHERE r.address.city.cityName = :cityName",
                Restaurant.class)
                .setParameter("cityName", cityName)
                .getResultList());
    }

    // ---------------------------------------------------------------
    // ------------------- Méthodes de modification -------------------
    // ---------------------------------------------------------------

    /**
     * Ajoute un nouveau restaurant dans la base de données.
     *
     * @param name            Nom du restaurant.
     * @param description     Description du restaurant.
     * @param website         URL du site web du restaurant.
     * @param street          Adresse (rue) du restaurant.
     * @param city            Ville dans laquelle se trouve le restaurant.
     * @param restaurantType  Type gastronomique du restaurant.
     * @return Le {@link Restaurant} nouvellement créé (et persisté).
     */
    public Restaurant addRestaurant(String name, String description, String website,
                                    String street, City city, RestaurantType restaurantType) {
        // Création d'un nouvel objet Restaurant et initialisation de ses attributs
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setWebsite(website);

        // Construction de l'objet Localisation lié à la ville
        Localisation localisation = new Localisation();
        localisation.setStreet(street);
        localisation.setCity(city);

        restaurant.setAddress(localisation);
        restaurant.setType(restaurantType);

        // Persistance dans la base via l'EntityManager
        entityManager.getTransaction().begin();
        entityManager.persist(restaurant);
        entityManager.getTransaction().commit();

        return restaurant;
    }

    /**
     * Supprime un restaurant de la base de données.
     *
     * @param restaurant Le restaurant à supprimer.
     */
    public void deleteRestaurant(Restaurant restaurant) {
        entityManager.getTransaction().begin();
        Restaurant managedRestaurant = entityManager.find(Restaurant.class, restaurant.getId());
        if (managedRestaurant != null) {
            entityManager.remove(managedRestaurant);
        }
        entityManager.getTransaction().commit();
    }

    /**
     * Met à jour les informations d'un restaurant existant.
     *
     * @param restaurant Le restaurant contenant les nouvelles informations.
     */
    public void editRestaurant(Restaurant restaurant) {
        entityManager.getTransaction().begin();
        entityManager.merge(restaurant);
        entityManager.getTransaction().commit();
    }

    /**
     * Retourne la ville correspondant à un code postal donné.
     * <p><b>Note :</b> le nom de la méthode contient une coquille ("Citi"). Conservé
     * tel quel pour compatibilité ; préférer un renommage ultérieur vers {@code getCityByZipCode}.</p>
     *
     * @param zipCode Code postal.
     * @return La {@link City} correspondant au code postal, ou {@code null} si introuvable.
     */
    public City getCitiByZipCode(String zipCode) {
        try {
            return entityManager.createQuery(
                    "SELECT c FROM City c WHERE c.zipCode = :zipCode",
                    City.class)
                    .setParameter("zipCode", zipCode)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Crée et persiste une nouvelle ville.
     *
     * @param city Ville à créer.
     * @return La {@link City} persistée.
     */
    public City createCity(City city) {
        entityManager.getTransaction().begin();
        entityManager.persist(city);
        entityManager.getTransaction().commit();
        return city;
    }

    /**
     * Retourne un type de restaurant par son libellé.
     *
     * @param label Libellé du type (ex. "Italien").
     * @return Le {@link RestaurantType} correspondant, ou {@code null} si introuvable.
     */
    public RestaurantType getTypeByLabel(String label) {
        try {
            return entityManager.createQuery(
                    "SELECT rt FROM RestaurantType rt WHERE rt.label = :label",
                    RestaurantType.class)
                    .setParameter("label", label)
                    .getSingleResult();
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Retourne l'ensemble des types de restaurant disponibles.
     *
     * @return Un ensemble de {@link RestaurantType}.
     */
    public Set<RestaurantType> getAllRestaurantTypes() {
        return Set.copyOf(entityManager.createQuery(
                "SELECT rt FROM RestaurantType rt",
                RestaurantType.class)
                .getResultList());
    }

    // ---------------------------------------------------------------
    // ---------------------- Cycle de vie / I/O ----------------------
    // ---------------------------------------------------------------

    /**
     * Ferme proprement les ressources utilisées par le service (EntityManager).
     * <p>
     * Délègue à {@link AbstractService#close()} afin de fermer l'EntityManager.
     * </p>
     */
    public void close() {
        super.close();
    }
}


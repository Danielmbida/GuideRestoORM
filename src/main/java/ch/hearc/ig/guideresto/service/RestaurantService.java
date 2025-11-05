package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.City;
import ch.hearc.ig.guideresto.business.Localisation;
import ch.hearc.ig.guideresto.business.Restaurant;
import ch.hearc.ig.guideresto.business.RestaurantType;
import ch.hearc.ig.guideresto.persistence.CityMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantMapper;
import ch.hearc.ig.guideresto.persistence.RestaurantTypeMapper;

import java.util.Set;

/**
 * Service métier principal pour la gestion des restaurants.
 * <p>
 * Cette classe agit comme une couche intermédiaire entre la couche de persistance
 * (mappers) et la logique applicative. Elle fournit des méthodes permettant de
 * manipuler les entités {@link Restaurant}, {@link City} et {@link RestaurantType}.
 * </p>
 * <p>
 * Le service suit le pattern Singleton afin de garantir une instance unique et
 * réutilisable dans toute l’application.
 * </p>
 */
public class RestaurantService extends AbstractService {

    // Instance unique du service (Singleton)
    private static RestaurantService restaurantService = null;

    // Mappers utilisés pour la persistance des entités liées aux restaurants
    private final RestaurantMapper restaurantMapper;
    private final CityMapper cityMapper;
    private final RestaurantTypeMapper restaurantTypeMapper;

    /**
     * Constructeur privé (pattern Singleton).
     * Initialise la connexion à la base via AbstractService et instancie les mappers.
     */
    private RestaurantService() {
        super();
        // Initialisation des mappers avec la connexion héritée d’AbstractService
        this.restaurantMapper = new RestaurantMapper(connection);
        this.cityMapper = new CityMapper(connection);
        this.restaurantTypeMapper = new RestaurantTypeMapper(connection);
    }

    /**
     * Retourne l’instance unique du service (pattern Singleton).
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
     * Récupère l’ensemble des restaurants enregistrés dans la base.
     *
     * @return Un ensemble d’objets {@link Restaurant}.
     */
    public Set<Restaurant> getRestaurants() {
        return restaurantMapper.findAll();
    }

    /**
     * Recherche un restaurant par son nom exact.
     *
     * @param name Nom exact du restaurant à rechercher.
     * @return L’objet {@link Restaurant} correspondant, ou {@code null} s’il n’existe pas.
     */
    public Restaurant getRestaurantByName(String name) {
        return restaurantMapper.findByName(name);
    }

    /**
     * Recherche des restaurants dont le nom contient une sous-chaîne donnée
     * (recherche "LIKE").
     *
     * @param name Sous-chaîne à rechercher dans le nom des restaurants.
     * @return Un ensemble de {@link Restaurant} dont le nom correspond partiellement.
     */
    public Set<Restaurant> getRestaurantByNameLike(String name) {
        return restaurantMapper.findByNameLike(name);
    }

    /**
     * Récupère la liste de toutes les villes associées à au moins un restaurant.
     *
     * @return Un ensemble d’objets {@link City}.
     */
    public Set<City> getVillesRestaurants() {
        return cityMapper.findAll();
    }

    /**
     * Recherche tous les restaurants appartenant à une ville donnée (par son nom).
     *
     * @param city Nom de la ville.
     * @return Un ensemble d’objets {@link Restaurant} situés dans la ville spécifiée.
     */
    public Set<Restaurant> getRestaurantsByCityName(String city) {
        City city1 = cityMapper.findByName(city);
        return restaurantMapper.findByCityId(city1.getId());
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
        // Création d’un nouvel objet Restaurant et initialisation de ses attributs
        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setDescription(description);
        restaurant.setWebsite(website);

        // Construction de l’objet Localisation lié à la ville
        Localisation localisation = new Localisation();
        localisation.setStreet(street);
        localisation.setCity(city);

        restaurant.setAddress(localisation);
        restaurant.setType(restaurantType);

        // Persistance dans la base via le mapper
        return restaurantMapper.create(restaurant);
    }

    /**
     * Supprime un restaurant de la base de données.
     *
     * @param restaurant Le restaurant à supprimer.
     */
    public void deleteRestaurant(Restaurant restaurant) {
        restaurantMapper.delete(restaurant);
    }

    /**
     * Met à jour les informations d’un restaurant existant.
     *
     * @param restaurant Le restaurant contenant les nouvelles informations.
     */
    public void editRestaurant(Restaurant restaurant) {
        restaurantMapper.update(restaurant);
    }

    /**
     * Retourne la ville correspondant à un code postal donné.
     * <p><b>Note :</b> le nom de la méthode contient une coquille (“Citi”). Conservé
     * tel quel pour compatibilité ; préférer un renommage ultérieur vers {@code getCityByZipCode}.</p>
     *
     * @param zipCode Code postal.
     * @return La {@link City} correspondant au code postal, ou {@code null} si introuvable.
     */
    public City getCitiByZipCode(String zipCode) {
        return cityMapper.findByZipCode(zipCode);
    }

    /**
     * Crée et persiste une nouvelle ville.
     *
     * @param city Ville à créer.
     * @return La {@link City} persistée.
     */
    public City createCity(City city) {
        return cityMapper.create(city);
    }

    /**
     * Retourne un type de restaurant par son libellé.
     *
     * @param label Libellé du type (ex. “Italien”).
     * @return Le {@link RestaurantType} correspondant, ou {@code null} si introuvable.
     */
    public RestaurantType getTypeByLabel(String label) {
        return restaurantTypeMapper.findByLabel(label);
    }

    /**
     * Retourne l’ensemble des types de restaurant disponibles.
     *
     * @return Un ensemble de {@link RestaurantType}.
     */
    public Set<RestaurantType> getAllRestaurantTypes() {
        return restaurantTypeMapper.findAll();
    }

    // ---------------------------------------------------------------
    // ---------------------- Cycle de vie / I/O ----------------------
    // ---------------------------------------------------------------

    /**
     * Ferme proprement les ressources utilisées par le service (connexion JDBC).
     * <p>
     * Délègue à {@link AbstractService#close()} afin de rendre la connexion au pool
     * (si pool) ou de fermer physiquement la connexion.
     * </p>
     */
    public void close() {
        super.close();
    }
}

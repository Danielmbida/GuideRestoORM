package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Restaurant;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

public class RestaurantMapper extends AbstractMapper<Restaurant> {
    private final Connection connection;

    private final RestaurantTypeMapper restaurantTypeMapper;
    private final CityMapper cityMapper;

    public RestaurantMapper(Connection connection) {
        this.connection = connection;
        this.restaurantTypeMapper = new RestaurantTypeMapper(connection);
        this.cityMapper = new CityMapper(connection);
    }

    /**
     * Recherche un restaurant dans la base de données par son identifiant (NUMERO).
     *
     * @param id Identifiant unique du restaurant à rechercher.
     * @return L'objet Restaurant correspondant, ou null s'il n'existe pas.
     */
    @Override
    public Restaurant findById(int id) {
        // Vérifie si le restaurant est déjà dans le cache
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        String query = "SELECT * FROM RESTAURANTS WHERE NUMERO = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Restaurant restaurant = new Restaurant(
                        rs.getInt("numero"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("site_web"),
                        rs.getString("adresse"),
                        cityMapper.findById(rs.getInt("fk_vill")),
                        restaurantTypeMapper.findById(rs.getInt("fk_type"))
                );
                this.addToCache(restaurant);
                return this.cache.get(restaurant.getId());
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du restaurant avec l'ID {} : {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }


    public Set<Restaurant> findByNameLike(String namePart) {
        String query = "SELECT * FROM RESTAURANTS WHERE UPPER(nom) LIKE ?";
        Set<Restaurant> results = new LinkedHashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + namePart.toUpperCase() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("numero");
                Restaurant restaurant = cache.get(id);

                if (restaurant == null) {
                    restaurant = new Restaurant(
                            id,
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("site_web"),
                            rs.getString("adresse"),
                            cityMapper.findById(rs.getInt("fk_vill")),
                            restaurantTypeMapper.findById(rs.getInt("fk_type"))
                    );
                    addToCache(restaurant);
                }
                results.add(restaurant);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des restaurants contenant '{}': {}", namePart, e.getMessage());
            throw new RuntimeException(e);
        }
        return results;
    }

    public Set<Restaurant> findByCity(String namePart) {
        String query = "SELECT * FROM RESTAURANTS R " +
                "inner join VILLES V on V.NUMERO = R.FK_VILL " +
                "WHERE UPPER(V.NOM_VILLE) LIKE ?";
        Set<Restaurant> results = new LinkedHashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, "%" + namePart.toUpperCase() + "%");
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("numero");
                Restaurant restaurant = cache.get(id);

                if (restaurant == null) {
                    restaurant = new Restaurant(
                            id,
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("site_web"),
                            rs.getString("adresse"),
                            cityMapper.findById(rs.getInt("fk_vill")),
                            restaurantTypeMapper.findById(rs.getInt("fk_type"))
                    );
                    addToCache(restaurant);
                }

                results.add(restaurant);
            }

        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche des restaurants contenant '{}': {}", namePart, e.getMessage());
            throw new RuntimeException(e);
        }

        return results;
    }


    /**
     * Recherche un restaurant dans la base de données par son nom.
     *
     * @param name Nom du restaurant à rechercher.
     * @return L'objet {@link Restaurant} correspondant, ou null s'il n'existe pas.
     */
    public Restaurant findByName(String name) {
        // Vérifie si un restaurant portant ce nom est déjà présent dans le cache
        for (Restaurant cachedRestaurant : cache.values()) {
            if (cachedRestaurant.getName().equalsIgnoreCase(name)) {
                return cachedRestaurant;
            }
        }

        String query = "SELECT * FROM RESTAURANTS WHERE UPPER(NOM) = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setString(1, name.toUpperCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                Restaurant restaurant = new Restaurant(
                        rs.getInt("numero"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getString("site_web"),
                        rs.getString("adresse"),
                        cityMapper.findById(rs.getInt("fk_vill")),
                        restaurantTypeMapper.findById(rs.getInt("fk_type"))
                );
                // Ajoute au cache pour les appels suivants
                this.addToCache(restaurant);
                return restaurant;
            }

        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du restaurant avec le nom '{}' : {}", name, e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Récupère l'ensemble des restaurants enregistrés dans la base de données.
     *
     * @return Un Set contenant tous les objets Restaurant.
     */
    @Override
    public Set<Restaurant> findAll() {
        String query = "SELECT * FROM RESTAURANTS";
        try (PreparedStatement stmt = this.connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            Set<Restaurant> restaurants = new HashSet<>();

            while (rs.next()) {
                int id = rs.getInt("numero");
                Restaurant restaurant = cache.get(id);
                if (restaurant == null) {
                    restaurant = new Restaurant(
                            id,
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("site_web"),
                            rs.getString("adresse"),
                            cityMapper.findById(rs.getInt("fk_vill")),
                            restaurantTypeMapper.findById(rs.getInt("fk_type"))
                    );
                    this.addToCache(restaurant);
                }
                restaurants.add(restaurant);
            }
            return restaurants;

        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de la liste des restaurants : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Recherche tous les restaurants appartenant à une ville donnée (clé étrangère fk_vill).
     *
     * @param cityId Identifiant unique de la ville (colonne fk_vill).
     * @return Un ensemble d’objets {@link Restaurant} appartenant à la ville spécifiée.
     */
    public Set<Restaurant> findByCityId(int cityId) {
        String query = "SELECT * FROM RESTAURANTS WHERE FK_VILL = ?";
        try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
            stmt.setInt(1, cityId);
            ResultSet rs = stmt.executeQuery();

            Set<Restaurant> restaurants = new HashSet<>();

            while (rs.next()) {
                int id = rs.getInt("numero");
                Restaurant restaurant = cache.get(id);

                if (restaurant == null) {
                    // Création d'un nouvel objet Restaurant si non présent dans le cache
                    restaurant = new Restaurant(
                            id,
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("site_web"),
                            rs.getString("adresse"),
                            cityMapper.findById(rs.getInt("fk_vill")),
                            restaurantTypeMapper.findById(rs.getInt("fk_type"))
                    );
                    this.addToCache(restaurant);
                }

                restaurants.add(restaurant);
            }

            return restaurants;

        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des restaurants pour la ville ID {} : {}", cityId, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Recherche tous les restaurants appartenant à un type gastronomique donné (clé étrangère fk_type).
     *
     * @param typeId Identifiant unique du type gastronomique (colonne fk_type).
     * @return Un ensemble d’objets {@link Restaurant} correspondant au type spécifié.
     */
    public Set<Restaurant> findByTypeId(int typeId) {
        String query = "SELECT * FROM RESTAURANTS WHERE FK_TYPE = ?";
        try (PreparedStatement stmt = this.connection.prepareStatement(query)) {
            stmt.setInt(1, typeId);
            ResultSet rs = stmt.executeQuery();

            Set<Restaurant> restaurants = new HashSet<>();

            while (rs.next()) {
                int id = rs.getInt("numero");
                Restaurant restaurant = cache.get(id);

                if (restaurant == null) {
                    // Création d'un nouvel objet Restaurant si non présent dans le cache
                    restaurant = new Restaurant(
                            id,
                            rs.getString("nom"),
                            rs.getString("description"),
                            rs.getString("site_web"),
                            rs.getString("adresse"),
                            cityMapper.findById(rs.getInt("fk_vill")),
                            restaurantTypeMapper.findById(rs.getInt("fk_type"))
                    );
                    this.addToCache(restaurant);
                }

                restaurants.add(restaurant);
            }

            return restaurants;

        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des restaurants pour le type ID {} : {}", typeId, e.getMessage());
            throw new RuntimeException(e);
        }
    }



    /**
     * Crée un nouveau restaurant dans la base de données.
     * Le champ NUMERO est alimenté automatiquement par une séquence.
     *
     * @param object L'objet Restaurant à insérer.
     * @return Le Restaurant nouvellement créé (récupéré depuis la base).
     */
    @Override
    public Restaurant create(Restaurant object) {
        Integer generatedId = getSequenceValue();
        object.setId(generatedId);

        String insertQuery = "INSERT INTO RESTAURANTS (NOM, DESCRIPTION, SITE_WEB, ADRESSE, FK_VILL, FK_TYPE) "
                + "VALUES (?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement insertStatement = connection.prepareStatement(insertQuery);
            insertStatement.setString(1, object.getName());
            insertStatement.setString(2, object.getDescription());
            insertStatement.setString(3, object.getWebsite());
            insertStatement.setString(4, object.getAddress().getStreet());
            insertStatement.setInt(5, object.getAddress().getCity().getId());
            insertStatement.setInt(6, object.getType().getId());
            insertStatement.executeUpdate();

            connection.commit();
            connection.close();
            return object;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création du restaurant '{}' : {}", object.getName(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Met à jour les informations d'un restaurant existant dans la base de données.
     *
     * @param object L'objet Restaurant contenant les nouvelles informations.
     * @return true si la mise à jour a été effectuée avec succès, false sinon.
     */
    @Override
    public boolean update(Restaurant object) {
        String query = "UPDATE RESTAURANTS SET nom = ?, description = ?, site_web = ?, adresse = ?, fk_vill = ?, fk_type = ? WHERE numero = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setString(1, object.getName());
            stmt.setString(2, object.getDescription());
            stmt.setString(3, object.getWebsite());
            stmt.setString(4, object.getAddress().getStreet());
            stmt.setInt(5, object.getAddress().getCity().getId());
            stmt.setInt(6, object.getType().getId());
            stmt.setInt(7, object.getId());

            stmt.executeUpdate();
            connection.commit();
            removeFromCache(object.getId());
            return stmt.getUpdateCount() == 1;

        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour du restaurant avec l'ID {} : {}", object.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Supprime un restaurant de la base de données à partir de son identifiant.
     *
     * @param object L'objet Restaurant à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean delete(Restaurant object) {
        String deleteGradesQuery = "DELETE FROM NOTES WHERE fk_comm IN (SELECT numero FROM COMMENTAIRES WHERE fk_rest = ?)";
        String deleteCommentsQuery = "DELETE FROM COMMENTAIRES WHERE fk_rest = ?";
        String deleteLikesQuery = "DELETE FROM LIKES WHERE fk_rest = ?";
        String deleteRestaurantQuery = "DELETE FROM RESTAURANTS WHERE numero = ?";

        try (
                PreparedStatement deleteGradesStmt = connection.prepareStatement(deleteGradesQuery);
                PreparedStatement deleteCommentsStmt = connection.prepareStatement(deleteCommentsQuery);
                PreparedStatement deleteLikesStmt = connection.prepareStatement(deleteLikesQuery);
                PreparedStatement deleteRestaurantStmt = connection.prepareStatement(deleteRestaurantQuery);
        ) {
            int restaurantId = object.getId();

            // Supprimer les notes liées aux commentaires du restaurant
            deleteGradesStmt.setInt(1, restaurantId);
            deleteGradesStmt.executeUpdate();

            // Supprimer les commentaires du restaurant
            deleteCommentsStmt.setInt(1, restaurantId);
            deleteCommentsStmt.executeUpdate();

            // Supprimer les likes du restaurant
            deleteLikesStmt.setInt(1, restaurantId);
            deleteLikesStmt.executeUpdate();

            // Supprimer le restaurant
            deleteRestaurantStmt.setInt(1, restaurantId);
            int rows = deleteRestaurantStmt.executeUpdate();

            connection.commit();
            removeFromCache(restaurantId);
            return rows == 1;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression du restaurant avec ID {} : {}", object.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean deleteById(int id) {
        return delete(findById(id));
    }

    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_RESTAURANTS.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "";
    }

    @Override
    protected String getCountQuery() {
        return "";
    }
}

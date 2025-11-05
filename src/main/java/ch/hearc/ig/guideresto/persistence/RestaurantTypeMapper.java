package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.RestaurantType;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

public class RestaurantTypeMapper extends AbstractMapper<RestaurantType> {

    private final Connection connection;

    public RestaurantTypeMapper(Connection connection) {
        this.connection = connection;
    }

    /**
     * Recherche un type gastronomique par son identifiant (NUMERO).
     *
     * @param id Identifiant unique du type gastronomique à rechercher.
     * @return L'objet RestaurantType correspondant, ou null s'il n'existe pas.
     */
    @Override
    public RestaurantType findById(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        String query = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE NUMERO = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                RestaurantType type = new RestaurantType(
                        rs.getInt("NUMERO"),
                        rs.getString("libelle"),
                        rs.getString("DESCRIPTION")
                );
                addToCache(type);
                return type;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du type gastronomique avec l'ID {} : {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    public RestaurantType findByLabel(String namePart) {
        String query = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE UPPER(LIBELLE)= ?";
        RestaurantType restaurantType = new RestaurantType();

        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, namePart.toUpperCase());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("numero");
                restaurantType = cache.get(id);

                if (restaurantType == null) {
                    restaurantType = new RestaurantType(
                            id,
                            rs.getString("libelle"),
                            rs.getString("DESCRIPTION")
                    );
                    addToCache(restaurantType);
                }
                            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du type gastronomique contenant '{}': {}", namePart, e.getMessage());
            throw new RuntimeException(e);
        }
        return restaurantType;
    }


    /**
     * Recherche un type gastronomique par son libellé.
     *
     * @param libelle Libellé du type gastronomique à rechercher.
     * @return L'objet {@link RestaurantType} correspondant, ou null s'il n'existe pas.
     */
    public RestaurantType findByType(String libelle) {
        // Vérifie si le type est déjà présent dans le cache
        for (RestaurantType cachedType : cache.values()) {
            if (cachedType.getLabel().equalsIgnoreCase(libelle)) {
                return cachedType;
            }
        }

        String query = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE LIBELLE = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setString(1, libelle);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                // Construction de l'objet métier RestaurantType à partir du ResultSet
                RestaurantType type = new RestaurantType(
                        rs.getInt("NUMERO"),
                        rs.getString("LIBELLE"),
                        rs.getString("DESCRIPTION")
                );
                addToCache(type); // Mise en cache pour éviter les doublons en mémoire
                return type;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du type gastronomique '{}' : {}", libelle, e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }


    /**
     * Récupère tous les types gastronomiques existants dans la base de données.
     *
     * @return Un Set contenant tous les objets RestaurantType.
     */
    @Override
    public Set<RestaurantType> findAll() {
        String query = "SELECT * FROM TYPES_GASTRONOMIQUES";
        Set<RestaurantType> restaurantTypes = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                int id = rs.getInt("NUMERO");
                RestaurantType type = cache.get(id);

                if (type == null) {
                    type = new RestaurantType(
                            id,
                            rs.getString("libelle"),
                            rs.getString("description")
                    );
                    addToCache(type);
                }

                restaurantTypes.add(type);
            }

        } catch (SQLException ex) {
            logger.error("Erreur lors de la récupération de la liste des types gastronomiques : {}", ex.getMessage());
            throw new RuntimeException(ex);
        }
        return restaurantTypes;
    }

    /**
     * Crée un nouveau type gastronomique dans la base de données.
     *
     * @param object L'objet RestaurantType à insérer.
     * @return Le RestaurantType nouvellement créé (récupéré depuis la base), ou null si l'insertion échoue.
     */
    @Override
    public RestaurantType create(RestaurantType object) {
        String query = "INSERT INTO TYPES_GASTRONOMIQUES (LIBELLE, description) VALUES (?, ?)";
        String getQuery = "SELECT * FROM TYPES_GASTRONOMIQUES WHERE LIBELLE = ?";

        try (
                PreparedStatement insertStmt = connection.prepareStatement(query);
                PreparedStatement selectStmt = connection.prepareStatement(getQuery)
        ) {
            insertStmt.setString(1, object.getLabel());
            insertStmt.setString(2, object.getDescription());
            insertStmt.executeUpdate();

            connection.commit();

            selectStmt.setString(1, object.getLabel());
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return new RestaurantType(
                        rs.getInt("numero"),
                        rs.getString("libelle"),
                        rs.getString("description")
                );
            }
            resetCache();
            return null;

        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                logger.error("Le nom '{}' existe déjà (contrainte unique)", object.getLabel());
            } else {
                logger.error("Erreur lors de la création du type gastronomique '{}' : {}", object.getLabel(), ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Met à jour un type gastronomique existant dans la base de données.
     *
     * @param object L'objet RestaurantType contenant les nouvelles informations.
     * @return true si la mise à jour a réussi, false sinon.
     */
    @Override
    public boolean update(RestaurantType object) {
        String query = "UPDATE TYPES_GASTRONOMIQUES SET LIBELLE = ?, description = ? WHERE numero = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setString(1, object.getLabel());
            stmt.setString(2, object.getDescription());
            stmt.setInt(3, object.getId());

            stmt.executeUpdate();
            connection.commit();
            removeFromCache(object.getId());
            return stmt.getUpdateCount() == 1;
        } catch (SQLException ex) {
            logger.error("Erreur lors de la mise à jour du type gastronomique avec l'ID {} : {}", object.getId(), ex.getMessage());
            return false;
        }
    }

    /**
     * Supprime un type gastronomique de la base de données.
     *
     * @param object L'objet RestaurantType à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean delete(RestaurantType object) {
        String query = "DELETE FROM TYPES_GASTRONOMIQUES WHERE numero = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setInt(1, object.getId());
            stmt.executeUpdate();
            connection.commit();
            removeFromCache(object.getId());

            return true;
        } catch (SQLException ex) {
            logger.error("Erreur lors de la suppression du type gastronomique avec l'ID {} : {}", object.getId(), ex.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteById(int id) {
        return delete(findById(id));
    }

    @Override
    protected String getSequenceQuery() {
        return "";
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

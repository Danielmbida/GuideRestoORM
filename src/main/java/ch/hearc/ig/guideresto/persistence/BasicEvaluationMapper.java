package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.BasicEvaluation;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Mapper responsable de la persistance des objets {@link BasicEvaluation}.
 * <p>
 * Gère les opérations CRUD sur la table LIKES (appréciations simples).
 * Convertit la valeur 'T'/'F' en booléen et résout les liens vers les restaurants.
 */
public class BasicEvaluationMapper extends AbstractMapper<BasicEvaluation> {

    private final Connection connection; // Connexion JDBC active
    private final RestaurantMapper restaurantMapper; // Mapper pour les restaurants liés

    /**
     * Constructeur du mapper.
     *
     * @param connection Connexion JDBC à la base de données.
     */
    public BasicEvaluationMapper(Connection connection) {
        this.connection = connection;
        this.restaurantMapper = new RestaurantMapper(connection);
    }

    /**
     * Recherche une évaluation par son identifiant unique (NUMERO).
     *
     * @param id Identifiant de l’évaluation.
     * @return L’objet {@link BasicEvaluation} correspondant, ou null s’il n’existe pas.
     */
    @Override
    public BasicEvaluation findById(int id) {
        // Vérifie si l'objet est déjà en cache
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM LIKES WHERE numero = ?");
            preparedStatement.setInt(1, id);
            ResultSet resultSet = preparedStatement.executeQuery();

            if (resultSet.next()) {
                // Récupération des données et conversion du champ APPRECIATION (char → boolean)
                BasicEvaluation basicEvaluation = new BasicEvaluation(
                        resultSet.getDate("date_eval"),
                        restaurantMapper.findById(resultSet.getInt("fk_rest")),
                        (Objects.equals(resultSet.getString("APPRECIATION"), "T")),
                        resultSet.getString("ADRESSE_IP")
                );
                this.addToCache(basicEvaluation);
                return this.cache.get(id);
            }
            return null;

        } catch (Exception e) {
            logger.error("Error en CityMapper.findById", e);
            return null;
        }
    }

    /**
     * Récupère toutes les évaluations présentes dans la base de données.
     *
     * @return Un ensemble d’objets {@link BasicEvaluation}.
     */
    @Override
    public Set<BasicEvaluation> findAll() {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("SELECT * FROM LIKES");
            ResultSet resultSet = preparedStatement.executeQuery();
            Set<BasicEvaluation> set = new HashSet<>();

            while (resultSet.next()) {
                int id = resultSet.getInt("numero");
                BasicEvaluation basicEvaluation = cache.get(id);

                if (basicEvaluation == null) {
                    // Création d’un nouvel objet et ajout au cache
                    basicEvaluation = new BasicEvaluation(
                            resultSet.getDate("date_eval"),
                            restaurantMapper.findById(resultSet.getInt("fk_rest")),
                            (resultSet.getString("APPRECIATION") == "T" ? true : false),
                            resultSet.getString("ADRESSE_IP")
                    );
                    this.addToCache(basicEvaluation);
                }
                set.add(basicEvaluation);
            }
            return set;

        } catch (Exception e) {
            logger.error("Error en CityMapper.findAll", e);
            return null;
        }
    }

    /**
     * Récupère toutes les évaluations associées à un restaurant donné (clé étrangère fk_rest).
     *
     * @param restaurantId Identifiant unique du restaurant (colonne fk_rest).
     * @return Un ensemble d’objets {@link BasicEvaluation} liés au restaurant spécifié.
     */
    public Set<BasicEvaluation> findByRestaurantId(int restaurantId) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "SELECT * FROM LIKES WHERE FK_REST = ?"
            );
            preparedStatement.setInt(1, restaurantId);
            ResultSet resultSet = preparedStatement.executeQuery();

            Set<BasicEvaluation> set = new HashSet<>();

            while (resultSet.next()) {
                int id = resultSet.getInt("numero");
                BasicEvaluation basicEvaluation = cache.get(id);

                if (basicEvaluation == null) {
                    // Création d’un nouvel objet BasicEvaluation et ajout au cache
                    basicEvaluation = new BasicEvaluation(
                            resultSet.getDate("date_eval"),
                            restaurantMapper.findById(resultSet.getInt("fk_rest")),
                            // Conversion 'T'/'F' en booléen
                            (resultSet.getString("APPRECIATION") == "T" ? true : false),
                            resultSet.getString("ADRESSE_IP")
                    );
                    this.addToCache(basicEvaluation);
                }
                set.add(basicEvaluation);
            }

            return set;

        } catch (Exception e) {
            logger.error("Erreur lors de la récupération des évaluations pour le restaurant ID {} : {}", restaurantId, e.getMessage());
            return null;
        }
    }


    /**
     * Insère une nouvelle évaluation dans la base de données.
     *
     * @param object L’objet {@link BasicEvaluation} à insérer.
     * @return L’évaluation créée, relue depuis la base, ou null en cas d’échec.
     */
    @Override
    public BasicEvaluation create(BasicEvaluation object) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "INSERT INTO LIKES(appreciation, date_eval, adresse_ip, fk_rest) VALUES (?, ?, ?, ?)"
            );
            preparedStatement.setString(1, (object.getLikeRestaurant()) ? "T" : "F");
            preparedStatement.setDate(2, new Date(object.getVisitDate().getTime()));
            preparedStatement.setString(3, object.getIpAddress());
            preparedStatement.setInt(4, object.getRestaurant().getId());
            preparedStatement.executeUpdate();

            connection.commit(); // Commit explicite après insertion

            // Relecture pour retrouver l’objet inséré
            PreparedStatement preparedStatement1 = connection.prepareStatement(
                    "SELECT * FROM LIKES WHERE date_eval = ? AND adresse_ip = ? AND fk_rest = ?"
            );
            preparedStatement1.setDate(1, new Date(object.getVisitDate().getTime()));
            preparedStatement1.setString(2, object.getIpAddress());
            preparedStatement1.setInt(3, object.getRestaurant().getId());
            ResultSet resultSet = preparedStatement1.executeQuery();

            if (resultSet.next()) {
                return new BasicEvaluation(
                        resultSet.getDate("date_eval"),
                        restaurantMapper.findById(resultSet.getInt("fk_rest")),
                        (resultSet.getString("APPRECIATION") == "T" ? true : false),
                        resultSet.getString("ADRESSE_IP")
                );
            }

            resetCache(); // Nettoyage du cache si la relecture échoue
            return null;

        } catch (Exception e) {
            logger.error("Error en CityMapper.create", e);
            return null;
        }
    }

    /**
     * Met à jour une évaluation existante.
     *
     * @param object L’objet {@link BasicEvaluation} à mettre à jour.
     * @return true si la mise à jour a réussi, false sinon.
     */
    @Override
    public boolean update(BasicEvaluation object) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement(
                    "UPDATE LIKES SET appreciation = ?, date_eval = ?, adresse_ip = ?, fk_rest = ? WHERE numero = ?"
            );
            preparedStatement.setString(1, (object.getLikeRestaurant()) ? "T" : "F");
            preparedStatement.setDate(2, new Date(object.getVisitDate().getTime()));
            preparedStatement.setString(3, object.getIpAddress());
            preparedStatement.setInt(4, object.getRestaurant().getId());
            preparedStatement.setInt(5, object.getId());
            preparedStatement.executeUpdate();

            removeFromCache(object.getId()); // Supprime du cache pour forcer la relecture
            return true;

        } catch (Exception e) {
            logger.error("Error en CityMapper.update", e);
            return false;
        }
    }

    /**
     * Supprime une évaluation de la base.
     *
     * @param object L’objet {@link BasicEvaluation} à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean delete(BasicEvaluation object) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM LIKES WHERE numero = ?");
            preparedStatement.setInt(1, object.getId());
            preparedStatement.executeUpdate();

            removeFromCache(object.getId()); // Nettoyage du cache après suppression
            return true;

        } catch (Exception e) {
            logger.error("Error en CityMapper.delete", e);
            return false;
        }
    }

    /**
     * Supprime une évaluation par son identifiant.
     *
     * @param id Identifiant de l’évaluation à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean deleteById(int id) {
        try {
            PreparedStatement preparedStatement = connection.prepareStatement("DELETE FROM LIKES WHERE numero = ?");
            preparedStatement.setInt(1, id);
            preparedStatement.executeUpdate();

            removeFromCache(id);
            return true;

        } catch (Exception e) {
            logger.error("Error en CityMapper.deleteById", e);
            return false;
        }
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

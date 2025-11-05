package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.CompleteEvaluation;

import java.sql.*;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mapper pour la gestion des évaluations complètes (CompleteEvaluation) en base de données.
 */
public class CompleteEvaluationMapper extends AbstractMapper<CompleteEvaluation> {

    private final Connection connection;
    private final RestaurantMapper restaurantMapper;

    public CompleteEvaluationMapper(Connection connection) {
        this.connection = connection;
        this.restaurantMapper = new RestaurantMapper(connection);
    }

    /**
     * Recherche une évaluation complète par son identifiant (numero).
     *
     * @param id L'identifiant unique de l'évaluation.
     * @return L'objet CompleteEvaluation correspondant, ou null si non trouvé.
     */
    @Override
    public CompleteEvaluation findById(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        String query = "SELECT * FROM COMMENTAIRES WHERE NUMERO = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    CompleteEvaluation evaluation = new CompleteEvaluation(
                            rs.getInt("numero"),
                            rs.getDate("date_eval"),
                            restaurantMapper.findById(rs.getInt("fk_rest")),
                            rs.getString("commentaire"),
                            rs.getString("nom_utilisateur")
                    );
                    addToCache(evaluation);
                    return evaluation;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche du commentaire avec l'ID {} : {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Recherche toutes les évaluations complètes associées à un restaurant donné (clé étrangère fk_rest).
     *
     * @param restaurantId Identifiant unique du restaurant (colonne fk_rest).
     * @return Un ensemble d’objets {@link CompleteEvaluation} liés au restaurant spécifié.
     */
    public Set<CompleteEvaluation> findByRestaurantId(int restaurantId) {
        String query = "SELECT * FROM COMMENTAIRES WHERE FK_REST = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, restaurantId);
            try (ResultSet rs = stmt.executeQuery()) {

                Set<CompleteEvaluation> evaluations = new LinkedHashSet<>();

                while (rs.next()) {
                    int id = rs.getInt("numero");
                    CompleteEvaluation evaluation = cache.get(id);

                    if (evaluation == null) {
                        // Création d’un nouvel objet CompleteEvaluation si non présent dans le cache
                        evaluation = new CompleteEvaluation(
                                id,
                                rs.getDate("date_eval"),
                                restaurantMapper.findById(rs.getInt("fk_rest")),
                                rs.getString("commentaire"),
                                rs.getString("nom_utilisateur")
                        );
                        addToCache(evaluation);
                    }

                    evaluations.add(evaluation);
                }

                return evaluations;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des évaluations pour le restaurant ID {} : {}", restaurantId, e.getMessage());
            throw new RuntimeException(e);
        }
    }


    /**
     * Récupère toutes les évaluations complètes de la base.
     *
     * @return Un Set contenant tous les CompleteEvaluation.
     */
    @Override
    public Set<CompleteEvaluation> findAll() {
        String query = "SELECT * FROM COMMENTAIRES";
        Set<CompleteEvaluation> evaluations = new LinkedHashSet<>();
        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("numero");
                CompleteEvaluation evaluation = cache.get(id);
                if (evaluation == null) {
                    evaluation = new CompleteEvaluation(
                            id,
                            rs.getDate("date_eval"),
                            restaurantMapper.findById(rs.getInt("fk_rest")),
                            rs.getString("commentaire"),
                            rs.getString("nom_utilisateur")
                    );
                    addToCache(evaluation);
                }

                evaluations.add(evaluation);
            }

        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des évaluations complètes : {}", e.getMessage());
            throw new RuntimeException(e);
        }

        return evaluations;
    }

    /**
     * Insère une nouvelle évaluation complète dans la base.
     *
     * @param object L'évaluation à insérer.
     * @return L'évaluation insérée avec son ID à jour.
     */
    @Override
    public CompleteEvaluation create(CompleteEvaluation object) {
        Integer generatedId = getSequenceValue();
        object.setId(generatedId);

        String query = "INSERT INTO COMMENTAIRES (NUMERO,DATE_EVAL, FK_REST, COMMENTAIRE, NOM_UTILISATEUR) VALUES (?,?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, generatedId);
            stmt.setDate(2, new Date(object.getVisitDate().getTime()));
            stmt.setInt(3, object.getRestaurant().getId());
            stmt.setString(4, object.getComment());
            stmt.setString(5, object.getUsername());
            int affectedRows = stmt.executeUpdate();

            if (affectedRows == 0) {
                throw new SQLException("La création de l'évaluation a échoué (aucune ligne insérée).");
            }

            connection.commit();
            resetCache();
            return object;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de l'évaluation complète : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Met à jour une évaluation existante.
     *
     * @param object L'objet à mettre à jour.
     * @return true si la mise à jour a réussi, false sinon.
     */
    @Override
    public boolean update(CompleteEvaluation object) {
        System.out.println(object.getRestaurant().getId());
        String query = "UPDATE COMMENTAIRES SET DATE_EVAL = ?, FK_REST = ?, COMMENTAIRE = ?, NOM_UTILISATEUR = ? WHERE NUMERO = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setDate(1, new Date(object.getVisitDate().getTime()));
            stmt.setInt(2, object.getRestaurant().getId());
            stmt.setString(3, object.getComment());
            stmt.setString(4, object.getUsername());
            stmt.setInt(5, object.getId());

            int rows = stmt.executeUpdate();
            removeFromCache(object.getId());
            connection.commit();
            return rows == 1;
        } catch (SQLException e) {
            logger.error("Erreur lors de la MAJ évaluation avec ID {} : {}", object.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Supprime une évaluation à partir de l'objet.
     *
     * @param object L'objet à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean delete(CompleteEvaluation object) {
        return deleteById(object.getId());
    }

    /**
     * Supprime une évaluation à partir de son ID.
     *
     * @param id L'identifiant de l'évaluation à supprimer.
     * @return true si la suppression a réussi, false sinon.
     */
    @Override
    public boolean deleteById(int id) {
        // Supprimer d'abord toutes les notes associées (NOTES.fk_comm = id)
        String deleteGradesQuery = "DELETE FROM NOTES WHERE fk_comm = ?";
        String deleteCommentQuery = "DELETE FROM COMMENTAIRES WHERE NUMERO = ?";

        try (
                PreparedStatement gradesStmt = connection.prepareStatement(deleteGradesQuery);
                PreparedStatement commentStmt = connection.prepareStatement(deleteCommentQuery)
        ) {
            // 1. Supprime toutes les notes associées
            gradesStmt.setInt(1, id);
            gradesStmt.executeUpdate();

            // 2. Supprime l'évaluation complète
            commentStmt.setInt(1, id);
            int rows = commentStmt.executeUpdate();

            connection.commit();
            removeFromCache(id);
            return rows == 1;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'évaluation complète avec ID {} : {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }


    @Override
    protected String getSequenceQuery() {
        // Si besoin d'une séquence Oracle : ex "SELECT SEQ_COMMENTAIRES.NEXTVAL FROM DUAL"
        return "SELECT SEQ_EVAL.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        // Pour vérifier l'existence d'une évaluation
        return "SELECT 1 FROM COMMENTAIRES WHERE NUMERO = ?";
    }

    @Override
    protected String getCountQuery() {
        // Pour compter le nombre total d'évaluations
        return "SELECT COUNT(*) FROM COMMENTAIRES";
    }
}

package ch.hearc.ig.guideresto.persistence;
import ch.hearc.ig.guideresto.business.EvaluationCriteria;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;

/**
 * Mapper JDBC pour la table CRITERES_EVALUATION.
 * Permet de faire le lien entre les objets métiers {@link EvaluationCriteria}
 * et la base de données relationnelle.
 */
public class EvaluationCriteriaMapper extends AbstractMapper<EvaluationCriteria> {

    private final Connection connection;

    public EvaluationCriteriaMapper(Connection connection) {
        this.connection = connection;
    }
    /**
     * Recherche un critère d'évaluation par son identifiant.
     * @param id identifiant du critère
     * @return le critère trouvé ou null si aucun résultat
     */
    @Override
    public EvaluationCriteria findById(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }

        String query = "SELECT * FROM CRITERES_EVALUATION WHERE numero = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                EvaluationCriteria criteria = new EvaluationCriteria(
                        rs.getInt("numero"),
                        rs.getString("nom"),
                        rs.getString("description")
                );
                addToCache(criteria);
                return criteria;
            }
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return null;
    }

    /**
     * Retourne tous les critères d'évaluation présents en base.
     * @return un Set de critères, ou null si erreur
     */
    @Override
    public Set<EvaluationCriteria> findAll() {
        String query = "SELECT * FROM CRITERES_EVALUATION";
        Set<EvaluationCriteria> evaluationCriteria = new HashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("numero");
                EvaluationCriteria criteria = cache.get(id);

                if (criteria == null) {
                    criteria = new EvaluationCriteria(
                            id,
                            rs.getString("nom"),
                            rs.getString("description")
                    );
                    addToCache(criteria);
                }

                evaluationCriteria.add(criteria);
            }

        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }

        return evaluationCriteria;
    }

    /**
     * Insère un nouveau critère d'évaluation dans la base.
     * @param object le critère à créer
     * @return l'objet créé, ou null en cas d'erreur
     */
    @Override
    public EvaluationCriteria create(EvaluationCriteria object) {
        String query = "INSERT INTO CRITERES_EVALUATION (nom, description) " +
                "VALUES (?, ?)";

        String getQuery = "SELECT * FROM CRITERES_EVALUATION WHERE nom = ?";

        try (
             PreparedStatement insertStmt = connection.prepareStatement(query);
             PreparedStatement selectStmt = connection.prepareStatement(getQuery)
        ){
            insertStmt.setString(1, object.getName());
            insertStmt.setString(2, object.getDescription());
            insertStmt.executeUpdate();

            connection.commit();
            selectStmt.setString(1, object.getName());
            ResultSet rs = selectStmt.executeQuery();
            if (rs.next()) {
                return new EvaluationCriteria(
                        rs.getInt("numero"),
                        rs.getString("nom"),
                        rs.getString("description"));
            }
            resetCache();
            return null;
        } catch (SQLException ex) {
            if (ex.getErrorCode() == 1) {
                logger.error("Le nom '{}' existe déjà (contrainte unique)", object.getName());
            } else {
                logger.error("SQLException: {}", ex.getMessage());
            }
            return null;
        }
    }

    /**
     * Met à jour un critère existant.
     * @param object le critère avec ses nouvelles valeurs
     * @return true si la mise à jour a réussi, false sinon
     */
    @Override
    public boolean update(EvaluationCriteria object) {
        String query = "UPDATE CRITERES_EVALUATION SET nom = ?, description = ? WHERE numero = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setString(1, object.getName());
            stmt.setString(2, object.getDescription());
            stmt.setInt(3, object.getId());
            stmt.executeUpdate();
            removeFromCache(object.getId());
            connection.commit();
            return stmt.getUpdateCount() == 1;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Supprime un critère de la base en fonction de l'objet.
     * @param object le critère à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean delete(EvaluationCriteria object) {
        String query = "DELETE FROM CRITERES_EVALUATION WHERE numero = ?";
        try {
            PreparedStatement stmt = this.connection.prepareStatement(query);
            stmt.setInt(1, object.getId());
            stmt.executeUpdate();
            removeFromCache(object.getId());
            connection.commit();
            return true;
        } catch (SQLException ex) {
            logger.error("SQLException: {}", ex.getMessage());
        }
        return false;
    }

    /**
     * Supprime un critère d'évaluation via son identifiant.
     * @param id identifiant du critère
     * @return true si la suppression a réussi, false sinon
     */
    @Override
    public boolean deleteById(int id) {
        return delete(findById(id));
    }

    /**
     * Retourne la requête permettant de récupérer la prochaine valeur de la séquence.
     * @return requête SQL
     */
    @Override
    protected String getSequenceQuery() {
        return "SELECT SEQ_CRITERES_EVALUATION.NEXTVAL FROM dual";
    }

    /**
     * Retourne la requête SQL pour vérifier l'existence d'un critère.
     *
     * @return requête SQL
     */
    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM CRITERES_EVALUATION WHERE numero = ?";
    }

    /**
     * Retourne la requête SQL permettant de compter le nombre total de critères.
     * @return requête SQL
     */
    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM CRITERES_EVALUATION";
    }
}

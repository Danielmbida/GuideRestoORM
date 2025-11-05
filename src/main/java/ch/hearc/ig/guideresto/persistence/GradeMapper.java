package ch.hearc.ig.guideresto.persistence;

import ch.hearc.ig.guideresto.business.Grade;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mapper pour la gestion des objets Grade en base de données (table NOTES).
 */
public class GradeMapper extends AbstractMapper<Grade> {

    private final Connection connection;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final EvaluationCriteriaMapper evaluationCriteriaMapper;

    public GradeMapper(Connection connection) {
        this.connection = connection;
        this.completeEvaluationMapper = new CompleteEvaluationMapper(connection);
        this.evaluationCriteriaMapper = new EvaluationCriteriaMapper(connection);
    }

    /**
     * Recherche une note par son identifiant (numero).
     *
     * @param id L'identifiant unique du grade.
     * @return L'objet Grade correspondant, ou null si non trouvé.
     */
    @Override
    public Grade findById(int id) {
        if (cache.containsKey(id)) {
            return cache.get(id);
        }
        String query = "SELECT * FROM NOTES WHERE numero = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    Grade grade = new Grade(
                            rs.getInt("numero"),
                            rs.getInt("note"),
                            completeEvaluationMapper.findById(rs.getInt("fk_comm")),
                            evaluationCriteriaMapper.findById(rs.getInt("fk_crit"))
                    );
                    addToCache(grade);
                    return grade;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de la note avec ID {} : {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Récupère toutes les notes en base de données.
     *
     * @return Un Set contenant tous les Grade.
     */
    @Override
    public Set<Grade> findAll() {
        String query = "SELECT * FROM NOTES";
        Set<Grade> grades = new LinkedHashSet<>();

        try (PreparedStatement stmt = connection.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("numero");
                Grade grade = cache.get(id);

                if (grade == null) {
                    grade = new Grade(
                            id,
                            rs.getInt("note"),
                            completeEvaluationMapper.findById(rs.getInt("fk_comm")),
                            evaluationCriteriaMapper.findById(rs.getInt("fk_crit"))
                    );
                    addToCache(grade);
                }
                grades.add(grade);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération des notes : {}", e.getMessage());
            throw new RuntimeException(e);
        }
        return grades;
    }

    /**
     * Insère une nouvelle note dans la table NOTES.
     *
     * @param object La note à insérer.
     * @return La note insérée avec son ID.
     */
    @Override
    public Grade create(Grade object) {
        Integer generatedId = getSequenceValue();
        object.setId(generatedId);

        String query = "INSERT INTO NOTES (numero, note, fk_comm, fk_crit) VALUES (?, ?, ?, ?)";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, object.getId());
            stmt.setInt(2, object.getGrade());
            stmt.setInt(3, object.getEvaluation().getId());
            stmt.setInt(4, object.getCriteria().getId());
            int affectedRows = stmt.executeUpdate();
            if (affectedRows == 0) {
                throw new SQLException("Aucune note insérée.");
            }
            resetCache();
            connection.commit();
            return object;
        } catch (SQLException e) {
            logger.error("Erreur lors de la création de la note : {}", e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Met à jour une note existante.
     *
     * @param object La note à mettre à jour.
     * @return true si la MAJ a réussi, false sinon.
     */
    @Override
    public boolean update(Grade object) {
        String query = "UPDATE NOTES SET note = ?, fk_comm = ?, fk_crit = ? WHERE numero = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, object.getGrade());
            stmt.setInt(2, object.getEvaluation().getId());
            stmt.setInt(3, object.getCriteria().getId());
            stmt.setInt(4, object.getId());
            int rows = stmt.executeUpdate();
            removeFromCache(object.getId());
            connection.commit();
            return rows == 1;
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de la note avec ID {} : {}", object.getId(), e.getMessage());
            throw new RuntimeException(e);
        }
    }

    /**
     * Supprime une note à partir de l'objet.
     *
     * @param object La note à supprimer.
     * @return true si suppression OK, false sinon.
     */
    @Override
    public boolean delete(Grade object) {
        return deleteById(object.getId());
    }

     /**
     * Supprime une note à partir de l'ID.
     *
     * @param id L'ID de la note à supprimer.
     * @return true si suppression OK, false sinon.
     */
    @Override
    public boolean deleteById(int id) {
        String query = "DELETE FROM NOTES WHERE numero = ?";
        try (PreparedStatement stmt = connection.prepareStatement(query)) {
            stmt.setInt(1, id);
            int rows = stmt.executeUpdate();
            removeFromCache(id);
            connection.commit();
            return rows == 1;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de la note avec ID {} : {}", id, e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected String getSequenceQuery() {
        // Exemple : "SELECT SEQ_NOTES.NEXTVAL FROM DUAL" si tu utilises une séquence Oracle
        return "SELECT SEQ_NOTES.NEXTVAL FROM DUAL";
    }

    @Override
    protected String getExistsQuery() {
        return "SELECT 1 FROM NOTES WHERE numero = ?";
    }

    @Override
    protected String getCountQuery() {
        return "SELECT COUNT(*) FROM NOTES";
    }
}

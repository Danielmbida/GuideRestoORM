package ch.hearc.ig.guideresto.business;

/**
 * @author cedric.baudet
 */

import jakarta.persistence.*;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

// NamedQuery : récupère l'ensemble des CompleteEvaluation pour un restaurant donné
// Utilisation :
//   - Nom : "CompleteEvaluation.getCompleteEvaluationsOfARestaurant"
//   - Paramètre attendu : :restaurantId (Integer) -> identifiant métier du restaurant
//   - Retour : ensemble/liste de CompleteEvaluation avec leurs grades associés (FETCH JOIN)
//   - Remarque : DISTINCT est utilisé pour éviter les doublons causés par le JOIN sur la collection grades.
//   - Ordre : tri décroissant par date de visite (visitDate) pour obtenir d'abord les évaluations les plus récentes.
@NamedQuery(
        name = "CompleteEvaluation.getCompleteEvaluationsOfARestaurant",
        query = "SELECT DISTINCT ce FROM CompleteEvaluation ce " +
                "INNER JOIN FETCH ce.grades " +
                "WHERE ce.restaurant.id = :restaurantId " +
                "ORDER BY ce.visitDate DESC"
)
@Entity
@Table(name = "COMMENTAIRES")
public class CompleteEvaluation extends Evaluation {

    @Lob
    @Column(name = "commentaire", nullable = false)
    private String comment;
    @Column(name="nom_utilisateur",nullable = false, length = 100)
    private String username;
    @OneToMany(mappedBy = "evaluation",cascade = CascadeType.ALL
            )
    private Set<Grade> grades;

    @Version
    private int version;

    public CompleteEvaluation() {
        this(null, null, null, null);
    }

    public CompleteEvaluation(Date visitDate, Restaurant restaurant, String comment, String username) {
        this(null, visitDate, restaurant, comment, username);
    }

    public CompleteEvaluation(Integer id, Date visitDate, Restaurant restaurant, String comment, String username) {
        super(id, visitDate, restaurant);
        this.comment = comment;
        this.username = username;
        this.grades = new HashSet();
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Set<Grade> getGrades() {
        return grades;
    }

    public void setGrades(Set<Grade> grades) {
        this.grades = grades;
    }
}
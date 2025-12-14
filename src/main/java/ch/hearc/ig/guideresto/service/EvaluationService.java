package ch.hearc.ig.guideresto.service;

import ch.hearc.ig.guideresto.business.*;
import ch.hearc.ig.guideresto.persistence.BasicEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.CompleteEvaluationMapper;
import ch.hearc.ig.guideresto.persistence.EvaluationCriteriaMapper;
import ch.hearc.ig.guideresto.persistence.GradeMapper;

import java.util.Date;
import java.util.Set;

/**
 * Service métier pour la gestion des évaluations (simples et complètes) des restaurants.
 * <p>
 * Singleton qui orchestre les opérations via les mappers JPA.
 */
public class EvaluationService extends AbstractService {
    // Instance unique (pattern Singleton)
    private static EvaluationService evaluationService = null;

    private final EvaluationCriteriaMapper criteriaMapper;
    private final BasicEvaluationMapper basicEvaluationMapper;
    private final CompleteEvaluationMapper completeEvaluationMapper;
    private final GradeMapper gradeMapper;

    /**
     * Constructeur privé : initialisation de l'EntityManager et des mappers.
     */
    private EvaluationService() {
        super();
        this.criteriaMapper = new EvaluationCriteriaMapper(this.entityManager);
        this.basicEvaluationMapper = new BasicEvaluationMapper(this.entityManager);
        this.completeEvaluationMapper = new CompleteEvaluationMapper(this.entityManager);
        this.gradeMapper = new GradeMapper(this.entityManager);
    }

    /**
     * Retourne l'ensemble des critères d'évaluation disponibles.
     *
     * @return un Set contenant toutes les {@link EvaluationCriteria} présentes en base (vide si aucune)
     */
    public Set<EvaluationCriteria> getAllEvaluationCriterias() {
        return criteriaMapper.findAll();
    }

    /**
     * Accès global à l'instance unique du service (Singleton).
     *
     * @return instance unique de {@link EvaluationService}
     */
    public static EvaluationService getInstance() {
        if (evaluationService == null) {
            evaluationService = new EvaluationService();
        }
        return evaluationService;
    }

    /**
     * Compte le nombre de "likes" associés à un restaurant donné.
     *
     * @param restaurant Le restaurant cible.
     * @return Le nombre total de likes pour ce restaurant.
     */
    public int getRestaurantAmountLikes(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) return 0;
        return basicEvaluationMapper.getLikesCountForRestaurant(restaurant.getId());
    }

    /**
     * Compte le nombre de "dislikes" associés à un restaurant donné.
     *
     * @param restaurant Le restaurant cible.
     * @return Le nombre total de dislikes pour ce restaurant.
     */
    public int getRestaurantAmountDislikes(Restaurant restaurant) {
        if (restaurant == null || restaurant.getId() == null) return 0;
        return basicEvaluationMapper.getDislikesCountForRestaurant(restaurant.getId());
    }

    /**
     * Récupère toutes les évaluations complètes (commentaires + notes) d'un restaurant.
     *
     * @param restaurant Le restaurant cible.
     * @return Un ensemble d'objets {@link CompleteEvaluation}.
     */
    public Set<CompleteEvaluation> getCompleteEvaluationsOfARestaurant(Restaurant restaurant){
        if (restaurant == null || restaurant.getId() == null) return Set.of();
        return completeEvaluationMapper.findByRestaurantId(restaurant.getId());
    }

    /**
     * Récupère toutes les évaluations simples (likes/dislikes) d'un restaurant.
     *
     * @param restaurant Le restaurant cible.
     * @return Un ensemble d'objets {@link BasicEvaluation}.
     */
    public Set<BasicEvaluation> getBasicEvaluationsOfARestaurant(Restaurant restaurant){
        if (restaurant == null || restaurant.getId() == null) return Set.of();
        return basicEvaluationMapper.findByRestaurantId(restaurant.getId());
    }

    /**
     * Ajoute un "like" pour un restaurant donné, depuis une adresse IP donnée.
     *
     * @param restaurant Le restaurant liké.
     * @param ipAddress  Adresse IP de l'utilisateur (clé métier utilisée côté mapper pour la relecture).
     */
    public void likeRestaurant(Restaurant restaurant, String ipAddress){
        BasicEvaluation basicEvaluation = new BasicEvaluation();
        basicEvaluation.setRestaurant(restaurant);
        basicEvaluation.setLikeRestaurant(true);
        basicEvaluation.setVisitDate(new Date());
        basicEvaluation.setIpAddress(ipAddress);

        // Utilisation du helper transactionnel d'AbstractService qui délègue à JpaUtils
        inJpaUtilsTransaction(em -> basicEvaluationMapper.create(basicEvaluation));
    }

    /**
     * Ajoute un "dislike" pour un restaurant donné, depuis une adresse IP donnée.
     *
     * @param restaurant Le restaurant disliké.
     * @param ipAddress  Adresse IP de l'utilisateur.
     */
    public void dislikeRestaurant(Restaurant restaurant, String ipAddress){
        BasicEvaluation basicEvaluation = new BasicEvaluation();
        basicEvaluation.setRestaurant(restaurant);
        basicEvaluation.setLikeRestaurant(false);
        basicEvaluation.setVisitDate(new Date());
        basicEvaluation.setIpAddress(ipAddress);

        inJpaUtilsTransaction(em -> basicEvaluationMapper.create(basicEvaluation));
    }

    /**
     * Ajoute une évaluation complète (commentaire + notes) pour un restaurant donné.
     *
     * @param restaurant Le restaurant évalué.
     * @param comment    Commentaire utilisateur.
     * @param username   Nom d’utilisateur affiché.
     * @param grades     Ensemble de notes (catégories/critères).
     */
    public void addEvaluation(Restaurant restaurant, String comment, String username, Set<Grade> grades){
        CompleteEvaluation completeEvaluation = new CompleteEvaluation();
        completeEvaluation.setRestaurant(restaurant);
        completeEvaluation.setVisitDate(new Date());
        completeEvaluation.setComment(comment);
        completeEvaluation.setUsername(username);

        inJpaUtilsTransaction(em -> {
            completeEvaluationMapper.create(completeEvaluation);
            for (Grade grade : grades) {
                grade.setEvaluation(completeEvaluation);
                gradeMapper.create(grade);
            }
            completeEvaluation.setGrades(grades);
        });
    }
}

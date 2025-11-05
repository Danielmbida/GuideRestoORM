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
 * Singleton qui orchestre les opérations via {@link BasicEvaluationMapper} et {@link CompleteEvaluationMapper}.
 * Hérite d'{@link AbstractService} pour la connexion JDBC.
 */
public class EvaluationService extends AbstractService {
    // Instance unique (pattern Singleton)
    private static EvaluationService evaluationService = null;

    // Mappers de persistance
    private BasicEvaluationMapper basicEvaluationMapper;
    private CompleteEvaluationMapper completeEvaluationMapper;
    private EvaluationCriteriaMapper evaluationCriteriaMapper;
    private GradeMapper gradeMapper;

    /**
     * Constructeur privé : initialisation des mappers avec la connexion héritée.
     */
    private EvaluationService()
    {
        super();
        // Initialisation des mappers de persistance
        this.basicEvaluationMapper = new BasicEvaluationMapper(connection);
        this.completeEvaluationMapper = new CompleteEvaluationMapper(connection);
        this.evaluationCriteriaMapper = new EvaluationCriteriaMapper(connection);
        this.gradeMapper = new GradeMapper(connection);
    }

    public Set<EvaluationCriteria> getAllEvaluationCriterias() {
        return evaluationCriteriaMapper.findAll();
    }

    /**
     * Accès global à l'instance unique du service (Singleton).
     *
     * @return instance unique de {@link EvaluationService}
     */
    public static EvaluationService getInstance()
    {
        if(evaluationService == null)
        {
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
        // Récupère toutes les évaluations simples liées au restaurant
        Set<BasicEvaluation> evaluations = this.basicEvaluationMapper.findByRestaurantId(restaurant.getId());

        int likeCount = 0;

        // Parcourt toutes les évaluations du restaurant
        for (BasicEvaluation evaluation : evaluations) {
            // Incrémente si l'appréciation est positive
            if (evaluation.getLikeRestaurant()) {
                likeCount++;
            }
        }

        return likeCount;
    }

    /**
     * Compte le nombre de "dislikes" associés à un restaurant donné.
     *
     * @param restaurant Le restaurant cible.
     * @return Le nombre total de dislikes pour ce restaurant.
     */
    public int getRestaurantAmountDislikes(Restaurant restaurant) {
        // Récupère toutes les évaluations simples liées au restaurant
        Set<BasicEvaluation> evaluations = this.basicEvaluationMapper.findByRestaurantId(restaurant.getId());

        int dislikeCount = 0;

        // Parcourt toutes les évaluations du restaurant
        for (BasicEvaluation evaluation : evaluations) {
            // Incrémente si l'appréciation est négative
            if (!evaluation.getLikeRestaurant()) {
                dislikeCount++;
            }
        }

        return dislikeCount;
    }

    /**
     * Récupère toutes les évaluations complètes (commentaires + notes) d’un restaurant.
     *
     * @param restaurant Le restaurant cible.
     * @return Un ensemble d’objets {@link CompleteEvaluation}.
     */
    public Set<CompleteEvaluation> getCompleteEvaluationsOfARestaurant(Restaurant restaurant){
        return this.completeEvaluationMapper.findByRestaurantId(restaurant.getId());
    }

    /**
     * Récupère toutes les évaluations simples (likes/dislikes) d’un restaurant.
     *
     * @param restaurant Le restaurant cible.
     * @return Un ensemble d’objets {@link BasicEvaluation}.
     */
    public Set<BasicEvaluation> getBasicEvaluationsOfARestaurant(Restaurant restaurant){
        return this.basicEvaluationMapper.findByRestaurantId(restaurant.getId());
    }

    /**
     * Ajoute un "like" pour un restaurant donné, depuis une adresse IP donnée.
     *
     * @param restaurant Le restaurant liké.
     * @param ipAddress  Adresse IP de l’utilisateur (clé métier utilisée côté mapper pour la relecture).
     */
    public void likeRestaurant(Restaurant restaurant, String ipAddress){
        BasicEvaluation basicEvaluation = new BasicEvaluation();
        basicEvaluation.setRestaurant(restaurant);
        basicEvaluation.setLikeRestaurant(true);
        // Date de visite = maintenant (java.util.Date)
        basicEvaluation.setVisitDate(new Date());
        basicEvaluation.setIpAddress(ipAddress);
        // Persistance
        this.basicEvaluationMapper.create(basicEvaluation);
    }

    /**
     * Ajoute un "dislike" pour un restaurant donné, depuis une adresse IP donnée.
     *
     * @param restaurant Le restaurant disliké.
     * @param ipAddress  Adresse IP de l’utilisateur.
     */
    public void dislikeRestaurant(Restaurant restaurant, String ipAddress){
        BasicEvaluation basicEvaluation = new BasicEvaluation();
        basicEvaluation.setRestaurant(restaurant);
        basicEvaluation.setLikeRestaurant(false);
        // Date de visite = maintenant (java.util.Date)
        basicEvaluation.setVisitDate(new Date());
        basicEvaluation.setIpAddress(ipAddress);
        // Persistance
        this.basicEvaluationMapper.create(basicEvaluation);
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
        // Date de visite = maintenant (java.util.Date)
        completeEvaluation.setVisitDate(new Date());
        completeEvaluation.setComment(comment);
        completeEvaluation.setUsername(username);
        completeEvaluation.setGrades(grades);

        System.out.println(completeEvaluation.getId());

        // Persistance
        this.completeEvaluationMapper.create(completeEvaluation);
        for (Grade grade : completeEvaluation.getGrades()) {
            grade.setEvaluation(completeEvaluation);
            gradeMapper.create(grade);
        }
    }
}

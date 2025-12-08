package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
/**
 * Recherche par id : NamedQuery ajoutée.
 */


@NamedQuery(
        name = "BasicEvaluation.getRestaurantAmountDislikes",
        query = "SELECT COUNT(be) FROM BasicEvaluation be WHERE be.restaurant.id = :restaurantId AND be.likeRestaurant = false"
)
@NamedQuery(
        name = "BasicEvaluation.getRestaurantAmountLikes",
        query = "SELECT COUNT(be) FROM BasicEvaluation be WHERE be.restaurant.id = :restaurantId AND be.likeRestaurant = true"
)
@Entity
@Table(name = "LIKES")
public class BasicEvaluation extends Evaluation {

    @Column(name = "appreciation", nullable = false, length = 1)
    @Convert(converter = BooleanToCharConverter.class)
    private Boolean likeRestaurant;
    @Column(name = "adresse_ip", length = 100, nullable = false)
    private String ipAddress;

    public BasicEvaluation() {
        this(null, null, null, null);
    }

    public BasicEvaluation(Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        this(null, visitDate, restaurant, likeRestaurant, ipAddress);
    }

    public BasicEvaluation(Integer id, Date visitDate, Restaurant restaurant, Boolean likeRestaurant, String ipAddress) {
        super(id, visitDate, restaurant);
        this.likeRestaurant = likeRestaurant;
        this.ipAddress = ipAddress;
    }

    public Boolean getLikeRestaurant() {
        return likeRestaurant;
    }

    public void setLikeRestaurant(Boolean likeRestaurant) {
        this.likeRestaurant = likeRestaurant;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

}
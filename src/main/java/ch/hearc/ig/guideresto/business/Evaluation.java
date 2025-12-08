package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.Date;

/**
 * @author cedric.baudet
 */
@NamedQuery(
        name = "Evaluation.findById",
        query = "SELECT b FROM Evaluation b WHERE b.id = :id"
)
@NamedQuery(
        name = "Evaluation.findAll",
        query = "SELECT b FROM Evaluation b"
)
@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
public abstract class Evaluation implements IBusinessObject {

    @Id
    @GeneratedValue(
            strategy=GenerationType.SEQUENCE,
            generator="SEQ_EVAL")
    @SequenceGenerator(name="SEQ_EVAL",
            sequenceName="SEQ_EVAL",
            initialValue=1, allocationSize=1)
    @Column(name="NUMERO")
    private Integer id;
    @Temporal(TemporalType.DATE)
    @Column(name="date_eval", nullable=false)
    private Date visitDate;
    @ManyToOne
    @JoinColumn(name="fk_rest", nullable=false)
    private Restaurant restaurant;

    public Evaluation() {
        this(null, null, null);
    }

    public Evaluation(Integer id, Date visitDate, Restaurant restaurant) {
        this.id = id;
        this.visitDate = visitDate;
        this.restaurant = restaurant;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Date getVisitDate() {
        return visitDate;
    }

    public void setVisitDate(Date visitDate) {
        this.visitDate = visitDate;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

}
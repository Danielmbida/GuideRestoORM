package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;
import org.apache.commons.collections4.CollectionUtils;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@NamedQuery(
        name = "Restaurant.findAll",
        query = "SELECT r FROM Restaurant r"
)
@NamedQuery(
        name = "Restaurant.findById",
        query = "SELECT r FROM Restaurant r WHERE r.id = :id"
)
@NamedQuery(
        name = "Restaurant.getRestaurantsByName",
        query = "SELECT r FROM Restaurant r WHERE upper(r.name) = upper(:name)"
)
@NamedQuery(
        name="Restaurant.getRestaurantByNameLike",
        query="SELECT r FROM Restaurant r WHERE upper(r.name) LIKE upper(:namePattern)"
)
@NamedQuery(
        name = "Restaurant.getRestaurantByCityNameLike",
        query = "SELECT r FROM Restaurant r \n" +
                "WHERE UPPER(r.address.city.cityName) LIKE UPPER(CONCAT(CONCAT('%', :cityName), '%'))\n"
)
@NamedQuery(
        name="Restaurant.getRestaurantsByTypeLabel",
        query = "SELECT r FROM Restaurant r WHERE upper(r.type.label) = upper(:typeLabel)"
)
@Entity
@Table(name = "RESTAURANTS")
public class Restaurant implements IBusinessObject {

    @Id
    @GeneratedValue(
            strategy=GenerationType.SEQUENCE,
            generator="SEQ_RESTAURANTS")
    @SequenceGenerator(name="SEQ_RESTAURANTS",
            sequenceName="SEQ_RESTAURANTS",
            initialValue=1, allocationSize=1)
    @Column(name= "numero")
    private Integer id;
    @Column(name = "nom", nullable = false, length = 100)
    private String name;
    @Lob
    @Column(name = "description")
    private String description;
    @Column (name = "site_web",length = 100)
    private String website;
    @OneToMany(mappedBy = "restaurant")
    private Set<Evaluation> evaluations;
    @Embedded
    private Localisation address;
    @ManyToOne
    @JoinColumn(name="fk_type")
    private RestaurantType type;

    @Version
    private int version;

    public Restaurant() {
        this(null, null, null, null, null, null, null);
    }

    public Restaurant(Integer id, String name, String description, String website, String street, City city, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.evaluations = new HashSet();
        this.address = new Localisation(street, city);
        this.type = type;
    }

    public Restaurant(Integer id, String name, String description, String website, Localisation address, RestaurantType type) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.website = website;
        this.evaluations = new HashSet();
        this.address = address;
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Set<Evaluation> getEvaluations() {
        return evaluations;
    }

    public void setEvaluations(Set<Evaluation> evaluations) {
        this.evaluations = evaluations;
    }

    public Localisation getAddress() {
        return address;
    }

    public void setAddress(Localisation address) {
        this.address = address;
    }

    public RestaurantType getType() {
        return type;
    }

    public void setType(RestaurantType type) {
        this.type = type;
    }

    public boolean hasEvaluations() {
        return CollectionUtils.isNotEmpty(evaluations);
    }
}
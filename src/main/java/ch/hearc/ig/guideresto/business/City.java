package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * @author cedric.baudet
 */
@NamedQuery(
        name = "City.findAll",
        query = "SELECT c FROM City c"
)
@NamedQuery(
        name = "City.findById",
        query = "SELECT c FROM City c WHERE c.id = :id"
)
@NamedQuery(
        name = "City.findByName",
        query = "SELECT c FROM City c WHERE upper(c.cityName) = upper(:cityName)"
)
@NamedQuery(
        name = "City.findByZipCode",
        query = "SELECT c FROM City c WHERE c.zipCode= :zipCode"
)
@Entity
@Table(name = "VILLES")
public class City implements IBusinessObject {
    @Id
    @GeneratedValue(
            strategy=GenerationType.SEQUENCE,
            generator="SEQ_VILLES")
    @SequenceGenerator(name="SEQ_VILLES",
            sequenceName="SEQ_VILLES",
            initialValue=1, allocationSize=1)
    @Column(name="NUMERO")
    private Integer id;
    @Column(name="code_postal",nullable = false, length = 100)
    private String zipCode;
    @Column(name="nom_ville",nullable = false, length = 100)
    private String cityName;
    @OneToMany
    @JoinColumn(name = "fk_vill")
    private Set<Restaurant> restaurants;

    @Version
    private int version;

    public City() {
        this(null, null);
    }

    public City(String zipCode, String cityName) {
        this(null, zipCode, cityName);
    }

    public City(Integer id, String zipCode, String cityName) {
        this.id = id;
        this.zipCode = zipCode;
        this.cityName = cityName;
        this.restaurants = new HashSet<>();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String city) {
        this.cityName = city;
    }

    public Set<Restaurant> getRestaurants() {
        return restaurants;
    }

    public void setRestaurants(Set<Restaurant> restaurants) {
        this.restaurants = restaurants;
    }

}

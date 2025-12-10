package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * @author cedric.baudet
 */
@NamedQueries({
        @NamedQuery(
                name = "EvaluationCriteria.findAll",
                query = "SELECT ec FROM EvaluationCriteria ec"
        ),
        @NamedQuery(
                name = "EvaluationCriteria.findById",
                query = "SELECT ec FROM EvaluationCriteria ec WHERE ec.id = :id"
        ),
        @NamedQuery(
                name = "EvaluationCriteria.findByName",
                query = "SELECT ec FROM EvaluationCriteria ec WHERE upper(ec.name) = upper(:name)"
        )
})
@Entity
@Table(name = "CRITERES_EVALUATION")
public class EvaluationCriteria implements IBusinessObject {
    @Id
    @GeneratedValue(
            strategy=GenerationType.SEQUENCE,
            generator="SEQ_CRITERES_EVALUATION")
    @SequenceGenerator(name="SEQ_CRITERES_EVALUATION",
            sequenceName="SEQ_CRITERES_EVALUATION",
            initialValue=1, allocationSize=1)
    @Column(name="numero")
    private Integer id;
    @Column(name="nom",nullable=false,unique=true,length=100)
    private String name;
    @Column(name="description",length = 512)
    private String description;

    @Version
    private int version;

    public EvaluationCriteria() {
        this(null, null);
    }

    public EvaluationCriteria(String name, String description) {
        this(null, name, description);
    }

    public EvaluationCriteria(Integer id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
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
}
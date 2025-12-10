package ch.hearc.ig.guideresto.business;

import jakarta.persistence.*;

/**
 * @author cedric.baudet
 */
@NamedQuery(
        name = "Grade.findAll",
        query = "SELECT gr FROM Grade gr"
)
@NamedQuery(
        name = "Grade.findById",
        query = "SELECT gr FROM Grade gr WHERE gr.id = :id"
)
@Entity
@Table(name = "NOTES")
public class Grade implements IBusinessObject {
    @Id
    @GeneratedValue(
            strategy=GenerationType.SEQUENCE,
            generator="SEQ_NOTES")
    @SequenceGenerator(name="SEQ_NOTES",
            sequenceName="SEQ_NOTES",
            initialValue=1, allocationSize=1)
    @Column(name="NUMERO")
    private Integer id;
    @Column(name="note", nullable=false,precision= 3)
    private Integer grade;
    @ManyToOne
    @JoinColumn(name="fk_comm", nullable=false)
    private CompleteEvaluation evaluation;
    @ManyToOne
    @JoinColumn(name="fk_crit", nullable=false)
    private EvaluationCriteria criteria;

    @Version
    private int version;

    public Grade() {
        this(null, null, null);
    }

    public Grade(Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this(null, grade, evaluation, criteria);
    }

    public Grade(Integer id, Integer grade, CompleteEvaluation evaluation, EvaluationCriteria criteria) {
        this.id = id;
        this.grade = grade;
        this.evaluation = evaluation;
        this.criteria = criteria;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGrade() {
        return grade;
    }

    public void setGrade(Integer grade) {
        this.grade = grade;
    }

    public CompleteEvaluation getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(CompleteEvaluation evaluation) {
        this.evaluation = evaluation;
    }

    public EvaluationCriteria getCriteria() {
        return criteria;
    }

    public void setCriteria(EvaluationCriteria criteria) {
        this.criteria = criteria;
    }


}
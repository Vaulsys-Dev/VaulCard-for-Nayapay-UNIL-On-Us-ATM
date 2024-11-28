package vaulsys.cms.base;

import vaulsys.persistence.BaseEntity;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;

import javax.persistence.*;

/**
 * Created by Raza on 27-Jan-19.
 */
@Entity
@Table(name = "CMS_SECUR_QUESTIONS")
@PrimaryKeyJoinColumn(name = "ID")
public class CMSSecurQuestions extends BaseEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator="CMS_SECUR_QUEST_ID_SEQ-gen")
    @GenericGenerator(name = "CMS_SECUR_QUEST_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "increment_size", value = "1"),
                    @Parameter(name = "sequence_name", value = "CMS_SECUR_QUEST_ID_SEQ")
            })
    private Long id;

    @Column(name = "QUESTION")
    private String question;

    @Column(name = "DESCRIPTION")
    private String description;


    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public CMSSecurQuestions(String Question, String Description)
    {
        this.question = Question;
        this.description = Description;
    }

    public CMSSecurQuestions(){}

}

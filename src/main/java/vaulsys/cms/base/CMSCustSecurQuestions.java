package vaulsys.cms.base;

import vaulsys.persistence.BaseEntity;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by Raza on 27-Jan-19.
 */

@Entity
@Table(name = "CMS_CUST_SECUR_QUESTIONS")
@PrimaryKeyJoinColumn(name = "ID")
public class CMSCustSecurQuestions extends BaseEntity<Long> { //Raza This Class should be ReadOnly, Not removing public Setter as this class is also used by UI

    @Id
    @GeneratedValue(generator = "CMS_CUST_SECUR_QUEST_ID_SEQ-gen")
    @GenericGenerator(name = "CMS_CUST_SECUR_QUEST_ID_SEQ-gen", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @Parameter(name = "optimizer", value = "pooled"),
                    @Parameter(name = "increment_size", value = "1"),
                    @Parameter(name = "sequence_name", value = "CMS_CUST_SECUR_QUEST_ID_SEQ")
            })
    private Long id;

    @Column(name = "QUESTION_ORDER")
    private Integer questionnumber; //This is the question number hierarchy like 1st security question, 2nd securtiy question etc.

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "SECUR_QUESTION")
    @Cascade(value = org.hibernate.annotations.CascadeType.ALL)
    @ForeignKey(name = "CMS_SECUR_QUEST_FK")
    private CMSSecurQuestions question;

    @Column(name = "ANSWER")
    private String answer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "CUSTOMER", nullable = true, updatable = true)
    @ForeignKey(name="CMS_SECUR_CUST_FK")
    @Index(name="idx_customer")
    private CMSCustomer customer;

    public CMSCustSecurQuestions() {}

    public CMSCustSecurQuestions(Integer QuestionNumber, CMSSecurQuestions Question, String Answer, CMSCustomer Customer)
    {
        this.questionnumber = QuestionNumber;
        this.question = Question;
        this.answer = Answer;
        this.customer = Customer;
    }

    @Override
    public Long getId() {
        return id;
    }

    @Override
    public void setId(Long id) {
        this.id = id;
    }

    public Integer getQuestionnumber() {
        return questionnumber;
    }

    public void setQuestionnumber(Integer questionnumber) {
        this.questionnumber = questionnumber;
    }

    public CMSSecurQuestions getQuestion() {
        return question;
    }

    public void setQuestion(CMSSecurQuestions question) {
        this.question = question;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public CMSCustomer getCustomer() {
        return customer;
    }

    public void setCustomer(CMSCustomer customer) {
        this.customer = customer;
    }
}

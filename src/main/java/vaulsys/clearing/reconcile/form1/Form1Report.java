package vaulsys.clearing.reconcile.form1;

import vaulsys.calendar.DateTime;
import vaulsys.persistence.IEntity;
import vaulsys.user.User;
import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: k.khodadi
 * Date: 6/28/14
 * Time: 12:31 PM
 * To change this template use File | Settings | File Templates.
 */

@Entity
@Table(name = "form_One_entity")
public class Form1Report implements IEntity<Long>{

    /*public Form1Report() {
    }*/

  /*  public Form1Report(User creatorUser, DateTime createdDateTime, Long numberTrxReconcile, boolean responseResult, String reponseResultStr) {
        this.creatorUser = creatorUser;
        this.createdDateTime = createdDateTime;
        this.numberTrxReconcile = numberTrxReconcile;
        this.responseResult = responseResult;
        this.reponseResultStr = reponseResultStr;
    }*/
	
	
	

    @Id
	@GeneratedValue(generator = "fine-seq-gen2")
	@SequenceGenerator(name = "fine-seq-gen2", allocationSize = 1, sequenceName = "fine_code_seq")
     protected  Long id;

    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "creator_user")
	@ForeignKey(name = "fine_user_fk")
	protected User creatorUser;

	@AttributeOverrides({
			@AttributeOverride(name = "dayDate.date", column = @Column(name = "created_date")),
			@AttributeOverride(name = "dayTime.dayTime", column = @Column(name = "created_time"))
	})

	protected DateTime createdDateTime;

	@Column(name = "number_trx_reconcile")
	protected Long numberTrxReconcile;

    @Column(name = "response_result")
	protected boolean responseResult;

    public String getReponseResultStr() {
        return reponseResultStr;
    }

    public void setReponseResultStr(String reponseResultStr) {
        this.reponseResultStr = reponseResultStr;
    }

    @Column(name = "response_result_str")
    protected String reponseResultStr;

    public User getCreatorUser() {
        return creatorUser;
    }

    public void setCreatorUser(User creatorUser) {
        this.creatorUser = creatorUser;
    }

    public DateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(DateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public Long getNumberTrxReconcile() {
        return numberTrxReconcile;
    }

    public void setNumberTrxReconcile(Long numberTrxReconcile) {
        this.numberTrxReconcile = numberTrxReconcile;
    }

    public boolean isResponseResult() {
        return responseResult;
    }

    public void setResponseResult(boolean responseResult) {
        this.responseResult = responseResult;
    }

    @Override
    public Long getId() {
        return id;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void setId(Long id) {
        //To change body of implemented methods use File | Settings | File Templates.
        this.id = id;
    }
}

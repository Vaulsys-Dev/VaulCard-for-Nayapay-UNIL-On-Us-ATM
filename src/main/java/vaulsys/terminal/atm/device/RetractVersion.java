package vaulsys.terminal.atm.device;

import org.hibernate.annotations.ForeignKey;

import javax.persistence.*;

/**
 * Created by IntelliJ IDEA.
 * User: Hamid Reza Khanmirza
 * Date: May 9, 2012
 * Time: 12:07:08 PM
 * To change this template use File | Settings | File Templates.
 */
@Entity
@DiscriminatorValue("RetractVersion")
public class RetractVersion extends  ATMDeviceVersion {    
    @ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent")
	@ForeignKey(name = "retract_vers_parent_fk")
	private Retract parent;

    protected Integer notes;

    public Retract getParent() {
        return parent;
    }

    public void setParent(Retract parent) {
        this.parent = parent;
    }

    public Integer getNotes() {
        return notes;
    }

    public void setNotes(Integer notes) {
        this.notes = notes;
    }
}

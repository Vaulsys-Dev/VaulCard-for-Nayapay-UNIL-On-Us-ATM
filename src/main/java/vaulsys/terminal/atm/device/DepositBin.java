package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("DepositBin")
public class DepositBin extends ATMDevice {

    protected int no;

    public DepositBin() {
    }

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public void add() {
        no++;
    }

}

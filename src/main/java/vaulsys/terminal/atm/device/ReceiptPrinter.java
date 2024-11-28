package vaulsys.terminal.atm.device;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

@Entity
@DiscriminatorValue("ReceiptPrinter")
public class ReceiptPrinter extends Printer {

    public ReceiptPrinter() {
    }

}
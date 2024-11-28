package vaulsys.clearing.components;

import vaulsys.clearing.base.Cutover;
import vaulsys.entity.impl.Institution;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;

public class EODComponent {
	
	private EODComponent() {}
	
    public static ISOMsg generateCutOverRqMessage(Institution institution) throws Exception {
//        Cutover cutoverBuilder = new Cutover();
        return Cutover.buildRq(institution);
    }
}

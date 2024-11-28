package vaulsys.security.hsm.payshield9000;


import vaulsys.security.hsm.payshield9000.PayShield9000Driver;
import vaulsys.security.ssm.base.BaseSMAdapter;

public class PayShieldSecurityModule extends BaseSMAdapter {
    PayShield9000Driver payShield9000Driver = PayShield9000Driver.getInstance();

}

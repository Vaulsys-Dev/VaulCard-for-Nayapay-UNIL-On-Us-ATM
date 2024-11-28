package vaulsys;

import vaulsys.entity.impl.Shop;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.impl.POSTerminal;
import org.hibernate.HibernateException;
import org.hibernate.engine.SessionImplementor;
import org.hibernate.id.enhanced.SequenceStyleGenerator;

import java.io.Serializable;

/**
 * Created by IntelliJ IDEA.
 * User: Sahar-hoseini-PC
 * Date: 7/11/12
 * Time: 2:18 PM
 * To change this template use File | Settings | File Templates.
 */
public class IdKeepingSequenceGenerator extends SequenceStyleGenerator {
    @Override
    public Serializable generate(SessionImplementor session, Object object)
            throws HibernateException {
        if (object instanceof Shop) {
            Shop persistent = (Shop) object;
            if (persistent.getId() != null && persistent.getId() > 0) {
                return persistent.getId();
            }
        }
        else if(object instanceof POSTerminal){
            POSTerminal persistent = (POSTerminal) object;
            if(persistent.getId() != null && persistent.getId() > 0){
                return persistent.getId();
            }

        }
        else if(object instanceof SecureKey){
            SecureKey persistent = (SecureKey) object;
            if(persistent.getId() != null && persistent.getId() >0 ){
                return persistent.getId();
            }

        }
        return super.generate(session, object);
    }
}
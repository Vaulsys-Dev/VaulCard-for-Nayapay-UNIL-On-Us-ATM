package vaulsys.exception.base;

import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;

public interface DecisionMakerException {
    public void alterIfxByErrorType(Ifx ifx);

    public boolean returnError();

    public void showCause(Ifx ifx);
    
    public void rollBack(Ifx ifx, Transaction transaction, Terminal terminal);
}

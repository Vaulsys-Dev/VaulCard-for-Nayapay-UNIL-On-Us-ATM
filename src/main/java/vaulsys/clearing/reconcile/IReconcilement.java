package vaulsys.clearing.reconcile;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

public interface IReconcilement {

    ProtocolMessage buildRequest(Terminal terminal) throws Exception;

    ProtocolMessage buildResponse(ProtocolMessage incomingMsg, Ifx ifx, Terminal terminal, ProcessContext processContext) throws Exception;

}

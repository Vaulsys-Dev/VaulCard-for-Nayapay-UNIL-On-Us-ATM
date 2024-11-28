package vaulsys.clearing.reconcile;

import vaulsys.calendar.DayDate;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.terminal.impl.Terminal;

import java.util.Map;

public interface IDataProcessor {

    Map<Integer, String> process(ProtocolMessage message, Terminal terminal, DayDate stlDate);

}

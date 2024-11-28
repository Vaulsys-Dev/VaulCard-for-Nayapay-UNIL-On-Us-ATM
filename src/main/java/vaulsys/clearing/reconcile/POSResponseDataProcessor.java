package vaulsys.clearing.reconcile;

import vaulsys.calendar.DayDate;
import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.terminal.impl.Terminal;

import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class POSResponseDataProcessor extends AbstractISODataProcessor {

    static Logger logger = Logger.getLogger(ISOResponseDataProcessor.class);

    public static final POSResponseDataProcessor Instance = new POSResponseDataProcessor();

    private POSResponseDataProcessor(){}

    public Map<Integer, String> process(ProtocolMessage message, Terminal terminal, DayDate stlDate) {
        Map<Integer, String> result = new HashMap<Integer, String>();
        result.put(66, "1");
        return result;
    }

}
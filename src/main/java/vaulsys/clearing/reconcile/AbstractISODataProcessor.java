package vaulsys.clearing.reconcile;

import vaulsys.calendar.DayDate;
import vaulsys.calendar.MonthDayDate;
import vaulsys.terminal.impl.Terminal;

public abstract class AbstractISODataProcessor extends AbstractDataProcessor implements IDataProcessor {

    protected ReconcilementInfo processFinancialData(Terminal terminal, DayDate date) {
    	MonthDayDate stlDate = new MonthDayDate(date);
		return getFinancialDataProcessor().processFinancialData(terminal, stlDate);
    }
    
    protected IFinancialProcessor getFinancialDataProcessor() {
		return ISOFinancialProcessor.Instance;
	}
		
}

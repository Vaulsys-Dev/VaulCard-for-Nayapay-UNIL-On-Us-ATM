package vaulsys.clearing.reconcile;

import vaulsys.calendar.MonthDayDate;
import vaulsys.terminal.impl.Terminal;


public interface IFinancialProcessor {

//    ReconcilementInfo processFinancialData(Collection<FinancialArchive> financialArchives);

    ReconcilementInfo processFinancialData(Terminal terminal, MonthDayDate stlDate);
}

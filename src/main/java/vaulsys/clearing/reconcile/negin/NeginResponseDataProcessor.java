package vaulsys.clearing.reconcile.negin;

import vaulsys.clearing.reconcile.IDataProcessor;
import vaulsys.clearing.reconcile.IFinancialProcessor;
import vaulsys.clearing.reconcile.ISOResponseDataProcessor;

public class NeginResponseDataProcessor extends ISOResponseDataProcessor {

	public static final IDataProcessor Instance = new NeginResponseDataProcessor();
	
	private NeginResponseDataProcessor(){}
	
	@Override
	protected IFinancialProcessor getFinancialDataProcessor() {
		return NeginFinancialProcessor.Instance;
	}
}

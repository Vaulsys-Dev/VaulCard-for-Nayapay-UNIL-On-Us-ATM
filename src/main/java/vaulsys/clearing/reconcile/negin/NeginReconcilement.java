package vaulsys.clearing.reconcile.negin;

import vaulsys.clearing.reconcile.ISOReconcilement;

public class NeginReconcilement extends ISOReconcilement {
	
	public static final NeginReconcilement Instance = new NeginReconcilement();
	
	private NeginReconcilement(){
		setResponseDataProcessor(NeginResponseDataProcessor.Instance);
	}

//	@Override
//	protected IDataProcessor getResponseDataProcessor() {
//		return (IDataProcessor) SwitchApplication.get().getBean("neginResponseDataProcessor");
//	}
}

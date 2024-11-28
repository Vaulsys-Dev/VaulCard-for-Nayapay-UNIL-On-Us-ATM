package vaulsys.clearing.reconcile;

import vaulsys.util.SwitchContext;

public abstract class AbstractReconcilement extends SwitchContext implements IReconcilement {

    protected IDataProcessor responseDataProcessor;
    protected IDataProcessor requestDataProcessor;

    public void setResponseDataProcessor(IDataProcessor responseDataProcessor) {
        this.responseDataProcessor = responseDataProcessor;
    }

    public void setRequestDataProcessor(IDataProcessor requestDataProcessor) {
        this.requestDataProcessor = requestDataProcessor;
    }

    protected IDataProcessor getResponseDataProcessor(){
    	return responseDataProcessor;
    }

    protected IDataProcessor getRequestDataProcessor(){
    	return requestDataProcessor;
    }

}

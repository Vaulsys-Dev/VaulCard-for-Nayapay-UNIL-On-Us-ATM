package vaulsys.base.components.handlers;

import vaulsys.util.SwitchContext;
import vaulsys.wfe.ProcessContext;


public abstract class BaseHandler extends SwitchContext{

	public abstract void execute(ProcessContext processContext) throws Exception;

    protected void leaveToEndState(ProcessContext processContext) {
        processContext.setNextState(ProcessContext.TO_ENDSTATE_TRANSITION);
    }

    protected void leaveNode(ProcessContext processContext, String transitionName) {
        processContext.setNextState(transitionName);
    }
}
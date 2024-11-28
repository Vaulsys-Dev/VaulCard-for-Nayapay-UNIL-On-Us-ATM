package vaulsys.wfe.base;

import vaulsys.message.Message;

public interface FlowDispatcher {
    public String dispatch(Message message) throws DispatcherException;
}

package vaulsys.exception.base;

import vaulsys.base.components.handlers.BaseHandler;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;

public abstract class BaseExceptionHandler extends BaseHandler {
    private static Logger logger = Logger.getLogger(BaseExceptionHandler.class);

    @Override
    public void execute(ProcessContext processContext) {

        List<Exception> exceptions = (List<Exception>) processContext.getExceptions();
        for (Exception ex : exceptions) {
            logger.error("BASE EXCEPTION HANDLER: " + ex.getMessage());
        }
    }

}

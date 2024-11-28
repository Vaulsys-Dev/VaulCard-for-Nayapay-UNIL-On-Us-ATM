package vaulsys.protocols.base;

import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.wfe.base.FlowDispatcher;


public abstract class Protocol {
    public static final String PROTOCOL = "Protocol";
    public static final String ID = "Id";
    public static final String NAME = "name";
    public static final String VERSION = "Version";
    public static final String PARSER = "Parser";

    protected int id;
    protected ProtocolFunctions mapper;
    private ProtocolSecurityFunctions securityFunctions;
    
    protected String name;
    protected ProtocolMessageValidator messageValidator;
    protected ProtocolDialog dialog;
    protected FlowDispatcher flowDispatcher;

    protected Protocol() {

    }

    protected Protocol(String name, ProtocolFunctions mapper, ProtocolSecurityFunctions securityFunctions,
                       ProtocolMessageValidator integrityCheck, ProtocolDialog dialog, FlowDispatcher flowDispatcher) {
        super();
        this.name = name;
        this.mapper = mapper;
        this.setSecurityFunctions(securityFunctions);
        this.messageValidator = integrityCheck;
        this.dialog = dialog;
        this.flowDispatcher = flowDispatcher;
    }

    /**
     * @return the id
     */
    public int getId() {
        return id;
    }

    /**
     * @param id the id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * @return the mapper
     */
    public ProtocolFunctions getMapper() {
        return mapper;
    }

    /**
     * @param mapper the mapper to set
     */
    public void setMapper(ProtocolFunctions mapper) {
        this.mapper = mapper;
    }

    /**
     * @return the dialog
     */
    public ProtocolDialog getDialog() {
        return dialog;
    }

    /**
     * @param dialog the dialog to set
     */
    public void setDialog(ProtocolDialog dialog) {
        this.dialog = dialog;
    }


    /**
     * @return the messageValidator
     */
    public ProtocolMessageValidator getMessageValidator() {
        return messageValidator;
    }

    /**
     * @param messageValidator the messageValidator to set
     */
    public void setMessageValidator(ProtocolMessageValidator messageValidator) {
        this.messageValidator = messageValidator;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public FlowDispatcher getFlowDispatcher() {
        return flowDispatcher;
    }

    public void setFlowDispatcher(FlowDispatcher flowDispatcher) {
        this.flowDispatcher = flowDispatcher;
    }

	public void setSecurityFunctions(ProtocolSecurityFunctions securityFunctions) {
		this.securityFunctions = securityFunctions;
	}

	public ProtocolSecurityFunctions getSecurityFunctions() {
		return securityFunctions;
	}

}
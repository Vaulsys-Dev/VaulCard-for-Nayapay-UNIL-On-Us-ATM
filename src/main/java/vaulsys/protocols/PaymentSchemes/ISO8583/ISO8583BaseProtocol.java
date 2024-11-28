package vaulsys.protocols.PaymentSchemes.ISO8583;

import vaulsys.config.ConfigurationManager;
import vaulsys.protocols.base.Protocol;
import vaulsys.protocols.base.ProtocolDialog;
import vaulsys.protocols.base.ProtocolFunctions;
import vaulsys.protocols.base.ProtocolMessageValidator;
import vaulsys.protocols.base.ProtocolSecurityFunctions;
import vaulsys.protocols.PaymentSchemes.ISO8583.packager.GenericPackager;
import vaulsys.wfe.base.FlowDispatcher;

import java.io.FileInputStream;

import org.apache.commons.configuration.Configuration;
import org.apache.log4j.Logger;

public abstract class ISO8583BaseProtocol extends Protocol {

	protected GenericPackager packager;
	protected transient Logger logger;

	protected ISO8583BaseProtocol(String name, ProtocolFunctions mapper, ProtocolSecurityFunctions securityFunctions,
			ProtocolMessageValidator integrityCheck, ProtocolDialog dialog, FlowDispatcher flowDispatcher) {
		super(name, mapper, securityFunctions, integrityCheck, dialog, flowDispatcher);

		//Configuration protocolCfg = ConfigurationManager.getInstance().getConfiguration("protocols");
		Configuration protocolCfg = ConfigurationManager.getInstance().getConfiguration("protocol");

		//System.out.println("ISO8583BaseProtocol:: DefinitionFile name [" + name + "]"); //Raza TEMP
		String definitionFile = protocolCfg.getString("/protocols/protocol[@name='" + name + "']/definition/@file");
		//System.out.println("ISO8583BaseProtocol:: DefinitionFile [" + definitionFile + "]"); //Raza TEMP


		try {
			if (definitionFile != null && !definitionFile.isEmpty()) {
				//System.out.println("ISO8583BaseProtocol:: definitionFile [" + definitionFile + "]"); //TEMP
				packager = new GenericPackager(this.getClass().getResourceAsStream(definitionFile));
			}
		} catch (Exception ex) {
			getLogger().error("Could not create Packager", ex);
		}

	}

	public GenericPackager getPackager() {
		return packager;
	}

	abstract protected Logger getLogger();


}

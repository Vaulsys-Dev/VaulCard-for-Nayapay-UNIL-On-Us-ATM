package vaulsys.protocols.base;


import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.ifx.imp.Ifx;

public interface ProtocolDialog {
    public Ifx refine(Ifx ifx);

    public ProtocolMessage refine(ProtocolMessage protocolMessage) throws Exception;

    public ProtocolMessage TranslateToFanap(ProtocolMessage protocolMessage) throws Exception; //Raza Adding for Field traslation

    public ProtocolMessage TranslateFromFanap(ProtocolMessage protocolMessage) throws Exception; //Raza Adding for Field traslation
}

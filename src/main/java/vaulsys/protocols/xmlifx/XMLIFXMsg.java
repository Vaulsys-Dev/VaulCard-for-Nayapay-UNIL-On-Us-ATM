package vaulsys.protocols.xmlifx;

import vaulsys.protocols.base.ProtocolMessage;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.protocols.PaymentSchemes.ISO8583.constants.ISOFinalMessageType;
import vaulsys.util.Util;

public class XMLIFXMsg extends Ifx implements Cloneable, ProtocolMessage{
	public String xml;

	public XMLIFXMsg(){
	}
	
	public XMLIFXMsg(Ifx ifx){
		if (ifx.getIfxDirection() != null)
			this.setIfxDirection(ifx.getIfxDirection().copy());

		this.setIfxType(ifx.getIfxType());
		this.setTrnType(ifx.getTrnType());

		if (ifx.getEMVRqData() != null)
			this.setEMVRqData(ifx.getEMVRqData().copy());

		if (ifx.getEMVRsData() != null)
			this.setEMVRsData(ifx.getEMVRsData().copy());

		if (ifx.getStatus() != null)
			this.setStatus(ifx.getStatus().copy());

		if (ifx.getNetworkTrnInfo() != null)
			this.setNetworkTrnInfo(ifx.getNetworkTrnInfo().copy());

//		if (ifx.getOrgRec() != null)
//			this.getNetworkTrnInfo().setOrgRec(ifx.getOrgRec().copy());

		this.setReceivedDt(ifx.getReceivedDt());

		if (ifx.getOriginalDataElements() != null)
			this.setOriginalDataElements(ifx.getOriginalDataElements().copy());

		if (ifx.getAtmSpecificData() != null)
			this.setAtmSpecificData(ifx.getAtmSpecificData().copy());

		if (ifx.getPosSpecificData() != null)
			this.setPosSpecificData(ifx.getPosSpecificData().copy());
		
		if (ifx.getChargeData() != null)
			this.setChargeData(ifx.getChargeData().copy());
		
		if (ifx.getLotteryData() != null)
			this.setLotteryData(ifx.getLotteryData().copy());

		if (ifx.getSettleDt() != null)
			this.setSettleDt(ifx.getSettleDt());

		if (ifx.getPostedDt() != null)
			this.setPostedDt(ifx.getPostedDt());

		if (ifx.getReceivedDt() != null)
			this.setReceivedDt(ifx.getReceivedDt());
		
		if (ifx.getReconciliationData() != null)
			this.setReconciliationData(ifx.getReconciliationData());
		
		if (Util.hasText(ifx.getMti()))
			this.setMti(ifx.getMti());
	}
	
	@Override
	public Boolean isRequest() throws Exception {
		return ISOFinalMessageType.isRequestMessage(this.getIfxType());
	}

	public Ifx ifxClone() {
		Ifx newIfx = getNewIfxInstance();

		if (this.getIfxDirection() != null)
			newIfx.setIfxDirection(this.getIfxDirection().copy());

		newIfx.setIfxType(this.getIfxType());
		newIfx.setTrnType(this.getTrnType());

		if (this.getEMVRqData() != null)
			newIfx.setEMVRqData(this.getEMVRqData().copy());

		if (this.getEMVRsData() != null)
			newIfx.setEMVRsData(this.getEMVRsData().copy());

		if (this.getStatus() != null)
			newIfx.setStatus(this.getStatus().copy());

		if (this.getNetworkTrnInfo() != null)
			newIfx.setNetworkTrnInfo(this.getNetworkTrnInfo().copy());

//		if (this.getOrgRec() != null)
//			newIfx.setOrgRec(this.getOrgRec().copy());

		newIfx.setReceivedDt(this.getReceivedDt());

		if (this.getOriginalDataElements() != null)
			newIfx.setOriginalDataElements(this.getOriginalDataElements().copy());

		if (this.getAtmSpecificData() != null)
			newIfx.setAtmSpecificData(this.getAtmSpecificData().copy());

		if (this.getPosSpecificData() != null)
			newIfx.setPosSpecificData(this.getPosSpecificData().copy());
		
		if (this.getChargeData() != null)
			newIfx.setChargeData(this.getChargeData().copy());
		
		if (this.getLotteryData() != null)
			newIfx.setLotteryData(this.getLotteryData().copy());

		if (this.getSettleDt() != null)
			newIfx.setSettleDt(this.getSettleDt());

		if (this.getPostedDt() != null)
			newIfx.setPostedDt(this.getPostedDt());

		if (this.getReceivedDt() != null)
			newIfx.setReceivedDt(this.getReceivedDt());
		
		if (this.getReconciliationData() != null)
			newIfx.setReconciliationData(this.getReconciliationData());
		
		if (Util.hasText(this.getMti()))
			newIfx.setMti(this.getMti());
		
		return newIfx;
	}

}

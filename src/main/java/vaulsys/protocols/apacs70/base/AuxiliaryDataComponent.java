package vaulsys.protocols.apacs70.base;

import static vaulsys.protocols.apacs70.base.ApacsConstants.GS;
import static vaulsys.protocols.apacs70.base.ApacsConstants.RS;
import vaulsys.protocols.apacs70.ApacsByteArrayReader;
import vaulsys.protocols.apacs70.ApacsByteArrayWriter;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.util.Util;

import java.io.IOException;

public class AuxiliaryDataComponent extends Apacs70Component {
    public String recordType;
    public OtherDataComponent odc;

    public RqAuxBase rqAux;
    public RqAuxVersions rqVersions;

    public RsAuxBase rsAux;
    public RsAuxReceipt rsReceipt;
    public RsAuxOtherDataRecords rsOthersDataRecords;
    public RqAuxOtherDataRecords rqOthersDataRecords;

    @Override
    public void unpack(ApacsByteArrayReader in) {
        int termCap = in.getIntegerFixed("termCapability", 1); // aux Data Msg Term Cap
        if(termCap != 3)
            throw new IllegalArgumentException("Wrong auxiliary data message terminal capability: " + termCap);

        int msgSize = in.getIntegerFixed("msgSize", 4); // aux Data Msg Size Limit
        if(msgSize != 1000)
            throw new IllegalArgumentException("Wrong message size: " + msgSize);

        in.skipToSep(RS); //skip first RS to reach the head of first component

        ApacsByteArrayReader dataRecord = in.getBytesMaxToSep("dataRecord", 1000, ApacsConstants.RS);
        if(dataRecord.getRemainSize() > 0) {
            recordType = dataRecord.getStringFixed("recordType", 2);
            int recordSubType = dataRecord.getIntegerFixed("recordSubType", 2);
            dataRecord.skipToSep(GS); // skip first GS to reach first field of component

            if(OtherDataComponent.RECORD_TYPE.equals(recordType)){
                odc = new OtherDataComponent(true);
                odc.unpack(dataRecord);
            }
            else if(RqAuxBase.RECORD_TYPE.equals(recordType)) {
                rqAux = RqAuxBase.createAux(recordSubType);
                rqAux.unpack(dataRecord);
            }
        }

        in.skipToSep(RS); //skip first RS to reach the head of first component
        if(in.getRemainSize() > 0) { // Versions data record
            String recordType = in.getStringFixed("recordType", 2);
            if(!"Z6".equals(recordType))
                throw new IllegalArgumentException("Wrong record type: " + recordType + ", expecting: Z6");
            int recordSubType = in.getIntegerFixed("recordSubType", 2);
            if(recordSubType != 49)
                throw new IllegalArgumentException("Wrong record subtype: " + recordSubType + ", expecting: 49");
            in.skipToSep(GS);
            rqVersions = new RqAuxVersions();
            rqVersions.unpack(in);
        }
//		try{
        in.skipToSep(RS); //skip first RS to reach the head of first component
        if(in.getRemainSize() > 0) { // rqOthersDataRecords data record
            String recordType = in.getStringFixed("recordType", 2);
            if(!"Z6".equals(recordType))
                throw new IllegalArgumentException("Wrong record type: " + recordType + ", expecting: Z6");
            int recordSubType = in.getIntegerFixed("recordSubType", 2);
            if(recordSubType != 48)
                throw new IllegalArgumentException("Wrong record subtype: " + recordSubType + ", expecting: 48");
            in.skipToSep(GS);
            int numOfKeys = Integer.parseInt(in.getStringMaxToSep("NumOfKeys", 3, GS));
            in.skipToSep(GS);
            if(numOfKeys > 0){
                rqOthersDataRecords = new RqAuxOtherDataRecords(numOfKeys);
                rqOthersDataRecords.unpack(in);
            }
        }
//		}catch(Exception e){

//		}
    }

    @Override
    public void pack(ApacsByteArrayWriter out) throws IOException {
        out.writePadded("", 2, true); // Message Count
        out.write(RS);
        if(odc != null)
            odc.pack(out);
        else {
            rsAux.pack(out);
            out.write(RS);
            if(rsReceipt != null)
                rsReceipt.pack(out);
            if(rsOthersDataRecords != null){
				/*if(rsReceipt != null)*/out.write(RS);
                rsOthersDataRecords.pack(out);
            }
        }
    }

    @Override
    public void toIfx(Ifx ifx) {
        if(odc != null)
            odc.toIfx(ifx);
        else if(rqAux != null) {
            ifx.setExtraInfo("opt");
            rqAux.toIfx(ifx);
        }
    }

    @Override
    public void fromIfx(Ifx ifx) {
        String appVer = ifx.getExtraInfo();
        if(appVer == null) {
            odc = new OtherDataComponent(false);
            odc.fromIfx(ifx);
        }
        else {
            rsAux = RsAuxBase.createAux(ifx);
            rsAux.fromIfx(ifx);

            if(ifx.getUpdateReceiptRequired() != null && ifx.getUpdateReceiptRequired()) {
                rsReceipt = new RsAuxReceipt();
                rsReceipt.fromIfx(ifx);
            }
            int appVersion = Util.intValueOf(ifx.getApplicationVersion());
            if(!(appVersion < 21800 || (appVersion >= 23000 && appVersion <= 24701) ||appVersion >= 50000)){//incompatible versions with key/value extension
                rsOthersDataRecords = new RsAuxOtherDataRecords();
                rsOthersDataRecords.fromIfx(ifx);
            }
        }
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("\n{ Aux:");
        if(odc != null)
            builder.append(odc);
        if(rqAux != null)
            builder.append(rqAux.toString().replaceAll("\n", "\n "));
        if(rqVersions != null)
            builder.append(rqVersions.toString().replaceAll("\n", "\n "));
        if(rsAux != null)
            builder.append(rsAux.toString().replaceAll("\n", "\n "));
        if(rsOthersDataRecords != null)
            builder.append(rsOthersDataRecords.toString().replaceAll("\n", "\n "));
        builder.append("\n}");
        return builder.toString();
    }
}

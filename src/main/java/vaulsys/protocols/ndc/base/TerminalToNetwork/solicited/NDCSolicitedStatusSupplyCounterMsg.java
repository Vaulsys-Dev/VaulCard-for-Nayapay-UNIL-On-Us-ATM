package vaulsys.protocols.ndc.base.TerminalToNetwork.solicited;

import vaulsys.clearing.settlement.CoreConfigDataManager;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.exception.exception.NotParsedBinaryToProtocolException;
import vaulsys.protocols.ndc.parsers.NDCParserUtils;
import vaulsys.terminal.atm.ATMLog;
import vaulsys.terminal.atm.ActionType;
import vaulsys.terminal.atm.device.CardBin;
import vaulsys.terminal.atm.device.CassetteA;
import vaulsys.terminal.atm.device.CassetteB;
import vaulsys.terminal.atm.device.CassetteC;
import vaulsys.terminal.atm.device.CassetteD;
import vaulsys.terminal.atm.device.CurrencyRejectBin;
import vaulsys.terminal.atm.device.DepositBin;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.util.MyInteger;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

import com.fanap.cms.business.corecommunication.biz.CoreGateway;
import com.fanap.cms.exception.BusinessException;

public class NDCSolicitedStatusSupplyCounterMsg extends NDCSolicitedStatusTerminalStateMsg {
	
	private static final Logger logger = Logger.getLogger(NDCSolicitedStatusSupplyCounterMsg.class);
	
    public static final int NOTES_DIGIT_NUMBER = 5;

    public String transactionSerialNo;
    public String accumulatedTransactionCount;
    public int[] notesInCassette;
    public int[] notesRejected;
    public int[] notesDispensed;
    public int[] lastTrxNotesDispensed;
    public int cardCaptured;
    public int envelopesDeposited;
    public String cameraFilmRemaining;
    public String lastEnvelpeSerialNo;

    public NDCSolicitedStatusSupplyCounterMsg(MyInteger offset, byte[] rawdata) throws NotParsedBinaryToProtocolException {
        super(offset, rawdata);
        transactionSerialNo = new String(rawdata, offset.value, 4);
        offset.value += 4;
        accumulatedTransactionCount = new String(rawdata, offset.value, 7);
        offset.value += 7;
        String notesInCassetteS = new String(rawdata, offset.value, 20);
        notesInCassette = NDCParserUtils.parseDispensedNote(notesInCassetteS, NOTES_DIGIT_NUMBER);
        offset.value += 20;
        String notesRejectedS = new String(rawdata, offset.value, 20);
        notesRejected = NDCParserUtils.parseDispensedNote(notesRejectedS, NOTES_DIGIT_NUMBER);
        offset.value += 20;
        String notesDispensedS = new String(rawdata, offset.value, 20);
        notesDispensed = NDCParserUtils.parseDispensedNote(notesDispensedS, NOTES_DIGIT_NUMBER);
        offset.value += 20;
        String lastTrxNotesDispensedS = new String(rawdata, offset.value, 20);
        lastTrxNotesDispensed = NDCParserUtils.parseDispensedNote(lastTrxNotesDispensedS, NOTES_DIGIT_NUMBER);
        offset.value += 20;
        String cardCapturedS = new String(rawdata, offset.value, 5);
        cardCaptured = Integer.parseInt(cardCapturedS);
        offset.value += 5;
        String envelopesDepositedS = new String(rawdata, offset.value, 5);
        envelopesDeposited = Integer.parseInt(envelopesDepositedS);
        offset.value += 5;
        cameraFilmRemaining = new String(rawdata, offset.value, 5);
        offset.value += 5;
        lastEnvelpeSerialNo = new String(rawdata, offset.value, 5);
        offset.value += 5;
    }

    public void updateStatus(ATMTerminal terminal) {
    	
    	boolean isCurrencyConfig = false;
    	
    	//Mirkamali(Task179): Currency ATM
    	if(Boolean.TRUE.equals(ProcessContext.get().getATMConfiguration(terminal.getOwnOrParentConfigurationId()).getIsCurrencyConfig())) {
    		isCurrencyConfig = true;
    	}
    	/*************/
        CassetteA cassetteA = terminal.getDevice(CassetteA.class);
        
        Integer preNoteInCassetteA = cassetteA.getNotes();
        cassetteA.setNotes(notesInCassette[0]);
        
        Integer preNoteDispensedA = cassetteA.getNotesDispensed();
        cassetteA.setNotesDispensed(notesDispensed[0]);
        
        Integer preNoteRejectedA = cassetteA.getNotesRejected();
        cassetteA.setNotesRejected(notesRejected[0]);
        
//        cassetteA.setDenomination(terminal.getOwnOrParentConfiguration().getCassetteADenomination());
        if(isCurrencyConfig)
        	cassetteA.setDenomination(terminal.getCurrency().getCassetteADenomination());
        else
        	cassetteA.setDenomination(ProcessContext.get().getATMConfiguration(terminal.getOwnOrParentConfigurationId()).getCassetteADenomination());
        
        GeneralDao.Instance.saveOrUpdate(cassetteA);
        /*************/

        CassetteB cassetteB = terminal.getDevice(CassetteB.class);
        
        Integer preNoteInCassetteB = cassetteB.getNotes();
        cassetteB.setNotes(notesInCassette[1]);
        
        Integer preNoteDispensedB = cassetteB.getNotesDispensed();
        cassetteB.setNotesDispensed(notesDispensed[1]);
        
        Integer preNoteRejectedB = cassetteB.getNotesRejected();
        cassetteB.setNotesRejected(notesRejected[1]);
        
//        cassetteB.setDenomination(terminal.getOwnOrParentConfiguration().getCassetteBDenomination());
        if(isCurrencyConfig)
        	cassetteB.setDenomination(terminal.getCurrency().getCassetteBDenomination());
        else
        	cassetteB.setDenomination(ProcessContext.get().getATMConfiguration(terminal.getOwnOrParentConfigurationId()).getCassetteBDenomination());
        
        GeneralDao.Instance.saveOrUpdate(cassetteB);
        /*************/

        CassetteC cassetteC = terminal.getDevice(CassetteC.class);
      
        Integer preNoteInCassetteC = cassetteC.getNotes();
        cassetteC.setNotes(notesInCassette[2]);
        
        Integer preNoteDispensedC = cassetteC.getNotesDispensed();
        cassetteC.setNotesDispensed(notesDispensed[2]);
        
        Integer preNoteRejectedC = cassetteC.getNotesRejected();
        cassetteC.setNotesRejected(notesRejected[2]);
        
//        cassetteC.setDenomination(terminal.getOwnOrParentConfiguration().getCassetteCDenomination());
        if(isCurrencyConfig)
        	cassetteC.setDenomination(terminal.getCurrency().getCassetteCDenomination());
        else
        	cassetteC.setDenomination(ProcessContext.get().getATMConfiguration(terminal.getOwnOrParentConfigurationId()).getCassetteCDenomination());
        
        GeneralDao.Instance.saveOrUpdate(cassetteC);
        /*************/

        CassetteD cassetteD = terminal.getDevice(CassetteD.class);
        
        Integer preNoteInCassetteD = cassetteD.getNotes();
        cassetteD.setNotes(notesInCassette[3]);
        
        Integer preNoteDispensedD = cassetteD.getNotesDispensed();
        cassetteD.setNotesDispensed(notesDispensed[3]);
        
        Integer preNoteRejectedD = cassetteD.getNotesRejected();
        cassetteD.setNotesRejected(notesRejected[3]);
        
//        cassetteD.setDenomination(terminal.getOwnOrParentConfiguration().getCassetteDDenomination());
        if(isCurrencyConfig)
        	cassetteD.setDenomination(terminal.getCurrency().getCassetteDDenomination());
        else
        	cassetteD.setDenomination(ProcessContext.get().getATMConfiguration(terminal.getOwnOrParentConfigurationId()).getCassetteDDenomination());
        
        GeneralDao.Instance.saveOrUpdate(cassetteD);
        /*************/

        CurrencyRejectBin reject = terminal.getDevice(CurrencyRejectBin.class);
        reject.setNotes(notesRejected[0] + notesRejected[1] + notesRejected[2] + notesRejected[3]);

        GeneralDao.Instance.saveOrUpdate(reject);
        /*************/
        
        CardBin cardBin = terminal.getDevice(CardBin.class);
        int preCardCaptured = cardBin.getNo(); 
        cardBin.setNo(cardCaptured);

        GeneralDao.Instance.saveOrUpdate(cardBin);
        /*************/
        DepositBin depositBin = terminal.getDevice(DepositBin.class);
        depositBin.setNo(envelopesDeposited);

        GeneralDao.Instance.saveOrUpdate(depositBin);
        /*************/
        String preNoteInCassette = preNoteInCassetteA + "-" + preNoteInCassetteB + "-" + preNoteInCassetteC + "-" + preNoteInCassetteD; 
        String preNoteDispensed = preNoteDispensedA + "-" + preNoteDispensedB + "-" + preNoteDispensedC + "-" + preNoteDispensedD; 
        String preNoteRejected = preNoteRejectedA + "-" + preNoteRejectedB + "-" + preNoteRejectedC + "-" + preNoteRejectedD; 
        
        String nxtNoteInCassette = notesInCassette[0] + "-" + notesInCassette[1] + "-" + notesInCassette[2] + "-" + notesInCassette[3]; 
        String nxtNoteDispensed = notesDispensed[0] + "-" + notesDispensed[1] + "-" + notesDispensed[2] + "-" + notesDispensed[3]; 
        String nxtNoteRejected = notesRejected[0] + "-" + notesRejected[1] + "-" + notesRejected[2] + "-" + notesRejected[3]; 
        /*************/
        
        Double balance = 0D;
        try {
//			balance = CoreGateway.getATMBoxBalance(terminal.getCode().toString(), terminal.getOwner().getCoreBranchCode(), getFanapCoreConfigValue(CoreConfigDataManager.CoreUrl));
		} catch (Exception e) {
			logger.error("Exception in getting ATM Box Balance, " + e, e);
		}
        
        ATMLog log = new ATMLog(terminal.getCode(), preNoteInCassette, preNoteRejected, preNoteDispensed, "", preCardCaptured, ATMLog.LogState.LAST_STATE, ActionType.SUPERVISOR_EXIT);
        log.setBalance(balance);
        GeneralDao.Instance.saveOrUpdate(log);
        
        log = new ATMLog(terminal.getCode(), nxtNoteInCassette, nxtNoteRejected, nxtNoteDispensed, "", cardCaptured, ATMLog.LogState.NEXT_STATE, ActionType.SUPERVISOR_EXIT);
        log.setBalance(balance);
        GeneralDao.Instance.saveOrUpdate(log);
    }
    
    public static String getFanapCoreConfigValue(String varName){
		return CoreConfigDataManager.getValue(varName);
	}
    
    @Override
    public String toString() {
    	return super.toString() +
        		"lastTrxSerialNo:\t\t" + transactionSerialNo + "\r\n" + 
        		"accumulatedTrxCount:\t\t" + accumulatedTransactionCount + "\r\n" + 
        		"notesInCass[0]:\t\t" + notesInCassette[0] + "\r\n" + 
        		"notesInCass[1]:\t\t" + notesInCassette[1] + "\r\n" + 
        		"notesInCass[2]:\t\t" + notesInCassette[2] + "\r\n" + 
        		"notesInCass[3]:\t\t" + notesInCassette[3] + "\r\n" + 
        		"notesRejected[0]:\t\t" + notesRejected[0] + "\r\n" + 
        		"notesRejected[1]:\t\t" + notesRejected[1] + "\r\n" + 
        		"notesRejected[2]:\t\t" + notesRejected[2] + "\r\n" + 
        		"notesRejected[3]:\t\t" + notesRejected[3] + "\r\n" + 
        		"notesDispensed[0]:\t\t" + notesDispensed[0] + "\r\n" + 
        		"notesDispensed[1]:\t\t" + notesDispensed[1] + "\r\n" + 
        		"notesDispensed[2]:\t\t" + notesDispensed[2] + "\r\n" + 
        		"notesDispensed[3]:\t\t" + notesDispensed[3] + "\r\n" + 
        		"lastTrxNotesDispensed[0]:\t\t" + lastTrxNotesDispensed[0] + "\r\n" + 
        		"lastTrxNotesDispensed[1]:\t\t" + lastTrxNotesDispensed[1] + "\r\n" + 
        		"lastTrxNotesDispensed[2]:\t\t" + lastTrxNotesDispensed[2] + "\r\n" + 
        		"lastTrxNotesDispensed[3]:\t\t" + lastTrxNotesDispensed[3] + "\r\n" + 
        		"cardCapturedNo:\t\t" + cardCaptured + "\r\n" + 
        		"envelopesDeposited:\t\t" + envelopesDeposited + "\r\n" + 
        		"cameraFilmRemaining:\t\t" + cameraFilmRemaining + "\r\n" + 
        		"lastEnvelpeSerialNo:\t\t" + lastEnvelpeSerialNo + "\r\n";
    	
    }

}
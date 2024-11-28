package vaulsys.clearing.reconcile;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.clearing.base.ClearingDate;
import vaulsys.clearing.base.ClearingDateManager;
import vaulsys.entity.FinancialEntityService;
import vaulsys.entity.impl.Institution;
import vaulsys.message.Message;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.ifx.enums.Severity;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.Util;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import org.apache.log4j.Logger;

public class ISOCutover extends AbstractCutover {

    transient Logger logger = Logger.getLogger(this.getClass());

    public static final ISOCutover Instance = new ISOCutover();

    private ISOCutover() {}


    public ISOMsg buildResponse(Message incommingMessage) throws Exception {
        ISOMsg isoMsg = (ISOMsg) incommingMessage.getProtocolMessage();
        Integer mti = new Integer(Integer.parseInt(isoMsg.getMTI()));

        ISOMsg outGoingMessage = null;
        if (mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87) || mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_87)) {

            MonthDayDate cutOverStartMonthDay = new MonthDayDate(MyDateFormatNew.parse("MMddHHmmss", (String) isoMsg.getValue(7)));
            DayTime cutOverStartDayTime = new DayTime(MyDateFormatNew.parse("MMddHHmmss", (String) isoMsg.getValue(7)));
            MonthDayDate cutOverDate = new MonthDayDate(MyDateFormatNew.parse("MMdd", (String) isoMsg.getValue(15)));
            String senderInstitutionCode = incommingMessage.getChannel().getInstitutionId();

            DateTime now = DateTime.now();
            if (cutOverStartMonthDay.getDay() != now.getDayDate().getDay() || cutOverStartMonthDay.getMonth() != now.getDayDate().getMonth())
                return buildCutoverRs(isoMsg, ISOResponseCodes.INVALID_CARD);

            if(cutOverStartDayTime.getHour() < 21)
                return buildCutoverRs(isoMsg, ISOResponseCodes.INVALID_CARD);

            if(now.getDayDate().nextDay().getDate().compareTo(cutOverDate.getDate()) < 0)
                return buildCutoverRs(isoMsg, ISOResponseCodes.INVALID_CARD);


            if (!authorizeMsg(isoMsg)){
//            	incommingMessage.getTransaction().setAuthorized(false);
                incommingMessage.getIfx().setStatusDesc("CUT_OVER message is not sent for our institution: (sent Inst: "+ isoMsg.getString(32)+", "+isoMsg.getString(33) +")");
                incommingMessage.getIfx().setSeverity(Severity.ERROR);
                return buildCutoverRs(isoMsg, ISOResponseCodes.INVALID_CARD_STATUS);
            } else {
                Institution institution = FinancialEntityService.findEntity(Institution.class, ""+senderInstitutionCode);
                ClearingDate lastworkingDay = FinancialEntityService.getLastWorkingDay(institution);

                if (lastworkingDay != null && lastworkingDay.getDate().equals(cutOverDate)) {
                    if (!lastworkingDay.isValid()) {
                        lastworkingDay.setValid(true);
                        institution.setLastWorkingDay(institution.getCurrentWorkingDay());
                        institution.setCurrentWorkingDay(lastworkingDay);
                    } else {
                        return buildCutoverRs(isoMsg, ISOResponseCodes.INVALID_CARD);
                    }
                } else if (lastworkingDay == null || lastworkingDay.getDate().before(cutOverDate)) {

                    ClearingDateManager.getInstance().push(cutOverDate, DateTime.now(), true, institution);

                }else if(lastworkingDay.getDate().after(cutOverDate)){
                    if (lastworkingDay != null) {

                        if (lastworkingDay.getDate().getMonth() == 12 && cutOverDate.getMonth() == 1){
                            cutOverDate.setYear(lastworkingDay.getDate().getYear() + 1);
                            ClearingDateManager.getInstance().push(cutOverDate, DateTime.now(), true, institution);
                        } else {
                            logger.error("shetab says that our working day back to " + cutOverDate.toString() + ", ignore it!");
                            return buildCutoverRs(isoMsg, ISOResponseCodes.INVALID_CARD);
                        }
                    }
                }

                outGoingMessage = buildCutoverRs(isoMsg, ISOResponseCodes.APPROVED);
                GeneralDao.Instance.saveOrUpdate(institution);
                GlobalContext.getInstance().setAllInstitutions();
                logger.info("after working day"+ institution.getCurrentWorkingDay().getDate());

            }
        }

        return outGoingMessage;
    }

    private boolean authorizeMsg(ISOMsg isoMsg) {
        String bankId = isoMsg.getString(32);
        String fwdBankId = isoMsg.getString(33);
//    	String myCode = GlobalContext.getInstance().getMyInstitution().getBin().toString();
        String myCode = ProcessContext.get().getMyInstitution().getBin().toString();

        if (myCode.equals(bankId) && myCode.equals(fwdBankId))
            return true;

//    	//TODO: Should be removed. This is just for NEGIN BUG (!!!) not filling both of 32 and 33 fields
        // It seems Negin has corrected its messages. It fills both fields!
//    	if (myCode.equals(bankId) || myCode.equals(fwdBankId))
//    		return true;

        return false;
    }


    private ISOMsg buildCutoverRs(ISOMsg isoMsg, String rsCode) throws Exception {
        Integer mti = Integer.parseInt(isoMsg.getMTI());
        ISOMsg outGoingMessage = new ISOMsg();
        String responseMTI = (mti.equals(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87) ? ISOMessageTypes.NETWORK_MANAGEMENT_RESPONSE_87 : ISOMessageTypes.NETWORK_MANAGEMENT_ADVICE_RESPONSE_87) + "";
        outGoingMessage.setMTI(responseMTI);
        outGoingMessage.set(7, isoMsg.getValue(7));
        outGoingMessage.set(11, isoMsg.getValue(11));
        outGoingMessage.set(15, "0000");
        outGoingMessage.set(32, isoMsg.getValue(32));
        outGoingMessage.set(33, isoMsg.getValue(33));
        outGoingMessage.set(39, rsCode);
        outGoingMessage.set(48, isoMsg.getValue(48));
        outGoingMessage.set(70, isoMsg.getValue(70));
        outGoingMessage.set(128, "0000000000000000");
        return outGoingMessage;
    }

    public ISOMsg buildRequset(Institution institution) throws Exception {
        ISOMsg isoMsg = new ISOMsg();
        try {

//            MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");

            DateTime currentSystemDate = DateTime.now();
            MonthDayDate nextWorkingDay = null;
            ClearingDate date = institution.getCurrentWorkingDay();

//            date = GeneralDao.Instance.load(ClearingDate.class, date.getId());

            if (date != null)
                nextWorkingDay = date.getDate().nextDay();
            else
                nextWorkingDay = MonthDayDate.now();

//            ClearingDateManager.getInstance().push(nextWorkingDay, currentSystemDate, institution);

            isoMsg.setMTI(String.valueOf(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87));

            // date and time at which data was sent to Shetab : MMDDhhmmss
            String dt = MyDateFormatNew.format("MMddHHmmss", currentSystemDate.toDate());
            isoMsg.set(7, dt);

            isoMsg.set(11, Util.generateTrnSeqCntr(6));

            // TODO: Date and time at which transaction will be reconciled in Shetab
            // YYMMDD
            // String stlDt = dateFormatMMDD.format(ClearingDateManager.getInstance().getNewStlDate());
            String stlDt = nextWorkingDay.toString();
            isoMsg.set(15, stlDt);

            // The acquire and issuer identification code
            isoMsg.set(32, institution.getBin().toString());
            isoMsg.set(33, institution.getBin().toString());

            isoMsg.set(48, "0".getBytes());
            isoMsg.set(53, "0000000000000000");

            isoMsg.set(70, NetworkManagementInfo.CUTOVER.getType());
//            isoMsg.set(96, "00000000000000000000000000000000");
            isoMsg.set(128, "0000000000000000");
        } catch (ISOException e) {
            logger.error("Encounter with an exception in building Cutover_rq for inst["+ (institution==null? "null": institution )+ "]. ("+e.getClass().getSimpleName()+" :"+ e.getMessage()+")", e);
//            e.printStackTrace();
        }
        return isoMsg;
    }
}

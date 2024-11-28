package vaulsys.clearing.base;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.MonthDayDate;
import vaulsys.entity.impl.Institution;
import vaulsys.protocols.ifx.enums.NetworkManagementInfo;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOException;
import vaulsys.protocols.PaymentSchemes.ISO8583.base.ISOMsg;
import vaulsys.protocols.PaymentSchemes.base.ISOMessageTypes;
import vaulsys.util.MyDateFormatNew;
import vaulsys.util.SwitchContext;
import vaulsys.util.Util;

import org.apache.log4j.Logger;

public class Cutover extends SwitchContext {

	transient static Logger logger = Logger.getLogger(Cutover.class);
	
	private Cutover() {}
	
    public static ISOMsg buildRq(Institution institution) throws Exception {
        ISOMsg isoMsg = new ISOMsg();
        try {

//            MyDateFormat dateFormatMMDDhhmmss = new MyDateFormat("MMddHHmmss");

            DateTime currentSystemDate = DateTime.now();
            MonthDayDate lastWorkingDay = null;
            ClearingDate date = institution.getCurrentWorkingDay();
//            	getFinancialEntityService().getLastValidWorkingDay(institution);
            if (date != null)
                lastWorkingDay = date.getDate().nextDay();
            else
                lastWorkingDay = new MonthDayDate(currentSystemDate.toDate());

            MonthDayDate nextWorkingDay = new MonthDayDate(lastWorkingDay);

            ClearingDateManager.getInstance().push(nextWorkingDay, currentSystemDate, institution);

            isoMsg.setMTI(String.valueOf(ISOMessageTypes.NETWORK_MANAGEMENT_REQUEST_87));

            // date and time at which data was sent to Shetab : MMDDhhmmss
            String dt = MyDateFormatNew.format("MMddHHmmss", currentSystemDate.toDate());
            isoMsg.set(7, dt);

            isoMsg.set(11, Util.generateTrnSeqCntr(6));

            String stlDt = nextWorkingDay.toString();
            isoMsg.set(15, stlDt);

            // The acquire and issuer identification code
            isoMsg.set(32, institution.getCode());
            isoMsg.set(33, institution.getCode());

            isoMsg.set(48, 0);
            isoMsg.set(53, 0);

            isoMsg.set(70, NetworkManagementInfo.CUTOVER);
            // isoMsg.set(96, "00000000000000000000000000000000");
            isoMsg.set(128, "0000000000000000");
        } catch (ISOException e) {
        	logger.error("Encounter with an exception.("+ e.getClass().getSimpleName()+": "+ e.getMessage());
//            e.printStackTrace();
        }
        return isoMsg;
    }

}

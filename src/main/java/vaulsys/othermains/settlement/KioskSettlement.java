package vaulsys.othermains.settlement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayTime;
import vaulsys.clearing.ClearingService;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.clearing.consts.FinancialEntityRole;
import vaulsys.clearing.settlement.BillPaymentSettlementServiceImpl;
import vaulsys.clearing.settlement.PerTransactionKioskBillPaymentSettlementServiceImpl;
import vaulsys.entity.impl.Organization;
import vaulsys.persistence.GeneralDao;
import vaulsys.terminal.impl.ATMTerminal;
import vaulsys.terminal.impl.KIOSKCardPresentTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.terminal.impl.ThirdPartyVirtualTerminal;
import vaulsys.thirdparty.consts.ThirdPartyType;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.List;

public class KioskSettlement {

    public static void main(String[] args) {
        ClearingProfile clearingProfile = null;
        GeneralDao.Instance.beginTransaction();
        GlobalContext.getInstance().startup();
        ProcessContext.get().init();
        List<Terminal> terminals = null;
        try {

            Terminal terminal = GeneralDao.Instance.load(ThirdPartyVirtualTerminal.class, 555L);
            terminal.getCode();
            terminal.getId();
            terminal.getTerminalType();
            terminal.getOwner();
            terminal.getOwner().getRole();
            ((Organization)terminal.getOwner()).getType().findThirdpartyType();
//                logger.info("Try to Settle Kiosk Terminal "+ terminal.getCode());
            terminals = new ArrayList<Terminal>();
            terminals.add(terminal);


            clearingProfile = ClearingService.findClearingProfile(170617L);
            GlobalContext.getInstance().getMyInstitution();
            GeneralDao.Instance.endTransaction();
        } catch (Exception e) {
            e.printStackTrace();
            GeneralDao.Instance.rollback();
            return;
        }

        if (clearingProfile != null) {
            int day = 0;
            int hour = 23;
            if (args.length == 2) {
                day = Integer.parseInt(args[0]);
                hour = Integer.parseInt(args[1]);
            }
            DateTime settleUntilTime = DateTime.beforeNow(Math.abs(day));
            settleUntilTime.setDayTime(new DayTime(hour, 59, 59));
            PerTransactionKioskBillPaymentSettlementServiceImpl.Instance.settle(terminals, clearingProfile, settleUntilTime, false, true, true, false);
        }
//		System.exit(0);
    }
}

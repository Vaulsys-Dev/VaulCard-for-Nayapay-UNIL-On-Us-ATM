package vaulsys.wallet.components;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import vaulsys.cms.base.CMSAccount;
import vaulsys.customer.Currency;
import vaulsys.entity.FBRATL;
import vaulsys.entity.Tax;
import vaulsys.entity.TaxType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.PaymentSchemes.base.ChannelCodes;
import vaulsys.protocols.PaymentSchemes.base.ISOResponseCodes;
import vaulsys.protocols.ifx.enums.AccType;
import vaulsys.util.Util;
import vaulsys.util.WebServiceUtil;
import vaulsys.wallet.base.ledgers.*;
import vaulsys.webservice.walletcardmgmtwebservice.entity.FundBank;
import vaulsys.webservice.walletcardmgmtwebservice.entity.WalletCMSWsEntity;
import vaulsys.wfe.GlobalContext;

import javax.transaction.Transactional;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by HP on 23-Nov-18.
 */
public class FinanceManager {
    private static final Logger logger = Logger.getLogger(FinanceManager.class);

    @Transactional
    public static boolean LoadWallet(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount linkaccount)
    {
        try {
            logger.info("Loading Wallet....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            //////////////////////////////////////////////////////////////////
            Long finalAmount = TxnAmount - SrcChargeAmount;
            Long UpdatedBalance = AcctActBalance + finalAmount;
            //////////////////////////////////////////////////////////////////

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (finalAmount < 0) {
                logger.error("Calculated amount [" + finalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance(UpdatedBalance + "");
            account.setAvailableBalance(UpdatedBalance + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setPreviousBalance(AcctAvailBalance+"");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setLinkedaccount(linkaccount);
            WGL2.setAmount((TxnAmount + SrcChargeAmount) + "");
            WGL2.setCurrency(linkaccount.getCurrency());
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setWalletflag(false);
            //WGL.setClosingBalance(account.getAvailableBalance());
            //WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.save(WGL2);

            WalletGeneralLedger WGL3 = new WalletGeneralLedger();
            WGL3.setTxnname(wsmodel.getServicename());
            WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL3.setTxnflag(TxnFlag.CREDIT);
            WGL3.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL3.setAmount(finalAmount + "");
            WGL3.setCurrency(account.getCurrency());
            WGL3.setWalletflag(false);
            WGL3.setClosingBalance(account.getAvailableBalance());
            WGL3.setPreviousBalance(AcctAvailBalance+"");
            WGL3.setMerchantid(wsmodel.getMerchantid());
            WGL3.setAgentid(wsmodel.getAgentid());
            WGL3.setBillerid(wsmodel.getBillerid());
            WGL3.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL3);
            */

            String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(linkaccount.getBranchId());
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN, AccType.CAT_SETT_ACCT);
            //m.rehman: 08-07-2020: need to apply charges so change isChargesApply to true
            //UpdateandLogPartnerBankCollectionAccount(wsmodel, linkaccount.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_IN,
            //        AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, false, false);
            UpdateandLogPartnerBankCollectionAccount(wsmodel, linkaccount.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_IN,
                            AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, true, false);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Loading Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Loading Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while loading wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean RevLoadWallet(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount linkaccount)
    {
        try {
            logger.info("Reversing Wallet for Load Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            /*if(AcctAvailBalance < TxnAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }*/

            //////////////////////////////////////////////////////////////////
            Long finalAmount = TxnAmount - SrcChargeAmount;
            Long UpdatedBalance = AcctActBalance - finalAmount;
            //////////////////////////////////////////////////////////////////

            account.setActualBalance(UpdatedBalance + "");
            account.setAvailableBalance(UpdatedBalance + "");
            GeneralDao.Instance.save(account);

            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            /*
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setCollectionaccount(GetEMICollectionAccount(account.getCurrency()));
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setPreviousBalance(AcctAvailBalance+"");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL);
            */

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setLinkedaccount(linkaccount);
            WGL2.setAmount((TxnAmount + SrcChargeAmount) + "");
            WGL2.setCurrency(linkaccount.getCurrency());
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setWalletflag(false);
            //WGL.setClosingBalance(account.getAvailableBalance());
            //WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.save(WGL2);

            /*
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_IN);
            */

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Reverse Load Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Reverse Load Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Reversing Load wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean UnLoadWallet(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount linkaccount)
    {
        try {
            logger.info("UnLoading Wallet for Unload Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            //////////////////////////////////////////////////////////////////
            Long finalAmount = TxnAmount + SrcChargeAmount;
            Long UpdatedBalance = AcctActBalance - finalAmount;
            //////////////////////////////////////////////////////////////////

            if(AcctAvailBalance < finalAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance(UpdatedBalance + "");
            account.setAvailableBalance(UpdatedBalance + "");
            GeneralDao.Instance.save(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount+"");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setPreviousBalance(AcctAvailBalance+"");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setLinkedaccount(linkaccount);
            WGL2.setAmount(TxnAmount + "");
            WGL2.setCurrency(linkaccount.getCurrency());
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setWalletflag(false);
            //WGL.setClosingBalance(account.getAvailableBalance());
            //WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.save(WGL2);

            WalletGeneralLedger WGL3 = new WalletGeneralLedger();
            WGL3.setTxnname(wsmodel.getServicename());
            WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL3.setTxnflag(TxnFlag.DEBIT);
            WGL3.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL3.setAmount(finalAmount + "");
            WGL3.setCurrency(account.getCurrency());
            WGL3.setWalletflag(false);
            WGL3.setClosingBalance(account.getAvailableBalance());
            WGL3.setPreviousBalance(AcctAvailBalance+"");
            WGL3.setMerchantid(wsmodel.getMerchantid());
            WGL3.setAgentid(wsmodel.getAgentid());
            WGL3.setBillerid(wsmodel.getBillerid());
            WGL3.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL3);
            */

            String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(linkaccount.getBranchId());
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);
            UpdateandLogPartnerBankCollectionAccount(wsmodel, linkaccount.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
                    AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, false, false);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("UnLoading Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("UnLoading Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Unloading wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean RevUnLoadWallet(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount linkaccount)
    {
        try {
            logger.info("Reversing Wallet for Unload Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            /*if(AcctAvailBalance < TxnAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }*/

            //////////////////////////////////////////////////////////////////
            Long finalAmount = TxnAmount + SrcChargeAmount;
            Long UpdatedBalance = AcctActBalance + finalAmount;
            //////////////////////////////////////////////////////////////////

            if(AcctActBalance < TxnAmount + SrcChargeAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            account.setActualBalance(UpdatedBalance + "");
            account.setAvailableBalance(UpdatedBalance + "");
            GeneralDao.Instance.save(account);

            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount+"");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setPreviousBalance(AcctAvailBalance+"");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setLinkedaccount(linkaccount);
            WGL2.setAmount(TxnAmount + "");
            WGL2.setCurrency(linkaccount.getCurrency());
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setWalletflag(false);
            //WGL.setClosingBalance(account.getAvailableBalance());
            //WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.save(WGL2);

            /*
            WalletGeneralLedger WGL3 = new WalletGeneralLedger();
            WGL3.setTxnname(wsmodel.getServicename());
            WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL3.setTxnflag(TxnFlag.CREDIT);
            WGL3.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL3.setAmount(finalAmount + "");
            WGL3.setCurrency(account.getCurrency());
            WGL3.setWalletflag(false);
            WGL3.setClosingBalance(account.getAvailableBalance());
            WGL3.setPreviousBalance(AcctAvailBalance+"");
            WGL3.setMerchantid(wsmodel.getMerchantid());
            WGL3.setAgentid(wsmodel.getAgentid());
            WGL3.setBillerid(wsmodel.getBillerid());
            WGL3.setTransaction(wsmodel);
            GeneralDao.Instance.save(WGL3);
            */

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Reverse UnLoad Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Reverse UnLoad Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Reversing Unload wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean WalletTransaction(WalletCMSWsEntity wsmodel, CMSAccount srcaccount, CMSAccount destaccount)
    {
        try {
            logger.info("Performing Wallet-To-Wallet Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long SrcAcctActBalance = (Util.hasText(srcaccount.getActualBalance()) ? Long.parseLong(srcaccount.getActualBalance()) : 0L);
            Long SrcAcctAvailBalance = (Util.hasText(srcaccount.getAvailableBalance()) ? Long.parseLong(srcaccount.getAvailableBalance()) : 0L);
            Long DestAcctActBalance = (Util.hasText(destaccount.getActualBalance()) ? Long.parseLong(destaccount.getActualBalance()) : 0L);
            Long DestAcctAvailBalance = (Util.hasText(destaccount.getAvailableBalance()) ? Long.parseLong(destaccount.getAvailableBalance()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long sourceFinalAmount = TxnAmount + SrcChargeAmount;
            Long destinationFinalAmount = TxnAmount - DestChargeAmount;
            Long sourceUpdatedBalance =  SrcAcctActBalance - sourceFinalAmount;
            Long destinationUpdatedBalance = DestAcctActBalance + destinationFinalAmount;

            if(SrcAcctAvailBalance < sourceFinalAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (destinationFinalAmount < 0) {
                logger.error("Calculated destination amount [" + destinationFinalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Source Wallet Balance ...");
            srcaccount.setActualBalance((sourceUpdatedBalance) + "");
            srcaccount.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(srcaccount);

            logger.info("Updating Destination Wallet Balance ...");
            destaccount.setActualBalance((destinationUpdatedBalance) + "");
            destaccount.setAvailableBalance((destinationUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(destaccount);

            logger.info("Updating Source Wallet Balance Log ...");
            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(srcaccount);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(sourceFinalAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(srcaccount.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.DEBIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(SrcAcctAvailBalance + "");
            wsmodel.setClosingbalance(srcaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Destination Wallet Balance Log ...");
            WalletBalanceLog destbalanceLog = new WalletBalanceLog();
            destbalanceLog.setWallet(destaccount);
            destbalanceLog.setChannelid(wsmodel.getChannelid());
            destbalanceLog.setAmount(destinationFinalAmount + "");
            destbalanceLog.setOriginalbalance(DestAcctAvailBalance+"");
            destbalanceLog.setUpdatedbalance(destaccount.getAvailableBalance());
            destbalanceLog.setTxnname(wsmodel.getServicename());
            destbalanceLog.setTransaction(wsmodel);
            destbalanceLog.setTxnnature(TxnFlag.CREDIT);
            destbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(destbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setDestOpeningbalance(DestAcctAvailBalance + "");
            wsmodel.setDestClosingbalance(destaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Source Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(srcaccount);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(srcaccount.getCurrency());
            WGL.setWalletflag(true);
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setClosingBalance((SrcAcctAvailBalance - TxnAmount) + "");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            logger.info("Updating Destination Wallet General Ledger ...");
            WalletGeneralLedger WGL4 = new WalletGeneralLedger();
            WGL4.setTxnname(wsmodel.getServicename());
            WGL4.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL4.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL4.setTxnflag(TxnFlag.CREDIT);
            WGL4.setWallet(destaccount);
            WGL4.setAmount((destinationFinalAmount) + "");
            WGL4.setCurrency(destaccount.getCurrency());
            WGL4.setWalletflag(true);
            WGL4.setClosingBalance(destaccount.getAvailableBalance());
            WGL4.setPreviousBalance(DestAcctAvailBalance+"");
            WGL4.setMerchantid(wsmodel.getMerchantid());
            WGL4.setAgentid(wsmodel.getAgentid());
            WGL4.setBillerid(wsmodel.getBillerid());
            WGL4.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL4);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Source Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(srcaccount);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(srcaccount.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setPreviousBalance((SrcAcctAvailBalance - TxnAmount) +"");
                WGL2.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL2);

                //UpdateandLogCollectionAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.DEBIT);
                UpdateandLogRevenueAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Source Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(srcaccount);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(srcaccount.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setPreviousBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount)) + "");
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL3);

                UpdateandLogTaxAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Wallet-To-Wallet Transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Wallet-To-Wallet Transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Wallet-To-Wallet Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean RevWalletTransaction(WalletCMSWsEntity wsmodel, CMSAccount srcaccount, CMSAccount destaccount)
    {
        try {
            logger.info("Reversing Wallet-To-Wallet Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long SrcAcctActBalance = Long.parseLong(srcaccount.getActualBalance());
            Long SrcAcctAvailBalance = Long.parseLong(srcaccount.getAvailableBalance());
            Long DestAcctActBalance = Long.parseLong(destaccount.getActualBalance());
            Long DestAcctAvailBalance = Long.parseLong(destaccount.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long sourceFinalAmount = TxnAmount + SrcChargeAmount;
            Long destinationFinalAmount = TxnAmount - DestChargeAmount;
            Long sourceUpdatedBalance =  SrcAcctActBalance + sourceFinalAmount;
            Long destinationUpdatedBalance = DestAcctActBalance - destinationFinalAmount;

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (destinationFinalAmount < 0) {
                logger.error("Calculated destination amount [" + destinationFinalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            srcaccount.setActualBalance((sourceUpdatedBalance) + "");
            srcaccount.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(srcaccount);

            destaccount.setActualBalance((destinationUpdatedBalance) + "");
            destaccount.setAvailableBalance((destinationUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(destaccount);

            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(srcaccount);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(sourceFinalAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(srcaccount.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.CREDIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(SrcAcctAvailBalance + "");
            wsmodel.setClosingbalance(srcaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            WalletBalanceLog destbalanceLog = new WalletBalanceLog();
            destbalanceLog.setWallet(destaccount);
            destbalanceLog.setChannelid(wsmodel.getChannelid());
            destbalanceLog.setAmount(destinationFinalAmount + "");
            destbalanceLog.setOriginalbalance(DestAcctAvailBalance+"");
            destbalanceLog.setUpdatedbalance(destaccount.getAvailableBalance());
            destbalanceLog.setTxnname(wsmodel.getServicename());
            destbalanceLog.setTransaction(wsmodel);
            destbalanceLog.setTxnnature(TxnFlag.DEBIT);
            destbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(destbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setDestOpeningbalance(DestAcctAvailBalance + "");
            wsmodel.setDestClosingbalance(destaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(srcaccount);
            WGL.setAmount(sourceFinalAmount + "");
            WGL.setCurrency(srcaccount.getCurrency());
            WGL.setWalletflag(true);
            WGL.setClosingBalance(srcaccount.getAvailableBalance());
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setWallet(destaccount);
            WGL2.setAmount((destinationFinalAmount) + "");
            WGL2.setCurrency(destaccount.getCurrency());
            WGL2.setWalletflag(true);
            WGL2.setClosingBalance(destaccount.getAvailableBalance());
            WGL2.setPreviousBalance(DestAcctAvailBalance+"");
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                //UpdateandLogCollectionAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
                UpdateandLogRevenueAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Wallet-To-Wallet Transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Wallet-To-Wallet Transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Wallet-To-Wallet Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantTransaction(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for MerchantTransaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long SrcAcctActBalance = Long.parseLong(account.getActualBalance());
            Long SrcAcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            if(SrcAcctAvailBalance < (TxnAmount + SrcChargeAmount))
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((SrcAcctActBalance - (TxnAmount + SrcChargeAmount)) + "");
            account.setAvailableBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount)) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount((TxnAmount + SrcChargeAmount) + "");
            balanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(SrcAcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(SrcAcctAvailBalance +"");
            WGL.setClosingBalance((SrcAcctAvailBalance - TxnAmount) + "");
            GeneralDao.Instance.save(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setBusinessaccount(GetBusinessWallet(account.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue()));
            WGL2.setAmount((TxnAmount - DestChargeAmount) + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            //UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);
            UpdateandLogMerchantSettlementWallet(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            //String tax = wsmodel.getNayapaytaxamount();
            //wsmodel.setNayapaytaxamount("");
            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((SrcAcctAvailBalance - TxnAmount)+"");
                WGL2.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }
            //wsmodel.setNayapaytaxamount(tax);

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Merchant transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //TODO: Raza Merchant account/payables will be maintained at Switch and will be handled at this API response
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Merchant transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Merchant Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantCoreTransaction(WalletCMSWsEntity wsmodel, CMSAccount linkAccount)
    {
        try {
            logger.info("Loading Business Wallet for MerchantCoreTransaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            /*
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(linkAccount);
            WGL.setAmount((TxnAmount + SrcChargeAmount) + "");
            WGL.setCurrency(linkAccount.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setBusinessaccount(GetBusinessWallet(linkAccount.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue()));
            WGL2.setAmount((TxnAmount - DestChargeAmount) + "");
            WGL2.setCurrency(linkAccount.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(linkAccount.getBranchId());

            //m.rehman: 07-07-2020: On Nayapay request, changing isChargeApply to true
            UpdateandLogPartnerBankCollectionAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT,
                    AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, true, false);

            UpdateandLogMerchantSettlementWallet(wsmodel, linkAccount.getCurrency(), TxnFlag.CREDIT);

            //String tax = wsmodel.getNayapaytaxamount();
            //wsmodel.setNayapaytaxamount("");
            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.CREDIT);
            }
            //wsmodel.setNayapaytaxamount(tax);

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Merchant Core transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Merchant Core transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Merchant Core Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean OnelinkCoreTransaction(WalletCMSWsEntity wsmodel, CMSAccount linkAccount)
    {
        try {
            logger.info("UnLoading Wallet for OnelinkCoreTransaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(linkAccount.getBranchId());
            UpdateandLogPartnerBankCollectionAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT,
                    AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, false, false);

            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.CREDIT, false, false, false, false);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, linkAccount.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Onelink Core transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Onelink Core transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Onelink Core Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean RevMerchantTransaction(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Rev-UnLoading Wallet for Rev-MerchantTransaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long SrcAcctActBalance = Long.parseLong(account.getActualBalance());
            Long SrcAcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            /*if(SrcAcctAvailBalance < (TxnAmount+SrcChargeAmount))
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }*/

            account.setActualBalance((SrcAcctActBalance + (TxnAmount + SrcChargeAmount)) + "");
            account.setAvailableBalance((SrcAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount((TxnAmount + SrcChargeAmount) + "");
            balanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(SrcAcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount((TxnAmount + SrcChargeAmount) + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setAmount((TxnAmount - DestChargeAmount) + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);

            /*
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT);

            if(SrcChargeAmount > 0 || DestChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }
            */

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Rev-Merchant transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //TODO: Raza Merchant account/payables will be maintained at Switch and will be handled at this API response
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Rev-Merchant transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Rev-Merchant Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CashDeposit(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for CashDeposit Transaction....");

            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            //Long DepositAmount = (Util.hasText(wsmodel.getDepositamount()) ? Long.parseLong(wsmodel.getDepositamount()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long finalAmount = (TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount));

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (finalAmount < 0) {
                logger.error("Calculated amount [" + finalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }
            /*
            if (finalAmount < 0)
            {
                logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }
            */

//            account.setActualBalance((AcctActBalance + (DepositAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount))) + "");
//            account.setAvailableBalance((AcctAvailBalance + (DepositAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount))) + "");

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance+"");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(DepositAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_IN,
                    AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), false, false);

            //m.rehman: 07-07-2020: On Nayapay request, stop charge to Partner bank (commenting below)
            //UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN,
            //        AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), true, true);

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN, AccType.CAT_SETT_ACCT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("CashDeposit in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("CashDeposit in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Depositing Cash in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean ChequeFT(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for ChequeFT Transaction....");

            Long TotalAmount = (Util.hasText(wsmodel.getTotalamount()) ? Long.parseLong(wsmodel.getTotalamount()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long NayaPayTaxAmount = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long DepositAmount = (Util.hasText(wsmodel.getDepositamount()) ? Long.parseLong(wsmodel.getDepositamount()) : 0L);


            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            if(DepositAmount < (TotalAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount)))
            {
                logger.error("Insufficient Amount for CashDeposit Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            account.setActualBalance((AcctActBalance + DepositAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + DepositAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTransaction(wsmodel);
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag("Credit");
            WGL.setWallet(account);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency("586");
            WGL.setWalletflag(true);
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTransaction(wsmodel);
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag("Debit");
            WGL2.setSettlementaccount("Settlement Account");
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency("586");
            WGL2.setWalletflag(false);
            GeneralDao.Instance.saveOrUpdate(WGL2);


            GeneralLedger GL = new GeneralLedger();
            GL.setDate(new Date());
            GL.setAccount(account);
            GL.setTransaction(wsmodel.getServicename());
            GL.setTxnFlag("Credit");
            GL.setCreditAmount(wsmodel.getDepositamount());
            GL.setCurrency(account.getCurrency());
            GL.setClosingBalance(account.getActualBalance());
            GL.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(GL);

            CustomerLedger CustLedger = new CustomerLedger();
            CustLedger.setDate(new Date());
            CustLedger.setCustomer(account.getCustomer());
            CustLedger.setAccount(account);
            CustLedger.setTransaction(wsmodel.getServicename());
            CustLedger.setTxnFlag("Credit");
            CustLedger.setCreditAmount(wsmodel.getDepositamount());
            CustLedger.setCurrency(account.getCurrency());
            CustLedger.setClosingBalance(account.getActualBalance());
            CustLedger.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(CustLedger);

            if(NayaPayChargeAmount != null && NayaPayChargeAmount > 0L) //TODO: Raza use with above Check
            {
                RevenueLedger RevLedger = new RevenueLedger();
                RevLedger.setDate(new Date());
                RevLedger.setAccount(account);
                RevLedger.setTransaction(wsmodel.getServicename());
                RevLedger.setTxnFlag("Credit");
                RevLedger.setCreditAmount(wsmodel.getNayapaycharges());
                RevLedger.setCurrency(account.getCurrency());
                //RevLedger.setClosingBalance(account.getActualBalance());
                RevLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(RevLedger);
            }

            if(NayaPayTaxAmount != null && NayaPayTaxAmount > 0L) //TODO: Raza use with above Check
            {
                SalesTaxLedger SalesTaxLedger = new SalesTaxLedger();
                SalesTaxLedger.setDate(new Date());
                SalesTaxLedger.setAccount(account);
                SalesTaxLedger.setTransaction(wsmodel.getServicename());
                SalesTaxLedger.setTxnFlag("Credit");
                SalesTaxLedger.setCreditAmount(wsmodel.getNayapaytaxamount());
                SalesTaxLedger.setCurrency(account.getCurrency());
                //SalesTaxLedger.setClosingBalance(account.getActualBalance());
                SalesTaxLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(SalesTaxLedger);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("ChequeFT in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("ChequeFT in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while executing Cheque Funds Transfer in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean ChequeClearing(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for ChequeClearing Transaction....");


            logger.info("No Ledger Operation Performed for ChequeClearing Transaction....");


            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("ChequeClearing in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("ChequeClearing in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Depositing Cash in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean ChequeBounce(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for ChequeBounce Transaction....");

            logger.info("No ChequeBounce Operation Performed for ChequeClearing Transaction....");

            wsmodel.setRespcode(ISOResponseCodes.APPROVED);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("ChequeBounce in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("ChequeBounce in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while executing ChequeBounce in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: for NayaPay, Onelink Bill Payment
    @Transactional
    public static boolean BillPayment(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for BillPayment Transaction....");

            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long Tax  = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);

            Long finalAmount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long finalAmount = null;
            //Long ClosingBalance = null;

            if(AcctAvailBalance < finalAmount || AcctActBalance < finalAmount)
            {
                logger.error("Insufficient Amount for BillPayment Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetBillerAccount((Util.hasText(wsmodel.getUtilcompanyid())) ? wsmodel.getUtilcompanyid() : wsmodel.getBillerid(), account.getCurrency()));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            //UpdateandLogBillerCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false, false);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("BillPayment in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("BillPayment in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while BillPayment in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean ReverseBillPayment(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for ReverseBillPayment Transaction....");

            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long Tax  = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);

            Long finalAmount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetBillerAccount((Util.hasText(wsmodel.getUtilcompanyid())) ? wsmodel.getUtilcompanyid() : wsmodel.getBillerid(), account.getCurrency()));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            //UpdateandLogBillerCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            // Asim Shahzad, Date : 15th Oct 2020, Tracking ID : VC-NAP-202010142
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false, true);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("ReverseBillPayment in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while ReverseBillPayment in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //Raza For 1Link Socket Issuing start
    @Transactional
    public static boolean BalanceInquiry(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for BalanceInquiry Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            //Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;
            SrcChargeAmount = SrcChargeAmount - Tax;
            Long finalAmount = AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for BalanceInquiry Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount((SrcChargeAmount - Tax) + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            wsmodel.setSrcchargeamount(StringUtils.leftPad(((SrcChargeAmount != null) ? SrcChargeAmount.toString() : "0"), 12, "0"));
            wsmodel.setNayapaytaxamount("");

            //m.rehman: Euronet integration
            //adding check for Euronet
            if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("PayPak")) {
            	// Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
                UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, true, true, false);
            	// ===================================================================================
            } else if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("Visa")) {
                UpdateandLog1LinkVisaSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, true, true);
            }

            /*
            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }
            */

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("BalanceInquiry in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("BalanceInquiry in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing BalanceInquiry in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CashWithDrawal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for CashWithDrawal Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            SrcChargeAmount = SrcChargeAmount - Tax;
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());


            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for CashWithdrawal Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            account.setActualBalance((AcctActBalance - (finalAmount + Tax)) + "");
            account.setAvailableBalance((AcctAvailBalance - (finalAmount + Tax)) + "");
            //=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            wsmodel.setSrcchargeamount(StringUtils.leftPad(((SrcChargeAmount != null) ? SrcChargeAmount.toString() : "0"), 12, "0"));
            wsmodel.setNayapaytaxamount("");
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);

			//m.rehman: Euronet integration
            //adding check for Euronet
            if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("PayPak")) {
	            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
	            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, true, false);
	            // ===================================================================================
            } else if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("Visa")) {
                //m.rehman: 15-07-2021, VC-NAP-202107151 - Total amount not crediting in Visa Local Cash Withdrawal/ECommerce reversal amount not crediting in wallet
                //setting isApplyOnlyCharges parameter from true to false
                UpdateandLog1LinkVisaSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, true);
            }

            /*
            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }
            */

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("CashWithdrawal in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("CashWithdrawal in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing Withdrawal Cash in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean Purchase(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for Purchase Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            SrcChargeAmount = SrcChargeAmount - Tax;
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());


            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for Purchase Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            wsmodel.setSrcchargeamount(StringUtils.leftPad(((SrcChargeAmount != null) ? SrcChargeAmount.toString() : "0"), 12, "0"));
            wsmodel.setNayapaytaxamount("");
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);

			//m.rehman: Euronet integration
            //adding check for Euronet
            if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("PayPak")) {
            	// Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            	UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, true, false);
            	// ===================================================================================
            } else if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("Visa")) {
                UpdateandLogVisaLocalSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, true, true);
            }

            /*
            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }
            */

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Purchase in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Purchase in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing Purchase in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CardBasedReversal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for " + wsmodel.getServicename() + " Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long finalAmount = txnAmount + SrcChargeAmount + BankCharges + BankTaxAmount + AmtTranFee;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);

            //m.rehman: Euronet integration
            //adding check for Euronet
            if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("PayPak")) {
            	// Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
                UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, true);
            	// ===================================================================================
            } else if (Util.hasText(wsmodel.getCardscheme()) && wsmodel.getCardscheme().equals("Visa")) {
                if (wsmodel.getOriginalapi().equals("Purchase") || wsmodel.getOriginalapi().equals("ECommerce")
                        || wsmodel.getOriginalapi().equals("MOTO")) {
                    UpdateandLogVisaLocalSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true);
                }
                else {
                    UpdateandLog1LinkVisaSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true);
                }
            }

            /*
            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }
            */

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info(wsmodel.getServicename() + " in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info(wsmodel.getServicename() + " in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing Withdrawal Cash in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }
    //Raza For 1Link Socket Issuing end

    @Transactional
    public static boolean EnvelopLoad(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount poolaccount)
    {
        try {
            logger.info("UnLoading Wallet for EnvelopLoad Transaction....");

            Long TotalAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long NayaPayTaxAmount = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            Long FinalWithdrawalAmount = TotalAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());


            if(FinalWithdrawalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for EnvelopLoad Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            account.setActualBalance((AcctActBalance - FinalWithdrawalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - FinalWithdrawalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);
            String PoolAcctPrevBalance = poolaccount.getActualBalance();
            poolaccount.setActualBalance((Long.parseLong(poolaccount.getActualBalance()) + TotalAmount) + "");
            poolaccount.setAvailableBalance((Long.parseLong(poolaccount.getAvailableBalance()) + TotalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(poolaccount);

            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTransaction(wsmodel);
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag("Debit");
            WGL.setWallet(account);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency("586");
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getActualBalance());
            WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTransaction(wsmodel);
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag("Credit");
            WGL2.setWallet(poolaccount);
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency("586");
            WGL2.setWalletflag(false);
            WGL2.setClosingBalance(poolaccount.getActualBalance());
            WGL2.setPreviousBalance(PoolAcctPrevBalance);
            GeneralDao.Instance.saveOrUpdate(WGL2);

            GeneralLedger GL = new GeneralLedger();
            GL.setDate(new Date());
            GL.setAccount(account);
            GL.setTransaction(wsmodel.getServicename());
            GL.setTxnFlag("Debit");
            GL.setDebitAmount(wsmodel.getAmounttransaction());
            GL.setCurrency(account.getCurrency());
            GL.setClosingBalance(account.getActualBalance());
            GL.setPreviousBalance(AcctActBalance+"");
            GL.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(GL);

            CustomerLedger CustLedger = new CustomerLedger();
            CustLedger.setDate(new Date());
            CustLedger.setCustomer(account.getCustomer());
            CustLedger.setAccount(account);
            CustLedger.setTransaction(wsmodel.getServicename());
            CustLedger.setTxnFlag("Debit");
            CustLedger.setDebitAmount(wsmodel.getAmounttransaction());
            CustLedger.setCurrency(account.getCurrency());
            CustLedger.setClosingBalance(account.getActualBalance());
            CustLedger.setPreviousBalance(AcctActBalance+"");
            CustLedger.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(CustLedger);

            if(NayaPayChargeAmount != null && NayaPayChargeAmount > 0L) //TODO: Raza use with above Check
            {
                RevenueLedger RevLedger = new RevenueLedger();
                RevLedger.setDate(new Date());
                RevLedger.setAccount(account);
                RevLedger.setTransaction(wsmodel.getServicename());
                RevLedger.setTxnFlag("Credit");
                RevLedger.setCreditAmount(wsmodel.getNayapaycharges());
                RevLedger.setCurrency(account.getCurrency());
                //RevLedger.setClosingBalance(account.getActualBalance());
                RevLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(RevLedger);
            }

            if(NayaPayTaxAmount != null && NayaPayTaxAmount > 0L) //TODO: Raza use with above Check
            {
                SalesTaxLedger SalesTaxLedger = new SalesTaxLedger();
                SalesTaxLedger.setDate(new Date());
                SalesTaxLedger.setAccount(account);
                SalesTaxLedger.setTransaction(wsmodel.getServicename());
                SalesTaxLedger.setTxnFlag("Credit");
                SalesTaxLedger.setCreditAmount(wsmodel.getNayapaytaxamount());
                SalesTaxLedger.setCurrency(account.getCurrency());
                //SalesTaxLedger.setClosingBalance(account.getActualBalance());
                SalesTaxLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(SalesTaxLedger);
            }

            logger.info("EnvelopLoad in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing EnvelopLoad in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean EnvelopUnLoad(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount poolaccount)
    {
        try {
            logger.info("UnLoading Wallet for UnEnvelopLoad Transaction....");

            Long TotalAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long NayaPayTaxAmount = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            Long FinalWithdrawalAmount = TotalAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());


            if(FinalWithdrawalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for EnvelopLoad Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            account.setActualBalance((AcctActBalance + FinalWithdrawalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + FinalWithdrawalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);
            String PoolAcctPrevBalance = poolaccount.getActualBalance();
            poolaccount.setActualBalance((Long.parseLong(poolaccount.getActualBalance()) - TotalAmount) + "");
            poolaccount.setAvailableBalance((Long.parseLong(poolaccount.getAvailableBalance()) - TotalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(poolaccount);


            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTransaction(wsmodel);
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag("Credit");
            WGL.setWallet(account);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency("586");
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getActualBalance());
            WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTransaction(wsmodel);
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag("Debit");
            WGL2.setWallet(poolaccount);
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency("586");
            WGL2.setWalletflag(false);
            WGL2.setClosingBalance(poolaccount.getActualBalance());
            WGL2.setPreviousBalance(PoolAcctPrevBalance);
            GeneralDao.Instance.saveOrUpdate(WGL2);

            GeneralLedger GL = new GeneralLedger();
            GL.setDate(new Date());
            GL.setAccount(account);
            GL.setTransaction(wsmodel.getServicename());
            GL.setTxnFlag("Credit");
            GL.setCreditAmount(wsmodel.getAmounttransaction());
            GL.setCurrency(account.getCurrency());
            GL.setClosingBalance(account.getActualBalance());
            GL.setPreviousBalance(AcctActBalance+"");
            GL.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(GL);

            CustomerLedger CustLedger = new CustomerLedger();
            CustLedger.setDate(new Date());
            CustLedger.setCustomer(account.getCustomer());
            CustLedger.setAccount(account);
            CustLedger.setTransaction(wsmodel.getServicename());
            CustLedger.setTxnFlag("Credit");
            CustLedger.setCreditAmount(wsmodel.getAmounttransaction());
            CustLedger.setCurrency(account.getCurrency());
            CustLedger.setClosingBalance(account.getActualBalance());
            CustLedger.setPreviousBalance(AcctActBalance+"");
            CustLedger.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(CustLedger);

            if(NayaPayChargeAmount != null && NayaPayChargeAmount > 0L) //TODO: Raza use with above Check
            {
                RevenueLedger RevLedger = new RevenueLedger();
                RevLedger.setDate(new Date());
                RevLedger.setAccount(account);
                RevLedger.setTransaction(wsmodel.getServicename());
                RevLedger.setTxnFlag("Credit");
                RevLedger.setCreditAmount(wsmodel.getNayapaycharges());
                RevLedger.setCurrency(account.getCurrency());
                //RevLedger.setClosingBalance(account.getActualBalance());
                RevLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(RevLedger);
            }

            if(NayaPayTaxAmount != null && NayaPayTaxAmount > 0L) //TODO: Raza use with above Check
            {
                SalesTaxLedger SalesTaxLedger = new SalesTaxLedger();
                SalesTaxLedger.setDate(new Date());
                SalesTaxLedger.setAccount(account);
                SalesTaxLedger.setTransaction(wsmodel.getServicename());
                SalesTaxLedger.setTxnFlag("Credit");
                SalesTaxLedger.setCreditAmount(wsmodel.getNayapaytaxamount());
                SalesTaxLedger.setCurrency(account.getCurrency());
                //SalesTaxLedger.setClosingBalance(account.getActualBalance());
                SalesTaxLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(SalesTaxLedger);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("EnvelopLoad in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("EnvelopLoad in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing EnvelopLoad in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean ReverseEnvelop(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount poolaccount)
    {
        try {
            logger.info("Loading Wallet for Reverse Transaction....");

            Long TotalAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long NayaPayTaxAmount = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            Long FinalWithdrawalAmount = TotalAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long PoolAcctActualBalance = Long.parseLong(poolaccount.getActualBalance());


            if(FinalWithdrawalAmount > PoolAcctActualBalance)
            {
                logger.error("Insufficient Amount in pool account for ReverseEnvelop Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            account.setActualBalance((AcctActBalance + FinalWithdrawalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + FinalWithdrawalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);
            String PoolAcctPrevBalance = poolaccount.getActualBalance();
            poolaccount.setActualBalance((Long.parseLong(poolaccount.getActualBalance()) - TotalAmount) + "");
            poolaccount.setAvailableBalance((Long.parseLong(poolaccount.getAvailableBalance()) - TotalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(poolaccount);


            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTransaction(wsmodel);
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag("Credit");
            WGL.setWallet(account);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency("586");
            WGL.setWalletflag(true);
            WGL.setClosingBalance(account.getActualBalance());
            WGL.setPreviousBalance(AcctActBalance+"");
            GeneralDao.Instance.saveOrUpdate(WGL);

            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTransaction(wsmodel);
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL2.setTxnflag("Debit");
            WGL2.setWallet(poolaccount);
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency("586");
            WGL2.setWalletflag(false);
            WGL2.setClosingBalance(poolaccount.getActualBalance());
            WGL2.setPreviousBalance(PoolAcctPrevBalance);
            GeneralDao.Instance.saveOrUpdate(WGL2);

            GeneralLedger GL = new GeneralLedger();
            GL.setDate(new Date());
            GL.setAccount(account);
            GL.setTransaction(wsmodel.getServicename());
            GL.setTxnFlag("Credit");
            GL.setCreditAmount(wsmodel.getAmounttransaction());
            GL.setCurrency(account.getCurrency());
            GL.setClosingBalance(account.getActualBalance());
            GL.setPreviousBalance(AcctActBalance+"");
            GL.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(GL);

            CustomerLedger CustLedger = new CustomerLedger();
            CustLedger.setDate(new Date());
            CustLedger.setCustomer(account.getCustomer());
            CustLedger.setAccount(account);
            CustLedger.setTransaction(wsmodel.getServicename());
            CustLedger.setTxnFlag("Credit");
            CustLedger.setCreditAmount(wsmodel.getAmounttransaction());
            CustLedger.setCurrency(account.getCurrency());
            CustLedger.setClosingBalance(account.getActualBalance());
            CustLedger.setPreviousBalance(AcctActBalance+"");
            CustLedger.setTxnReference(wsmodel.getTranrefnumber());
            GeneralDao.Instance.saveOrUpdate(CustLedger);

            if(NayaPayChargeAmount != null && NayaPayChargeAmount > 0L) //TODO: Raza use with above Check
            {
                RevenueLedger RevLedger = new RevenueLedger();
                RevLedger.setDate(new Date());
                RevLedger.setAccount(account);
                RevLedger.setTransaction(wsmodel.getServicename());
                RevLedger.setTxnFlag("Credit");
                RevLedger.setCreditAmount(wsmodel.getNayapaycharges());
                RevLedger.setCurrency(account.getCurrency());
                //RevLedger.setClosingBalance(account.getActualBalance());
                RevLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(RevLedger);
            }

            if(NayaPayTaxAmount != null && NayaPayTaxAmount > 0L) //TODO: Raza use with above Check
            {
                SalesTaxLedger SalesTaxLedger = new SalesTaxLedger();
                SalesTaxLedger.setDate(new Date());
                SalesTaxLedger.setAccount(account);
                SalesTaxLedger.setTransaction(wsmodel.getServicename());
                SalesTaxLedger.setTxnFlag("Credit");
                SalesTaxLedger.setCreditAmount(wsmodel.getNayapaytaxamount());
                SalesTaxLedger.setCurrency(account.getCurrency());
                //SalesTaxLedger.setClosingBalance(account.getActualBalance());
                SalesTaxLedger.setTxnReference(wsmodel.getTranrefnumber());
                GeneralDao.Instance.saveOrUpdate(SalesTaxLedger);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("EnvelopLoad in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("EnvelopLoad in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing EnvelopLoad in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: for NayaPay, adding new call for document 2.0 <start>
    @Transactional
    public static boolean MerchantReversalTransaction(WalletCMSWsEntity wsmodel, CMSAccount account, CMSAccount linkaccount)
    {
        try {
            logger.info("Loading Wallet for MerchantReversalTransaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long txnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            Long finalAmount = txnAmount + SrcChargeAmount;

            //m.rehman: 28-07-2020, case handling for Wallet to Merchant and Linked account
            // Asim Shahzad, Date : 30th Sep 2020, Tracking ID : VC-NAP-202008311
            if (Util.hasText(wsmodel.getNayapaytrantype()) &&
                    (wsmodel.getNayapaytrantype().equals("MerchantBillerCoreTransaction")
                            || wsmodel.getNayapaytrantype().equals("MerchantRetailCoreTransaction"))) {
            // ==================================================================
                String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(linkaccount.getBranchId());
                UpdateandLogPartnerBankCollectionAccount(wsmodel, linkaccount.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
                        AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, true, false);
            } else {

                logger.info("Updating Wallet Balance ...");
                account.setActualBalance((AcctActBalance + finalAmount) + "");
                account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log ...");
                WalletBalanceLog balanceLog = new WalletBalanceLog();
                balanceLog.setWallet(account);
                balanceLog.setChannelid(wsmodel.getChannelid());
                balanceLog.setAmount(finalAmount + "");
                balanceLog.setOriginalbalance(AcctAvailBalance + "");
                balanceLog.setUpdatedbalance(account.getAvailableBalance());
                balanceLog.setTxnname(wsmodel.getServicename());
                balanceLog.setTransaction(wsmodel);
                balanceLog.setTxnnature(TxnFlag.CREDIT);
                balanceLog.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLog);

                //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
                wsmodel.setOpeningbalance(AcctAvailBalance + "");
                wsmodel.setClosingbalance(account.getAvailableBalance());
                /////////////////////////////////////////////////////////////////////////////////////////////////////

                logger.info("Updating Wallet General Ledger ...");
                WalletGeneralLedger WGL = new WalletGeneralLedger();
                WGL.setTxnname(wsmodel.getServicename());
                WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
                WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL.setTxnflag(TxnFlag.CREDIT);
                WGL.setWallet(account);
                WGL.setAmount(finalAmount + "");
                WGL.setCurrency(account.getCurrency());
                WGL.setWalletflag(true);
                WGL.setMerchantid(wsmodel.getMerchantid());
                WGL.setAgentid(wsmodel.getAgentid());
                WGL.setBillerid(wsmodel.getBillerid());
                WGL.setTransaction(wsmodel);
                WGL.setPreviousBalance(AcctAvailBalance + "");
                WGL.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGL);
            }

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setBusinessaccount(GetBusinessWallet(account.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue()));
            WGL2.setAmount(txnAmount - DestChargeAmount - Tax + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogMerchantSettlementWallet(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Merchant Reversal transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //TODO: Raza Merchant account/payables will be maintained at Switch and will be handled at this API response
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Merchant Reversal transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Loading wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }
    //m.rehman: for NayaPay, adding new call for document 2.0 <end>

    @Transactional
    public static boolean CNICBasedCashWithdrawal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for CNICBasedCashWithdrawal Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long tranFeeAmount = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long finalAmount = txnAmount + NayapayChargeAmount + BankCharges + BankTaxAmount + tranFeeAmount + SrcChargeAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for CNICBasedCashWithdrawal Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);
            UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
                        AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), false, false);

            //m.rehman: 07-07-2020: On Nayapay request, stop charge to Partner bank (commenting below)
            //UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
            //        AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), true, true);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("CNICBasedCashWithdrawal in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("CNICBasedCashWithdrawal in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing CNICBasedCashWithdrawal in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CNICBasedCashWithdrawalReversal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for CNICBasedCashWithdrawalReversal Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long tranFeeAmount = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long finalAmount = txnAmount + NayapayChargeAmount + BankCharges + BankTaxAmount + tranFeeAmount + SrcChargeAmount;
            SrcChargeAmount = SrcChargeAmount - Tax;//s.mehtab: 15-12-2020 VC-NAP-202012151

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");//s.mehtab: 15-12-2020 VC-NAP-202012151
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);
            UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT,
                    AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), false, false);

            //m.rehman: 07-07-2020: On Nayapay request, stop charge to Partner bank (commenting below)
            //UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT,
            //        AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), true, true);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("CNICBasedCashWithdrawalReversal in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("CNICBasedCashWithdrawalReversal in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////

        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing CNICBasedCashWithdrawalReversal in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CNICBasedCashWithdrawalOnUSReversal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for On Us CashWithDrawalOnUsReversal Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Double NayapayChargeTaxAmount = 0.0;
            Long OnUsFee = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Double OnUsFeeTaxAmount = 0.0;
            Long receiptCharges = (Util.hasText(wsmodel.getReceiptcharges()) ? Long.parseLong(wsmodel.getReceiptcharges()) : 0L);//ReceiptCharges
            Double receiptChargesTax = 0.0;
            Long balInqCharges = (Util.hasText(wsmodel.getBalanceinquirycharges()) ? Long.parseLong(wsmodel.getBalanceinquirycharges()) : 0L);
            Double balInqChargesTax = 0.0;
            Long tax = 0L;

            Tax sstTax;
            String dbQuery = null;
            Map<String, Object> params = new HashMap<>();

            dbQuery = "from " + Tax.class.getName() + " t where title='SST'";
            params = new HashMap<>();
            sstTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);

            if (sstTax != null) {
                logger.info("Tax rate found, calculating amount ...");
                if(NayapayChargeAmount > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        NayapayChargeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0) * (NayapayChargeAmount / 100.0);
                        logger.info("Update amount for Nayapay ChargeTax Amount [" + NayapayChargeTaxAmount + "]");
                    } else {
                        NayapayChargeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for Nayapay ChargeTax Amount [" + NayapayChargeTaxAmount + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedNPChargeAmtTaxAmountStr = dft.format(NayapayChargeTaxAmount);
                    updatedNPChargeAmtTaxAmountStr = updatedNPChargeAmtTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Charge Tax Amount [" + updatedNPChargeAmtTaxAmountStr + "]");
                    wsmodel.setNayapaychargestax(StringUtils.leftPad(updatedNPChargeAmtTaxAmountStr, 12, "0"));
                }
                if(OnUsFee > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        OnUsFeeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0) * (OnUsFee / 100.0);
                        logger.info("Update amount for OnUs Charges Tax Amount [" + OnUsFeeTaxAmount + "]");
                    } else {
                        OnUsFeeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for OnUs Charges Tax Amount [" + OnUsFeeTaxAmount + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedBankTaxAmountStr = dft.format(OnUsFeeTaxAmount);
                    updatedBankTaxAmountStr = updatedBankTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay OnUs Charges Tax Amount [" + updatedBankTaxAmountStr + "]");
                    wsmodel.setOnuschargestax(StringUtils.leftPad(updatedBankTaxAmountStr, 12, "0"));
                }
                if(receiptCharges > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        receiptChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0) * (receiptCharges / 100.0);
                        logger.info("Update amount for receiptChargesTax [" + receiptChargesTax + "]");
                    } else {
                        receiptChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for receiptChargesTax [" + receiptChargesTax + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedreceiptChargesTaxAmountStr = dft.format(receiptChargesTax);
                    updatedreceiptChargesTaxAmountStr = updatedreceiptChargesTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Receipt Charges Tax Amount [" + updatedreceiptChargesTaxAmountStr + "]");
                    wsmodel.setReceiptchargestax(StringUtils.leftPad(updatedreceiptChargesTaxAmountStr, 12, "0"));
                }
                if(balInqCharges > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        balInqChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0) * (balInqCharges / 100.0);
                        logger.info("Update amount for Balance Inquiry Charges Tax [" + balInqChargesTax + "]");
                    } else {
                        balInqChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for Balance Inquiry Charges  [" + balInqChargesTax + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedbalInqChargesTaxAmountStr = dft.format(balInqChargesTax);
                    updatedbalInqChargesTaxAmountStr = updatedbalInqChargesTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Receipt Charges Tax Amount [" + updatedbalInqChargesTaxAmountStr + "]");
                    wsmodel.setBalinqchargestax(StringUtils.leftPad(updatedbalInqChargesTaxAmountStr, 12, "0"));
                }
            }

            //tax = Math.round(NayapayChargeTaxAmount + BankTaxAmount + receiptChargesTax);

            Long finalAmount = txnAmount + (Util.hasText(wsmodel.getNayapaychargestax()) ? Long.parseLong(wsmodel.getNayapaychargestax()) : 0L) / 100 +
                    (Util.hasText(wsmodel.getOnuschargestax()) ? Long.parseLong(wsmodel.getOnuschargestax()) : 0L) / 100 +
                    (Util.hasText(wsmodel.getReceiptchargestax()) ? Long.parseLong(wsmodel.getReceiptchargestax()) : 0L) / 100 +
                    (Util.hasText(wsmodel.getBalinqchargestax()) ? Long.parseLong(wsmodel.getBalinqchargestax()) : 0L) / 100;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());


            //Committed by Huzaifa
//            if(finalAmount > AcctAvailBalance)
//            {
//                logger.error("Insufficient Amount for On Us CashWithdrawal Transaction, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
//                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
//                wsmodel.setAcctbalance(AcctAvailBalance.toString());
//                // ====================================================================================
//                return false;
//            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + txnAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(txnAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 22nd Feb 2022, Tracking ID : VC-NAP-202202104
            //balanceLog.setTransDT(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            // ==================================================================

            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            CMSEMIAccountCollection settlementaccount=null;
            CMSEMIWallet settlementWallet=null;

            if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                settlementaccount = GetEMICollectionAccountForUnil(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_SETT_ACCT);
//                settlementWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_SETT_WLLT);
                settlementWallet = settlementaccount.getEmiwallet();
            }

            logger.info(settlementaccount.getCategory()  + "-" + settlementWallet.getCategory());

            if(settlementaccount != null && settlementWallet != null) {

                UpdateandLogOnUsATMSettlementtAccount(wsmodel, TxnFlag.DEBIT, false, false, false, false, settlementaccount, settlementWallet);
            }

            if(NayapayChargeAmount > 0) {
                logger.info("Updating Wallet Balance for Nayapay charges and tax...");

                //Huzaifa refreshing balance = 12/01/2023
                AcctActBalance = Long.parseLong(account.getActualBalance());
                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
                //=======================================

                account.setActualBalance((AcctActBalance + NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax())) + "");
                account.setAvailableBalance((AcctAvailBalance + NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax())) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log for Nayapay charges and tax...");
                WalletBalanceLog balanceLogNPCharges = new WalletBalanceLog();
                balanceLogNPCharges.setWallet(account);
                balanceLogNPCharges.setChannelid(wsmodel.getChannelid());
                balanceLogNPCharges.setAmount(NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax()) + "");
                balanceLogNPCharges.setOriginalbalance(AcctAvailBalance + "");
                balanceLogNPCharges.setUpdatedbalance(account.getAvailableBalance());
                balanceLogNPCharges.setTxnname(wsmodel.getServicename());
                balanceLogNPCharges.setTransaction(wsmodel);
                balanceLogNPCharges.setTxnnature(TxnFlag.CREDIT);
                balanceLogNPCharges.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLogNPCharges);

                logger.info("Updating Wallet General Ledger for Nayapay charges and tax...");
                WalletGeneralLedger WGLNPCharges = new WalletGeneralLedger();
                WGLNPCharges.setTxnname(wsmodel.getServicename());
                WGLNPCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGLNPCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGLNPCharges.setTxnflag(TxnFlag.CREDIT);
                WGLNPCharges.setWallet(account);
                WGLNPCharges.setAmount(NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax()) + "");
                WGLNPCharges.setCurrency(account.getCurrency());
                WGLNPCharges.setWalletflag(true);
                WGLNPCharges.setMerchantid(wsmodel.getMerchantid());
                WGLNPCharges.setAgentid(wsmodel.getAgentid());
                WGLNPCharges.setBillerid(wsmodel.getBillerid());
                WGLNPCharges.setTransaction(wsmodel);
                WGLNPCharges.setPreviousBalance(AcctAvailBalance + "");
                WGLNPCharges.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGLNPCharges);

                CMSEMIAccountCollection onUsATMFeeAccount=null;
                CMSEMIWallet onUsATMFeeWallet=null;

                if(wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    onUsATMFeeAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_FEE_ACCT);
                    onUsATMFeeWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_FEE_WLLT);
                }

                UpdateandLogOnUsATMFeeAccount(wsmodel, TxnFlag.DEBIT, onUsATMFeeAccount, onUsATMFeeWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getNayapaychargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(OnUsFee > 0)
            {
                //UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                logger.info("Updating Wallet Balance for ONUS Fee and tax...");

                //Huzaifa refreshing balance = 12/01/2023
                AcctActBalance = Long.parseLong(account.getActualBalance());
                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
                //==============================================================

                account.setActualBalance((AcctActBalance + OnUsFee + Long.parseLong(wsmodel.getOnuschargestax())) + "");
                account.setAvailableBalance((AcctAvailBalance + OnUsFee + Long.parseLong(wsmodel.getOnuschargestax())) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log for ONUS Fee and tax...");
                WalletBalanceLog balanceLogOnUsFee = new WalletBalanceLog();
                balanceLogOnUsFee.setWallet(account);
                balanceLogOnUsFee.setChannelid(wsmodel.getChannelid());
                balanceLogOnUsFee.setAmount(OnUsFee + Long.parseLong(wsmodel.getOnuschargestax()) + "");
                balanceLogOnUsFee.setOriginalbalance(AcctAvailBalance + "");
                balanceLogOnUsFee.setUpdatedbalance(account.getAvailableBalance());
                balanceLogOnUsFee.setTxnname(wsmodel.getServicename());
                balanceLogOnUsFee.setTransaction(wsmodel);
                balanceLogOnUsFee.setTxnnature(TxnFlag.CREDIT);
                balanceLogOnUsFee.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLogOnUsFee);

                logger.info("Updating Wallet General Ledger for ONUS Fee and tax...");
                WalletGeneralLedger WGLONUSFee = new WalletGeneralLedger();
                WGLONUSFee.setTxnname(wsmodel.getServicename());
                WGLONUSFee.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGLONUSFee.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGLONUSFee.setTxnflag(TxnFlag.CREDIT);
                WGLONUSFee.setWallet(account);
                WGLONUSFee.setAmount(OnUsFee + Long.parseLong(wsmodel.getOnuschargestax()) + "");
                WGLONUSFee.setCurrency(account.getCurrency());
                WGLONUSFee.setWalletflag(true);
                WGLONUSFee.setMerchantid(wsmodel.getMerchantid());
                WGLONUSFee.setAgentid(wsmodel.getAgentid());
                WGLONUSFee.setBillerid(wsmodel.getBillerid());
                WGLONUSFee.setTransaction(wsmodel);
                WGLONUSFee.setPreviousBalance(AcctAvailBalance + "");
                WGLONUSFee.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGLONUSFee);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

                wsmodel.setNayapaytaxamount(wsmodel.getOnuschargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(receiptCharges > 0)
            {
                //UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                logger.info("Updating Wallet Balance for Receipt charges and tax...");

                //Huzaifa refreshing balance = 12/01/2023
                AcctActBalance = Long.parseLong(account.getActualBalance());
                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
                //==============================================================

                account.setActualBalance((AcctActBalance + receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax())) + "");
                account.setAvailableBalance((AcctAvailBalance + receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax())) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log for Receipt charges and tax...");
                WalletBalanceLog balanceLogReciptCharges = new WalletBalanceLog();
                balanceLogReciptCharges.setWallet(account);
                balanceLogReciptCharges.setChannelid(wsmodel.getChannelid());
                balanceLogReciptCharges.setAmount(receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()) + "");
                balanceLogReciptCharges.setOriginalbalance(AcctAvailBalance + "");
                balanceLogReciptCharges.setUpdatedbalance(account.getAvailableBalance());
                balanceLogReciptCharges.setTxnname(wsmodel.getServicename());
                balanceLogReciptCharges.setTransaction(wsmodel);
                balanceLogReciptCharges.setTxnnature(TxnFlag.CREDIT);
                balanceLogReciptCharges.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLogReciptCharges);

                logger.info("Updating Wallet General Ledger for Receipt charges and tax...");
                WalletGeneralLedger WGLReciptCharges = new WalletGeneralLedger();
                WGLReciptCharges.setTxnname(wsmodel.getServicename());
                WGLReciptCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGLReciptCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGLReciptCharges.setTxnflag(TxnFlag.CREDIT);
                WGLReciptCharges.setWallet(account);
                WGLReciptCharges.setAmount(receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()) + "");
                WGLReciptCharges.setCurrency(account.getCurrency());
                WGLReciptCharges.setWalletflag(true);
                WGLReciptCharges.setMerchantid(wsmodel.getMerchantid());
                WGLReciptCharges.setAgentid(wsmodel.getAgentid());
                WGLReciptCharges.setBillerid(wsmodel.getBillerid());
                WGLReciptCharges.setTransaction(wsmodel);
                WGLReciptCharges.setPreviousBalance(AcctAvailBalance + "");
                WGLReciptCharges.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGLReciptCharges);

                CMSEMIAccountCollection receiptChargesAccount=null;
                CMSEMIWallet receiptChargesWallet=null;

                if(wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    receiptChargesAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_ACCT);
                    receiptChargesWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_WLLT);
                }

                UpdateandLogOnUsATMReceiptChargesAccount(wsmodel, TxnFlag.DEBIT, receiptChargesAccount, receiptChargesWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getReceiptchargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(balInqCharges > 0)
            {
                logger.info("Updating Wallet Balance for Balance Inquiry charges and tax...");
                //Huzaifa refreshing balance = 12/01/2023
                AcctActBalance = Long.parseLong(account.getActualBalance());
                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
                //==============================================================

                account.setActualBalance((AcctActBalance + balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax())) + "");
                account.setAvailableBalance((AcctAvailBalance + balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax())) + "");
                GeneralDao.Instance.saveOrUpdate(account);


                logger.info("Updating Wallet Balance Log for Balance Inquiry charges and tax...");
                WalletBalanceLog balanceLogbalInqCharges = new WalletBalanceLog();
                balanceLogbalInqCharges.setWallet(account);
                balanceLogbalInqCharges.setChannelid(wsmodel.getChannelid());
                balanceLogbalInqCharges.setAmount(balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()) + "");
                balanceLogbalInqCharges.setOriginalbalance(AcctAvailBalance + "");
                balanceLogbalInqCharges.setUpdatedbalance(account.getAvailableBalance());
                balanceLogbalInqCharges.setTxnname(wsmodel.getServicename());
                balanceLogbalInqCharges.setTransaction(wsmodel);
                balanceLogbalInqCharges.setTxnnature(TxnFlag.CREDIT);
                balanceLogbalInqCharges.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLogbalInqCharges);

                logger.info("Updating Wallet General Ledger for Balance Inquiry charges and tax...");
                WalletGeneralLedger WGLBalInqCharges = new WalletGeneralLedger();
                WGLBalInqCharges.setTxnname(wsmodel.getServicename());
                WGLBalInqCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGLBalInqCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGLBalInqCharges.setTxnflag(TxnFlag.CREDIT);
                WGLBalInqCharges.setWallet(account);
                WGLBalInqCharges.setAmount(balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()) + "");
                WGLBalInqCharges.setCurrency(account.getCurrency());
                WGLBalInqCharges.setWalletflag(true);
                WGLBalInqCharges.setMerchantid(wsmodel.getMerchantid());
                WGLBalInqCharges.setAgentid(wsmodel.getAgentid());
                WGLBalInqCharges.setBillerid(wsmodel.getBillerid());
                WGLBalInqCharges.setTransaction(wsmodel);
                WGLBalInqCharges.setPreviousBalance(AcctAvailBalance + "");
                WGLBalInqCharges.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGLBalInqCharges);

                CMSEMIAccountCollection BalanceInquiryAccount=null;
                CMSEMIWallet BalanceInquiryWallet=null;

                if(wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    BalanceInquiryAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_BALANCE_INQUIRY_CHRG_ACCT);
                    BalanceInquiryWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_BALANCE_INQUIRY_CHRG_WLLT);
                }

                UpdateandLogOnUsATMBalanceInquiryChargesAccount(wsmodel, TxnFlag.DEBIT, BalanceInquiryAccount, BalanceInquiryWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getBalinqchargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("On Us CashWithdrawal Reversal in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing On Us Withdrawal Reversal Cash in wallet...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean BioEnableUpgradeWallet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for BioEnable/UpgradeWallet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            //Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;
            //SrcChargeAmount = SrcChargeAmount - Tax;
            Long finalAmount = AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for BioEnable/UpgradeWallet Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            if (finalAmount > 0) {
                logger.info("Updating Wallet Balance ...");
                account.setActualBalance((AcctActBalance - finalAmount) + "");
                account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log ...");
                WalletBalanceLog balanceLog = new WalletBalanceLog();
                balanceLog.setWallet(account);
                balanceLog.setChannelid(wsmodel.getChannelid());
                balanceLog.setAmount(finalAmount + "");
                balanceLog.setOriginalbalance(AcctAvailBalance + "");
                balanceLog.setUpdatedbalance(account.getAvailableBalance());
                balanceLog.setTxnname(wsmodel.getServicename());
                balanceLog.setTransaction(wsmodel);
                balanceLog.setTxnnature(TxnFlag.DEBIT);
                balanceLog.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLog);

                //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
                wsmodel.setOpeningbalance(AcctAvailBalance + "");
                wsmodel.setClosingbalance(account.getAvailableBalance());
                /////////////////////////////////////////////////////////////////////////////////////////////////////

                logger.info("Updating Wallet General Ledger ...");
                WalletGeneralLedger WGL = new WalletGeneralLedger();
                WGL.setTxnname(wsmodel.getServicename());
                WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
                WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL.setTxnflag(TxnFlag.DEBIT);
                WGL.setWallet(account);
                WGL.setAmount(finalAmount + "");
                WGL.setCurrency(account.getCurrency());
                WGL.setWalletflag(true);
                WGL.setMerchantid(wsmodel.getMerchantid());
                WGL.setAgentid(wsmodel.getAgentid());
                WGL.setBillerid(wsmodel.getBillerid());
                WGL.setTransaction(wsmodel);
                WGL.setPreviousBalance(AcctAvailBalance + "");
                WGL.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGL);

                //m.rehman: 07-07-2020: On Nayapay request, stop charge to Partner bank (commenting below) and add Charges and Tax
                //UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
                //        AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), true, false);

                if(SrcChargeAmount > 0) //TODO: Raza use with above Check
                {
                    UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
                }

                if(Tax > 0)
                {
                    UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                }
                ///////////////////////////////////////////////////////////////////////////////////////////////////////
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("BioEnable/UpgradeWallet in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("BioEnable/UpgradeWallet in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing BioEnable/UpgradeWallet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantRefund(WalletCMSWsEntity wsmodel, WalletCMSWsEntity origTxn, CMSAccount merchantWallet, CMSAccount userWallet)
    {
        try {
            logger.info("Performing Merchant Refund Transaction....");

            Long merchantWalletActBalance = Long.parseLong(merchantWallet.getActualBalance());
            Long merchantWalletAvailBalance = Long.parseLong(merchantWallet.getAvailableBalance());
            Long userWalletActBalance = Long.parseLong(userWallet.getActualBalance());
            Long userWalletAvailBalance = Long.parseLong(userWallet.getAvailableBalance());
            Long TxnAmount;

            //if partial flag is true, use partial amount, else use original amount
            if (Util.hasText(wsmodel.getPartialflag()) && wsmodel.getPartialflag().equals("true")) {
                TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            } else {
                TxnAmount = Long.parseLong(origTxn.getAmounttransaction());
            }

            if(merchantWalletAvailBalance < TxnAmount)
            {
                logger.error("Insufficient Funds in Merchant Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE);
                return false;
            }

            logger.info("Updating Merchant Wallet Balance ...");
            merchantWallet.setActualBalance((merchantWalletActBalance - TxnAmount) + "");
            merchantWallet.setAvailableBalance((merchantWalletAvailBalance - TxnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(merchantWallet);

            logger.info("Updating Consumer Wallet Balance ...");
            userWallet.setActualBalance((userWalletActBalance + TxnAmount) + "");
            userWallet.setAvailableBalance((userWalletAvailBalance + TxnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(userWallet);

            logger.info("Updating Merchant Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(merchantWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(TxnAmount + "");
            balanceLog.setOriginalbalance(merchantWalletAvailBalance + "");
            balanceLog.setUpdatedbalance(merchantWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(merchantWalletAvailBalance + "");
            wsmodel.setClosingbalance(merchantWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Consumer Wallet Balance Log ...");
            WalletBalanceLog balanceLog2 = new WalletBalanceLog();
            balanceLog.setWallet(userWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(TxnAmount + "");
            balanceLog.setOriginalbalance(userWalletAvailBalance + "");
            balanceLog.setUpdatedbalance(userWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog2);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(userWalletAvailBalance + "");
            wsmodel.setClosingbalance(userWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Merchant Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(merchantWallet);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency(merchantWallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(merchantWalletActBalance + "");
            WGL.setClosingBalance(merchantWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            logger.info("Updating Consumer Wallet General Ledger ...");
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setWallet(userWallet);
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency(userWallet.getCurrency());
            WGL2.setWalletflag(true);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setPreviousBalance(userWalletActBalance + "");
            WGL2.setClosingBalance(userWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL2);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Merchant Refund Transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Merchant Refund Transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Merchant Refund Transaction...");
            e.printStackTrace();
            //s.mehtab: 15-10-2020 - VP-NAP-202008211 / VC-NAP-202008211- Creation of Dispute transactions settlement feature
            logger.error(e);
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantDebitCard(WalletCMSWsEntity wsmodel, CMSAccount sourceWallet, CMSAccount destWallet)
    {
        try {
            logger.info("Performing Merchant Debit Card Load/Unload Transaction....");

            Long merchantWalletActBalance = Long.parseLong(sourceWallet.getActualBalance());
            Long merchantWalletAvailBalance = Long.parseLong(sourceWallet.getAvailableBalance());
            Long userWalletActBalance = Long.parseLong(destWallet.getActualBalance());
            Long userWalletAvailBalance = Long.parseLong(destWallet.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());

            if(merchantWalletAvailBalance < TxnAmount)
            {
                logger.error("Insufficient Funds in Merchant Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE);
                return false;
            }

            logger.info("Updating Source Wallet Balance ...");
            sourceWallet.setActualBalance((merchantWalletActBalance - TxnAmount) + "");
            sourceWallet.setAvailableBalance((merchantWalletAvailBalance - TxnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(sourceWallet);

            logger.info("Updating Destination Wallet Balance ...");
            destWallet.setActualBalance((userWalletActBalance + TxnAmount) + "");
            destWallet.setAvailableBalance((userWalletAvailBalance + TxnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(destWallet);

            logger.info("Updating Source Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(sourceWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(TxnAmount + "");
            balanceLog.setOriginalbalance(merchantWalletAvailBalance + "");
            balanceLog.setUpdatedbalance(sourceWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(merchantWalletAvailBalance + "");
            wsmodel.setClosingbalance(sourceWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Destination Wallet Balance Log ...");
            WalletBalanceLog balanceLog2 = new WalletBalanceLog();
            balanceLog.setWallet(destWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(TxnAmount + "");
            balanceLog.setOriginalbalance(userWalletAvailBalance + "");
            balanceLog.setUpdatedbalance(destWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog2);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(userWalletAvailBalance + "");
            wsmodel.setClosingbalance(destWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Source Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(sourceWallet);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency(sourceWallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(merchantWalletActBalance + "");
            WGL.setClosingBalance(sourceWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            logger.info("Updating Destination Wallet General Ledger ...");
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setWallet(destWallet);
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency(destWallet.getCurrency());
            WGL2.setWalletflag(true);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setPreviousBalance(userWalletActBalance + "");
            WGL2.setClosingBalance(destWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL2);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Merchant Refund Transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Merchant Refund Transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Merchant Refund Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantUnloadWallet(WalletCMSWsEntity wsmodel, CMSAccount merchantWallet)
    {
        try {
            logger.info("UnLoading Wallet for MerchantUnloadWallet Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount())) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L;
            Long AcctActBalance = Long.parseLong(merchantWallet.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(merchantWallet.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());

            Long finalAmount = TxnAmount + SrcChargeAmount;

            if(AcctAvailBalance < finalAmount)
            {
                logger.error("Insufficient Funds in Merchant Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Merchant Wallet Balance ...");
            merchantWallet.setActualBalance((AcctActBalance - finalAmount) + "");
            merchantWallet.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(merchantWallet);

            logger.info("Updating Merchant Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(merchantWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(merchantWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(merchantWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Merchant Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(merchantWallet);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency(merchantWallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(merchantWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(merchantWallet.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency(merchantWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogCollectionAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("UnLoading Merchant Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("UnLoading Merchant Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while MerchantUnloadWallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantLoadWallet(WalletCMSWsEntity wsmodel, CMSAccount merchantWallet)
    {
        try {
            logger.info("Loading Merchant Wallet for " + wsmodel.getServicename()  + " Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount())) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L;
            Long AcctActBalance = Long.parseLong(merchantWallet.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(merchantWallet.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long finalAmount = TxnAmount + SrcChargeAmount;

            logger.info("Updating Merchant Wallet Balance ...");
            merchantWallet.setActualBalance((AcctActBalance + finalAmount) + "");
            merchantWallet.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(merchantWallet);

            logger.info("Updating Merchant Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(merchantWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(merchantWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(merchantWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Merchant Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(merchantWallet);
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency(merchantWallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(merchantWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(merchantWallet.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency(merchantWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogCollectionAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN, AccType.CAT_SETT_ACCT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Loading Merchant Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Loading Merchant Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while MerchantLoadWallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantSettlementLoadWallet(WalletCMSWsEntity wsmodel, CMSAccount merchantWallet)
    {
        try {
            logger.info("Loading Wallet for MerchantSettlementLoadWallet Transaction....");

            Long AcctActBalance = (Util.hasText(merchantWallet.getActualBalance())) ? Long.parseLong(merchantWallet.getActualBalance()) : 0L;
            Long AcctAvailBalance = (Util.hasText(merchantWallet.getAvailableBalance())) ? Long.parseLong(merchantWallet.getAvailableBalance()) : 0L;
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction())) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L;
            Long ChargeAmount = (Util.hasText(wsmodel.getDestchargeamount())) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L;
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount())) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L;
            //m.rehman: 08-07-2020: Need to add tax here because tax donot apply on destination charges
            //Long FinalAmount = TxnAmount - ChargeAmount;
            Long FinalAmount = TxnAmount - (ChargeAmount + Tax);

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (FinalAmount < 0) {
                logger.error("Calculated amount [" + FinalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Merchant Wallet Balance ...");
            merchantWallet.setActualBalance((AcctActBalance + FinalAmount) + "");
            merchantWallet.setAvailableBalance((AcctAvailBalance + FinalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(merchantWallet);

            logger.info("Updating Merchant Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(merchantWallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(FinalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(merchantWallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(merchantWallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Merchant Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(merchantWallet);
            WGL.setAmount(FinalAmount + "");
            WGL.setCurrency(merchantWallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(merchantWallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setBusinessaccount(GetBusinessWallet(merchantWallet.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue()));
            WGL2.setAmount(wsmodel.getAmounttransaction());
            WGL2.setCurrency(merchantWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogMerchantSettlementWallet(wsmodel, merchantWallet.getCurrency(), TxnFlag.DEBIT);

            if(ChargeAmount > 0) //TODO: Raza use with above Check
            {
                //m.rehman: 08-07-2020: Need to add tax here because tax donot apply on destination charges
                //String chargeAmount = wsmodel.getDestchargeamount();
                String srcChargeBackup = wsmodel.getSrcchargeamount();
                String chargeAmount = Long.toString(ChargeAmount + Tax);
                wsmodel.setSrcchargeamount(chargeAmount);
                UpdateandLogRevenueAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.CREDIT);
                wsmodel.setSrcchargeamount(srcChargeBackup);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, merchantWallet.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Loading Settlement Amount in Merchant Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Loading Settlement Amount in Merchant Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while MerchantSettlementLoadWallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean DebitCardRequest(WalletCMSWsEntity wsmodel, CMSAccount wallet)
    {
        try {
            logger.info("UnLoading Wallet for DebitCardRequest Transaction....");

            Long AcctActBalance = (Util.hasText(wallet.getActualBalance())) ? Long.parseLong(wallet.getActualBalance()) : 0L;
            Long AcctAvailBalance = (Util.hasText(wallet.getAvailableBalance())) ? Long.parseLong(wallet.getAvailableBalance()) : 0L;
            Long chargeAmount = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount())) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L;

            if (AcctActBalance < chargeAmount) {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            wallet.setActualBalance((AcctActBalance - chargeAmount) + "");
            wallet.setAvailableBalance((AcctAvailBalance - chargeAmount) + "");
            GeneralDao.Instance.saveOrUpdate(wallet);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(wallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(chargeAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(wallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(wallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(wallet);
            WGL.setAmount(chargeAmount + "");
            WGL.setCurrency(wallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(wallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(wallet.getCurrency()));
            WGL2.setAmount(chargeAmount + "");
            WGL2.setCurrency(wallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);

            UpdateandLogCollectionAccount(wsmodel, wallet.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT);
            */

            if(chargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, wallet.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, wallet.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Unloading Debit Card Request Charge Amount in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Unloading Debit Card Request Charge Amount in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while DebitCardRequest...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.NP_PERMISSION_DENIED); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean TopupBillPayment(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for TopupBillPayment Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long DepositAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long finalAmount = txnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount);

            if(DepositAmount < finalAmount || DepositAmount == 0L)
            {
                logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (finalAmount < 0) {
                logger.error("Calculated amount [" + finalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false, false);
            // ===================================================================================

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN, AccType.CAT_SETT_ACCT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("TopupBillPayment in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence

            ///logger.info("TopupBillPayment in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while TopupBillPayment in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean IBFTIn(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for IBFTIn Transaction....");

            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long DepositAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long finalAmount = DepositAmount - (AmtTranFee + SrcChargeAmount);

            if(DepositAmount < finalAmount || DepositAmount == 0L)
            {
                logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (finalAmount < 0) {
                logger.error("Calculated amount [" + finalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.CREDIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN, AccType.CAT_SETT_ACCT);
            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, false);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("IBFTIn in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("IBFTIn in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while IBFTIn Cash in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean MerchantSettlementLogging(WalletCMSWsEntity wsmodel)
    {
        try {
            logger.info("Unloading Wallet for MerchantSettlementLogging Transaction....");

            Currency baseCurrency = GlobalContext.getInstance().getBaseCurrency();

            if (baseCurrency == null) {
                logger.error("Base currency not found, rejecting ...");
                return false;
            }

            // Asim Shahzad, Date : 15th June 2020, Desc : Added Revenue and Tax legs for settlement transactions
            //m.rehman: 08-07-2020: Need to add tax here because tax donot apply on destination charges
            //Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            // ===================================================================================================

            /*
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setBusinessaccount(GetBusinessWallet(baseCurrency.getCode().toString(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue()));
            WGL.setAmount(wsmodel.getAmounttransaction());
            WGL.setCurrency(baseCurrency.getCode().toString());
            WGL.setWalletflag(false);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);
            */

            UpdateandLogMerchantSettlementWallet(wsmodel, baseCurrency.getCode().toString(), TxnFlag.DEBIT);

            //if merchant has no wallet and customer performed transactions from wallet,
            // then we need to deduct amount from collection account as funds is moving out of the system

            // Asim Shahzad, Date : 19th Oct 2020, Desc : commented this check to rectify issue in settlement, Tracking ID : VP-NAP-202010051
//            if (Util.hasText(wsmodel.getTotalamount()) && Long.parseLong(wsmodel.getTotalamount()) > 0) {
                CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(baseCurrency.getCode().toString(), AccType.CAT_SETT_ACCT);

                Long TxnAmount = (Util.hasText(wsmodel.getTotalamount()) ? Long.parseLong(wsmodel.getTotalamount()) : 0L);
                Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
                Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L);

                logger.info("Updating Settlement Collection Account Balance ...");
                AcctActBalance = AcctActBalance - TxnAmount;
                settlementaccount.setActualBalance(AcctActBalance + "");
                settlementaccount.setAvailableBalance(AcctActBalance + "");
                GeneralDao.Instance.save(settlementaccount);

                /*
                logger.info("Updating Wallet Balance Log ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setCollectionaccount(settlementaccount);
                WGL2.setAmount(wsmodel.getTotalamount());
                WGL2.setCurrency(baseCurrency.getCode().toString());
                WGL2.setWalletflag(false);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance(AcctAvailBalance + "");
                WGL2.setClosingBalance(settlementaccount.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGL2);
                */

                /*
                EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                balanceLog.setCollectionaccount(settlementaccount);
                balanceLog.setChannelid(wsmodel.getChannelid());
                balanceLog.setOriginalbalance(AcctAvailBalance+"");
                balanceLog.setUpdatedbalance(settlementaccount.getAvailableBalance());
                balanceLog.setTxnname(wsmodel.getServicename());
                balanceLog.setAmount((TxnAmount) + "");
                balanceLog.setTransaction(wsmodel);
                balanceLog.setTxnnature(TxnFlag.DEBIT);
                balanceLog.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLog);
                */

                logger.info("Updating Settlement Account Balance Log ...");
                EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
                balanceLog.setBankId(settlementaccount.getBankCode());
                balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
                balanceLog.setTxnId(wsmodel.getTranrefnumber());
                balanceLog.setDebitAmount(TxnAmount + "");
                balanceLog.setAccountNature(settlementaccount.getAccountType());
                balanceLog.setTranDate(wsmodel.getTransdatetime());
                GeneralDao.Instance.save(balanceLog);

                // Asim Shahzad, Date : 15th June 2020, Desc : Added Revenue and Tax legs for settlement transactions
                //m.rehman: 04-08-2020, bank acronym is available in FundBank object
                //String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(settlementaccount.getBankCode().getBankAcro());
                String bankAcronym = settlementaccount.getBankCode().getBankAcro();
                //m.rehman: 08-07-2020: Need to add tax here because tax donot apply on destination charges
                //UpdateandLogPartnerBankCollectionAccount(wsmodel, baseCurrency.getCode().toString(), TxnFlag.DEBIT, TxnFlag.CASH_IN,
                //        AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, false, false);
                String srcChargeBackup = wsmodel.getSrcchargeamount();
                String chargeAmount = Long.toString(DestChargeAmount + Tax);
                wsmodel.setSrcchargeamount(chargeAmount);
                UpdateandLogPartnerBankCollectionAccount(wsmodel, baseCurrency.getCode().toString(), TxnFlag.CREDIT, TxnFlag.CASH_IN,
                        AccType.CAT_PARTNER_BANK_SETT_ACCT, bankAcronym, true, false);

                if(DestChargeAmount > 0) //TODO: Raza use with above Check
                {
                    UpdateandLogRevenueAccount(wsmodel, baseCurrency.getCode().toString(), TxnFlag.CREDIT);
                }

                if(Tax > 0)
                {
                    UpdateandLogTaxAccount(wsmodel, baseCurrency.getCode().toString(), TxnFlag.CREDIT);
                }
                wsmodel.setSrcchargeamount(srcChargeBackup);
                // =======================================================================================================
//            } // Asim Shahzad, Date : 19th Oct 2020, Desc : commented this check to rectify issue in settlement, Tracking ID : VP-NAP-202010051

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("UnLoading Settlement Amount in Business Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("UnLoading Settlement Amount in Business Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while MerchantSettlementLoadWallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean FundManagement(WalletCMSWsEntity wsmodel)
    {
        try {
            logger.info("Adding information in Fund Management GL Accounts");

            String debitAccountNo="", creditAccountNo="";
            if (Util.hasText(wsmodel.getReserved())) {
                String[] account = wsmodel.getReserved().split("\\|");
                if (account != null && account.length > 0) {
                    debitAccountNo = account[0];
                    creditAccountNo = account[1];
                }
            } else {
                debitAccountNo = wsmodel.getAccountnumber();
                creditAccountNo = wsmodel.getDestaccount();
            }

            //String dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " a where a.id = :ACCT_NO";
            String dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " a where "; // + " a.id = :ACCT_NO";
            Map<String, Object> params = new HashMap<String, Object>();
            if (Util.hasText(wsmodel.getReserved())) {
                dbQuery += " a.id = :ACCT_NO ";
                params.put("ACCT_NO", Long.parseLong(debitAccountNo));
            } else {
                dbQuery += " a.AccountNumber = :ACCT_NO ";
                params.put("ACCT_NO", debitAccountNo);
            }
            CMSEMIAccountCollection fundBankAccount = (CMSEMIAccountCollection) GeneralDao.Instance.findObject(dbQuery, params);
            if (fundBankAccount == null) {
                logger.error("No Fund Account found with Account Number [" + wsmodel.getAccountnumber() + "] ...");
                return false;

            } else {

                Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction())) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L;
                Long incomingActualBalance = (Util.hasText(wsmodel.getActualbaldebitacc())) ? Long.parseLong(wsmodel.getActualbaldebitacc()) : 0L;
                Long glUpdatedBalance = incomingActualBalance - txnAmount;

                Long acctActualBalance = (Util.hasText(fundBankAccount.getActualBalance())) ? Long.parseLong(fundBankAccount.getActualBalance()) : 0L;
                Long acctAvailBalance = (Util.hasText(fundBankAccount.getAvailableBalance())) ? Long.parseLong(fundBankAccount.getAvailableBalance()) : 0L;
                Long acctActualUpdatedBalance = acctActualBalance - txnAmount;
                Long acctAvailUpdatedBalance = acctAvailBalance - txnAmount;

                if (Util.hasText(wsmodel.getOriginalapi()) && wsmodel.getOriginalapi().equals("IBFTOut")) {
                    UpdateandLogPartnerBankCollectionAccount(wsmodel, fundBankAccount.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT,
                            AccType.CAT_PARTNER_BANK_SETT_ACCT, fundBankAccount.getBankCode().getBankAcro(), false, false);
                } else {

                    /*
                    FundBankGl debitGL = new FundBankGl();
                    debitGL.setDebitAccount(wsmodel.getAccountnumber());
                    debitGL.setDebitAmount(wsmodel.getAmounttransaction());
                    debitGL.setTxnId(wsmodel.getTranrefnumber());
                    debitGL.setBankId(fundBankAccount.getBankCode());
                    debitGL.setAccountNature(fundBankAccount.getAccountType());
                    debitGL.setTranDate(wsmodel.getTransdatetime());
                    debitGL.setClosingBalance(StringUtils.leftPad(glUpdatedBalance.toString(), 12, "0"));
                    debitGL.setVoucherId(wsmodel.getFundsvoucherid());
                    GeneralDao.Instance.saveOrUpdate(debitGL);
                    */

                    EMICollectionBalanceLog debitGL = new EMICollectionBalanceLog();
                    debitGL.setDebitAccount(wsmodel.getAccountnumber());
                    debitGL.setDebitAmount(wsmodel.getAmounttransaction());
                    debitGL.setTxnId(wsmodel.getTranrefnumber());
                    debitGL.setBankId(fundBankAccount.getBankCode());
                    debitGL.setAccountNature(fundBankAccount.getAccountType());
                    debitGL.setTranDate(wsmodel.getTransdatetime());
                    //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
                    //debitGL.setClosingBalance(StringUtils.leftPad(glUpdatedBalance.toString(), 12, "0"));
                    debitGL.setClosingBalance(glUpdatedBalance.toString());
                    debitGL.setVoucherId(wsmodel.getFundsvoucherid());
                    GeneralDao.Instance.saveOrUpdate(debitGL);

                    /*
                    EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                    balanceLog.setCollectionaccount(fundBankAccount);
                    balanceLog.setAmount(wsmodel.getAmounttransaction());
                    balanceLog.setOriginalbalance(acctActualBalance.toString());
                    balanceLog.setUpdatedbalance(acctUpdatedBalance.toString());
                    balanceLog.setTransaction(wsmodel);
                    balanceLog.setTxnname(wsmodel.getServicename());
                    balanceLog.setChannelid(wsmodel.getChannelid());
                    balanceLog.setTxnnature(TxnFlag.DEBIT);
                    balanceLog.setCreatedate(new Date());
                    GeneralDao.Instance.save(balanceLog);
                    */

//                    fundBankAccount.setActualBalance(StringUtils.leftPad(acctActualUpdatedBalance.toString(), 12, "0"));
//                    fundBankAccount.setAvailableBalance(StringUtils.leftPad(acctAvailUpdatedBalance.toString(), 12, "0"));
                    fundBankAccount.setActualBalance(acctActualUpdatedBalance.toString());
                    fundBankAccount.setAvailableBalance(acctAvailUpdatedBalance.toString());
                    GeneralDao.Instance.saveOrUpdate(fundBankAccount);
                }
            }

            //dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " a where a.id = :ACCT_NO";
            dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " a where ";
            params = new HashMap<String, Object>();
            if (Util.hasText(wsmodel.getReserved())) {
                dbQuery += " a.id = :ACCT_NO ";
                params.put("ACCT_NO", Long.parseLong(creditAccountNo));
            } else {
                dbQuery += " a.AccountNumber = :ACCT_NO ";
                params.put("ACCT_NO", creditAccountNo);
            }
            fundBankAccount = (CMSEMIAccountCollection) GeneralDao.Instance.findObject(dbQuery, params);
            if (fundBankAccount == null) {
                logger.error("No Fund Account found with Account Number [" + wsmodel.getDestaccount() + "] ...");
                return false;

            } else {

                Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction())) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L;
                Long incomingActualBalance = (Util.hasText(wsmodel.getActualbalcreditacc())) ? Long.parseLong(wsmodel.getActualbalcreditacc()) : 0L;
                Long glUpdatedBalance = incomingActualBalance + txnAmount;

                Long acctActualBalance = (Util.hasText(fundBankAccount.getActualBalance())) ? Long.parseLong(fundBankAccount.getActualBalance()) : 0L;
                Long acctAvailBalance = (Util.hasText(fundBankAccount.getAvailableBalance())) ? Long.parseLong(fundBankAccount.getAvailableBalance()) : 0L;
                Long acctActualUpdatedBalance = acctActualBalance + txnAmount;
                Long acctAvailUpdatedBalance = acctAvailBalance + txnAmount;

                if (Util.hasText(wsmodel.getOriginalapi()) && wsmodel.getOriginalapi().equals("IBFTOut")) {
                    // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
                    UpdateandLog1LinkAccount(wsmodel, fundBankAccount.getCurrency(), TxnFlag.CREDIT, false, false, false, false);
                    // ===================================================================================
                } else {

                    EMICollectionBalanceLog creditGL = new EMICollectionBalanceLog();
                    creditGL.setCreditAccount(wsmodel.getDestaccount());
                    creditGL.setCreditAmount(wsmodel.getAmounttransaction());
                    creditGL.setTxnId(wsmodel.getTranrefnumber());
                    creditGL.setBankId(fundBankAccount.getBankCode());
                    creditGL.setAccountNature(fundBankAccount.getAccountType());
                    creditGL.setTranDate(wsmodel.getTransdatetime());
                    //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
                    //creditGL.setClosingBalance(StringUtils.leftPad(glUpdatedBalance.toString(), 12, "0"));
                    creditGL.setClosingBalance(glUpdatedBalance.toString());
                    creditGL.setVoucherId(wsmodel.getFundsvoucherid());
                    GeneralDao.Instance.saveOrUpdate(creditGL);

                    /*
                    EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                    balanceLog.setCollectionaccount(fundBankAccount);
                    balanceLog.setAmount(wsmodel.getAmounttransaction());
                    balanceLog.setOriginalbalance(acctActualBalance.toString());
                    balanceLog.setUpdatedbalance(acctUpdatedBalance.toString());
                    balanceLog.setTransaction(wsmodel);
                    balanceLog.setTxnname(wsmodel.getServicename());
                    balanceLog.setChannelid(wsmodel.getChannelid());
                    balanceLog.setTxnnature(TxnFlag.CREDIT);
                    balanceLog.setCreatedate(new Date());
                    GeneralDao.Instance.save(balanceLog);
                    */

//                    fundBankAccount.setActualBalance(StringUtils.leftPad(acctActualUpdatedBalance.toString(), 12, "0"));
//                    fundBankAccount.setAvailableBalance(StringUtils.leftPad(acctAvailUpdatedBalance.toString(), 12, "0"));
                    fundBankAccount.setActualBalance(acctActualUpdatedBalance.toString());
                    fundBankAccount.setAvailableBalance(acctAvailUpdatedBalance.toString());
                    GeneralDao.Instance.saveOrUpdate(fundBankAccount);
                }
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                //logger.info("UnLoading Settlement Amount in Business Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("Exception caught while Executing FundManagement..!");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //Raza Refer to Document '12'
            return false;
        }
    }

    @Transactional
    public static boolean QRMerchantPayment(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Unloading Wallet for QRMerchantPayment Transaction....");

            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long finalAmount = TxnAmount - (AmtTranFee + SrcChargeAmount);

            if(TxnAmount < finalAmount || TxnAmount == 0L)
            {
                logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (finalAmount < 0) {
                logger.error("Calculated amount [" + finalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogEuronetAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, true, false, false);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("QRMerchantPayment in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("QRMerchantPayment in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while QRMerchantPayment in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean QRMerchantPaymentReversal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for QRMerchantPaymentReversal Transaction....");

            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long finalAmount = TxnAmount + (AmtTranFee + SrcChargeAmount);

            if(TxnAmount < finalAmount || TxnAmount == 0L)
            {
                logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogEuronetAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, false, true);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("QRMerchantPaymentReversal in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("QRMerchantPaymentReversal in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while QRMerchantPaymentReversal in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean QRMerchantPaymentRefund(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for QRMerchantPaymentRefund Transaction....");

            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long finalAmount = TxnAmount;// - (AmtTranFee + SrcChargeAmount);

            if(TxnAmount < finalAmount || TxnAmount == 0L)
            {
                logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogEuronetAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, false, true);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("QRMerchantPaymentRefund in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("QRMerchantPaymentRefund in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while QRMerchantPaymentRefund in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean IBFT(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Unloading Wallet for IBFT Transaction....");

            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            Long finalAmount = TxnAmount + (AmtTranFee + SrcChargeAmount);

            //m.rehman: 16-03-2021, VC-NAP-202103162 - Low balance issue in IBFT
            //updating check
            //if(TxnAmount < finalAmount || TxnAmount == 0L)
            if (AcctActBalance < finalAmount)
            {
                logger.error("Insufficient balance for " + wsmodel.getServicename() + " Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, true, false);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("IBFT in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("IBFT in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while IBFT in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static void UpdateandLogMerchantSettlementWallet(WalletCMSWsEntity wsmodel, String currency, String txnFlag)
    {

        CMSBusinessWallet settlementaccount = GetBusinessWallet(currency, AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue());

        // Asim Shahzad, Date : 25th June 2020, Desc : Added Merchant Settlement account in emi_account_collection
        CMSEMIAccountCollection settlementCollAccount = GetEMICollectionAccount(currency, AccType.CAT_MERCHANT_SETTLEMENT_ACCT);
        // =========================================================================================================

        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;

        // Asim Shahzad, Date : 25th June 2020, Desc : Added Merchant Settlement account in emi_account_collection
        Long CollAcctActBalance = (Util.hasText(settlementCollAccount.getActualBalance()) ? Long.parseLong(settlementCollAccount.getActualBalance()) : 0L);
        Long CollAcctAvailBalance = (Util.hasText(settlementCollAccount.getAvailableBalance()) ? Long.parseLong(settlementCollAccount.getAvailableBalance()) : 0L);
        // =========================================================================================================

        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountBusinessWallet = TxnAmount;// - DestChargeAmount - Tax;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            CollAcctActBalance = CollAcctActBalance - AmountBusinessWallet; // Asim Shahzad, Date : 25th June 2020, Desc : Added Merchant Settlement account in emi_account_collection
            AcctActBalance = AcctActBalance - AmountBusinessWallet;
        }
        else //Credit
        {
            CollAcctActBalance = CollAcctActBalance + AmountBusinessWallet; // Asim Shahzad, Date : 25th June 2020, Desc : Added Merchant Settlement account in emi_account_collection
            AcctActBalance = AcctActBalance + AmountBusinessWallet;
        }

        logger.info("Updating Merchant Settlement Collection Account Balance ...");
        // Asim Shahzad, Date : 25th June 2020, Desc : Added Merchant Settlement account in emi_account_collection
        settlementCollAccount.setActualBalance(AcctActBalance + "");
        settlementCollAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementCollAccount);
        // =========================================================================================================

        logger.info("Updating Merchant Settlement Business Wallet Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.save(settlementaccount);

        logger.info("Updating Merchant Settlement Business Wallet Balance Log ...");
        BusinessWalletBalanceLog balanceLog = new BusinessWalletBalanceLog();
        balanceLog.setBusinessWallet(settlementaccount);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(AcctAvailBalance+"");
        balanceLog.setUpdatedbalance(settlementaccount.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setAmount((AmountBusinessWallet) + "");
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Merchant Settlement Collection Account Balance Log ...");
        // Asim Shahzad, Date : 25th June 2020, Desc : Added Merchant Settlement account in emi_account_collection
        EMICollectionBalanceLog collbalanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            collbalanceLog.setDebitAccount(settlementCollAccount.getAccountNumber());
            collbalanceLog.setDebitAmount(AmountBusinessWallet + "");
        } else {
            collbalanceLog.setCreditAccount(settlementCollAccount.getAccountNumber());
            collbalanceLog.setCreditAmount(AmountBusinessWallet + "");
        }
        collbalanceLog.setTxnId(wsmodel.getTranrefnumber());
        collbalanceLog.setBankId(settlementCollAccount.getBankCode());
        collbalanceLog.setAccountNature(settlementCollAccount.getAccountType());
        collbalanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        collbalanceLog.setClosingBalance(StringUtils.leftPad(settlementCollAccount.getAvailableBalance(), 12, "0"));
        collbalanceLog.setClosingBalance(settlementCollAccount.getAvailableBalance());
        collbalanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(collbalanceLog);
        // =========================================================================================================

        logger.info("Updating Merchant Settlement Business Wallet Balance Log ...");
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL.setTxnflag(txnFlag);
        WGL.setBusinessaccount(settlementaccount);
        WGL.setAmount(AmountBusinessWallet + "");
        WGL.setCurrency(settlementaccount.getCurrency());
        WGL.setWalletflag(false);
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        WGL.setPreviousBalance(AcctAvailBalance + "");
        WGL.setClosingBalance(settlementaccount.getAvailableBalance());
        GeneralDao.Instance.save(WGL);
    }

    @Transactional
    public static void UpdateandLog1LinkAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd, boolean isReversalFlag)
    {

        //Note: Raza In case of Source Charge, Destination Charge and Tax ; Pool/Settlement Account would have no impact as
        // it has mapping for Transaction Amount and Tax/Charges are extracted from transaction amount
        //eg. case: Txn Amount = 100 , Src Charge = 5 , Dest Charge = 5 , Tax Amount = 10 ==> Final AMount = 85(100-5-10){for source/sender} and 80(85-5){for destination/receiver} ; However ,Settlement Account will have 100

        // Asim Shahzad, Date : 24th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081

//        CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_1LINK_SETT_ACCT);
//        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
//        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
//
//        CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_1LINK_SETT_WLLT);
//        Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
//        Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;


        if(!isReversalFlag) {
            if (txnFlag.equals(TxnFlag.DEBIT)) {

                logger.info("This is not a reversal transaction....");
                logger.info("Inside " + TxnFlag.DEBIT + " transaction flow..");

                CMSEMIAccountCollection settlementaccountreceivable = GetEMICollectionAccount(currency, AccType.CAT_1LINK_SETT_RECEIVABLE_ACCT);
                Long AcctActBalance = (Util.hasText(settlementaccountreceivable.getActualBalance()) ? Long.parseLong(settlementaccountreceivable.getActualBalance()) : 0L);
                Long AcctAvailBalance = (Util.hasText(settlementaccountreceivable.getAvailableBalance()) ? Long.parseLong(settlementaccountreceivable.getAvailableBalance()) : 0L);

                CMSEMIWallet settlementWalletReceivable = GetEMIWallet(currency, AccType.CAT_1LINK_SETT_RECEIVABLE_WLLT);
                Long WlltActBalance = (Util.hasText(settlementWalletReceivable.getActualBalance()) ? Long.parseLong(settlementWalletReceivable.getActualBalance()) : 0L);
                Long WlltAvailBalance = (Util.hasText(settlementWalletReceivable.getAvailableBalance()) ? Long.parseLong(settlementWalletReceivable.getAvailableBalance()) : 0L);

                Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
                Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
                Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
                Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
                Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
                Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
                Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

                Long AmountCollectionAccount;
                if (isChargesApply) {
                    if (isApplyOnlyCharges) {
                        AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        if (isChargesAdd) {
                            AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                        } else {
                            AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                        }
                    }
                } else {
                    AmountCollectionAccount = TxnAmount;
                }

//            if (txnFlag.equals(TxnFlag.DEBIT)) {
//                AcctActBalance = AcctActBalance - AmountCollectionAccount;
//                WlltActBalance = WlltActBalance - AmountCollectionAccount;
//            } else //Credit
//            {
                AcctActBalance = AcctActBalance + AmountCollectionAccount;
                WlltActBalance = WlltActBalance + AmountCollectionAccount;
//            }

                logger.info("Updating 1Link Receivable Settlement Collection Account Balance ...");
                settlementaccountreceivable.setActualBalance(AcctActBalance + "");
                settlementaccountreceivable.setAvailableBalance(AcctActBalance + "");
                GeneralDao.Instance.saveOrUpdate(settlementaccountreceivable);

                logger.info("Updating 1Link Receivable Wallet Balance ...");
                settlementWalletReceivable.setActualBalance(WlltActBalance + "");
                settlementWalletReceivable.setAvailableBalance(WlltActBalance + "");
                GeneralDao.Instance.saveOrUpdate(settlementWalletReceivable);

        /*
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        balanceLog.setCollectionaccount(settlementaccount);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(AcctAvailBalance+"");
        balanceLog.setUpdatedbalance(settlementaccount.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setAmount((AmountCollectionAccount) + "");
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(balanceLog);
        */

                logger.info("Updating 1Link Receivable Collection Account Balance Log ...");
                EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                if (txnFlag.equals(TxnFlag.DEBIT)) {
                    balanceLog.setDebitAccount(settlementaccountreceivable.getAccountNumber());
                    balanceLog.setDebitAmount(AmountCollectionAccount + "");
                } else {
                    balanceLog.setCreditAccount(settlementaccountreceivable.getAccountNumber());
                    balanceLog.setCreditAmount(AmountCollectionAccount + "");
                }
                balanceLog.setTxnId(wsmodel.getTranrefnumber());
                balanceLog.setBankId(settlementaccountreceivable.getBankCode());
                balanceLog.setAccountNature(settlementaccountreceivable.getAccountType());
                balanceLog.setTranDate(wsmodel.getTransdatetime());
                //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//                balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccountreceivable.getAvailableBalance(), 12, "0"));
                balanceLog.setClosingBalance(settlementaccountreceivable.getAvailableBalance());
                balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
                GeneralDao.Instance.save(balanceLog);

                logger.info("Updating 1Link Receivable Settlement Wallet Balance Log ...");
                EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
                wlltBalanceLog.setEmiwallet(settlementWalletReceivable);
                wlltBalanceLog.setAmount(AmountCollectionAccount + "");
                wlltBalanceLog.setChannelid(wsmodel.getChannelid());
                wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
                wlltBalanceLog.setUpdatedbalance(settlementWalletReceivable.getAvailableBalance());
                wlltBalanceLog.setTransaction(wsmodel);
                wlltBalanceLog.setTxnname(wsmodel.getServicename());
                wlltBalanceLog.setTxnnature(txnFlag);
                wlltBalanceLog.setCreatedate(new Date());

                // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch new value of the sequence
                GeneralDao.Instance.getNextValEmiCollBalLog();
                // ============================================================================================

                GeneralDao.Instance.save(wlltBalanceLog);

        /*
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL.setTxnflag(txnFlag);
        WGL.setCollectionaccount(settlementaccount);
        WGL.setAmount(AmountCollectionAccount + "");
        WGL.setCurrency(settlementaccount.getCurrency());
        WGL.setWalletflag(false);
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        WGL.setPreviousBalance(AcctAvailBalance + "");
        WGL.setClosingBalance(settlementaccount.getAvailableBalance());
        GeneralDao.Instance.saveOrUpdate(WGL);
        */

                logger.info("Updating 1Link Receivable Settlement Wallet General Ledger ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
                WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL2.setTxnflag(txnFlag);
                WGL2.setEmiaccount(settlementWalletReceivable);
                WGL2.setAmount(AmountCollectionAccount + "");
                WGL2.setCurrency(settlementWalletReceivable.getCurrency());
                WGL2.setWalletflag(false);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance(WlltAvailBalance + "");
                WGL2.setClosingBalance(settlementWalletReceivable.getAvailableBalance());
                GeneralDao.Instance.save(WGL2);
            } else {

                CMSEMIAccountCollection settlementaccountpayable = GetEMICollectionAccount(currency, AccType.CAT_1LINK_SETT_PAYABLE_ACCT);
                Long AcctActBalance = (Util.hasText(settlementaccountpayable.getActualBalance()) ? Long.parseLong(settlementaccountpayable.getActualBalance()) : 0L);
                Long AcctAvailBalance = (Util.hasText(settlementaccountpayable.getAvailableBalance()) ? Long.parseLong(settlementaccountpayable.getAvailableBalance()) : 0L);

                CMSEMIWallet settlementWalletPayable = GetEMIWallet(currency, AccType.CAT_1LINK_SETT_PAYABLE_WLLT);
                Long WlltActBalance = (Util.hasText(settlementWalletPayable.getActualBalance()) ? Long.parseLong(settlementWalletPayable.getActualBalance()) : 0L);
                Long WlltAvailBalance = (Util.hasText(settlementWalletPayable.getAvailableBalance()) ? Long.parseLong(settlementWalletPayable.getAvailableBalance()) : 0L);

                Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
                Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
                Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
                Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
                Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
                Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
                Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

                Long AmountCollectionAccount;
                if (isChargesApply) {
                    if (isApplyOnlyCharges) {
                        AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        if (isChargesAdd) {
                            AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                        } else {
                            AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                        }
                    }
                } else {
                    AmountCollectionAccount = TxnAmount;
                }

//            if (txnFlag.equals(TxnFlag.DEBIT)) {
//                AcctActBalance = AcctActBalance - AmountCollectionAccount;
//                WlltActBalance = WlltActBalance - AmountCollectionAccount;
//            } else //Credit
//            {
                AcctActBalance = AcctActBalance + AmountCollectionAccount;
                WlltActBalance = WlltActBalance + AmountCollectionAccount;
//            }

                logger.info("Updating 1Link Payable Settlement Collection Account Balance ...");
                settlementaccountpayable.setActualBalance(AcctActBalance + "");
                settlementaccountpayable.setAvailableBalance(AcctActBalance + "");
                GeneralDao.Instance.saveOrUpdate(settlementaccountpayable);

                logger.info("Updating 1Link Payable Wallet Balance ...");
                settlementWalletPayable.setActualBalance(WlltActBalance + "");
                settlementWalletPayable.setAvailableBalance(WlltActBalance + "");
                GeneralDao.Instance.saveOrUpdate(settlementWalletPayable);

        /*
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        balanceLog.setCollectionaccount(settlementaccount);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(AcctAvailBalance+"");
        balanceLog.setUpdatedbalance(settlementaccount.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setAmount((AmountCollectionAccount) + "");
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(balanceLog);
        */

                logger.info("Updating 1Link Payable Collection Account Balance Log ...");
                EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                if (txnFlag.equals(TxnFlag.DEBIT)) {
                    balanceLog.setDebitAccount(settlementaccountpayable.getAccountNumber());
                    balanceLog.setDebitAmount(AmountCollectionAccount + "");
                } else {
                    balanceLog.setCreditAccount(settlementaccountpayable.getAccountNumber());
                    balanceLog.setCreditAmount(AmountCollectionAccount + "");
                }
                balanceLog.setTxnId(wsmodel.getTranrefnumber());
                balanceLog.setBankId(settlementaccountpayable.getBankCode());
                balanceLog.setAccountNature(settlementaccountpayable.getAccountType());
                balanceLog.setTranDate(wsmodel.getTransdatetime());
                //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//                balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccountpayable.getAvailableBalance(), 12, "0"));
                balanceLog.setClosingBalance(settlementaccountpayable.getAvailableBalance());
                balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
                GeneralDao.Instance.save(balanceLog);

                logger.info("Updating 1Link Payable Settlement Wallet Balance Log ...");
                EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
                wlltBalanceLog.setEmiwallet(settlementWalletPayable);
                wlltBalanceLog.setAmount(AmountCollectionAccount + "");
                wlltBalanceLog.setChannelid(wsmodel.getChannelid());
                wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
                wlltBalanceLog.setUpdatedbalance(settlementWalletPayable.getAvailableBalance());
                wlltBalanceLog.setTransaction(wsmodel);
                wlltBalanceLog.setTxnname(wsmodel.getServicename());
                wlltBalanceLog.setTxnnature(txnFlag);
                wlltBalanceLog.setCreatedate(new Date());

                // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
                GeneralDao.Instance.getNextValEmiCollBalLog();
                // ============================================================================================

                GeneralDao.Instance.save(wlltBalanceLog);

        /*
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL.setTxnflag(txnFlag);
        WGL.setCollectionaccount(settlementaccount);
        WGL.setAmount(AmountCollectionAccount + "");
        WGL.setCurrency(settlementaccount.getCurrency());
        WGL.setWalletflag(false);
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        WGL.setPreviousBalance(AcctAvailBalance + "");
        WGL.setClosingBalance(settlementaccount.getAvailableBalance());
        GeneralDao.Instance.saveOrUpdate(WGL);
        */

                logger.info("Updating 1Link Payable Settlement Wallet General Ledger ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
                WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL2.setTxnflag(txnFlag);
                WGL2.setEmiaccount(settlementWalletPayable);
                WGL2.setAmount(AmountCollectionAccount + "");
                WGL2.setCurrency(settlementWalletPayable.getCurrency());
                WGL2.setWalletflag(false);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance(WlltAvailBalance + "");
                WGL2.setClosingBalance(settlementWalletPayable.getAvailableBalance());
                GeneralDao.Instance.save(WGL2);
            }
        }
        else {
            if(txnFlag.equals(TxnFlag.DEBIT)) {

                logger.info("Executing Reversal Flow ...");

                CMSEMIAccountCollection settlementaccountpayable = GetEMICollectionAccount(currency, AccType.CAT_1LINK_SETT_PAYABLE_ACCT);
                Long AcctActBalance = (Util.hasText(settlementaccountpayable.getActualBalance()) ? Long.parseLong(settlementaccountpayable.getActualBalance()) : 0L);
                Long AcctAvailBalance = (Util.hasText(settlementaccountpayable.getAvailableBalance()) ? Long.parseLong(settlementaccountpayable.getAvailableBalance()) : 0L);

                CMSEMIWallet settlementWalletPayable = GetEMIWallet(currency, AccType.CAT_1LINK_SETT_PAYABLE_WLLT);
                Long WlltActBalance = (Util.hasText(settlementWalletPayable.getActualBalance()) ? Long.parseLong(settlementWalletPayable.getActualBalance()) : 0L);
                Long WlltAvailBalance = (Util.hasText(settlementWalletPayable.getAvailableBalance()) ? Long.parseLong(settlementWalletPayable.getAvailableBalance()) : 0L);

                Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
                Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
                Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
                Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
                Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
                Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
                Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

                Long AmountCollectionAccount;
                if (isChargesApply) {
                    if (isApplyOnlyCharges) {
                        AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        if (isChargesAdd) {
                            AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                        } else {
                            AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                        }
                    }
                } else {
                    AmountCollectionAccount = TxnAmount;
                }

                if (txnFlag.equals(TxnFlag.DEBIT)) {
                    AcctActBalance = AcctActBalance - AmountCollectionAccount;
                    WlltActBalance = WlltActBalance - AmountCollectionAccount;
                } else //Credit
                {
                    AcctActBalance = AcctActBalance + AmountCollectionAccount;
                    WlltActBalance = WlltActBalance + AmountCollectionAccount;
                }

                logger.info("Updating 1Link Payable Settlement Collection Account Balance ...");
                settlementaccountpayable.setActualBalance(AcctActBalance + "");
                settlementaccountpayable.setAvailableBalance(AcctActBalance + "");
                GeneralDao.Instance.saveOrUpdate(settlementaccountpayable);

                logger.info("Updating 1Link Payable Wallet Balance ...");
                settlementWalletPayable.setActualBalance(WlltActBalance + "");
                settlementWalletPayable.setAvailableBalance(WlltActBalance + "");
                GeneralDao.Instance.saveOrUpdate(settlementWalletPayable);

                logger.info("Updating 1Link Payable Collection Account Balance Log ...");
                EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
                if (txnFlag.equals(TxnFlag.DEBIT)) {
                    balanceLog.setDebitAccount(settlementaccountpayable.getAccountNumber());
                    balanceLog.setDebitAmount(AmountCollectionAccount + "");
                } else {
                    balanceLog.setCreditAccount(settlementaccountpayable.getAccountNumber());
                    balanceLog.setCreditAmount(AmountCollectionAccount + "");
                }
                balanceLog.setTxnId(wsmodel.getTranrefnumber());
                balanceLog.setBankId(settlementaccountpayable.getBankCode());
                balanceLog.setAccountNature(settlementaccountpayable.getAccountType());
                balanceLog.setTranDate(wsmodel.getTransdatetime());
                //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//                balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccountpayable.getAvailableBalance(), 12, "0"));
                balanceLog.setClosingBalance(settlementaccountpayable.getAvailableBalance());
                balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
                GeneralDao.Instance.save(balanceLog);

                logger.info("Updating 1Link Payable Settlement Wallet Balance Log ...");
                EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
                wlltBalanceLog.setEmiwallet(settlementWalletPayable);
                wlltBalanceLog.setAmount(AmountCollectionAccount + "");
                wlltBalanceLog.setChannelid(wsmodel.getChannelid());
                wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
                wlltBalanceLog.setUpdatedbalance(settlementWalletPayable.getAvailableBalance());
                wlltBalanceLog.setTransaction(wsmodel);
                wlltBalanceLog.setTxnname(wsmodel.getServicename());
                wlltBalanceLog.setTxnnature(txnFlag);
                wlltBalanceLog.setCreatedate(new Date());

                // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
                GeneralDao.Instance.getNextValEmiCollBalLog();
                // ============================================================================================

                GeneralDao.Instance.save(wlltBalanceLog);

                logger.info("Updating 1Link Payable Settlement Wallet General Ledger ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
                WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL2.setTxnflag(txnFlag);
                WGL2.setEmiaccount(settlementWalletPayable);
                WGL2.setAmount(AmountCollectionAccount + "");
                WGL2.setCurrency(settlementWalletPayable.getCurrency());
                WGL2.setWalletflag(false);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance(WlltAvailBalance + "");
                WGL2.setClosingBalance(settlementWalletPayable.getAvailableBalance());
                GeneralDao.Instance.save(WGL2);
            }
        }

        // ====================================================================================
    }

    @Transactional
    public static void UpdateandLogRevenueAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag)
    {
        CMSEMIWallet reveneuewallet = GetRevenueWallet(currency);
        CMSEMIAccountCollection revenueAccount = GetEMICollectionAccount(currency, AccType.CAT_REVENUE_ACCT);

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long WlltActBalance = (Util.hasText(reveneuewallet.getActualBalance()) ? Long.parseLong(reveneuewallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(reveneuewallet.getAvailableBalance()) ? Long.parseLong(reveneuewallet.getAvailableBalance()) : 0L);
        Long AcctActBalance = (Util.hasText(revenueAccount.getActualBalance()) ? Long.parseLong(revenueAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(revenueAccount.getAvailableBalance()) ? Long.parseLong(revenueAccount.getAvailableBalance()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L) ;
        //Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        //Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        //Long tranFeeAmount = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

        //Long finalAmount = NayapayChargeAmount + SrcChargeAmount - Tax + BankCharges - BankTaxAmount + tranFeeAmount;// + DestChargeAmount;
//        Long finalAmount = (SrcChargeAmount - Tax);// + tranFeeAmount;// + DestChargeAmount;
        Long finalAmount = NayapayChargeAmount;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - finalAmount;
            WlltActBalance = WlltActBalance - finalAmount;
        }
        else
        {
            AcctActBalance = AcctActBalance + finalAmount;
            WlltActBalance = WlltActBalance + finalAmount;
        }

        logger.info("Updating Revenue Collection Account Balance ...");
        revenueAccount.setActualBalance(AcctActBalance + "");
        revenueAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(revenueAccount);

        logger.info("Updating Revenue Wallet Balance ...");
        reveneuewallet.setActualBalance(WlltActBalance + "");
        reveneuewallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(reveneuewallet);

        logger.info("Updating Revenue Wallet Balance Log ...");
        EMIWalletBalanceLog balanceLog = new EMIWalletBalanceLog();
        balanceLog.setEmiwallet(reveneuewallet);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(WlltActBalance+"");
        balanceLog.setUpdatedbalance(reveneuewallet.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setAmount(finalAmount + "");
        balanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================

        GeneralDao.Instance.save(balanceLog);

        /*
        EMICollectionBalanceLog collBalanceLog = new EMICollectionBalanceLog();
        collBalanceLog.setCollectionaccount(revenueAccount);
        collBalanceLog.setChannelid(wsmodel.getChannelid());
        collBalanceLog.setOriginalbalance(AcctAvailBalance+"");
        collBalanceLog.setUpdatedbalance(revenueAccount.getAvailableBalance());
        collBalanceLog.setTxnname(wsmodel.getServicename());
        collBalanceLog.setTransaction(wsmodel);
        collBalanceLog.setTxnnature(txnFlag);
        collBalanceLog.setAmount(finalAmount + "");
        collBalanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(collBalanceLog);
        */

        logger.info("Updating Revenue Collection Account Balance Log ...");
        EMICollectionBalanceLog collbalanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            collbalanceLog.setDebitAccount(revenueAccount.getAccountNumber());
            collbalanceLog.setDebitAmount(finalAmount + "");
        } else {
            collbalanceLog.setCreditAccount(revenueAccount.getAccountNumber());
            collbalanceLog.setCreditAmount(finalAmount + "");
        }
        collbalanceLog.setTxnId(wsmodel.getTranrefnumber());
        collbalanceLog.setBankId(revenueAccount.getBankCode());
        collbalanceLog.setAccountNature(revenueAccount.getAccountType());
        collbalanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        collbalanceLog.setClosingBalance(StringUtils.leftPad(revenueAccount.getAvailableBalance(), 12, "0"));
        collbalanceLog.setClosingBalance(revenueAccount.getAvailableBalance());
        collbalanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(collbalanceLog);

        logger.info("Updating Revenue Wallet General Ledger ...");
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL.setTxnflag(txnFlag);
        WGL.setEmiaccount(reveneuewallet);
        WGL.setAmount(finalAmount + "");
        WGL.setCurrency(currency);
        WGL.setWalletflag(false);
        WGL.setClosingBalance(reveneuewallet.getAvailableBalance());
        WGL.setPreviousBalance(WlltAvailBalance+"");
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL);

        /*
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL2.setTxnflag(txnFlag);
        WGL2.setCollectionaccount(revenueAccount);
        WGL2.setAmount(finalAmount + "");
        WGL2.setCurrency(currency);
        WGL2.setWalletflag(false);
        WGL2.setClosingBalance(revenueAccount.getAvailableBalance());
        WGL2.setPreviousBalance(AcctAvailBalance+"");
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL2);
        */
    }

    @Transactional
    public static void UpdateandLogTaxAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag)
    {
        CMSEMIWallet taxwallet = GetSalesTaxWallet(currency);
        CMSEMIAccountCollection liabilityAccount = GetEMICollectionAccount(currency, AccType.CAT_LIABILITY_ACCT);

        Long WlltActBalance = (Util.hasText(taxwallet.getActualBalance()) ? Long.parseLong(taxwallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(taxwallet.getAvailableBalance()) ? Long.parseLong(taxwallet.getAvailableBalance()) : 0L) ;
        Long AcctActBalance = (Util.hasText(liabilityAccount.getActualBalance()) ? Long.parseLong(liabilityAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(liabilityAccount.getAvailableBalance()) ? Long.parseLong(liabilityAccount.getAvailableBalance()) : 0L) ;
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L) ;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            WlltActBalance = WlltActBalance - Tax;
            AcctActBalance = AcctActBalance - Tax;
        }
        else
        {
            WlltActBalance = WlltActBalance + Tax;
            AcctActBalance = AcctActBalance + Tax;
        }

        logger.info("Updating Tax Wallet Balance ...");
        taxwallet.setActualBalance(WlltActBalance + "");
        taxwallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(taxwallet);

        logger.info("Updating Wallet Collection Account Balance ...");
        liabilityAccount.setActualBalance(AcctActBalance + "");
        liabilityAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(liabilityAccount);

        logger.info("Updating Tax Wallet Balance Log ...");
        EMIWalletBalanceLog balanceLog = new EMIWalletBalanceLog();
        balanceLog.setEmiwallet(taxwallet);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(WlltAvailBalance+"");
        balanceLog.setUpdatedbalance(taxwallet.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setAmount(Tax + "");
        balanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================

        GeneralDao.Instance.save(balanceLog);

        /*
        EMICollectionBalanceLog acctBalanceLog = new EMICollectionBalanceLog();
        acctBalanceLog.setCollectionaccount(liabilityAccount);
        acctBalanceLog.setChannelid(wsmodel.getChannelid());
        acctBalanceLog.setOriginalbalance(AcctAvailBalance+"");
        acctBalanceLog.setUpdatedbalance(liabilityAccount.getAvailableBalance());
        acctBalanceLog.setTxnname(wsmodel.getServicename());
        acctBalanceLog.setTransaction(wsmodel);
        acctBalanceLog.setTxnnature(txnFlag);
        acctBalanceLog.setAmount(Tax + "");
        acctBalanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(acctBalanceLog);
        */

        logger.info("Updating Tax Collection Account Balance Log ...");
        EMICollectionBalanceLog collbalanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            collbalanceLog.setDebitAccount(liabilityAccount.getAccountNumber());
            collbalanceLog.setDebitAmount(Tax + "");
        } else {
            collbalanceLog.setCreditAccount(liabilityAccount.getAccountNumber());
            collbalanceLog.setCreditAmount(Tax + "");
        }
        collbalanceLog.setTxnId(wsmodel.getTranrefnumber());
        collbalanceLog.setBankId(liabilityAccount.getBankCode());
        collbalanceLog.setAccountNature(liabilityAccount.getAccountType());
        collbalanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        collbalanceLog.setClosingBalance(StringUtils.leftPad(liabilityAccount.getAvailableBalance(), 12, "0"));
        collbalanceLog.setClosingBalance(liabilityAccount.getAvailableBalance());
        collbalanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(collbalanceLog);

        logger.info("Updating Tax Wallet General Ledger ...");
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL.setTxnflag(txnFlag);
        WGL.setEmiaccount(taxwallet);
        WGL.setAmount(Tax + "");
        WGL.setCurrency(currency);
        WGL.setWalletflag(false);
        WGL.setClosingBalance(taxwallet.getAvailableBalance());
        WGL.setPreviousBalance(WlltAvailBalance+"");
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL);

        /*
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL2.setTxnflag(txnFlag);
        WGL2.setCollectionaccount(liabilityAccount);
        WGL2.setAmount(Tax + "");
        WGL2.setCurrency(currency);
        WGL2.setWalletflag(false);
        WGL2.setClosingBalance(liabilityAccount.getAvailableBalance());
        WGL2.setPreviousBalance(AcctAvailBalance+"");
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL2);
        */
    }

    @Transactional
    public static void UpdateandLogCollectionAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag, String txnFlow, AccType category)
    {

        //Note: Raza In case of Source Charge, Destination Charge and Tax ; Pool/Settlement Account would have no impact as
        // it has mapping for Transaction Amount and Tax/Charges are extracted from transaction amount
        //eg. case: Txn Amount = 100 , Src Charge = 5 , Dest Charge = 5 , Tax Amount = 10 ==> Final AMount = 85(100-5-10){for source/sender} and 80(85-5){for destination/receiver} ; However ,Settlement Account will have 100

        CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, category);
        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

        //Long AmountCollectionAccount = Long.parseLong(GetUpdatedAmountForCollectionAccount(wsmodel));
        Long AmountCollectionAccount;// = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount;
        if (txnFlow.equals(TxnFlag.CASH_IN)) {
            AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount + AmtTranFee);
        } else {
            AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount + AmtTranFee;
        }

        //Long AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount;
        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
        }

        logger.info("Updating Collection Account Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccount);

        /*
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        balanceLog.setCollectionaccount(settlementaccount);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(AcctAvailBalance+"");
        balanceLog.setUpdatedbalance(settlementaccount.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setAmount((AmountCollectionAccount) + "");
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(balanceLog);
        */

        logger.info("Updating Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccount.getBankCode());
        balanceLog.setAccountNature(settlementaccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        /*
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL.setTxnflag(txnFlag);
        WGL.setCollectionaccount(settlementaccount);
        WGL.setAmount(AmountCollectionAccount + "");
        WGL.setCurrency(settlementaccount.getCurrency());
        WGL.setWalletflag(false);
        WGL.setClosingBalance(settlementaccount.getAvailableBalance());
        WGL.setPreviousBalance(AcctAvailBalance+"");
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL);
        */
    }

    @Transactional
    public static void UpdateandLogPartnerBankCollectionAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                                String txnFlow, AccType category, String bankAcronym,
                                                                boolean isChargeApply, boolean isApplyOnlyCharges)
    {

        //Note: Raza In case of Source Charge, Destination Charge and Tax ; Pool/Settlement Account would have no impact as
        // it has mapping for Transaction Amount and Tax/Charges are extracted from transaction amount
        //eg. case: Txn Amount = 100 , Src Charge = 5 , Dest Charge = 5 , Tax Amount = 10 ==> Final AMount = 85(100-5-10){for source/sender} and 80(85-5){for destination/receiver} ; However ,Settlement Account will have 100

        //String bankAcronym = GlobalContext.getInstance().getBankCodeByBin(linkAccount.getBranchId());
        CMSEMIAccountCollection settlementaccount = GetPartnerBankEMICollectionAccount(currency, category, bankAcronym);
        //CMSEMIWallet cmsEmiWallet = GetEMIWallet(currency, AccType.CAT_PARTNER_BANK_SETT_WLLT);
        logger.info("Fetching Partner Bank Settlement Wallet ...");
        CMSEMIWallet cmsEmiWallet = settlementaccount.getEmiwallet();
        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
        Long WlltActBalance = (Util.hasText(cmsEmiWallet.getActualBalance()) ? Long.parseLong(cmsEmiWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(cmsEmiWallet.getAvailableBalance()) ? Long.parseLong(cmsEmiWallet.getAvailableBalance()) : 0L) ;
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

        //Long AmountCollectionAccount = Long.parseLong(GetUpdatedAmountForCollectionAccount(wsmodel));
        Long AmountCollectionAccount;// = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount;
        if (isChargeApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + AmtTranFee;
            } else {
                if (txnFlow.equals(TxnFlag.CASH_IN)) {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount + AmtTranFee);
                } else {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount + AmtTranFee;
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }

        //Long AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + SrcChargeAmount;
        //m.rehman: 08-07-2020: Changing signs on Nayapay request
        /*
        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }
        */
        if(txnFlag.equals(TxnFlag.CREDIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //debit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating Partner Bank Settlement Account Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccount);

        logger.info("Updating Partner Bank Settlement Wallet Balance ...");
        cmsEmiWallet.setActualBalance(WlltActBalance + "");
        cmsEmiWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(cmsEmiWallet);

        /*
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        balanceLog.setCollectionaccount(settlementaccount);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(AcctAvailBalance+"");
        balanceLog.setUpdatedbalance(settlementaccount.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setAmount((AmountCollectionAccount) + "");
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setCreatedate(new Date());
        GeneralDao.Instance.save(balanceLog);
        */

        logger.info("Updating Partner Bank Settlement Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccount.getBankCode());
        balanceLog.setAccountNature(settlementaccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Partner Bank Settlement Wallet Balance Log ...");
        EMIWalletBalanceLog wlltbalanceLog = new EMIWalletBalanceLog();
        wlltbalanceLog.setEmiwallet(cmsEmiWallet);
        wlltbalanceLog.setChannelid(wsmodel.getChannelid());
        wlltbalanceLog.setOriginalbalance(WlltAvailBalance+"");
        wlltbalanceLog.setUpdatedbalance(cmsEmiWallet.getAvailableBalance());
        wlltbalanceLog.setTxnname(wsmodel.getServicename());
        wlltbalanceLog.setTransaction(wsmodel);
        wlltbalanceLog.setTxnnature(txnFlag);
        wlltbalanceLog.setAmount(AmountCollectionAccount + "");
        wlltbalanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================

        GeneralDao.Instance.save(wlltbalanceLog);

        /*
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL.setTxnflag(txnFlag);
        WGL.setCollectionaccount(settlementaccount);
        WGL.setAmount(AmountCollectionAccount + "");
        WGL.setCurrency(settlementaccount.getCurrency());
        WGL.setWalletflag(false);
        WGL.setClosingBalance(settlementaccount.getAvailableBalance());
        WGL.setPreviousBalance(AcctAvailBalance+"");
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL);
        */

        logger.info("Updating Partner Bank Settlement Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(cmsEmiWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(cmsEmiWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setClosingBalance(cmsEmiWallet.getAvailableBalance());
        WGL2.setPreviousBalance(AcctAvailBalance+"");
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL2);
    }

    @Transactional
    public static CMSEMIWallet GetRevenueWallet(String currency)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Fetching Revenue Wallet ...");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIWallet.class.getName() + " c where "+
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR" +
                " c.category = :CAT ";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        params.put("CAT", AccType.CAT_REVENUE_WLLT.StringValue());

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSEMIWallet revenuewallet = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params);
        CMSEMIWallet revenuewallet = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(revenuewallet != null && revenuewallet.getStatus().equals("00") && revenuewallet.getStatus().equals("00"))
        {
            return revenuewallet;
        }
        else
        {
            logger.error("Invalid or No Revenue account with currency [" + currency + "]");
            return null;
        }
    }

    @Transactional
    public static CMSEMIWallet GetSalesTaxWallet(String currency)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Fetching Tax Wallet ...");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIWallet.class.getName() + " c where "+
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR" +
                " c.category = :CAT ";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        params.put("CAT", AccType.CAT_SALESTAX_WLLT.StringValue());

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSEMIWallet salestaxwallet = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params);
        CMSEMIWallet salestaxwallet = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(salestaxwallet != null && salestaxwallet.getStatus().equals("00") && salestaxwallet.getStatus().equals("00"))
        {
            return salestaxwallet;
        }
        else
        {
            logger.error("Invalid or No SalesTax account found with currency [" + currency + "]");
            return null;
        }
    }

    @Transactional
    public static CMSEMIAccountCollection GetEMICollectionAccountForUnil(String currency, AccType category)
    {
        String dbQuery;
        Map<String, Object> params;

        dbQuery = "from " + FundBank.class.getName() + " c where c.bankCode= :CODE ";
        params = new HashMap<String, Object>();
        params.put("CODE", "588974");

        FundBank fundbank = (FundBank)GeneralDao.Instance.findObject(dbQuery, params);

        logger.info("Fetching Bank [" + fundbank.getBankName() + "] ...");

        logger.info("Fetching Account Collection of Category [" + category.StringValue() + "] ...");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " c where "+
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR" +
                " c.category = :CAT" + " and c.BankCode = :BANK ";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        //params.put("CAT", AccType.CAT_SETT_ACCT.StringValue());
        params.put("CAT", category.StringValue());
        params.put("BANK", fundbank);

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSEMIAccountCollection settlementaccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params);
        CMSEMIAccountCollection settlementaccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(settlementaccount != null && settlementaccount.getStatus().equals("00"))
        {
            return settlementaccount;
        }
        else
        {
            logger.error("Invalid or No Pool/Settlement account with currency [" + currency + "]");
            return null;
        }
    }

    @Transactional
    public static CMSEMIAccountCollection GetEMICollectionAccount(String currency, AccType category)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Fetching Account Collection of Category [" + category.StringValue() + "] ...");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " c where "+
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR" +
                " c.category = :CAT ";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        //params.put("CAT", AccType.CAT_SETT_ACCT.StringValue());
        params.put("CAT", category.StringValue());

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSEMIAccountCollection settlementaccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params);
        CMSEMIAccountCollection settlementaccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(settlementaccount != null && settlementaccount.getStatus().equals("00"))
        {
            return settlementaccount;
        }
        else
        {
            logger.error("Invalid or No Pool/Settlement account with currency [" + currency + "]");
            return null;
        }
    }

    @Transactional
    public static CMSEMIAccountCollection GetPartnerBankEMICollectionAccount(String currency, AccType category, String bankAcronym)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Fetching Partner Bank Settlement Collection Account with Currency : [" + currency + "], Bank Acronym : [" + bankAcronym + "]");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " c where " +
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR " +
                " c.category = :CAT " +
                " and c.BankCode.bankAcro = :BANK_ACR";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        //params.put("CAT", AccType.CAT_SETT_ACCT.StringValue());
        params.put("CAT", category.StringValue());
        params.put("BANK_ACR", bankAcronym);

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSEMIAccountCollection settlementaccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params);
        CMSEMIAccountCollection settlementaccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(settlementaccount != null && settlementaccount.getStatus().equals("00"))
        {
            return settlementaccount;
        }
        else
        {
            logger.error("Invalid or No Pool/Settlement account with currency [" + currency + "]");
            return null;
        }
    }

    @Transactional
    public static CMSEMIWallet GetEMIWallet(String currency, AccType category)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Fetching EMI Wallet of Category [" + category.StringValue() + "] ...");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIWallet.class.getName() + " c where "+
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR" +
                " c.category = :CAT ";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        //params.put("CAT", AccType.CAT_SETT_WALLET.StringValue());
        params.put("CAT", category.StringValue());

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSEMIWallet settlementaccount = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params);
        CMSEMIWallet settlementaccount = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(settlementaccount != null && settlementaccount.getStatus().equals("00"))
        {
            return settlementaccount;
        }
        else
        {
            logger.error("Invalid or No Pool/Settlement account with currency [" + currency + "]");
            return null;
        }
    }

    @Transactional
    public static CMSBusinessWallet GetBusinessWallet(String currency, String category)
    {
        String dbQuery;
        Map<String, Object> params;

        logger.info("Fetching Business Wallet of Category [" + category + "] ...");
        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSBusinessWallet.class.getName() + " c where c.Currency= :CURR" + " and c.category = :CAT ";
        params = new HashMap<String, Object>();
        params.put("CURR", currency);
        params.put("CAT", category);

        //m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
        //CMSBusinessWallet businessWallet = (CMSBusinessWallet)GeneralDao.Instance.findObject(dbQuery, params);
        CMSBusinessWallet businessWallet = (CMSBusinessWallet)GeneralDao.Instance.findObject(dbQuery, params, "c", LockMode.UPGRADE);

        if(businessWallet != null && businessWallet.getStatus().equals("00"))
        {
            return businessWallet;
        }
        else
        {
            logger.error("Invalid or No Business wallet with currency [" + currency + "]");
            return null;
        }
    }

    public static CMSEMIWallet GetThunesSettLedgerAccount(String currency)
    {
        String dbQuery;
        Map<String, Object> params;

        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSBusinessWallet.class.getName() + " c where c.Currency= :CURR" + " and c.category = :CAT ";
        params = new HashMap<String, Object>();
        params.put("CURR", currency);
        params.put("CAT", AccType.CAT_SETT_WALLET.StringValue());

        CMSEMIWallet settlementaccount = (CMSEMIWallet)GeneralDao.Instance.findObject(dbQuery, params);

        if(settlementaccount != null && settlementaccount.getStatus().equals("00"))
        {
            return settlementaccount;
        }
        else
        {
            logger.error("Invalid or No Pool/Settlement account with currency [" + currency + "]");
            return null;
        }
    }

    public static String GetUpdatedAmountForCollectionAccount(WalletCMSWsEntity wsmodel)
    {
        if(wsmodel.getServicename().equals("LoadWallet"))
        {
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long DestCharges = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);

            return (TxnAmount - DestCharges) + "";
        }
        else if(wsmodel.getServicename().equals("UnloadWallet"))
        {
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcCharges = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);

            return (TxnAmount + SrcCharges) + "";
        }
        else
        {
            Long SrcCharges = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestCharges = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);

            return (SrcCharges + DestCharges) + "";
        }
    }

    public static CMSEMIAccountCollection GetBillerAccount(String companyid, String currency)
    {
        String dbQuery;
        Map<String, Object> params;

        //Raza Wallet Must be verified before every transaction, as if it is blocked, every thing should be rejected
        dbQuery = "from " + CMSEMIAccountCollection.class.getName() + " c where "+
                //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//                " c.Currency= :CURR" +
                " c.category = :CAT and c.userid = :USERID ";
        params = new HashMap<String, Object>();
        //Commented by Moiz: RE: VC-NAP-202406121==>ORA Handling - Production
//        params.put("CURR", currency);
        params.put("CAT", AccType.CAT_BILLER_ACCT.StringValue());
        params.put("USERID", companyid);

        CMSEMIAccountCollection billeraccount = (CMSEMIAccountCollection)GeneralDao.Instance.findObject(dbQuery, params);

        if(billeraccount != null && billeraccount.getStatus().equals("00"))
        {
            return billeraccount;
        }
        else
        {
            logger.error("Invalid or No Biller Settlement account found with currency [" + currency + "] and UserId [" + companyid + "]");
            return null;
        }
    }

    //m.rehman: Euronet Integration
    @Transactional
    public static void UpdateandLogEuronetAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                  boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd,
                                                  boolean isLocal, boolean isReversal)
    {

        CMSEMIAccountCollection settlementaccount = null;
        CMSEMIWallet settlementWallet = null;

        //setting account and wallet information
        if (isLocal) {

        } else {
            if (isReversal) {
                settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_VISA_INTL_SETT_RECEIVABLE_ACCT);
                settlementWallet = GetEMIWallet(currency, AccType.CAT_VISA_INTL_SETT_RECEIVABLE_WLLT);
            } else {
                settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_VISA_INTL_SETT_PAYABLE_ACCT);
                settlementWallet = GetEMIWallet(currency, AccType.CAT_VISA_INTL_SETT_PAYABLE_WLLT);
            }
        }

        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountCollectionAccount;
        if (isChargesApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount-Tax) + TranFee;
            } else {
                if (isChargesAdd) {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating Euronet Settlement Collection Account Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccount);

        logger.info("Updating Euronet Settlement Wallet Balance ...");
        settlementWallet.setActualBalance(WlltActBalance + "");
        settlementWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementWallet);

        logger.info("Updating Euronet Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccount.getBankCode());
        balanceLog.setAccountNature(settlementaccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Euronet Settlement Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(settlementWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();

        GeneralDao.Instance.save(wlltBalanceLog);


        logger.info("Updating Euronet Settlement Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(settlementWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(settlementWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }

    //m.rehman: VP-NAP-202008211 / VC-NAP-202008211 - 17-09-2020 - Creation of Dispute transactions settlement feature (Phase I)
    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundWalletTransaction(WalletCMSWsEntity wsmodel, CMSAccount srcaccount, CMSAccount destaccount,
                                                  WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Performing Refund Wallet-To-Wallet Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long SrcAcctActBalance = (Util.hasText(srcaccount.getActualBalance()) ? Long.parseLong(srcaccount.getActualBalance()) : 0L);
            Long SrcAcctAvailBalance = (Util.hasText(srcaccount.getAvailableBalance()) ? Long.parseLong(srcaccount.getAvailableBalance()) : 0L);
            Long DestAcctActBalance = (Util.hasText(destaccount.getActualBalance()) ? Long.parseLong(destaccount.getActualBalance()) : 0L);
            Long DestAcctAvailBalance = (Util.hasText(destaccount.getAvailableBalance()) ? Long.parseLong(destaccount.getAvailableBalance()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long sourceFinalAmount = TxnAmount + SrcChargeAmount;
            Long destinationFinalAmount = TxnAmount - DestChargeAmount;
            Long sourceUpdatedBalance =  SrcAcctActBalance - sourceFinalAmount;
            Long destinationUpdatedBalance = DestAcctActBalance + destinationFinalAmount;

            if(SrcAcctAvailBalance < sourceFinalAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
            if (destinationFinalAmount < 0) {
                logger.error("Calculated destination amount [" + destinationFinalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Source Wallet Balance ...");
            srcaccount.setActualBalance((sourceUpdatedBalance) + "");
            srcaccount.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(srcaccount);

            logger.info("Updating Destination Wallet Balance ...");
            destaccount.setActualBalance((destinationUpdatedBalance) + "");
            destaccount.setAvailableBalance((destinationUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(destaccount);

            logger.info("Updating Source Wallet Balance Log ...");
            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(srcaccount);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(sourceFinalAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(srcaccount.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.DEBIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(SrcAcctAvailBalance + "");
            wsEntity.setClosingbalance(srcaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Destination Wallet Balance Log ...");
            WalletBalanceLog destbalanceLog = new WalletBalanceLog();
            destbalanceLog.setWallet(destaccount);
            destbalanceLog.setChannelid(wsmodel.getChannelid());
            destbalanceLog.setAmount(destinationFinalAmount + "");
            destbalanceLog.setOriginalbalance(DestAcctAvailBalance+"");
            destbalanceLog.setUpdatedbalance(destaccount.getAvailableBalance());
            destbalanceLog.setTxnname(wsmodel.getServicename());
            destbalanceLog.setTransaction(wsmodel);
            destbalanceLog.setTxnnature(TxnFlag.CREDIT);
            destbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(destbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setDestOpeningbalance(DestAcctAvailBalance + "");
            wsEntity.setDestClosingbalance(destaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Source Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(srcaccount);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(srcaccount.getCurrency());
            WGL.setWalletflag(true);
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setClosingBalance((SrcAcctAvailBalance - TxnAmount) + "");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            logger.info("Updating Destination Wallet General Ledger ...");
            WalletGeneralLedger WGL4 = new WalletGeneralLedger();
            WGL4.setTxnname(wsmodel.getServicename());
            WGL4.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL4.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL4.setTxnflag(TxnFlag.CREDIT);
            WGL4.setWallet(destaccount);
            WGL4.setAmount((destinationFinalAmount) + "");
            WGL4.setCurrency(destaccount.getCurrency());
            WGL4.setWalletflag(true);
            WGL4.setClosingBalance(destaccount.getAvailableBalance());
            WGL4.setPreviousBalance(DestAcctAvailBalance+"");
            WGL4.setMerchantid(wsmodel.getMerchantid());
            WGL4.setAgentid(wsmodel.getAgentid());
            WGL4.setBillerid(wsmodel.getBillerid());
            WGL4.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL4);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Source Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(srcaccount);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(srcaccount.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setPreviousBalance((SrcAcctAvailBalance - TxnAmount) +"");
                WGL2.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL2);

                //UpdateandLogCollectionAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.DEBIT);
                UpdateandLogRevenueAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Source Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(srcaccount);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(srcaccount.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setPreviousBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount)) + "");
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL3);

                UpdateandLogTaxAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund Wallet-To-Wallet Transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Refund Wallet-To-Wallet Transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Refund Wallet-To-Wallet Transaction...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundMerchantTransaction(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refunding Wallet for " + wsmodel.getOriginalapi() + "....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long DestAcctActBalance = Long.parseLong(account.getActualBalance());
            Long DestAcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long totalAmount = TxnAmount + SrcChargeAmount;

            //Getting merchant payable account
            CMSBusinessWallet settlementaccount = FinanceManager.GetBusinessWallet(account.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue());
            CMSEMIAccountCollection settlementCollAccount = FinanceManager.GetEMICollectionAccount(account.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_ACCT);

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
            Long CollAcctActBalance = (Util.hasText(settlementCollAccount.getActualBalance()) ? Long.parseLong(settlementCollAccount.getActualBalance()) : 0L);
            Long CollAcctAvailBalance = (Util.hasText(settlementCollAccount.getAvailableBalance()) ? Long.parseLong(settlementCollAccount.getAvailableBalance()) : 0L);

            //checking balances
            if (totalAmount > AcctAvailBalance || totalAmount > CollAcctAvailBalance) {
                logger.info("Merchant Settlement Wallet/Account has Low Balance, rejecting ...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((DestAcctActBalance + (TxnAmount + SrcChargeAmount)) + "");
            account.setAvailableBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount((TxnAmount + SrcChargeAmount) + "");
            balanceLog.setOriginalbalance(DestAcctAvailBalance+"");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(DestAcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(DestAcctAvailBalance +"");
            WGL.setClosingBalance((DestAcctAvailBalance + TxnAmount) + "");
            GeneralDao.Instance.save(WGL);

            UpdateandLogMerchantSettlementWallet(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((DestAcctAvailBalance + TxnAmount)+"");
                WGL2.setClosingBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount - Tax)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }
            //wsmodel.setNayapaytaxamount(tax);

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund " + wsmodel.getOriginalapi() + " completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Refund " + wsmodel.getOriginalapi() + " completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Refund " + wsmodel.getOriginalapi() + "...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundOnelinkBillerTransaction(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refunding Wallet for " + wsmodel.getOriginalapi() + "....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long DestAcctActBalance = Long.parseLong(account.getActualBalance());
            Long DestAcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long totalAmount = TxnAmount + SrcChargeAmount;

            //Getting merchant payable account
            //m.rehman: 21-04-2021, VC-NAP-202104201 - Error in refunds processing - updating settlement account and wallet
            //Bilal category updated : VC-NAP-202104201 - Error in refunds processing
            CMSEMIWallet settlementaccount = FinanceManager.GetEMIWallet(account.getCurrency(), AccType.CAT_1LINK_SETT_PAYABLE_WLLT);
            CMSEMIAccountCollection settlementCollAccount = FinanceManager.GetEMICollectionAccount(account.getCurrency(), AccType.CAT_1LINK_SETT_PAYABLE_ACCT);
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
            Long CollAcctActBalance = (Util.hasText(settlementCollAccount.getActualBalance()) ? Long.parseLong(settlementCollAccount.getActualBalance()) : 0L);
            Long CollAcctAvailBalance = (Util.hasText(settlementCollAccount.getAvailableBalance()) ? Long.parseLong(settlementCollAccount.getAvailableBalance()) : 0L);

            //checking balances
            if (totalAmount > AcctAvailBalance || totalAmount > CollAcctAvailBalance) {
                logger.info("Onelink Biller Settlement Wallet/Account has Low Balance, rejecting ...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((DestAcctActBalance + (TxnAmount + SrcChargeAmount)) + "");
            account.setAvailableBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount((TxnAmount + SrcChargeAmount) + "");
            balanceLog.setOriginalbalance(DestAcctAvailBalance+"");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(DestAcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(DestAcctAvailBalance +"");
            WGL.setClosingBalance((DestAcctAvailBalance + TxnAmount) + "");
            GeneralDao.Instance.save(WGL);

            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, true);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((DestAcctAvailBalance + TxnAmount)+"");
                WGL2.setClosingBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount - Tax)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }
            //wsmodel.setNayapaytaxamount(tax);

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund " + wsmodel.getOriginalapi() + " completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Refund " + wsmodel.getOriginalapi() + " completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Refund " + wsmodel.getOriginalapi() + "...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundCNICBasedCashWithdrawal(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refund Wallet for CNICBasedCashWithdrawal Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long tranFeeAmount = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long totalAmount = txnAmount + NayapayChargeAmount + BankCharges + BankTaxAmount + tranFeeAmount + SrcChargeAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            //Getting merchant payable account
			//s.mehtab 17/11/2020 deployment 10-11-2020
            CMSEMIWallet settlementaccount = FinanceManager.GetEMIWallet(account.getCurrency(), AccType.CAT_PARTNER_BANK_SETT_WLLT);
//            CMSEMIAccountCollection settlementCollAccount = FinanceManager.GetPartnerBankEMICollectionAccount(account.getCurrency(), AccType.CAT_PARTNER_BANK_SETT_ACCT);
            CMSEMIAccountCollection settlementCollAccount = FinanceManager.GetPartnerBankEMICollectionAccount(account.getCurrency(), AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode());

            Long bankAcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long bankAcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
            Long CollAcctActBalance = (Util.hasText(settlementCollAccount.getActualBalance()) ? Long.parseLong(settlementCollAccount.getActualBalance()) : 0L);
            Long CollAcctAvailBalance = (Util.hasText(settlementCollAccount.getAvailableBalance()) ? Long.parseLong(settlementCollAccount.getAvailableBalance()) : 0L);

            //checking balances
            if (totalAmount > bankAcctActBalance || totalAmount > CollAcctAvailBalance) {
                logger.info("Partner Bank Settlement Wallet/Account has Low Balance, rejecting ...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE);
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + totalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + totalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(totalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(totalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);
            UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT,
                    AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), false, false);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund CNICBasedCashWithdrawal in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Refund CNICBasedCashWithdrawal in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing Refund CNICBasedCashWithdrawal in wallet...");
            logger.error(e); // Asim Shahzad, Date : 26th Oct 2020, Tracking ID : VP-NAP-202008211/VC-NAP-202008211
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundCardBasedTransaction(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refund Wallet for Purchase Transaction....");

            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            SrcChargeAmount = SrcChargeAmount - Tax;
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            CMSEMIAccountCollection settlementaccount = null;
            CMSEMIWallet settlementWallet = null;
            Long ColAcctActBalance, ColAcctAvailBalance, WlltActBalance, WlltAvailBalance;
            if (Util.hasText(wsmodel.getChannelid()) && wsmodel.getChannelid().equals(ChannelCodes.ONELINK)) {

                //m.rehman: 21-04-2021, VC-NAP-202104201 - Error in refunds processing - updating settlement account and wallet
                settlementaccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_1LINK_SETT_PAYABLE_ACCT);
                settlementWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_1LINK_SETT_PAYABLE_WLLT);
                ///////////////////////////////////////////////////////////////////////////////////////////////////////
            }
            else if (Util.hasText(wsmodel.getChannelid()) && wsmodel.getChannelid().equals(ChannelCodes.EURONET)) {
                settlementaccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_VISA_INTL_SETT_PAYABLE_ACCT);
                settlementWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_VISA_INTL_SETT_PAYABLE_WLLT);
            }

            ColAcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            ColAcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
            WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
            WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;

            if(finalAmount > ColAcctAvailBalance || finalAmount > WlltAvailBalance)
            {
                logger.error("Insufficient Amount for Refund Purchase Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctActBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            wsmodel.setSrcchargeamount(StringUtils.leftPad(((SrcChargeAmount != null) ? SrcChargeAmount.toString() : "0"), 12, "0"));
            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);

            wsmodel.setNayapaytaxamount("");
            if (Util.hasText(wsmodel.getChannelid()) && wsmodel.getChannelid().equals(ChannelCodes.ONELINK)) {
                // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
                UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, true);
                // ===================================================================================
            } else if (Util.hasText(wsmodel.getChannelid()) && wsmodel.getChannelid().equals(ChannelCodes.EURONET)) {
                UpdateandLogEuronetAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, false, true);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund Purchase in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Refund Purchase in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing Refund Purchase in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundIBFT(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refund Wallet for IBFT Transaction....");

            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            Long finalAmount = TxnAmount + (AmtTranFee + SrcChargeAmount);

            //m.rehman: 21-04-2021, VC-NAP-202104201 - Error in refunds processing - updating settlement account and wallet
            CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_1LINK_SETT_PAYABLE_ACCT);
            CMSEMIWallet settlementWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_1LINK_SETT_PAYABLE_WLLT);
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////

            Long ColAcctActBalance = Long.parseLong(settlementaccount.getActualBalance());
            Long ColAcctAvailBalance = Long.parseLong(settlementaccount.getAvailableBalance());
            Long wlltAcctActBalance = Long.parseLong(settlementWallet.getActualBalance());
            Long wlltAcctAvailBalance = Long.parseLong(settlementWallet.getAvailableBalance());


            if(finalAmount > ColAcctAvailBalance || finalAmount > wlltAcctAvailBalance)
            {
                logger.error("Insufficient Amount for Refund IBFT Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
            UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, true);
            // ===================================================================================

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund IBFT in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Refund IBFT in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Refung IBFT in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundDebitCardRequest(WalletCMSWsEntity wsmodel, CMSAccount wallet, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refund Wallet for DebitCardRequest Transaction....");

            Long AcctActBalance = (Util.hasText(wallet.getActualBalance())) ? Long.parseLong(wallet.getActualBalance()) : 0L;
            Long AcctAvailBalance = (Util.hasText(wallet.getAvailableBalance())) ? Long.parseLong(wallet.getAvailableBalance()) : 0L;
            Long chargeAmount = (Util.hasText(wsmodel.getSrcchargeamount())) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L;
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount())) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L;

            logger.info("Updating Wallet Balance ...");
            wallet.setActualBalance((AcctActBalance + chargeAmount) + "");
            wallet.setAvailableBalance((AcctAvailBalance + chargeAmount) + "");
            GeneralDao.Instance.saveOrUpdate(wallet);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(wallet);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(chargeAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(wallet.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(wallet.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(wallet);
            WGL.setAmount(chargeAmount + "");
            WGL.setCurrency(wallet.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctActBalance + "");
            WGL.setClosingBalance(wallet.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            if(chargeAmount > 0) //TODO: Raza use with above Check
            {
                UpdateandLogRevenueAccount(wsmodel, wallet.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, wallet.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund Debit Card Request Charge Amount in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Refund Debit Card Request Charge Amount in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Refund DebitCardRequest...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    // Asim Shahzad, Date : 29th Sep 2020, Tracking ID : VC-NAP-202009253
    @Transactional
    public static void UpdateandLogSuspenseAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag, boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {
        CMSEMIAccountCollection suspenseAccount = GetEMICollectionAccount(currency, AccType.CAT_SUSPENSE_ACCT);
        Long AcctActBalance = (Util.hasText(suspenseAccount.getActualBalance()) ? Long.parseLong(suspenseAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(suspenseAccount.getAvailableBalance()) ? Long.parseLong(suspenseAccount.getAvailableBalance()) : 0L);

        CMSEMIWallet suspenseWallet = GetEMIWallet(currency, AccType.CAT_SUSPENSE_WLLT);
        Long WlltActBalance = (Util.hasText(suspenseWallet.getActualBalance()) ? Long.parseLong(suspenseWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(suspenseWallet.getAvailableBalance()) ? Long.parseLong(suspenseWallet.getAvailableBalance()) : 0L);

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountCollectionAccount;
        if (isChargesApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
            } else {
                if (isChargesAdd) {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }

        AcctActBalance = AcctActBalance + AmountCollectionAccount;
        WlltActBalance = WlltActBalance + AmountCollectionAccount;


        logger.info("Updating Suspense Collection Account Balance ...");
        suspenseAccount.setActualBalance(AcctActBalance + "");
        suspenseAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(suspenseAccount);

        logger.info("Updating Suspense Wallet Balance ...");
        suspenseWallet.setActualBalance(WlltActBalance + "");
        suspenseWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(suspenseWallet);

        logger.info("Updating Suspense Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if (txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(suspenseAccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(suspenseAccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(suspenseAccount.getBankCode());
        balanceLog.setAccountNature(suspenseAccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        balanceLog.setClosingBalance(StringUtils.leftPad(suspenseAccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(suspenseAccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Suspense Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(suspenseWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(suspenseWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================

        GeneralDao.Instance.save(wlltBalanceLog);
    }

    @Transactional
    public static void UpdateandLog1LinkReceivableAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd) {

        // Asim Shahzad, Date : 19th May 2021, Tracking ID : VC-NAP-202105061
        CMSEMIAccountCollection settlementaccountreceiveable = GetEMICollectionAccount(currency, AccType.CAT_1LINK_SETT_RECEIVABLE_ACCT);
        Long AcctActBalance = (Util.hasText(settlementaccountreceiveable.getActualBalance()) ? Long.parseLong(settlementaccountreceiveable.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccountreceiveable.getAvailableBalance()) ? Long.parseLong(settlementaccountreceiveable.getAvailableBalance()) : 0L);

        CMSEMIWallet settlementWalletReceiveable = GetEMIWallet(currency, AccType.CAT_1LINK_SETT_RECEIVABLE_WLLT);
        Long WlltActBalance = (Util.hasText(settlementWalletReceiveable.getActualBalance()) ? Long.parseLong(settlementWalletReceiveable.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(settlementWalletReceiveable.getAvailableBalance()) ? Long.parseLong(settlementWalletReceiveable.getAvailableBalance()) : 0L);

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountCollectionAccount;
        if (isChargesApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
            } else {
                if (isChargesAdd) {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }


        AcctActBalance = AcctActBalance + AmountCollectionAccount;
        WlltActBalance = WlltActBalance + AmountCollectionAccount;


        logger.info("Updating 1Link Receivable Settlement Collection Account Balance ...");
        settlementaccountreceiveable.setActualBalance(AcctActBalance + "");
        settlementaccountreceiveable.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccountreceiveable);

        logger.info("Updating 1Link Receivable Wallet Balance ...");
        settlementWalletReceiveable.setActualBalance(WlltActBalance + "");
        settlementWalletReceiveable.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementWalletReceiveable);

        logger.info("Updating 1Link Receivable Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if (txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccountreceiveable.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccountreceiveable.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccountreceiveable.getBankCode());
        balanceLog.setAccountNature(settlementaccountreceiveable.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccountpayable.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccountreceiveable.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating 1Link Receivable Settlement Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(settlementWalletReceiveable);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(settlementWalletReceiveable.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================

        GeneralDao.Instance.save(wlltBalanceLog);


        logger.info("Updating Wallet General Ledger ...");
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL.setTxnflag(txnFlag);
        WGL.setEmiaccount(settlementWalletReceiveable);
        WGL.setAmount(AmountCollectionAccount + "");
        WGL.setCurrency(settlementaccountreceiveable.getCurrency());
        WGL.setWalletflag(false);
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        WGL.setPreviousBalance(WlltAvailBalance + "");
        WGL.setClosingBalance(settlementaccountreceiveable.getAvailableBalance());
        GeneralDao.Instance.saveOrUpdate(WGL);

        // ============================================================================================
    }

    // ==================================================================

    //m.rehman: Euronet integration
    @Transactional
    public static boolean LocalPurchaseFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for LocalPurchaseFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - Tax;
            //=======================================================================================================
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            /*
            Long filerTaxAmount = 0L;

            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("Tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("Tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount + filerTaxAmount;
                logger.info("Filer/Non-Filer tax amount [" + filerTaxAmount + "]");

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }
            */

            SrcChargeAmount = SrcChargeAmount - Tax;

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for LocalPurchaseFromEuronet Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            account.setActualBalance((AcctActBalance - (finalAmount + Tax)) + "");
            account.setAvailableBalance((AcctAvailBalance - (finalAmount + Tax)) + "");
            //=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            //WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
			//=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaLocalSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + Tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            /*
            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + Tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + Tax + filerTaxAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, filerTaxAmount);
            }
            */

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("LocalPurchaseFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing LocalPurchaseFromEuronet in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean LocalPreAuthorizationFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for LocalPreAuthorizationFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            SrcChargeAmount = SrcChargeAmount - Tax;

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for LocalPreAuthorizationFromEuronet Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaSundryAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + Tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("LocalPreAuthorizationFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing LocalPreAuthorizationFromEuronet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean LocalPreAuthCompletionFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for LocalPreAuthCompletionFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            SrcChargeAmount = SrcChargeAmount - Tax;

            /*
            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for LocalPreAuthCompletionFromEuronet Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);
            */

            UpdateandLogVisaSundryAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);
            UpdateandLogVisaLocalSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + Tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("LocalPreAuthCompletionFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing LocalPreAuthorizationFromEuronet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalBalanceInquiryFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for BalanceInquiry from Euronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);

            //Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;
            Long finalAmount = AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;

            SrcChargeAmount = SrcChargeAmount - Tax;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for BalanceInquiry Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            //wsmodel.setSrcchargeamount(StringUtils.leftPad(((SrcChargeAmount != null) ? SrcChargeAmount.toString() : "0"), 12, "0"));
            //wsmodel.setNayapaytaxamount("");
            //UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, true, true);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            logger.info("BalanceInquiryFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Performing BalanceInquiryFromEuronet in wallet...");
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalCashWithDrawalFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for InternationalCashWithDrawalFromEuronet Transaction....");

            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long filerTaxAmount = 0L;

            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }

            SrcChargeAmount = SrcChargeAmount - tax;

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for CashWithdrawal Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            account.setActualBalance((AcctActBalance - (finalAmount + tax)) + "");
            account.setAvailableBalance((AcctAvailBalance - (finalAmount + tax)) + "");
            //=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            //WGL.setClosingBalance(account.getAvailableBalance());
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
            //=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, filerTaxAmount);
            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("InternationalCashWithDrawalFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing InternationalCashWithDrawalFromEuronet in wallet...");
            logger.error(e);
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalPurchaseFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for Purchase Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long filerTaxAmount = 0L;

            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("Tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("Tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021
                logger.info("Filer/Non-Filer tax amount [" + filerTaxAmount + "]");

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }

            SrcChargeAmount = SrcChargeAmount - tax;

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for Purchase Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            account.setActualBalance((AcctActBalance - (finalAmount + tax)) + "");
            account.setAvailableBalance((AcctAvailBalance - (finalAmount + tax)) + "");
            //=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            //Arsalan Akhter, Date: 09-Aug-2021, Ticket: VC-NAP-202108091(Issue in Customer Wallet Balance/Statement)
            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
            //=======================================================================================================
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, filerTaxAmount);
            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("Purchase in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing Purchase in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalPreAuthorizationFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for InternationalPreAuthorizationFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long filerTaxAmount = 0L;

            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("Tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("Tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }

            SrcChargeAmount = SrcChargeAmount - tax;

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for Purchase Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaSundryAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, filerTaxAmount);
            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("InternationalPreAuthorizationFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing InternationalPreAuthorizationFromEuronet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalPreAuthCompletionFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for InternationalPreAuthCompletionFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long filerTaxAmount = 0L;

            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("Tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("Tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }

            SrcChargeAmount = SrcChargeAmount - tax;

            /*
            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for Purchase Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(WGL);
            */

            UpdateandLogVisaSundryAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);
            UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, filerTaxAmount);
            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("InternationalPreAuthCompletionFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing InternationalPreAuthorizationFromEuronet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean LocalCardBasedReversalFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for LocalCardBasedReversalFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long finalAmount = txnAmount + SrcChargeAmount + BankCharges + BankTaxAmount + AmtTranFee;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance((AcctAvailBalance + txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaLocalSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true);

            if(SrcChargeAmount > 0 || AmtTranFee > 0)
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount((SrcChargeAmount - tax) + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax) + tax) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId());
            logger.info("LocalCardBasedReversalFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing LocalCardBasedReversalFromEuronet in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalCardBasedReversalFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for InternationalCardBasedReversal Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long finalAmount = txnAmount + SrcChargeAmount + BankCharges + BankTaxAmount + AmtTranFee;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long filerTaxAmount = (Util.hasText(wsmodel.getWithholdingtaxamount()) ? Long.parseLong(wsmodel.getWithholdingtaxamount()) : 0L);
            finalAmount = finalAmount + filerTaxAmount;

            /*
            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount - filerTaxAmount;

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }
            */

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance((AcctAvailBalance + txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true);

            if(SrcChargeAmount > 0 || AmtTranFee > 0)
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount((SrcChargeAmount - tax) + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax) + tax) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax) + tax) + "");
                WGL3.setClosingBalance((AcctAvailBalance + txnAmount + (SrcChargeAmount - tax)  + tax + filerTaxAmount) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, filerTaxAmount);
            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId());
            logger.info("InternationalCardBasedReversal in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing InternationalCardBasedReversal in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean LocalPreAuthorizationReversalFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for LocalPreAuthorizationReversalFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            SrcChargeAmount = SrcChargeAmount - Tax;

            /*
            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for LocalPreAuthorizationReversalFromEuronet Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }
            */

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaSundryAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + Tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("LocalPreAuthorizationReversalFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing LocalPreAuthorizationReversalFromEuronet in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean InternationalPreAuthorizationReversalFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for InternationalPreAuthorizationReversalFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax;
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long filerTaxAmount = (Util.hasText(wsmodel.getWithholdingtaxamount()) ? Long.parseLong(wsmodel.getWithholdingtaxamount()) : 0L);
            finalAmount = finalAmount + filerTaxAmount;
            //Long filerTaxAmount = 0L;

            /*
            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
            Map<String, Object> params = new HashMap<>();
            params.put("CNIC", account.getCustomer().getCnic());
            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
            Tax filerTax;
            if (fbratl == null) {
                logger.info("Tax payer information not found in system, getting non-filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            } else {
                logger.info("Tax payer information found in system, getting filer tax ...");
                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
                params = new HashMap<>();
                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
            }

            if (filerTax != null) {
                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021

                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
                // =======================================================================================
            }
            */

            SrcChargeAmount = SrcChargeAmount - tax;

            /*
            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for Purchase Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }
            */

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaSundryAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(filerTaxAmount > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(filerTaxAmount + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, filerTaxAmount);
            }

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("InternationalPreAuthorizationReversalFromEuronet in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing InternationalPreAuthorizationReversalFromEuronet in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static void UpdateandLog1LinkVisaSettlementtAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                  boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {

        CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_1LINK_VISA_SETT_PAYABLE_ACCT);
        CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_1LINK_VISA_SETT_PAYABLE_WLLT);

        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountCollectionAccount;
        if (isChargesApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount-Tax) + TranFee;
            } else {
                if (isChargesAdd) {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating Euronet Settlement Collection Account Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccount);

        logger.info("Updating Euronet Settlement Wallet Balance ...");
        settlementWallet.setActualBalance(WlltActBalance + "");
        settlementWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementWallet);

        logger.info("Updating Euronet Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccount.getBankCode());
        balanceLog.setAccountNature(settlementaccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Euronet Settlement Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(settlementWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();

        GeneralDao.Instance.save(wlltBalanceLog);

        logger.info("Updating Euronet Settlement Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(settlementWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(settlementWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }

    @Transactional
    public static void UpdateandLogVisaLocalSettlementtAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                               boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {

        CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_VISA_LCL_SETT_PAYABLE_ACCT);
        CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_VISA_LCL_SETT_PAYABLE_WLLT);

        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountCollectionAccount;
        if (isChargesApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount-Tax) + TranFee;
            } else {
                if (isChargesAdd) {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - TxnAmount;
            WlltActBalance = WlltActBalance - TxnAmount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + TxnAmount;
            WlltActBalance = WlltActBalance + TxnAmount;
        }

        logger.info("Updating Euronet Settlement Collection Account Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccount);

        logger.info("Updating Euronet Settlement Wallet Balance ...");
        settlementWallet.setActualBalance(WlltActBalance + "");
        settlementWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementWallet);

        logger.info("Updating Euronet Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
            balanceLog.setDebitAmount(TxnAmount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
            balanceLog.setCreditAmount(TxnAmount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccount.getBankCode());
        balanceLog.setAccountNature(settlementaccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Euronet Settlement Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(settlementWallet);
        wlltBalanceLog.setAmount(TxnAmount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();

        GeneralDao.Instance.save(wlltBalanceLog);

        logger.info("Updating Euronet Settlement Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(settlementWallet);
        WGL2.setAmount(TxnAmount + "");
        WGL2.setCurrency(settlementWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }

    @Transactional
    public static void UpdateandLogVisaIntlSettlementtAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                              boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {

        try {
            CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_VISA_INTL_SETT_PAYABLE_ACCT);
            CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_VISA_INTL_SETT_PAYABLE_WLLT);

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L);

            Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
            Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L);

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            //Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            Long TxnAmount;
            //in case of transaction in other currency
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            if (Util.hasText(wsmodel.getCbillamount())) {
                if (Util.hasText(wsmodel.getReserved()) && wsmodel.getReserved().equals("partial")) {
                    logger.info("Using Transaction Amount [" + wsmodel.getAmounttransaction() + "]");
                    TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                }
                else {
                    logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                    TxnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
                }
            }
            //=====================================================================================
            else {
                TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmountCollectionAccount;
            if (isChargesApply) {
                if (isApplyOnlyCharges) {
                    AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    if (isChargesAdd) {
                        AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                    }
                }
            } else {
                AmountCollectionAccount = TxnAmount;
            }

            if (txnFlag.equals(TxnFlag.DEBIT)) {
                AcctActBalance = AcctActBalance - TxnAmount;
                WlltActBalance = WlltActBalance - TxnAmount;
            } else //Credit
            {
                AcctActBalance = AcctActBalance + TxnAmount;
                WlltActBalance = WlltActBalance + TxnAmount;
            }

            logger.info("Updating Visa International Settlement Collection Account Balance ...");
            settlementaccount.setActualBalance(AcctActBalance + "");
            settlementaccount.setAvailableBalance(AcctActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementaccount);

            logger.info("Updating Visa International Settlement Wallet Balance ...");
            settlementWallet.setActualBalance(WlltActBalance + "");
            settlementWallet.setAvailableBalance(WlltActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementWallet);

            logger.info("Updating Visa International Collection Account Balance Log ...");
            EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
            if (txnFlag.equals(TxnFlag.DEBIT)) {
                balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
                balanceLog.setDebitAmount(TxnAmount + "");
            } else {
                balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
                balanceLog.setCreditAmount(TxnAmount + "");
            }
            balanceLog.setTxnId(wsmodel.getTranrefnumber());
            balanceLog.setBankId(settlementaccount.getBankCode());
            balanceLog.setAccountNature(settlementaccount.getAccountType());
            balanceLog.setTranDate(wsmodel.getTransdatetime());
//            balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
            balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
            balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Visa International Settlement Wallet Balance Log ...");
            EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
            wlltBalanceLog.setEmiwallet(settlementWallet);
            wlltBalanceLog.setAmount(TxnAmount + "");
            wlltBalanceLog.setChannelid(wsmodel.getChannelid());
            wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
            wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
            wlltBalanceLog.setTransaction(wsmodel);
            wlltBalanceLog.setTxnname(wsmodel.getServicename());
            wlltBalanceLog.setTxnnature(txnFlag);
            wlltBalanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
            GeneralDao.Instance.getNextValEmiCollBalLog();

            GeneralDao.Instance.save(wlltBalanceLog);

            logger.info("Updating Visa International Settlement Wallet General Ledger ...");
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(txnFlag);
            WGL2.setEmiaccount(settlementWallet);
            WGL2.setAmount(TxnAmount + "");
            WGL2.setCurrency(settlementWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setPreviousBalance(WlltAvailBalance + "");
            WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
            GeneralDao.Instance.save(WGL2);

        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Performing VisaIntlSettlementFromEuronet in wallet...");
        }
    }

    @Transactional
    public static void UpdateandLogVisaSundryAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                     boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {

        try {
            CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_PREAUTH_SNDRY_ACCT);
            CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_PREAUTH_SNDRY_WLLT);

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L);

            Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
            Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L);

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            //Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            Long TxnAmount;
            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                TxnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmountCollectionAccount;
            if (isChargesApply) {
                if (isApplyOnlyCharges) {
                    AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    if (isChargesAdd) {
                        AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                    }
                }
            } else {
                AmountCollectionAccount = TxnAmount;
            }

            if (txnFlag.equals(TxnFlag.DEBIT)) {
                AcctActBalance = AcctActBalance - AmountCollectionAccount;
                WlltActBalance = WlltActBalance - AmountCollectionAccount;
            } else //Credit
            {
                AcctActBalance = AcctActBalance + AmountCollectionAccount;
                WlltActBalance = WlltActBalance + AmountCollectionAccount;
            }

            logger.info("Updating PreAuthorization Sundry Collection Account Balance ...");
            settlementaccount.setActualBalance(AcctActBalance + "");
            settlementaccount.setAvailableBalance(AcctActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementaccount);

            logger.info("Updating PreAuthorization Sundry Wallet Balance ...");
            settlementWallet.setActualBalance(WlltActBalance + "");
            settlementWallet.setAvailableBalance(WlltActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementWallet);

            logger.info("Updating PreAuthorization Sundry Account Balance Log ...");
            EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
            if (txnFlag.equals(TxnFlag.DEBIT)) {
                balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
                balanceLog.setDebitAmount(AmountCollectionAccount + "");
            } else {
                balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
                balanceLog.setCreditAmount(AmountCollectionAccount + "");
            }
            balanceLog.setTxnId(wsmodel.getTranrefnumber());
            balanceLog.setBankId(settlementaccount.getBankCode());
            balanceLog.setAccountNature(settlementaccount.getAccountType());
            balanceLog.setTranDate(wsmodel.getTransdatetime());
            //balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
            balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
            balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating PreAuthorization Sundry Wallet Balance Log ...");
            EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
            wlltBalanceLog.setEmiwallet(settlementWallet);
            wlltBalanceLog.setAmount(AmountCollectionAccount + "");
            wlltBalanceLog.setChannelid(wsmodel.getChannelid());
            wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
            wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
            wlltBalanceLog.setTransaction(wsmodel);
            wlltBalanceLog.setTxnname(wsmodel.getServicename());
            wlltBalanceLog.setTxnnature(txnFlag);
            wlltBalanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
            GeneralDao.Instance.getNextValEmiCollBalLog();

            GeneralDao.Instance.save(wlltBalanceLog);

            logger.info("Updating PreAuthorization Sundry Wallet General Ledger ...");
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(txnFlag);
            WGL2.setEmiaccount(settlementWallet);
            WGL2.setAmount(AmountCollectionAccount + "");
            WGL2.setCurrency(settlementWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setPreviousBalance(WlltAvailBalance + "");
            WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
            GeneralDao.Instance.save(WGL2);

        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Performing PreAuthorization Sundry in wallet...");
        }
    }

    @Transactional
    public static void UpdateandLogFilerTaxAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag, Long Tax)
    {
        CMSEMIWallet taxwallet = GetEMIWallet(currency, AccType.CAT_WITHHOLDING_INCOME_TAX_WLLT);
        CMSEMIAccountCollection liabilityAccount = GetEMICollectionAccount(currency, AccType.CAT_WITHHOLDING_INCOME_TAX_ACCT);

        Long WlltActBalance = (Util.hasText(taxwallet.getActualBalance()) ? Long.parseLong(taxwallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(taxwallet.getAvailableBalance()) ? Long.parseLong(taxwallet.getAvailableBalance()) : 0L) ;
        Long AcctActBalance = (Util.hasText(liabilityAccount.getActualBalance()) ? Long.parseLong(liabilityAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(liabilityAccount.getAvailableBalance()) ? Long.parseLong(liabilityAccount.getAvailableBalance()) : 0L) ;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            WlltActBalance = WlltActBalance - Tax;
            AcctActBalance = AcctActBalance - Tax;
        }
        else
        {
            WlltActBalance = WlltActBalance + Tax;
            AcctActBalance = AcctActBalance + Tax;
        }

        logger.info("Updating Filer Tax Wallet Balance ...");
        taxwallet.setActualBalance(WlltActBalance + "");
        taxwallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(taxwallet);

        logger.info("Updating Filer Tax Wallet Collection Account Balance ...");
        liabilityAccount.setActualBalance(AcctActBalance + "");
        liabilityAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(liabilityAccount);

        logger.info("Updating Filer Tax Wallet Balance Log ...");
        EMIWalletBalanceLog balanceLog = new EMIWalletBalanceLog();
        balanceLog.setEmiwallet(taxwallet);
        balanceLog.setChannelid(wsmodel.getChannelid());
        balanceLog.setOriginalbalance(WlltAvailBalance+"");
        balanceLog.setUpdatedbalance(taxwallet.getAvailableBalance());
        balanceLog.setTxnname(wsmodel.getServicename());
        balanceLog.setTransaction(wsmodel);
        balanceLog.setTxnnature(txnFlag);
        balanceLog.setAmount(Tax + "");
        balanceLog.setCreatedate(new Date());

        // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================

        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating Filer Tax Collection Account Balance Log ...");
        EMICollectionBalanceLog collbalanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            collbalanceLog.setDebitAccount(liabilityAccount.getAccountNumber());
            collbalanceLog.setDebitAmount(Tax + "");
        } else {
            collbalanceLog.setCreditAccount(liabilityAccount.getAccountNumber());
            collbalanceLog.setCreditAmount(Tax + "");
        }
        collbalanceLog.setTxnId(wsmodel.getTranrefnumber());
        collbalanceLog.setBankId(liabilityAccount.getBankCode());
        collbalanceLog.setAccountNature(liabilityAccount.getAccountType());
        collbalanceLog.setTranDate(wsmodel.getTransdatetime());
        //Added by Mehtab on 22/01/2021 tracking id: VC-NAP-202101191
//        collbalanceLog.setClosingBalance(StringUtils.leftPad(liabilityAccount.getAvailableBalance(), 12, "0"));
        collbalanceLog.setClosingBalance(liabilityAccount.getAvailableBalance());
        collbalanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(collbalanceLog);

        logger.info("Updating Filer Tax Wallet General Ledger ...");
        WalletGeneralLedger WGL = new WalletGeneralLedger();
        WGL.setTxnname(wsmodel.getServicename());
        WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL.setTxnrefnum(wsmodel.getTranrefnumber());
        WGL.setTxnflag(txnFlag);
        WGL.setEmiaccount(taxwallet);
        WGL.setAmount(Tax + "");
        WGL.setCurrency(currency);
        WGL.setWalletflag(false);
        WGL.setClosingBalance(taxwallet.getAvailableBalance());
        WGL.setPreviousBalance(WlltAvailBalance+"");
        WGL.setMerchantid(wsmodel.getMerchantid());
        WGL.setAgentid(wsmodel.getAgentid());
        WGL.setBillerid(wsmodel.getBillerid());
        WGL.setTransaction(wsmodel);
        GeneralDao.Instance.save(WGL);

        //m.rehman: 29-04-2021, VG-NAP-202104271 / VP-NAP-202104261 / VC-NAP-202104261 - VISA transaction charging update
        logger.info("Adding WithHoldingTax transaction ...");
        WalletCMSWsEntity wsEntity = wsmodel.copy();
        wsEntity.setOrigdataelement(wsmodel.getTranrefnumber());
        //Bilal : 24-6-2021 commenting below line against : VC-NAP-202106141
        //wsEntity.setOriginalapi(wsmodel.getServicename());
        wsEntity.setTranrefnumber("WH" + wsmodel.getTranrefnumber());
        wsEntity.setServicename("WithHoldingTax");
        wsEntity.setAmounttransaction(Tax.toString());
        wsEntity.setWithholdingtaxamount("");
        wsEntity.setSrcchargeamount("");
        wsEntity.setNayapaytaxamount("");
        wsEntity.setAmttranfee("");
        GeneralDao.Instance.save(wsEntity);
        ////////////////////////////////////////////////////////////////////////////////////////////////////////
    }


    // Asim Shahzad, Date : 3rd March 2021, Tracking ID : VC-NAP-202103033
    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundLocalPurchaseFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Loading Wallet for RefundLocalPurchaseFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            if (Util.hasText(wsmodel.getCbillamount())) {
                if (Util.hasText(wsmodel.getReserved()) && wsmodel.getReserved().equals("partial")) {
                    logger.info("Using Transaction Amount [" + wsmodel.getAmounttransaction() + "]");
                    txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                }
                else {
                    logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                    txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
                }
            }
            //=====================================================================================
            else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            Long WithHoldingTax = (Util.hasText(wsmodel.getWithholdingtaxamount()) ? Long.parseLong(wsmodel.getWithholdingtaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount + WithHoldingTax;
            //======================================================================================
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            SrcChargeAmount = SrcChargeAmount - Tax;

//            if(finalAmount > AcctAvailBalance)
//            {
//                logger.error("Insufficient Amount for LocalPurchaseFromEuronet Transaction, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
//                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
//                wsmodel.setAcctbalance(AcctAvailBalance.toString());
//                // ====================================================================================
//                return false;
//            }

            logger.info("Updating Wallet Balance ...");
//            account.setActualBalance((AcctActBalance - finalAmount) + "");
//            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            account.setActualBalance((AcctActBalance + finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance + finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            //WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setClosingBalance((AcctAvailBalance + txnAmount) + "");
            //======================================================================================
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaLocalSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                //======================================================================================
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
//                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
//                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + Tax)) + "");
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + Tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);

            }

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("RefundLocalPurchaseFromEuronet in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("RefundLocalPurchaseFromEuronet in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing RefundLocalPurchaseFromEuronet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundInternationalPurchaseFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Loading Wallet for RefundInternationalPurchaseFromEuronet Transaction....");

            //Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long txnAmount;

            //in case of transaction in other currency
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            if (Util.hasText(wsmodel.getCbillamount())) {
                if (Util.hasText(wsmodel.getReserved()) && wsmodel.getReserved().equals("partial")) {
                    logger.info("Using Transaction Amount [" + wsmodel.getAmounttransaction() + "]");
                    txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                }
                else {
                    logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                    txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
                }
            }
            //=====================================================================================
            else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            Long WithHoldingTax = (Util.hasText(wsmodel.getWithholdingtaxamount()) ? Long.parseLong(wsmodel.getWithholdingtaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax + WithHoldingTax;
            //======================================================================
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            //Long filerTaxAmount = 0L;
            //=====================================================================================

            //Arsalan Commited, Date: 05-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)

//            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
//            Map<String, Object> params = new HashMap<>();
//            params.put("CNIC", account.getCustomer().getCnic());
//            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
//            Tax filerTax;
//            if (fbratl == null) {
//                logger.info("Tax payer information not found in system, getting non-filer tax ...");
//                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
//                params = new HashMap<>();
//                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
//            } else {
//                logger.info("Tax payer information found in system, getting filer tax ...");
//                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
//                params = new HashMap<>();
//                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
//            }
//
//            if (filerTax != null) {
//                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
//                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
//                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021
//                logger.info("Filer/Non-Filer tax amount [" + filerTaxAmount + "]");
//
//                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
//                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
//                // =======================================================================================
//            }
            // =========================================================================================================

            SrcChargeAmount = SrcChargeAmount - tax;

//            if(finalAmount > AcctAvailBalance)
//            {
//                logger.error("Insufficient Amount for Purchase Transaction, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
//                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
//                wsmodel.setAcctbalance(AcctAvailBalance.toString());
//                // ====================================================================================
//                return false;
//            }

            logger.info("Updating Wallet Balance ...");
//            account.setActualBalance((AcctActBalance - finalAmount) + "");
//            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");

            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            account.setActualBalance((AcctActBalance + (finalAmount + tax)) + "");
            account.setAvailableBalance((AcctAvailBalance + (finalAmount + tax)) + "");
            // ====================================================================================
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
//            WGL.setClosingBalance((AcctAvailBalance - txnAmount) + "");
            WGL.setClosingBalance((AcctAvailBalance + txnAmount) + "");
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
//                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
//                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
//                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
//                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            if(WithHoldingTax > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(WithHoldingTax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
//                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
//                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax + WithHoldingTax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, WithHoldingTax);
            }
            //=====================================================================================

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("RefundInternationalPurchaseFromEuronet in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("RefundInternationalPurchaseFromEuronet in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing RefundInternationalPurchaseFromEuronet in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundInternationalCashWithDrawalFromEuronet(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Loading Wallet for RefundInternationalCashWithDrawalFromEuronet Transaction....");

            Long txnAmount;

            //in case of transaction in other currency
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            if (Util.hasText(wsmodel.getCbillamount())) {
                if (Util.hasText(wsmodel.getReserved()) && wsmodel.getReserved().equals("partial")) {
                    logger.info("Using Transaction Amount [" + wsmodel.getAmounttransaction() + "]");
                    txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
                }
                else {
                    logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                    txnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
                }
            }
            //=====================================================================================
            else {
                txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            //Arsalan Akhter, Date: 04-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)
            Long WithHoldingTax = (Util.hasText(wsmodel.getWithholdingtaxamount()) ? Long.parseLong(wsmodel.getWithholdingtaxamount()) : 0L);
            Long finalAmount = txnAmount + AmtTranFee + SrcChargeAmount + BankCharges + BankTaxAmount - tax + WithHoldingTax;
            //=====================================================================================
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            //Long filerTaxAmount = 0L;
            //=====================================================================================

            //Arsalan Commited, Date: 05-Aug-2021, Ticket: VP-NAP-202103292 / VC-NAP-202103293(Refund Module Part 2)

//            String dbQuery = "from " + FBRATL.class.getName() + " f where f.ntnCnic= :CNIC ";
//            Map<String, Object> params = new HashMap<>();
//            params.put("CNIC", account.getCustomer().getCnic());
//            FBRATL fbratl = (FBRATL) GeneralDao.Instance.findObject(dbQuery, params);
//            Tax filerTax;
//            if (fbratl == null) {
//                logger.info("tax payer information not found in system, getting non-filer tax ...");
//                dbQuery = "from " + Tax.class.getName() + " t where title='NON-FILER'";
//                params = new HashMap<>();
//                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
//            } else {
//                logger.info("tax payer information found in system, getting filer tax ...");
//                dbQuery = "from " + Tax.class.getName() + " t where title='FILER'";
//                params = new HashMap<>();
//                filerTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);
//            }
//
//            if (filerTax != null) {
//                logger.info("Filer/Non-Filer tax found in system, applying tax ...");
//                filerTaxAmount = Long.parseLong(new DecimalFormat("0.00").format(((txnAmount/100.0) * (Double.parseDouble(filerTax.getValue())/100.0))).replace(".", ""));
//                finalAmount = finalAmount + filerTaxAmount; //Arsalan Akhter , Date: 10-June-2021
//
//                // Asim Shahzad, Date : 18th March 2021, Tracking ID : VP-NAP-202103117 / VC-NAP-202103116
//                wsmodel.setWithholdingtaxamount(filerTaxAmount.toString());
//                // =======================================================================================
//            }
            // =========================================================================================================

            SrcChargeAmount = SrcChargeAmount - tax;

//            if(finalAmount > AcctAvailBalance)
//            {
//                logger.error("Insufficient Amount for CashWithdrawal Transaction, rejecting...");
//                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
//                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
//                wsmodel.setAcctbalance(AcctAvailBalance.toString());
//                // ====================================================================================
//                return false;
//            }

            logger.info("Updating Wallet Balance ...");
//            account.setActualBalance((AcctActBalance - finalAmount) + "");
//            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            account.setActualBalance((AcctActBalance + (finalAmount + tax)) + "");
            account.setAvailableBalance((AcctAvailBalance + (finalAmount + tax)) + "");
            // ====================================================================================
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.CREDIT);
            balanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(AcctAvailBalance + "");
            wsEntity.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            WGL.setAmount(txnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            //WGL.setClosingBalance(account.getAvailableBalance());
            WGL.setClosingBalance((AcctAvailBalance + txnAmount) + "");
            //=====================================================================================
            GeneralDao.Instance.saveOrUpdate(WGL);

            UpdateandLogVisaIntlSettlementtAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, false, false, false);

            if(SrcChargeAmount > 0 || AmtTranFee > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.CREDIT);
                WGL2.setWallet(account);
                WGL2.setAmount(SrcChargeAmount + "");
                WGL2.setCurrency(account.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
//                WGL2.setPreviousBalance((AcctAvailBalance - txnAmount)+"");
//                WGL2.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
                WGL2.setPreviousBalance((AcctAvailBalance + txnAmount)+"");
                WGL2.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.save(WGL2);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            if(tax > 0)
            {
                logger.info("Updating Wallet General Ledger for tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(tax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
//                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount)) + "");
//                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
            }

            //Arsalan Akhter, Date:24-Aug-2021, Ticket: VC-NAP-202108241(Issues in VISA Refunds)
            if(WithHoldingTax > 0)
            {
                logger.info("Updating Wallet General Ledger for filer tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.CREDIT);
                WGL3.setWallet(account);
                WGL3.setAmount(WithHoldingTax + "");
                WGL3.setCurrency(account.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
//                WGL3.setPreviousBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax)) + "");
//                WGL3.setClosingBalance((AcctAvailBalance - (txnAmount + SrcChargeAmount + tax + filerTaxAmount)) + "");
                WGL3.setPreviousBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax)) + "");
                WGL3.setClosingBalance((AcctAvailBalance + (txnAmount + SrcChargeAmount + tax + WithHoldingTax)) + "");
                GeneralDao.Instance.save(WGL3);

                UpdateandLogFilerTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, WithHoldingTax);
            }
            //=====================================================================================

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("RefundInternationalCashWithDrawalFromEuronet in Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("RefundInternationalCashWithDrawalFromEuronet in Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing RefundInternationalCashWithDrawalFromEuronet in wallet...");
            logger.error(e);
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }
    // =======================================================================================================


    // Asim Shahzad, Date : 11th March 2021, Tracking ID : VP-NAP-202103111 / VC-NAP-202103111
    @Transactional
    public static boolean CloseWallet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Loading Wallet for CloseWallet Transaction....");

//            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long DepositAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
//            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
//            Long AmtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            wsmodel.setAmounttransaction(account.getActualBalance());
            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            if (AcctActBalance > 0 && AcctAvailBalance > 0) {
                Long DepositAmount = AcctActBalance;
                Long finalAmount = DepositAmount;

//                if (DepositAmount < finalAmount || DepositAmount == 0L) {
//                    logger.error("Insufficient Amount for " + wsmodel.getServicename() + " Transaction, rejecting...");
//                    wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
//                    return false;
//                }

                //m.rehman: 27-07-2020: ignoring trasaction with calculated amount less than or equal to zero
//            if (finalAmount < 0) {
//                logger.error("Calculated amount [" + finalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
//                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
//                return false;
//            }

                logger.info("Updating Wallet Balance ...");
                account.setActualBalance((AcctActBalance - finalAmount) + "");
                account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log ...");
                WalletBalanceLog balanceLog = new WalletBalanceLog();
                balanceLog.setWallet(account);
                balanceLog.setChannelid(wsmodel.getChannelid());
                balanceLog.setAmount(finalAmount + "");
                balanceLog.setOriginalbalance(AcctAvailBalance + "");
                balanceLog.setUpdatedbalance(account.getAvailableBalance());
                balanceLog.setTxnname(wsmodel.getServicename());
                balanceLog.setTransaction(wsmodel);
                balanceLog.setTxnnature(TxnFlag.DEBIT);
                balanceLog.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLog);

                //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
                wsmodel.setOpeningbalance(AcctAvailBalance + "");
                wsmodel.setClosingbalance(account.getAvailableBalance());
                /////////////////////////////////////////////////////////////////////////////////////////////////////

                logger.info("Updating Wallet General Ledger ...");
                WalletGeneralLedger WGL = new WalletGeneralLedger();
                WGL.setTxnname(wsmodel.getServicename());
                WGL.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
                WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGL.setTxnflag(TxnFlag.DEBIT);
                WGL.setWallet(account);
                WGL.setAmount(finalAmount + "");
                WGL.setCurrency(account.getCurrency());
                WGL.setWalletflag(true);
                WGL.setMerchantid(wsmodel.getMerchantid());
                WGL.setAgentid(wsmodel.getAgentid());
                WGL.setBillerid(wsmodel.getBillerid());
                WGL.setTransaction(wsmodel);
                WGL.setPreviousBalance(AcctActBalance + "");
                WGL.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGL);

                //UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_IN, AccType.CAT_SETT_ACCT);
                // Asim Shahzad, Date : 25th Sep 2020, Tracking ID : VP-NAP-202009081/VC-NAP-202009081
                //UpdateandLog1LinkAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true, false);
                // ===================================================================================

                logger.error("Updating suspense account balances and logging...");
                FinanceManager.UpdateandLogSuspenseAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, false);

//            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
//            {
//                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
//            }
//
//            if(Tax > 0)
//            {
//                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
//
//            }

                //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
                //adding below check to handle database exception
                boolean isCommitted;
                try {
                    //GeneralDao.Instance.endTransaction();
                    GeneralDao.Instance.commit();
                    isCommitted = true;
                } catch (HibernateException e) {
                    logger.error("Error occurred while committing transaction, rejecting ...");
                    GeneralDao.Instance.rollback();
                    isCommitted = false;
                }
                finally {
                    GeneralDao.Instance.close();
                }
                GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
                if (isCommitted) {
                    logger.info("CloseWallet completed in Wallet successfully!");
                    return true;
                } else {
                    wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                    return false;
                }
                //logger.info("CloseWallet completed in Wallet successfully!");
                //return true;
                //////////////////////////////////////////////////////////////////////////////////////////////////////
            }
            else {
                logger.info("Wallet balance already Zero for " + wsmodel.getServicename() + " Transaction, rejecting...");
                logger.info("CloseWallet completed in Wallet successfully!");
                return true;
            }
        }
        catch (Exception e)
        {
            logger.error("Exception caught while executing CloseWallet transaction in wallet...");
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    // =======================================================================================================

    //m.rehman: 05-04-2021, VP-NAP-202103292 / VC-NAP-202103293 - Refund Module Part 2
    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new p+arameter in function signature for current transaction
    @Transactional
    public static boolean CompleteRefundWalletTransaction(WalletCMSWsEntity wsmodel, CMSAccount srcaccount, CMSAccount destaccount,
                                                          WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Performing Complete Refund Wallet-To-Wallet Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long SrcAcctActBalance = (Util.hasText(srcaccount.getActualBalance()) ? Long.parseLong(srcaccount.getActualBalance()) : 0L);
            Long SrcAcctAvailBalance = (Util.hasText(srcaccount.getAvailableBalance()) ? Long.parseLong(srcaccount.getAvailableBalance()) : 0L);
            Long DestAcctActBalance = (Util.hasText(destaccount.getActualBalance()) ? Long.parseLong(destaccount.getActualBalance()) : 0L);
            Long DestAcctAvailBalance = (Util.hasText(destaccount.getAvailableBalance()) ? Long.parseLong(destaccount.getAvailableBalance()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long sourceFinalAmount = TxnAmount + SrcChargeAmount;
            Long destinationFinalAmount = TxnAmount - DestChargeAmount;
            Long sourceUpdatedBalance =  SrcAcctActBalance - sourceFinalAmount;
            Long destinationUpdatedBalance = DestAcctActBalance + destinationFinalAmount;

            //m.rehman: 27-07-2020: ignoring transaction with calculated amount less than or equal to zero
            if (destinationFinalAmount < 0) {
                logger.error("Calculated destination amount [" + destinationFinalAmount.toString() + "] is less than zero, ignoring transaction with ref # [" + wsmodel.getTranrefnumber() + "]");
                wsmodel.setRespcode(ISOResponseCodes.NP_INVALID_OPERATION);
                return false;
            }

            logger.info("Updating Destination Wallet Balance ...");
            destaccount.setActualBalance((destinationUpdatedBalance) + "");
            destaccount.setAvailableBalance((destinationUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(destaccount);

            logger.info("Updating Destination Wallet Balance Log ...");
            WalletBalanceLog destbalanceLog = new WalletBalanceLog();
            destbalanceLog.setWallet(destaccount);
            destbalanceLog.setChannelid(wsmodel.getChannelid());
            destbalanceLog.setAmount(destinationFinalAmount + "");
            destbalanceLog.setOriginalbalance(DestAcctAvailBalance+"");
            destbalanceLog.setUpdatedbalance(destaccount.getAvailableBalance());
            destbalanceLog.setTxnname(wsmodel.getServicename());
            destbalanceLog.setTransaction(wsmodel);
            destbalanceLog.setTxnnature(TxnFlag.CREDIT);
            destbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(destbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setDestOpeningbalance(DestAcctAvailBalance + "");
            wsEntity.setDestClosingbalance(destaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Destination Wallet General Ledger ...");
            WalletGeneralLedger WGL4 = new WalletGeneralLedger();
            WGL4.setTxnname(wsmodel.getServicename());
            WGL4.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL4.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL4.setTxnflag(TxnFlag.CREDIT);
            WGL4.setWallet(destaccount);
            WGL4.setAmount((destinationFinalAmount) + "");
            WGL4.setCurrency(destaccount.getCurrency());
            WGL4.setWalletflag(true);
            WGL4.setClosingBalance(destaccount.getAvailableBalance());
            WGL4.setPreviousBalance(DestAcctAvailBalance+"");
            WGL4.setMerchantid(wsmodel.getMerchantid());
            WGL4.setAgentid(wsmodel.getAgentid());
            WGL4.setBillerid(wsmodel.getBillerid());
            WGL4.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL4);

            UpdateandLogDisputeSettlementReceivableAccount(wsmodel, destaccount.getCurrency(), TxnFlag.DEBIT, true, false, false);

            if(SrcAcctAvailBalance < sourceFinalAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_ERROR_29); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Source Wallet Balance ...");
            srcaccount.setActualBalance((sourceUpdatedBalance) + "");
            srcaccount.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(srcaccount);

            logger.info("Updating Source Wallet Balance Log ...");
            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(srcaccount);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(sourceFinalAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(srcaccount.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.DEBIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(SrcAcctActBalance + "");
            wsEntity.setClosingbalance(srcaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Source Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(srcaccount);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(srcaccount.getCurrency());
            WGL.setWalletflag(true);
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setClosingBalance((SrcAcctAvailBalance - TxnAmount) + "");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Source Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(srcaccount);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(srcaccount.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setPreviousBalance((SrcAcctAvailBalance - TxnAmount) +"");
                WGL2.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL2);

                //UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
                UpdateandLogRevenueAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Source Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(srcaccount);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(srcaccount.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setPreviousBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount)) + "");
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL3);

                UpdateandLogTaxAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            UpdateandLogDisputeSettlementReceivableAccount(wsmodel, destaccount.getCurrency(), TxnFlag.CREDIT, true, false, false);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Refund Wallet-To-Wallet Transaction completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            //logger.info("Refund Wallet-To-Wallet Transaction completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Refund Wallet-To-Wallet Transaction...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean PartialRefundWalletTransaction(WalletCMSWsEntity wsmodel, CMSAccount srcaccount, CMSAccount destaccount,
                                                         WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Performing Partial Refund Wallet-To-Wallet Transaction....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long SrcAcctActBalance = (Util.hasText(srcaccount.getActualBalance()) ? Long.parseLong(srcaccount.getActualBalance()) : 0L);
            Long SrcAcctAvailBalance = (Util.hasText(srcaccount.getAvailableBalance()) ? Long.parseLong(srcaccount.getAvailableBalance()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            //Long FinalAmount = null;
            //Long ClosingBalance = null;

            Long sourceFinalAmount = TxnAmount + SrcChargeAmount;
            Long sourceUpdatedBalance =  SrcAcctActBalance - sourceFinalAmount;

            if(SrcAcctAvailBalance < sourceFinalAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_ERROR_29); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Source Wallet Balance ...");
            srcaccount.setActualBalance((sourceUpdatedBalance) + "");
            srcaccount.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(srcaccount);

            logger.info("Updating Source Wallet Balance Log ...");
            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(srcaccount);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(sourceFinalAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(srcaccount.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.DEBIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsEntity.setOpeningbalance(SrcAcctAvailBalance + "");
            wsEntity.setClosingbalance(srcaccount.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Source Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(srcaccount);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(srcaccount.getCurrency());
            WGL.setWalletflag(true);
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setClosingBalance((SrcAcctAvailBalance - TxnAmount) + "");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            if(SrcChargeAmount > 0) //TODO: Raza use with above Check
            {
                logger.info("Updating Source Wallet General Ledger for Charges ...");
                WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                WGL2.setTxnname(wsmodel.getServicename());
                WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL2.setTxnflag(TxnFlag.DEBIT);
                WGL2.setWallet(srcaccount);
                WGL2.setAmount((SrcChargeAmount - Tax) + "");
                WGL2.setCurrency(srcaccount.getCurrency());
                WGL2.setWalletflag(true);
                WGL2.setPreviousBalance((SrcAcctAvailBalance - TxnAmount) +"");
                WGL2.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL2.setMerchantid(wsmodel.getMerchantid());
                WGL2.setAgentid(wsmodel.getAgentid());
                WGL2.setBillerid(wsmodel.getBillerid());
                WGL2.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL2);

                //UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
                UpdateandLogRevenueAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            if(Tax > 0)
            {
                logger.info("Updating Source Wallet General Ledger for Tax ...");
                WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                WGL3.setTxnname(wsmodel.getServicename());
                WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL3.setTxnflag(TxnFlag.DEBIT);
                WGL3.setWallet(srcaccount);
                WGL3.setAmount(Tax + "");
                WGL3.setCurrency(srcaccount.getCurrency());
                WGL3.setWalletflag(true);
                WGL3.setPreviousBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount - Tax)) + "");
                WGL3.setClosingBalance((SrcAcctAvailBalance - (TxnAmount + SrcChargeAmount)) + "");
                WGL3.setMerchantid(wsmodel.getMerchantid());
                WGL3.setAgentid(wsmodel.getAgentid());
                WGL3.setBillerid(wsmodel.getBillerid());
                WGL3.setTransaction(wsmodel);
                GeneralDao.Instance.saveOrUpdate(WGL3);

                UpdateandLogTaxAccount(wsmodel, srcaccount.getCurrency(), TxnFlag.CREDIT);
            }

            UpdateandLogDisputeSettlementReceivableAccount(wsmodel, destaccount.getCurrency(), TxnFlag.CREDIT, true, false, false);

            //wsmodel.setTranauthid(Util.generateTrnAuthId()); //TODO: Raza use sequence
            logger.info("Refund Wallet-To-Wallet Transaction completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Refund Wallet-To-Wallet Transaction...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
    //adding new parameter in function signature for current transaction
    @Transactional
    public static boolean RefundSettledMerchantTransaction(WalletCMSWsEntity wsmodel, CMSAccount account, WalletCMSWsEntity wsEntity)
    {
        try {
            logger.info("Refunding Wallet for " + wsmodel.getOriginalapi() + "....");

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long DestAcctActBalance = Long.parseLong(account.getActualBalance());
            Long DestAcctAvailBalance = Long.parseLong(account.getAvailableBalance());
            Long TxnAmount = Long.parseLong(wsmodel.getAmounttransaction());
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long totalAmount = TxnAmount + SrcChargeAmount;

            //Getting merchant payable account
            CMSBusinessWallet settlementaccount = FinanceManager.GetBusinessWallet(account.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_WALLET.StringValue());
            CMSEMIAccountCollection settlementCollAccount = FinanceManager.GetEMICollectionAccount(account.getCurrency(), AccType.CAT_MERCHANT_SETTLEMENT_ACCT);

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;
            Long CollAcctActBalance = (Util.hasText(settlementCollAccount.getActualBalance()) ? Long.parseLong(settlementCollAccount.getActualBalance()) : 0L);
            Long CollAcctAvailBalance = (Util.hasText(settlementCollAccount.getAvailableBalance()) ? Long.parseLong(settlementCollAccount.getAvailableBalance()) : 0L);

            if (Util.hasText(wsmodel.getMerchantfavorflag()) && wsmodel.getMerchantfavorflag().equals("true")) {

                //checking balances
                if (totalAmount > AcctAvailBalance || totalAmount > CollAcctAvailBalance) {
                    logger.info("Merchant Settlement Wallet/Account has Low Balance, rejecting ...");
                    wsmodel.setRespcode(ISOResponseCodes.LOW_BALANCE);
                    return false;
                }

                UpdateandLogMerchantSettlementWallet(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
                UpdateandLogDisputeSettlementPayableAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, true, false, true);

            } else {

                logger.info("Updating Wallet Balance ...");
                account.setActualBalance((DestAcctActBalance + (TxnAmount + SrcChargeAmount)) + "");
                account.setAvailableBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log ...");
                WalletBalanceLog balanceLog = new WalletBalanceLog();
                balanceLog.setWallet(account);
                balanceLog.setChannelid(wsmodel.getChannelid());
                balanceLog.setAmount((TxnAmount + SrcChargeAmount) + "");
                balanceLog.setOriginalbalance(DestAcctAvailBalance+"");
                balanceLog.setUpdatedbalance(account.getAvailableBalance());
                balanceLog.setTxnname(wsmodel.getServicename());
                balanceLog.setTransaction(wsmodel);
                balanceLog.setTxnnature(TxnFlag.CREDIT);
                balanceLog.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLog);

                //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
                wsEntity.setOpeningbalance(DestAcctAvailBalance + "");
                wsEntity.setClosingbalance(account.getAvailableBalance());
                /////////////////////////////////////////////////////////////////////////////////////////////////////

                logger.info("Updating Wallet General Ledger ...");
                WalletGeneralLedger WGL = new WalletGeneralLedger();
                WGL.setTxnname(wsmodel.getServicename());
                WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGL.setTxnrefnum(wsmodel.getTranrefnumber());
                WGL.setTxnflag(TxnFlag.CREDIT);
                WGL.setWallet(account);
                WGL.setAmount(TxnAmount + "");
                WGL.setCurrency(account.getCurrency());
                WGL.setWalletflag(true);
                WGL.setMerchantid(wsmodel.getMerchantid());
                WGL.setAgentid(wsmodel.getAgentid());
                WGL.setBillerid(wsmodel.getBillerid());
                WGL.setTransaction(wsmodel);
                WGL.setPreviousBalance(DestAcctAvailBalance +"");
                WGL.setClosingBalance((DestAcctAvailBalance + TxnAmount) + "");
                GeneralDao.Instance.save(WGL);

                if(SrcChargeAmount > 0) //TODO: Raza use with above Check
                {
                    logger.info("Updating Wallet General Ledger for Charges ...");
                    WalletGeneralLedger WGL2 = new WalletGeneralLedger();
                    WGL2.setTxnname(wsmodel.getServicename());
                    WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                    WGL2.setTxnrefnum(wsmodel.getTranrefnumber());
                    WGL2.setTxnflag(TxnFlag.CREDIT);
                    WGL2.setWallet(account);
                    WGL2.setAmount((SrcChargeAmount - Tax) + "");
                    WGL2.setCurrency(account.getCurrency());
                    WGL2.setWalletflag(true);
                    WGL2.setMerchantid(wsmodel.getMerchantid());
                    WGL2.setAgentid(wsmodel.getAgentid());
                    WGL2.setBillerid(wsmodel.getBillerid());
                    WGL2.setTransaction(wsmodel);
                    WGL2.setPreviousBalance((DestAcctAvailBalance + TxnAmount)+"");
                    WGL2.setClosingBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount - Tax)) + "");
                    GeneralDao.Instance.save(WGL2);

                    UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
                }
                //wsmodel.setNayapaytaxamount(tax);

                if(Tax > 0)
                {
                    logger.info("Updating Wallet General Ledger for Tax ...");
                    WalletGeneralLedger WGL3 = new WalletGeneralLedger();
                    WGL3.setTxnname(wsmodel.getServicename());
                    WGL3.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                    WGL3.setTxnrefnum(wsmodel.getTranrefnumber());
                    WGL3.setTxnflag(TxnFlag.CREDIT);
                    WGL3.setWallet(account);
                    WGL3.setAmount(Tax + "");
                    WGL3.setCurrency(account.getCurrency());
                    WGL3.setWalletflag(true);
                    WGL3.setMerchantid(wsmodel.getMerchantid());
                    WGL3.setAgentid(wsmodel.getAgentid());
                    WGL3.setBillerid(wsmodel.getBillerid());
                    WGL3.setTransaction(wsmodel);
                    WGL3.setPreviousBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount - Tax)) + "");
                    WGL3.setClosingBalance((DestAcctAvailBalance + (TxnAmount + SrcChargeAmount)) + "");
                    GeneralDao.Instance.save(WGL3);

                    UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT);
                }

                UpdateandLogDisputeSettlementPayableAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, true, false, true);
            }

            logger.info("Refund " + wsmodel.getOriginalapi() + " completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Refund " + wsmodel.getOriginalapi() + "...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static void UpdateandLogDisputeSettlementReceivableAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                                      boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {

        try {
            CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_DISPUTE_SETT_RECEIVABLE_ACCT);
            CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_DISPUTE_SETT_RECEIVABLE_WLLT);

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L);

            Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
            Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L);

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            //Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            Long TxnAmount;
            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                TxnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmountCollectionAccount;
            if (isChargesApply) {
                if (isApplyOnlyCharges) {
                    AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    if (isChargesAdd) {
                        AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                    }
                }
            } else {
                AmountCollectionAccount = TxnAmount;
            }

            if (txnFlag.equals(TxnFlag.DEBIT)) {
                AcctActBalance = AcctActBalance + AmountCollectionAccount;
                WlltActBalance = WlltActBalance + AmountCollectionAccount;
            } else //Credit
            {
                AcctActBalance = AcctActBalance - AmountCollectionAccount;
                WlltActBalance = WlltActBalance - AmountCollectionAccount;
            }

            logger.info("Updating Dispute Settlement Collection Account Balance ...");
            settlementaccount.setActualBalance(AcctActBalance + "");
            settlementaccount.setAvailableBalance(AcctActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementaccount);

            logger.info("Updating Dispute Settlement Wallet Balance ...");
            settlementWallet.setActualBalance(WlltActBalance + "");
            settlementWallet.setAvailableBalance(WlltActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementWallet);

            logger.info("Updating Dispute Settlement Account Balance Log ...");
            EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
            if (txnFlag.equals(TxnFlag.DEBIT)) {
                balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
                balanceLog.setDebitAmount(AmountCollectionAccount + "");
            } else {
                balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
                balanceLog.setCreditAmount(AmountCollectionAccount + "");
            }
            balanceLog.setTxnId(wsmodel.getTranrefnumber());
            balanceLog.setBankId(settlementaccount.getBankCode());
            balanceLog.setAccountNature(settlementaccount.getAccountType());
            balanceLog.setTranDate(wsmodel.getTransdatetime());
//            balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
            balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
            balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Dispute Settlement Wallet Balance Log ...");
            EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
            wlltBalanceLog.setEmiwallet(settlementWallet);
            wlltBalanceLog.setAmount(AmountCollectionAccount + "");
            wlltBalanceLog.setChannelid(wsmodel.getChannelid());
            wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
            wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
            wlltBalanceLog.setTransaction(wsmodel);
            wlltBalanceLog.setTxnname(wsmodel.getServicename());
            wlltBalanceLog.setTxnnature(txnFlag);
            wlltBalanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
            GeneralDao.Instance.getNextValEmiCollBalLog();

            GeneralDao.Instance.save(wlltBalanceLog);

            logger.info("Updating Dispute Settlement Wallet General Ledger ...");
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(txnFlag);
            WGL2.setEmiaccount(settlementWallet);
            WGL2.setAmount(AmountCollectionAccount + "");
            WGL2.setCurrency(settlementWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setPreviousBalance(WlltAvailBalance + "");
            WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
            GeneralDao.Instance.save(WGL2);

        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Performing PreAuthorization Sundry in wallet...");
        }
    }

    @Transactional
    public static void UpdateandLogDisputeSettlementPayableAccount(WalletCMSWsEntity wsmodel, String currency, String txnFlag,
                                                                      boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd)
    {

        try {
            CMSEMIAccountCollection settlementaccount = GetEMICollectionAccount(currency, AccType.CAT_DISPUTE_SETT_PAYABLE_ACCT);
            CMSEMIWallet settlementWallet = GetEMIWallet(currency, AccType.CAT_DISPUTE_SETT_PAYABLE_WLLT);

            Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
            Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L);

            Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
            Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L);

            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
            Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
            Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            //Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

            Long TxnAmount;
            //in case of transaction in other currency
            if (Util.hasText(wsmodel.getCbillamount())) {
                logger.info("Using Cardholder Billing Amount [" + wsmodel.getCbillamount() + "]");
                TxnAmount = (Util.hasText(wsmodel.getCbillamount()) ? Long.parseLong(wsmodel.getCbillamount()) : 0L);
            } else {
                TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            }

            Long AmountCollectionAccount;
            if (isChargesApply) {
                if (isApplyOnlyCharges) {
                    AmountCollectionAccount = NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                } else {
                    if (isChargesAdd) {
                        AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee;
                    } else {
                        AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + BankTaxAmount + (SrcChargeAmount - Tax) + TranFee);
                    }
                }
            } else {
                AmountCollectionAccount = TxnAmount;
            }

            if (txnFlag.equals(TxnFlag.DEBIT)) {
                AcctActBalance = AcctActBalance - AmountCollectionAccount;
                WlltActBalance = WlltActBalance - AmountCollectionAccount;
            } else //Credit
            {
                AcctActBalance = AcctActBalance + AmountCollectionAccount;
                WlltActBalance = WlltActBalance + AmountCollectionAccount;
            }

            logger.info("Updating Dispute Settlement Collection Account Balance ...");
            settlementaccount.setActualBalance(AcctActBalance + "");
            settlementaccount.setAvailableBalance(AcctActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementaccount);

            logger.info("Updating Dispute Settlement Wallet Balance ...");
            settlementWallet.setActualBalance(WlltActBalance + "");
            settlementWallet.setAvailableBalance(WlltActBalance + "");
            GeneralDao.Instance.saveOrUpdate(settlementWallet);

            logger.info("Updating Dispute Settlement Account Balance Log ...");
            EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
            if (txnFlag.equals(TxnFlag.DEBIT)) {
                balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
                balanceLog.setDebitAmount(AmountCollectionAccount + "");
            } else {
                balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
                balanceLog.setCreditAmount(AmountCollectionAccount + "");
            }
            balanceLog.setTxnId(wsmodel.getTranrefnumber());
            balanceLog.setBankId(settlementaccount.getBankCode());
            balanceLog.setAccountNature(settlementaccount.getAccountType());
            balanceLog.setTranDate(wsmodel.getTransdatetime());
//            balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
            balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
            balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Dispute Settlement Wallet Balance Log ...");
            EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
            wlltBalanceLog.setEmiwallet(settlementWallet);
            wlltBalanceLog.setAmount(AmountCollectionAccount + "");
            wlltBalanceLog.setChannelid(wsmodel.getChannelid());
            wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
            wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
            wlltBalanceLog.setTransaction(wsmodel);
            wlltBalanceLog.setTxnname(wsmodel.getServicename());
            wlltBalanceLog.setTxnnature(txnFlag);
            wlltBalanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
            GeneralDao.Instance.getNextValEmiCollBalLog();

            GeneralDao.Instance.save(wlltBalanceLog);

            logger.info("Updating Dispute Settlement Wallet General Ledger ...");
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(txnFlag);
            WGL2.setEmiaccount(settlementWallet);
            WGL2.setAmount(AmountCollectionAccount + "");
            WGL2.setCurrency(settlementWallet.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            WGL2.setPreviousBalance(WlltAvailBalance + "");
            WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
            GeneralDao.Instance.save(WGL2);

        } catch (Exception e) {
            logger.error(WebServiceUtil.getStrException(e));
            logger.error("Exception caught while Performing PreAuthorization Sundry in wallet...");
        }
    }

    @Transactional
    public static boolean DebitWallet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Performing Debit Wallet ....");

            Long SrcAcctActBalance = (Util.hasText(account.getActualBalance()) ? Long.parseLong(account.getActualBalance()) : 0L);
            Long SrcAcctAvailBalance = (Util.hasText(account.getAvailableBalance()) ? Long.parseLong(account.getAvailableBalance()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long sourceUpdatedBalance =  SrcAcctActBalance - TxnAmount;

            if(SrcAcctAvailBalance < TxnAmount)
            {
                logger.error("Insufficient Funds in Wallet to Perform Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                return false;
            }

            logger.info("Updating Debit Wallet Balance ...");
            account.setActualBalance((sourceUpdatedBalance) + "");
            account.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Debit Wallet Balance Log ...");
            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(account);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(TxnAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(account.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.DEBIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(SrcAcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Debit Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setClosingBalance((SrcAcctAvailBalance - TxnAmount) + "");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Debit Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Debit Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Debit Wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CreditWallet(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("Performing Credit Wallet ....");

            Long SrcAcctActBalance = (Util.hasText(account.getActualBalance()) ? Long.parseLong(account.getActualBalance()) : 0L);
            Long SrcAcctAvailBalance = (Util.hasText(account.getAvailableBalance()) ? Long.parseLong(account.getAvailableBalance()) : 0L);
            Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long sourceUpdatedBalance =  SrcAcctActBalance + TxnAmount;

            logger.info("Updating Credit Wallet Balance ...");
            account.setActualBalance((sourceUpdatedBalance) + "");
            account.setAvailableBalance((sourceUpdatedBalance) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Credit Wallet Balance Log ...");
            WalletBalanceLog srcbalanceLog = new WalletBalanceLog();
            srcbalanceLog.setWallet(account);
            srcbalanceLog.setChannelid(wsmodel.getChannelid());
            srcbalanceLog.setAmount(TxnAmount+"");
            srcbalanceLog.setOriginalbalance(SrcAcctAvailBalance+"");
            srcbalanceLog.setUpdatedbalance(account.getAvailableBalance());
            srcbalanceLog.setTxnname(wsmodel.getServicename());
            srcbalanceLog.setTransaction(wsmodel);
            srcbalanceLog.setTxnnature(TxnFlag.CREDIT);
            srcbalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(srcbalanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(SrcAcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Credit Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(TxnFlag.CREDIT);
            WGL.setWallet(account);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setPreviousBalance(SrcAcctAvailBalance+"");
            WGL.setClosingBalance((SrcAcctAvailBalance + TxnAmount) + "");
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL);

            //m.rehman: 26-07-2022, VC-NAP-202207261 - Transaction rollback on DB Exception
            //adding below check to handle database exception
            boolean isCommitted;
            try {
                //GeneralDao.Instance.endTransaction();
                GeneralDao.Instance.commit();
                isCommitted = true;
            } catch (HibernateException e) {
                logger.error("Error occurred while committing transaction, rejecting ...");
                GeneralDao.Instance.rollback();
                isCommitted = false;
            }
            finally {
                GeneralDao.Instance.close();
            }
            GeneralDao.Instance.beginTransaction(GeneralDao.OPTIMIZER_MODE_FIRST_ROWS);
            if (isCommitted) {
                logger.info("Credit Wallet completed successfully!");
                return true;
            } else {
                wsmodel.setRespcode(ISOResponseCodes.INTERNAL_DATABASE_ERROR);
                return false;
            }
            //logger.info("Credit Wallet completed successfully!");
            //return true;
            //////////////////////////////////////////////////////////////////////////////////////////////////////
        }
        catch (Exception e)
        {
            logger.error("Exception caught while performing Credit Wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static void updateAndLogEMICollectionAccount(WalletCMSWsEntity wsmodel, CMSEMIAccountCollection account, String txnFlag)
    {
        CMSEMIWallet emiwalllet = null;
        CMSBusinessWallet businessWallet = null;
        Long WlltActBalance = 0L, WlltAvailBalance = 0L;
        Long AcctActBalance = (Util.hasText(account.getActualBalance()) ? Long.parseLong(account.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(account.getAvailableBalance()) ? Long.parseLong(account.getAvailableBalance()) : 0L) ;
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);

        // Asim Shahzad, Date : 4th Aug 2021, Tracking ID : VP-NAP-202103292 / VC-NAP-202103293
        String dbQuery = "";
        Map<String, Object> params = new HashMap<>();
        ChartOfAccount chartOfAccount = null;

        dbQuery = "from " + ChartOfAccount.class.getName() + " f where f.linkAcc= :LINKACC ";
        params.put("LINKACC", account);

        chartOfAccount = (ChartOfAccount) GeneralDao.Instance.findObject(dbQuery, params);

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            if(null != chartOfAccount && (chartOfAccount.getAccountCode().substring(0, 1).equals("1") || chartOfAccount.getAccountCode().substring(0, 1).equals("5"))) {
                AcctActBalance = AcctActBalance + TxnAmount;
            }
            else {
                AcctActBalance = AcctActBalance - TxnAmount;
            }
        }
        else //Credit
        {
            if(null != chartOfAccount && (chartOfAccount.getAccountCode().substring(0, 1).equals("1") || chartOfAccount.getAccountCode().substring(0, 1).equals("5"))) {
                AcctActBalance = AcctActBalance - TxnAmount;
            }
            else {
                AcctActBalance = AcctActBalance + TxnAmount;
            }
        }
        // =====================================================================================

        logger.info("Updating Collection Account Balance ...");
        account.setActualBalance(AcctActBalance + "");
        account.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(account);

        logger.info("Updating Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(account.getAccountNumber());
            balanceLog.setDebitAmount(StringUtils.leftPad(TxnAmount.toString(),12,"0")); // Asim Shahzad, Date : 15th Jul 2021, Tracking ID : VP-NAP-202103292 / VC-NAP-202103293
        } else {
            balanceLog.setCreditAccount(account.getAccountNumber());
            balanceLog.setCreditAmount(StringUtils.leftPad(TxnAmount.toString(),12,"0")); // Asim Shahzad, Date : 15th Jul 2021, Tracking ID : VP-NAP-202103292 / VC-NAP-202103293
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(account.getBankCode());
        balanceLog.setAccountNature(account.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
        balanceLog.setClosingBalance(account.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        if (account.getEmiwallet() != null) {
            emiwalllet = account.getEmiwallet();
            WlltActBalance = (Util.hasText(emiwalllet.getActualBalance()) ? Long.parseLong(emiwalllet.getActualBalance()) : 0L);
            WlltAvailBalance = (Util.hasText(emiwalllet.getActualBalance()) ? Long.parseLong(emiwalllet.getAvailableBalance()) : 0L);

            // Asim Shahzad, Date : 4th Aug 2021, Tracking ID : VP-NAP-202103292 / VC-NAP-202103293
            if(txnFlag.equals(TxnFlag.DEBIT))
            {
                if(null != chartOfAccount && (chartOfAccount.getAccountCode().substring(0, 1).equals("1") || chartOfAccount.getAccountCode().substring(0, 1).equals("5"))) {
                    WlltActBalance = WlltActBalance + TxnAmount;
                }
                else {
                    WlltActBalance = WlltActBalance - TxnAmount;
                }
            }
            else //Credit
            {
                if(null != chartOfAccount && (chartOfAccount.getAccountCode().substring(0, 1).equals("1") || chartOfAccount.getAccountCode().substring(0, 1).equals("5"))) {
                    WlltActBalance = WlltActBalance - TxnAmount;
                }
                else {
                    WlltActBalance = WlltActBalance + TxnAmount;
                }
            }
            // =====================================================================================

            logger.info("Updating EMI Wallet Balance ...");
            emiwalllet.setActualBalance(WlltActBalance + "");
            emiwalllet.setAvailableBalance(WlltActBalance + "");
            GeneralDao.Instance.saveOrUpdate(emiwalllet);

            logger.info("Updating EMI Wallet Balance Log ...");
            EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
            wlltBalanceLog.setEmiwallet(emiwalllet);
            wlltBalanceLog.setAmount(TxnAmount + "");
            wlltBalanceLog.setChannelid(wsmodel.getChannelid());
            wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
            wlltBalanceLog.setUpdatedbalance(emiwalllet.getAvailableBalance());
            wlltBalanceLog.setTransaction(wsmodel);
            wlltBalanceLog.setTxnname(wsmodel.getServicename());
            wlltBalanceLog.setTxnnature(txnFlag);
            wlltBalanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
            GeneralDao.Instance.getNextValEmiCollBalLog();

            GeneralDao.Instance.save(wlltBalanceLog); //Bilal Hussain : against tracking id: VP-NAP-202103292 / VC-NAP-202103293 on 14-june-2021

            logger.info("Updating EMI Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(txnFlag);
            WGL.setEmiaccount(emiwalllet);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(emiwalllet.getCurrency());
            WGL.setWalletflag(false);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(WlltAvailBalance + "");
            WGL.setClosingBalance(emiwalllet.getAvailableBalance());
            GeneralDao.Instance.save(WGL);

        } else {
            businessWallet = account.getBusinesswallet();
            WlltActBalance = (Util.hasText(businessWallet.getActualBalance()) ? Long.parseLong(businessWallet.getActualBalance()) : 0L);
            WlltAvailBalance = (Util.hasText(businessWallet.getActualBalance()) ? Long.parseLong(businessWallet.getAvailableBalance()) : 0L);

            // Asim Shahzad, Date : 4th Aug 2021, Tracking ID : VP-NAP-202103292 / VC-NAP-202103293
            if(txnFlag.equals(TxnFlag.DEBIT))
            {
                if(null != chartOfAccount && (chartOfAccount.getAccountCode().substring(0, 1).equals("1") || chartOfAccount.getAccountCode().substring(0, 1).equals("5"))) {
                    WlltActBalance = WlltActBalance + TxnAmount;
                }
                else {
                    WlltActBalance = WlltActBalance - TxnAmount;
                }
            }
            else //Credit
            {
                if(null != chartOfAccount && (chartOfAccount.getAccountCode().substring(0, 1).equals("1") || chartOfAccount.getAccountCode().substring(0, 1).equals("5"))) {
                    WlltActBalance = WlltActBalance - TxnAmount;
                }
                else {
                    WlltActBalance = WlltActBalance + TxnAmount;
                }
            }
            // ====================================================================================

            logger.info("Updating Business Wallet Balance ...");
            businessWallet.setActualBalance(WlltActBalance + "");
            businessWallet.setAvailableBalance(WlltActBalance + "");
            GeneralDao.Instance.saveOrUpdate(businessWallet);

            logger.info("Updating Business Wallet Balance Log ...");
            BusinessWalletBalanceLog businessWalletBalanceLog = new BusinessWalletBalanceLog();
            businessWalletBalanceLog.setBusinessWallet(businessWallet);
            businessWalletBalanceLog.setChannelid(wsmodel.getChannelid());
            businessWalletBalanceLog.setOriginalbalance(WlltAvailBalance+"");
            businessWalletBalanceLog.setUpdatedbalance(businessWallet.getAvailableBalance());
            businessWalletBalanceLog.setTxnname(wsmodel.getServicename());
            businessWalletBalanceLog.setAmount((TxnAmount) + "");
            businessWalletBalanceLog.setTransaction(wsmodel);
            businessWalletBalanceLog.setTxnnature(txnFlag);
            businessWalletBalanceLog.setCreatedate(new Date());
            GeneralDao.Instance.save(businessWalletBalanceLog);

            logger.info("Updating Business Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(wsmodel.getTranrefnumber());
            WGL.setTxnflag(txnFlag);
            WGL.setBusinessaccount(businessWallet);
            WGL.setAmount(TxnAmount + "");
            WGL.setCurrency(businessWallet.getCurrency());
            WGL.setWalletflag(false);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(WlltAvailBalance + "");
            WGL.setClosingBalance(businessWallet.getAvailableBalance());
            GeneralDao.Instance.save(WGL);
        }
    }
    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    //m.rehman: 10-12-2021 - VP-NAP-202111291 / VC-NAP-202111291 / VG-NAP-202111291 - Meezan ATM On-Us Withdrawal/Balance Inquiry and Reversal
    //Huzaifa: 11/08/2023: FW: NAP-P5-23 ==> [ Logging email ] ==> Segregation of ATM On Us Channels Bank - UBL & BAFL
    @Transactional
    public static boolean OnUsBalanceInquiry(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for OnUsBalanceInquiry Transaction....");

//            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
//            Double NayapayChargeTaxAmount = 0.0;
//            Long OnUsFee = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
//            Double OnUsFeeTaxAmount = 0.0;
            Long receiptCharges = (Util.hasText(wsmodel.getReceiptcharges()) ? Long.parseLong(wsmodel.getReceiptcharges()) : 0L);//ReceiptCharges
            Double receiptChargesTax = 0.0;
            Long balInqCharges = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Double balInqChargesTax = 0.0;

            Tax sstTax;
            String dbQuery = null;
            Map<String, Object> params = new HashMap<>();

            dbQuery = "from " + Tax.class.getName() + " t where title='SST'";
            params = new HashMap<>();
            sstTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);

            if (sstTax != null) {
                logger.info("Tax rate found, calculating amount ...");
//                if(NayapayChargeAmount > 0){
//                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
//                        NayapayChargeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0) * (NayapayChargeAmount / 100.0);
//                        logger.info("Update amount for Nayapay ChargeTax Amount [" + NayapayChargeTaxAmount + "]");
//                    } else {
//                        NayapayChargeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0);
//                        logger.info("Update amount for Nayapay ChargeTax Amount [" + NayapayChargeTaxAmount + "]");
//                    }
//
//                    DecimalFormat dft = new DecimalFormat("0.00");
//                    String updatedNPChargeAmtTaxAmountStr = dft.format(NayapayChargeTaxAmount);
//                    updatedNPChargeAmtTaxAmountStr = updatedNPChargeAmtTaxAmountStr.replace(".", "");
//                    logger.info("Final amount for Nayapay Charge Tax Amount [" + updatedNPChargeAmtTaxAmountStr + "]");
//                    wsmodel.setNayapaychargestax(StringUtils.leftPad(updatedNPChargeAmtTaxAmountStr, 12, "0"));
//                }
//                if(OnUsFee > 0){
//                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
//                        OnUsFeeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0) * (OnUsFee / 100.0);
//                        logger.info("Update amount for OnUs Charges Tax Amount [" + OnUsFeeTaxAmount + "]");
//                    } else {
//                        OnUsFeeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0);
//                        logger.info("Update amount for OnUs Charges Tax Amount [" + OnUsFeeTaxAmount + "]");
//                    }
//
//                    DecimalFormat dft = new DecimalFormat("0.00");
//                    String updatedBankTaxAmountStr = dft.format(OnUsFeeTaxAmount);
//                    updatedBankTaxAmountStr = updatedBankTaxAmountStr.replace(".", "");
//                    logger.info("Final amount for Nayapay OnUs Charges Tax Amount [" + updatedBankTaxAmountStr + "]");
//                    wsmodel.setOnuschargestax(StringUtils.leftPad(updatedBankTaxAmountStr, 12, "0"));
//                }
                if(receiptCharges > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        receiptChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0) * (receiptCharges / 100.0);
                        logger.info("Update amount for receiptChargesTax [" + receiptChargesTax + "]");
                    } else {
                        receiptChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for receiptChargesTax [" + receiptChargesTax + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedreceiptChargesTaxAmountStr = dft.format(receiptChargesTax);
                    updatedreceiptChargesTaxAmountStr = updatedreceiptChargesTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Receipt Charges Tax Amount [" + updatedreceiptChargesTaxAmountStr + "]");
                    wsmodel.setReceiptchargestax(StringUtils.leftPad(updatedreceiptChargesTaxAmountStr, 12, "0"));
                }
                if(balInqCharges > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        balInqChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0) * (balInqCharges / 100.0);
                        logger.info("Update amount for Balance Inquiry Charges Tax [" + balInqChargesTax + "]");
                    } else {
                        balInqChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for Balance Inquiry Charges  [" + balInqChargesTax + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedbalInqChargesTaxAmountStr = dft.format(balInqChargesTax);
                    updatedbalInqChargesTaxAmountStr = updatedbalInqChargesTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Receipt Charges Tax Amount [" + updatedbalInqChargesTaxAmountStr + "]");
                    wsmodel.setBalinqchargestax(StringUtils.leftPad(updatedbalInqChargesTaxAmountStr, 12, "0"));
                }
            }

            logger.info("Bal Inquiry charges: " + wsmodel.getBalinqchargestax());

            Long finalAmount = balInqCharges + receiptCharges +
//                    (Util.hasText(wsmodel.getNayapaychargestax()) ? Long.parseLong(wsmodel.getNayapaychargestax()) : 0L)+
//                    (Util.hasText(wsmodel.getOnuschargestax()) ? Long.parseLong(wsmodel.getOnuschargestax()) : 0L) +
                    (Util.hasText(wsmodel.getReceiptchargestax()) ? Long.parseLong(wsmodel.getReceiptchargestax()) : 0L)  +
                    (Util.hasText(wsmodel.getBalinqchargestax()) ? Long.parseLong(wsmodel.getBalinqchargestax()) : 0L) ;

            logger.info("Final Amount: " + finalAmount);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for On Us Balance Inquiry Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

//            if(NayapayChargeAmount > 0) {
//                logger.info("Updating Wallet Balance for Nayapay charges and tax...");
//
//                //Huzaifa refreshing balance = 12/01/2023
//                AcctActBalance = Long.parseLong(account.getActualBalance());
//                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
//                //=======================================
//
//                account.setActualBalance((AcctActBalance - NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax())) + "");
//                account.setAvailableBalance((AcctAvailBalance - NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax())) + "");
//                GeneralDao.Instance.saveOrUpdate(account);
//
//                logger.info("Updating Wallet Balance Log for Nayapay charges and tax...");
//                WalletBalanceLog balanceLogOnUsFee = new WalletBalanceLog();
//                balanceLogOnUsFee.setWallet(account);
//                balanceLogOnUsFee.setChannelid(wsmodel.getChannelid());
//                balanceLogOnUsFee.setAmount(NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax()) + "");
//                balanceLogOnUsFee.setOriginalbalance(AcctAvailBalance + "");
//                balanceLogOnUsFee.setUpdatedbalance(account.getAvailableBalance());
//                balanceLogOnUsFee.setTxnname(wsmodel.getServicename());
//                balanceLogOnUsFee.setTransaction(wsmodel);
//                balanceLogOnUsFee.setTxnnature(TxnFlag.DEBIT);
//                balanceLogOnUsFee.setCreatedate(new Date());
//                GeneralDao.Instance.save(balanceLogOnUsFee);
//
//                logger.info("Updating Wallet General Ledger for Nayapay charges and tax...");
//                WalletGeneralLedger WGLONUSFee = new WalletGeneralLedger();
//                WGLONUSFee.setTxnname(wsmodel.getServicename());
//                WGLONUSFee.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
//                WGLONUSFee.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
//                WGLONUSFee.setTxnflag(TxnFlag.DEBIT);
//                WGLONUSFee.setWallet(account);
//                WGLONUSFee.setAmount(NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax()) + "");
//                WGLONUSFee.setCurrency(account.getCurrency());
//                WGLONUSFee.setWalletflag(true);
//                WGLONUSFee.setMerchantid(wsmodel.getMerchantid());
//                WGLONUSFee.setAgentid(wsmodel.getAgentid());
//                WGLONUSFee.setBillerid(wsmodel.getBillerid());
//                WGLONUSFee.setTransaction(wsmodel);
//                WGLONUSFee.setPreviousBalance(AcctAvailBalance + "");
//                WGLONUSFee.setClosingBalance(account.getAvailableBalance());
//                GeneralDao.Instance.saveOrUpdate(WGLONUSFee);
//
//                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
//            }
//
//            if(OnUsFee > 0)
//            {
//                logger.info("Updating Wallet Balance for ONUS Fee and tax...");
//
//                //Huzaifa refreshing balance = 12/01/2023
//                AcctActBalance = Long.parseLong(account.getActualBalance());
//                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
//                //==============================================================
//
//                account.setActualBalance((AcctActBalance - OnUsFee + Long.parseLong(wsmodel.getOnuschargestax())) + "");
//                account.setAvailableBalance((AcctAvailBalance - OnUsFee + Long.parseLong(wsmodel.getOnuschargestax())) + "");
//                GeneralDao.Instance.saveOrUpdate(account);
//
//                logger.info("Updating Wallet Balance Log for ONUS Fee and tax...");
//                WalletBalanceLog balanceLogOnUsFee = new WalletBalanceLog();
//                balanceLogOnUsFee.setWallet(account);
//                balanceLogOnUsFee.setChannelid(wsmodel.getChannelid());
//                balanceLogOnUsFee.setAmount(OnUsFee + Long.parseLong(wsmodel.getOnuschargestax()) + "");
//                balanceLogOnUsFee.setOriginalbalance(AcctAvailBalance + "");
//                balanceLogOnUsFee.setUpdatedbalance(account.getAvailableBalance());
//                balanceLogOnUsFee.setTxnname(wsmodel.getServicename());
//                balanceLogOnUsFee.setTransaction(wsmodel);
//                balanceLogOnUsFee.setTxnnature(TxnFlag.DEBIT);
//                balanceLogOnUsFee.setCreatedate(new Date());
//                GeneralDao.Instance.save(balanceLogOnUsFee);
//
//                logger.info("Updating Wallet General Ledger for ONUS Fee and tax...");
//                WalletGeneralLedger WGLONUSFee = new WalletGeneralLedger();
//                WGLONUSFee.setTxnname(wsmodel.getServicename());
//                WGLONUSFee.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
//                WGLONUSFee.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
//                WGLONUSFee.setTxnflag(TxnFlag.DEBIT);
//                WGLONUSFee.setWallet(account);
//                WGLONUSFee.setAmount(OnUsFee + Long.parseLong(wsmodel.getOnuschargestax()) + "");
//                WGLONUSFee.setCurrency(account.getCurrency());
//                WGLONUSFee.setWalletflag(true);
//                WGLONUSFee.setMerchantid(wsmodel.getMerchantid());
//                WGLONUSFee.setAgentid(wsmodel.getAgentid());
//                WGLONUSFee.setBillerid(wsmodel.getBillerid());
//                WGLONUSFee.setTransaction(wsmodel);
//                WGLONUSFee.setPreviousBalance(AcctAvailBalance + "");
//                WGLONUSFee.setClosingBalance(account.getAvailableBalance());
//                GeneralDao.Instance.saveOrUpdate(WGLONUSFee);
//
//                CMSEMIAccountCollection onUsATMFeeAccount=null;
//                CMSEMIWallet onUsATMFeeWallet=null;
//
//                if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
//                    onUsATMFeeAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_FEE_ACCT);
//                    onUsATMFeeWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_FEE_WLLT);
//                }
//
//                UpdateandLogOnUsATMFeeAccount(wsmodel, TxnFlag.CREDIT, onUsATMFeeAccount, onUsATMFeeWallet);
//
//                wsmodel.setNayapaytaxamount(wsmodel.getOnuschargestax());
//                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
//            }
//
            if(receiptCharges > 0)
            {
                logger.info("Updating Wallet Balance for Receipt charges and tax...");

                //Huzaifa refreshing balance = 12/01/2023
                AcctActBalance = Long.parseLong(account.getActualBalance());
                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
                //==============================================================

                account.setActualBalance((AcctActBalance - (receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()))) + "");
                account.setAvailableBalance((AcctAvailBalance - (receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()))) + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log for Receipt charges and tax...");
                WalletBalanceLog balanceLogReciptCharges = new WalletBalanceLog();
                balanceLogReciptCharges.setWallet(account);
                balanceLogReciptCharges.setChannelid(wsmodel.getChannelid());
                balanceLogReciptCharges.setAmount(receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()) + "");
                balanceLogReciptCharges.setOriginalbalance(AcctAvailBalance + "");
                balanceLogReciptCharges.setUpdatedbalance(account.getAvailableBalance());
                balanceLogReciptCharges.setTxnname(wsmodel.getServicename());
                balanceLogReciptCharges.setTransaction(wsmodel);
                balanceLogReciptCharges.setTxnnature(TxnFlag.DEBIT);
                balanceLogReciptCharges.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLogReciptCharges);

                logger.info("Updating Wallet General Ledger for Receipt charges and tax...");
                WalletGeneralLedger WGLReciptCharges = new WalletGeneralLedger();
                WGLReciptCharges.setTxnname(wsmodel.getServicename());
                WGLReciptCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGLReciptCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGLReciptCharges.setTxnflag(TxnFlag.DEBIT);
                WGLReciptCharges.setWallet(account);
                WGLReciptCharges.setAmount(receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()) + "");
                WGLReciptCharges.setCurrency(account.getCurrency());
                WGLReciptCharges.setWalletflag(true);
                WGLReciptCharges.setMerchantid(wsmodel.getMerchantid());
                WGLReciptCharges.setAgentid(wsmodel.getAgentid());
                WGLReciptCharges.setBillerid(wsmodel.getBillerid());
                WGLReciptCharges.setTransaction(wsmodel);
                WGLReciptCharges.setPreviousBalance(AcctAvailBalance + "");
                WGLReciptCharges.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGLReciptCharges);

                CMSEMIAccountCollection receiptChargesAccount=null;
                CMSEMIWallet receiptChargesWallet=null;

                if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    receiptChargesAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_ACCT);
                    receiptChargesWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_WLLT);
                }

                UpdateandLogOnUsATMReceiptChargesAccount(wsmodel, TxnFlag.CREDIT, receiptChargesAccount, receiptChargesWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getReceiptchargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(balInqCharges > 0)
            {
                logger.info("Updating Wallet Balance for Balance Inquiry charges and tax...");

                //Huzaifa refreshing balance = 12/01/2023
                AcctActBalance = Long.parseLong(account.getActualBalance());
                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
                //==============================================================

                account.setActualBalance((AcctActBalance - (balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()))) + "");
                account.setAvailableBalance((AcctAvailBalance - (balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()))) + "");
//                account.setActualBalance(AcctActBalance - finalAmount + "");
//                account.setAvailableBalance(AcctAvailBalance - finalAmount + "");
                GeneralDao.Instance.saveOrUpdate(account);

                logger.info("Updating Wallet Balance Log for Balance Inquiry charges and tax...");
                WalletBalanceLog balanceLogbalInqCharges = new WalletBalanceLog();
                balanceLogbalInqCharges.setWallet(account);
                balanceLogbalInqCharges.setChannelid(wsmodel.getChannelid());
                balanceLogbalInqCharges.setAmount(balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()) + "");
//                balanceLogbalInqCharges.setAmount(finalAmount + "");
                balanceLogbalInqCharges.setOriginalbalance(AcctAvailBalance + "");
                balanceLogbalInqCharges.setUpdatedbalance(account.getAvailableBalance());
                balanceLogbalInqCharges.setTxnname(wsmodel.getServicename());
                balanceLogbalInqCharges.setTransaction(wsmodel);
                balanceLogbalInqCharges.setTxnnature(TxnFlag.DEBIT);
                balanceLogbalInqCharges.setCreatedate(new Date());
                GeneralDao.Instance.save(balanceLogbalInqCharges);

                logger.info("Updating Wallet General Ledger for Balance Inquiry charges and tax...");
                WalletGeneralLedger WGLBalInqCharges = new WalletGeneralLedger();
                WGLBalInqCharges.setTxnname(wsmodel.getServicename());
                WGLBalInqCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
                WGLBalInqCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
                WGLBalInqCharges.setTxnflag(TxnFlag.DEBIT);
                WGLBalInqCharges.setWallet(account);
                WGLBalInqCharges.setAmount(balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()) + "");
//                WGLBalInqCharges.setAmount(finalAmount + "");
                WGLBalInqCharges.setCurrency(account.getCurrency());
                WGLBalInqCharges.setWalletflag(true);
                WGLBalInqCharges.setMerchantid(wsmodel.getMerchantid());
                WGLBalInqCharges.setAgentid(wsmodel.getAgentid());
                WGLBalInqCharges.setBillerid(wsmodel.getBillerid());
                WGLBalInqCharges.setTransaction(wsmodel);
                WGLBalInqCharges.setPreviousBalance(AcctAvailBalance + "");
                WGLBalInqCharges.setClosingBalance(account.getAvailableBalance());
                GeneralDao.Instance.saveOrUpdate(WGLBalInqCharges);

                CMSEMIAccountCollection BalanceInquiryAccount=null;
                CMSEMIWallet BalanceInquiryWallet=null;

                logger.info("wsg-"+ wsmodel.getBankcode() +"-"+ wsmodel.getAcqbin());

                if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    logger.info("Huzaifa is Here");
                    BalanceInquiryAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_BALANCE_INQUIRY_CHRG_ACCT);
                    BalanceInquiryWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_BALANCE_INQUIRY_CHRG_WLLT);
                }

                UpdateandLogOnUsATMBalanceInquiryChargesAccount(wsmodel, TxnFlag.CREDIT, BalanceInquiryAccount, BalanceInquiryWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getBalinqchargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            wsmodel.setSrcchargeamount(StringUtils.leftPad(finalAmount.toString(), 12, "0"));

            logger.info("OnUsBalanceInquiry in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing OnUsBalanceInquiry in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }
    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

    // Asim Shahzad, Date : 6th June 2023, Tracking ID : VP-NAP-202303091

    @Transactional
    public static boolean CashWithDrawalOnUs(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for On Us CashWithDrawalOnUs Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
            Double NayapayChargeTaxAmount = 0.0;
            Long OnUsFee = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
            Double OnUsFeeTaxAmount = 0.0;
            Long receiptCharges = (Util.hasText(wsmodel.getReceiptcharges()) ? Long.parseLong(wsmodel.getReceiptcharges()) : 0L);//ReceiptCharges
            Double receiptChargesTax = 0.0;
            Long balInqCharges = (Util.hasText(wsmodel.getBalanceinquirycharges()) ? Long.parseLong(wsmodel.getBalanceinquirycharges()) : 0L);
            Double balInqChargesTax = 0.0;
            Long tax = 0L;

            Tax sstTax;
            String dbQuery = null;
            Map<String, Object> params = new HashMap<>();

            dbQuery = "from " + Tax.class.getName() + " t where title='SST'";
            params = new HashMap<>();
            sstTax = (Tax) GeneralDao.Instance.findObject(dbQuery, params);

            if (sstTax != null) {
                logger.info("Tax rate found, calculating amount ...");
                if(NayapayChargeAmount > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        NayapayChargeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0) * (NayapayChargeAmount / 100.0);
                        logger.info("Update amount for Nayapay ChargeTax Amount [" + NayapayChargeTaxAmount + "]");
                    } else {
                        NayapayChargeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for Nayapay ChargeTax Amount [" + NayapayChargeTaxAmount + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedNPChargeAmtTaxAmountStr = dft.format(NayapayChargeTaxAmount);
                    updatedNPChargeAmtTaxAmountStr = updatedNPChargeAmtTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Charge Tax Amount [" + updatedNPChargeAmtTaxAmountStr + "]");
                    wsmodel.setNayapaychargestax(StringUtils.leftPad(updatedNPChargeAmtTaxAmountStr, 12, "0"));
                }
                if(OnUsFee > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        OnUsFeeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0) * (OnUsFee / 100.0);
                        logger.info("Update amount for OnUs Charges Tax Amount [" + OnUsFeeTaxAmount + "]");
                    } else {
                        OnUsFeeTaxAmount = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for OnUs Charges Tax Amount [" + OnUsFeeTaxAmount + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedBankTaxAmountStr = dft.format(OnUsFeeTaxAmount);
                    updatedBankTaxAmountStr = updatedBankTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay OnUs Charges Tax Amount [" + updatedBankTaxAmountStr + "]");
                    wsmodel.setOnuschargestax(StringUtils.leftPad(updatedBankTaxAmountStr, 12, "0"));
                }
                if(receiptCharges > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        receiptChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0) * (receiptCharges / 100.0);
                        logger.info("Update amount for receiptChargesTax [" + receiptChargesTax + "]");
                    } else {
                        receiptChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for receiptChargesTax [" + receiptChargesTax + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedreceiptChargesTaxAmountStr = dft.format(receiptChargesTax);
                    updatedreceiptChargesTaxAmountStr = updatedreceiptChargesTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Receipt Charges Tax Amount [" + updatedreceiptChargesTaxAmountStr + "]");
                    wsmodel.setReceiptchargestax(StringUtils.leftPad(updatedreceiptChargesTaxAmountStr, 12, "0"));
                }
                if(balInqCharges > 0){
                    if (sstTax.getValueType().equals(TaxType.PERCENTAGE_VALUE.toString())) {
                        balInqChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0) * (balInqCharges / 100.0);
                        logger.info("Update amount for Balance Inquiry Charges Tax [" + balInqChargesTax + "]");
                    } else {
                        balInqChargesTax = (Double.parseDouble(sstTax.getValue()) / 100.0);
                        logger.info("Update amount for Balance Inquiry Charges  [" + balInqChargesTax + "]");
                    }

                    DecimalFormat dft = new DecimalFormat("0.00");
                    String updatedbalInqChargesTaxAmountStr = dft.format(balInqChargesTax);
                    updatedbalInqChargesTaxAmountStr = updatedbalInqChargesTaxAmountStr.replace(".", "");
                    logger.info("Final amount for Nayapay Receipt Charges Tax Amount [" + updatedbalInqChargesTaxAmountStr + "]");
                    wsmodel.setBalinqchargestax(StringUtils.leftPad(updatedbalInqChargesTaxAmountStr, 12, "0"));
                }
            }

            logger.info("Nayapay tax amount: " + wsmodel.getNayapaychargestax());
            //tax = Math.round(NayapayChargeTaxAmount + BankTaxAmount + receiptChargesTax);

            Long finalAmount = txnAmount + NayapayChargeAmount + OnUsFee + balInqCharges + receiptCharges +
                    (Util.hasText(wsmodel.getNayapaychargestax()) ? Long.parseLong(wsmodel.getNayapaychargestax()) : 0L) +
                    (Util.hasText(wsmodel.getOnuschargestax()) ? Long.parseLong(wsmodel.getOnuschargestax()) : 0L) +
                    (Util.hasText(wsmodel.getReceiptchargestax()) ? Long.parseLong(wsmodel.getReceiptchargestax()) : 0L) +
                    (Util.hasText(wsmodel.getBalinqchargestax()) ? Long.parseLong(wsmodel.getBalinqchargestax()) : 0L) ;

            logger.info("Final Amount: " + finalAmount);

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            logger.info("AcctActBalance: " + AcctActBalance);
            logger.info("AcctAvailBalance: " + AcctAvailBalance);


            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for On Us CashWithdrawal Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                // Asim Shahzad, Date : 24th Nov 2020, Tracking ID : VP-NAP-202011102/ VC-NAP-202011101
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                // ====================================================================================
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            //Huzaifa Commented - 180124
//            account.setActualBalance((AcctActBalance - txnAmount) + "");
//            account.setAvailableBalance((AcctAvailBalance - txnAmount) + "");
            //=======================
            account.setActualBalance(AcctActBalance - finalAmount + "");
            account.setAvailableBalance(AcctAvailBalance - finalAmount + "");
            GeneralDao.Instance.saveOrUpdate(account);
//            GeneralDao.Instance.getCurrentSession().refresh(account);

            logger.info("AcctActBalance: " + account.getActualBalance());
            logger.info("AcctAvailBalance: " + account.getAvailableBalance());

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 22nd Feb 2022, Tracking ID : VC-NAP-202202104
            //balanceLog.setTransDT(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            // ==================================================================

            GeneralDao.Instance.save(balanceLog);

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            CMSEMIAccountCollection settlementaccount=null;
            CMSEMIWallet settlementWallet=null;

            if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                settlementaccount = GetEMICollectionAccountForUnil(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_SETT_ACCT);
//                settlementWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_SETT_WLLT);
                settlementWallet = settlementaccount.getEmiwallet();
            }

            logger.info(settlementaccount.getCategory()  + "-" + settlementWallet.getCategory());

            if(settlementaccount != null && settlementWallet != null) {
                
                UpdateandLogOnUsATMSettlementtAccount(wsmodel, TxnFlag.CREDIT, false, false, false, false, settlementaccount, settlementWallet);
            }

            if(NayapayChargeAmount > 0) {
                logger.info("Updating Wallet Balance for Nayapay charges and tax...");

//                 //Huzaifa refreshing balance = 12/01/2023
//                 AcctActBalance = Long.parseLong(account.getActualBalance());
//                 AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
//                 //=======================================
//
//                account.setActualBalance((AcctActBalance - NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax())) + "");
//                account.setAvailableBalance((AcctAvailBalance - NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax())) + "");
//                GeneralDao.Instance.saveOrUpdate(account);
//                GeneralDao.Instance.getCurrentSession().refresh(account);
//
//                logger.info("Updating Wallet Balance Log for Nayapay charges and tax...");
//                WalletBalanceLog balanceLogNPCharges = new WalletBalanceLog();
//                balanceLogNPCharges.setWallet(account);
//                balanceLogNPCharges.setChannelid(wsmodel.getChannelid());
//                balanceLogNPCharges.setAmount(NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax()) + "");
//                balanceLogNPCharges.setOriginalbalance(AcctAvailBalance + "");
//                balanceLogNPCharges.setUpdatedbalance(account.getAvailableBalance());
//                balanceLogNPCharges.setTxnname(wsmodel.getServicename());
//                balanceLogNPCharges.setTransaction(wsmodel);
//                balanceLogNPCharges.setTxnnature(TxnFlag.DEBIT);
//                balanceLogNPCharges.setCreatedate(new Date());
//                GeneralDao.Instance.save(balanceLogNPCharges);
//
//                logger.info("Updating Wallet General Ledger for Nayapay charges and tax...");
//                WalletGeneralLedger WGLNPCharges = new WalletGeneralLedger();
//                WGLNPCharges.setTxnname(wsmodel.getServicename());
//                WGLNPCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
//                WGLNPCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
//                WGLNPCharges.setTxnflag(TxnFlag.DEBIT);
//                WGLNPCharges.setWallet(account);
//                WGLNPCharges.setAmount(NayapayChargeAmount + Long.parseLong(wsmodel.getNayapaychargestax()) + "");
//                WGLNPCharges.setCurrency(account.getCurrency());
//                WGLNPCharges.setWalletflag(true);
//                WGLNPCharges.setMerchantid(wsmodel.getMerchantid());
//                WGLNPCharges.setAgentid(wsmodel.getAgentid());
//                WGLNPCharges.setBillerid(wsmodel.getBillerid());
//                WGLNPCharges.setTransaction(wsmodel);
//                WGLNPCharges.setPreviousBalance(AcctAvailBalance + "");
//                WGLNPCharges.setClosingBalance(account.getAvailableBalance());
//                GeneralDao.Instance.saveOrUpdate(WGLNPCharges);

                CMSEMIAccountCollection onUsATMFeeAccount=null;
                CMSEMIWallet onUsATMFeeWallet=null;

                if(wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    onUsATMFeeAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_FEE_ACCT);
                    onUsATMFeeWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_FEE_WLLT);
                }

                UpdateandLogOnUsATMFeeAccount(wsmodel, TxnFlag.CREDIT, onUsATMFeeAccount, onUsATMFeeWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getNayapaychargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                logger.info("AcctActBalance 1: " + account.getActualBalance());
                logger.info("AcctAvailBalance 2: " + account.getAvailableBalance());
            }

            if(OnUsFee > 0)
            {
                //UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                logger.info("Updating Wallet Balance for ONUS Fee and tax...");
//
//                //Huzaifa refreshing balance = 12/01/2023
//                AcctActBalance = Long.parseLong(account.getActualBalance());
//                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
//                //==============================================================
//
//                account.setActualBalance((AcctActBalance - OnUsFee + Long.parseLong(wsmodel.getOnuschargestax())) + "");
//                account.setAvailableBalance((AcctAvailBalance - OnUsFee + Long.parseLong(wsmodel.getOnuschargestax())) + "");
//                GeneralDao.Instance.saveOrUpdate(account);
//                GeneralDao.Instance.getCurrentSession().refresh(account);
//
//                logger.info("Updating Wallet Balance Log for ONUS Fee and tax...");
//                WalletBalanceLog balanceLogOnUsFee = new WalletBalanceLog();
//                balanceLogOnUsFee.setWallet(account);
//                balanceLogOnUsFee.setChannelid(wsmodel.getChannelid());
//                balanceLogOnUsFee.setAmount(OnUsFee + Long.parseLong(wsmodel.getOnuschargestax()) + "");
//                balanceLogOnUsFee.setOriginalbalance(AcctAvailBalance + "");
//                balanceLogOnUsFee.setUpdatedbalance(account.getAvailableBalance());
//                balanceLogOnUsFee.setTxnname(wsmodel.getServicename());
//                balanceLogOnUsFee.setTransaction(wsmodel);
//                balanceLogOnUsFee.setTxnnature(TxnFlag.DEBIT);
//                balanceLogOnUsFee.setCreatedate(new Date());
//                GeneralDao.Instance.save(balanceLogOnUsFee);
//
//                logger.info("Updating Wallet General Ledger for ONUS Fee and tax...");
//                WalletGeneralLedger WGLONUSFee = new WalletGeneralLedger();
//                WGLONUSFee.setTxnname(wsmodel.getServicename());
//                WGLONUSFee.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
//                WGLONUSFee.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
//                WGLONUSFee.setTxnflag(TxnFlag.DEBIT);
//                WGLONUSFee.setWallet(account);
//                WGLONUSFee.setAmount(OnUsFee + Long.parseLong(wsmodel.getOnuschargestax()) + "");
//                WGLONUSFee.setCurrency(account.getCurrency());
//                WGLONUSFee.setWalletflag(true);
//                WGLONUSFee.setMerchantid(wsmodel.getMerchantid());
//                WGLONUSFee.setAgentid(wsmodel.getAgentid());
//                WGLONUSFee.setBillerid(wsmodel.getBillerid());
//                WGLONUSFee.setTransaction(wsmodel);
//                WGLONUSFee.setPreviousBalance(AcctAvailBalance + "");
//                WGLONUSFee.setClosingBalance(account.getAvailableBalance());
//                GeneralDao.Instance.saveOrUpdate(WGLONUSFee);

                UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                wsmodel.setNayapaytaxamount(wsmodel.getOnuschargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(receiptCharges > 0)
            {
                //UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                logger.info("Updating Wallet Balance for Receipt charges and tax...");

//                //Huzaifa refreshing balance = 12/01/2023
//                AcctActBalance = Long.parseLong(account.getActualBalance());
//                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
//                //==============================================================
//
//                account.setActualBalance((AcctActBalance - receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax())) + "");
//                account.setAvailableBalance((AcctAvailBalance - receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax())) + "");
//                GeneralDao.Instance.saveOrUpdate(account);
//                GeneralDao.Instance.getCurrentSession().refresh(account);
//
//                logger.info("Updating Wallet Balance Log for Receipt charges and tax...");
//                WalletBalanceLog balanceLogReciptCharges = new WalletBalanceLog();
//                balanceLogReciptCharges.setWallet(account);
//                balanceLogReciptCharges.setChannelid(wsmodel.getChannelid());
//                balanceLogReciptCharges.setAmount(receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()) + "");
//                balanceLogReciptCharges.setOriginalbalance(AcctAvailBalance + "");
//                balanceLogReciptCharges.setUpdatedbalance(account.getAvailableBalance());
//                balanceLogReciptCharges.setTxnname(wsmodel.getServicename());
//                balanceLogReciptCharges.setTransaction(wsmodel);
//                balanceLogReciptCharges.setTxnnature(TxnFlag.DEBIT);
//                balanceLogReciptCharges.setCreatedate(new Date());
//                GeneralDao.Instance.save(balanceLogReciptCharges);
//
//                logger.info("Updating Wallet General Ledger for Receipt charges and tax...");
//                WalletGeneralLedger WGLReciptCharges = new WalletGeneralLedger();
//                WGLReciptCharges.setTxnname(wsmodel.getServicename());
//                WGLReciptCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
//                WGLReciptCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
//                WGLReciptCharges.setTxnflag(TxnFlag.DEBIT);
//                WGLReciptCharges.setWallet(account);
//                WGLReciptCharges.setAmount(receiptCharges + Long.parseLong(wsmodel.getReceiptchargestax()) + "");
//                WGLReciptCharges.setCurrency(account.getCurrency());
//                WGLReciptCharges.setWalletflag(true);
//                WGLReciptCharges.setMerchantid(wsmodel.getMerchantid());
//                WGLReciptCharges.setAgentid(wsmodel.getAgentid());
//                WGLReciptCharges.setBillerid(wsmodel.getBillerid());
//                WGLReciptCharges.setTransaction(wsmodel);
//                WGLReciptCharges.setPreviousBalance(AcctAvailBalance + "");
//                WGLReciptCharges.setClosingBalance(account.getAvailableBalance());
//                GeneralDao.Instance.saveOrUpdate(WGLReciptCharges);

                CMSEMIAccountCollection receiptChargesAccount=null;
                CMSEMIWallet receiptChargesWallet=null;

                if(wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    receiptChargesAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_ACCT);
                    receiptChargesWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_WLLT);
                }

                UpdateandLogOnUsATMReceiptChargesAccount(wsmodel, TxnFlag.CREDIT, receiptChargesAccount, receiptChargesWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getReceiptchargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            if(balInqCharges > 0)
            {
                logger.info("Updating Wallet Balance for Balance Inquiry charges and tax...");

//                //Huzaifa refreshing balance = 12/01/2023
//                AcctActBalance = Long.parseLong(account.getActualBalance());
//                AcctAvailBalance = Long.parseLong(account.getAvailableBalance());
//                //==============================================================
//
//                account.setActualBalance((AcctActBalance - balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax())) + "");
//                account.setAvailableBalance((AcctAvailBalance - balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax())) + "");
//                GeneralDao.Instance.saveOrUpdate(account);
//                GeneralDao.Instance.getCurrentSession().refresh(account);
//
//                logger.info("Updating Wallet Balance Log for Balance Inquiry charges and tax...");
//                WalletBalanceLog balanceLogbalInqCharges = new WalletBalanceLog();
//                balanceLogbalInqCharges.setWallet(account);
//                balanceLogbalInqCharges.setChannelid(wsmodel.getChannelid());
//                balanceLogbalInqCharges.setAmount(balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()) + "");
//                balanceLogbalInqCharges.setOriginalbalance(AcctAvailBalance + "");
//                balanceLogbalInqCharges.setUpdatedbalance(account.getAvailableBalance());
//                balanceLogbalInqCharges.setTxnname(wsmodel.getServicename());
//                balanceLogbalInqCharges.setTransaction(wsmodel);
//                balanceLogbalInqCharges.setTxnnature(TxnFlag.DEBIT);
//                balanceLogbalInqCharges.setCreatedate(new Date());
//                GeneralDao.Instance.save(balanceLogbalInqCharges);
//
//                logger.info("Updating Wallet General Ledger for Balance Inquiry charges and tax...");
//                WalletGeneralLedger WGLBalInqCharges = new WalletGeneralLedger();
//                WGLBalInqCharges.setTxnname(wsmodel.getServicename());
//                WGLBalInqCharges.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
//                WGLBalInqCharges.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
//                WGLBalInqCharges.setTxnflag(TxnFlag.DEBIT);
//                WGLBalInqCharges.setWallet(account);
//                WGLBalInqCharges.setAmount(balInqCharges + Long.parseLong(wsmodel.getBalinqchargestax()) + "");
//                WGLBalInqCharges.setCurrency(account.getCurrency());
//                WGLBalInqCharges.setWalletflag(true);
//                WGLBalInqCharges.setMerchantid(wsmodel.getMerchantid());
//                WGLBalInqCharges.setAgentid(wsmodel.getAgentid());
//                WGLBalInqCharges.setBillerid(wsmodel.getBillerid());
//                WGLBalInqCharges.setTransaction(wsmodel);
//                WGLBalInqCharges.setPreviousBalance(AcctAvailBalance + "");
//                WGLBalInqCharges.setClosingBalance(account.getAvailableBalance());
//                GeneralDao.Instance.saveOrUpdate(WGLBalInqCharges);

                CMSEMIAccountCollection BalanceInquiryAccount=null;
                CMSEMIWallet BalanceInquiryWallet=null;

                if(wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    BalanceInquiryAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_BALANCE_INQUIRY_CHRG_ACCT);
                    BalanceInquiryWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_BALANCE_INQUIRY_CHRG_WLLT);
                }

                UpdateandLogOnUsATMBalanceInquiryChargesAccount(wsmodel, TxnFlag.CREDIT, BalanceInquiryAccount, BalanceInquiryWallet);

                wsmodel.setNayapaytaxamount(wsmodel.getBalinqchargestax());
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
            }

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            Long srcchargeamount = finalAmount - txnAmount;
            wsmodel.setSrcchargeamount(StringUtils.leftPad(srcchargeamount.toString(), 12, "0"));

            logger.info("On Us CashWithdrawal in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing On Us Withdrawal Cash in wallet...");
            //e.printStackTrace();
            logger.error(WebServiceUtil.getStrException(e));
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }

    @Transactional
    public static boolean CNICBasedOnUsCashWithdrawal(WalletCMSWsEntity wsmodel, CMSAccount account)
    {
        try {
            logger.info("UnLoading Wallet for On Us CNICBasedCashWithdrawal Transaction....");

            Long txnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
            Long NayapayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
//            Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
            Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);
            Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
//            Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
//            Long tranFeeAmount = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
            Long amtTranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);

            Long finalAmount = txnAmount + NayapayChargeAmount + BankCharges + Tax;

            Long AcctActBalance = Long.parseLong(account.getActualBalance());
            Long AcctAvailBalance = Long.parseLong(account.getAvailableBalance());

            if(finalAmount > AcctAvailBalance)
            {
                logger.error("Insufficient Amount for On Us CNICBasedCashWithdrawal Transaction, rejecting...");
                wsmodel.setRespcode(ISOResponseCodes.NP_SRC_INSUFFICEIENT_BALANCE); //04 - Low Balance ; refer to Doc
                wsmodel.setAcctbalance(AcctAvailBalance.toString());
                return false;
            }

            logger.info("Updating Wallet Balance ...");
            account.setActualBalance((AcctActBalance - finalAmount) + "");
            account.setAvailableBalance((AcctAvailBalance - finalAmount) + "");
            GeneralDao.Instance.saveOrUpdate(account);

            logger.info("Updating Wallet Balance Log ...");
            WalletBalanceLog balanceLog = new WalletBalanceLog();
            balanceLog.setWallet(account);
            balanceLog.setChannelid(wsmodel.getChannelid());
            balanceLog.setAmount(finalAmount + "");
            balanceLog.setOriginalbalance(AcctAvailBalance + "");
            balanceLog.setUpdatedbalance(account.getAvailableBalance());
            balanceLog.setTxnname(wsmodel.getServicename());
            balanceLog.setTransaction(wsmodel);
            balanceLog.setTxnnature(TxnFlag.DEBIT);
            balanceLog.setCreatedate(new Date());

            // Asim Shahzad, Date : 22nd Feb 2022, Tracking ID : VC-NAP-202202104
            //balanceLog.setTransDT(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date()));
            // ==================================================================

            GeneralDao.Instance.save(balanceLog);

            //m.rehman: 15-10-2021, PS-VP-NAP-202109301 / PS-VC-NAP-202109301 / PS-VG-NAP-202109301 - Time-out on switch (when calling wallet-API) for wallet statement
            wsmodel.setOpeningbalance(AcctAvailBalance + "");
            wsmodel.setClosingbalance(account.getAvailableBalance());
            /////////////////////////////////////////////////////////////////////////////////////////////////////

            logger.info("Updating Wallet General Ledger ...");
            WalletGeneralLedger WGL = new WalletGeneralLedger();
            WGL.setTxnname(wsmodel.getServicename());
            WGL.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL.setTxnflag(TxnFlag.DEBIT);
            WGL.setWallet(account);
            WGL.setAmount(finalAmount + "");
            WGL.setCurrency(account.getCurrency());
            WGL.setWalletflag(true);
            WGL.setMerchantid(wsmodel.getMerchantid());
            WGL.setAgentid(wsmodel.getAgentid());
            WGL.setBillerid(wsmodel.getBillerid());
            WGL.setTransaction(wsmodel);
            WGL.setPreviousBalance(AcctAvailBalance + "");
            WGL.setClosingBalance(account.getAvailableBalance());
            GeneralDao.Instance.saveOrUpdate(WGL);

            /*
            WalletGeneralLedger WGL2 = new WalletGeneralLedger();
            WGL2.setTxnname(wsmodel.getServicename());
            WGL2.setTransdatetime( Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
            WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
            WGL2.setTxnflag(TxnFlag.DEBIT);
            WGL2.setCollectionaccount(GetEMICollectionAccount(account.getCurrency(), AccType.CAT_SETT_ACCT));
            WGL2.setAmount(finalAmount + "");
            WGL2.setCurrency(account.getCurrency());
            WGL2.setWalletflag(false);
            WGL2.setMerchantid(wsmodel.getMerchantid());
            WGL2.setAgentid(wsmodel.getAgentid());
            WGL2.setBillerid(wsmodel.getBillerid());
            WGL2.setTransaction(wsmodel);
            GeneralDao.Instance.saveOrUpdate(WGL2);
            */

//            UpdateandLogCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.DEBIT, TxnFlag.CASH_OUT, AccType.CAT_SETT_ACCT);
//            UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
//                    AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), false, false);


            CMSEMIAccountCollection settlementaccount=null;
            CMSEMIWallet settlementWallet=null;

			if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                settlementaccount = GetEMICollectionAccountForUnil(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_SETT_ACCT);
//                settlementWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_SETT_WLLT);
                settlementWallet = settlementaccount.getEmiwallet();
            }

            

            if(null != settlementaccount && null != settlementWallet) {
                UpdateandLogOnUsATMSettlementtAccount(wsmodel, TxnFlag.CREDIT, false, false, false, false, settlementaccount, settlementWallet);
            }

            //m.rehman: 07-07-2020: On Nayapay request, stop charge to Partner bank (commenting below)
            //UpdateandLogPartnerBankCollectionAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT, TxnFlag.CASH_OUT,
            //        AccType.CAT_PARTNER_BANK_SETT_ACCT, wsmodel.getBankcode(), true, true);

            if(NayapayChargeAmount > 0)
            {
                //UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);
                UpdateandLogOnUsATMSettlementtAccount(wsmodel, TxnFlag.CREDIT, false, false, false, false, settlementaccount, settlementWallet);
            }

            if(Tax > 0)
            {
                UpdateandLogTaxAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

            }

            if(amtTranFee > 0)
            {
                //UpdateandLogRevenueAccount(wsmodel, account.getCurrency(), TxnFlag.CREDIT);

                CMSEMIAccountCollection receiptChargesAccount=null;
                CMSEMIWallet receiptChargesWallet=null;

			if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
                    receiptChargesAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_ACCT);
                    receiptChargesWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_WLLT);
            }

                
                UpdateandLogOnUsATMReceiptChargesAccount(wsmodel, TxnFlag.DEBIT, receiptChargesAccount, receiptChargesWallet);
                UpdateandLogOnUsATMSettlementtAccount(wsmodel, TxnFlag.CREDIT, false, false, false, true, settlementaccount, settlementWallet);
            }
            if(BankCharges > 0) {
//                CMSEMIAccountCollection chargesAccount=null;
//                CMSEMIWallet chargesWallet=null;
//
//                if(wsmodel.getBankcode().equals("ALFH") && wsmodel.getAcqbin().equals("1000001")) {
//                    chargesAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_BAFL_ONUS_ATM_RECEIPT_CHRG_ACCT);
//                    chargesWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_BAFL_ONUS_ATM_RECEIPT_CHRG_WLLT);
//                }
//                else if (wsmodel.getBankcode().equals("UNIL") && wsmodel.getAcqbin().equals("1000009")) {
//                    chargesAccount = GetEMICollectionAccount(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_ACCT);
//                    chargesWallet = GetEMIWallet(account.getCurrency(), AccType.CAT_UBL_ONUS_ATM_RECEIPT_CHRG_WLLT);
//                }

                UpdateandLogOnUsATMSettlementtAccount(wsmodel, TxnFlag.CREDIT, true, true, false, false, settlementaccount, settlementWallet);
            }


            logger.info("On Us CNICBasedCashWithdrawal in Wallet completed successfully!");
            return true;
        }
        catch (Exception e)
        {
            logger.error("Exception caught while Performing On Us CNICBasedCashWithdrawal in wallet...");
            logger.error(WebServiceUtil.getStrException(e));
            e.printStackTrace();
            wsmodel.setRespcode(ISOResponseCodes.UNABLE_TO_PROCESS); //46 - Unable to Process ; Refer to Doc
            return false;
        }
    }


    @Transactional
    public static void UpdateandLogOnUsATMSettlementtAccount(WalletCMSWsEntity wsmodel, String txnFlag,
                                                               boolean isChargesApply, boolean isApplyOnlyCharges, boolean isChargesAdd, boolean applyReceiptCharges,
                                                                 CMSEMIAccountCollection settlementaccount, CMSEMIWallet settlementWallet)
    {

        Long AcctActBalance = (Util.hasText(settlementaccount.getActualBalance()) ? Long.parseLong(settlementaccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(settlementaccount.getAvailableBalance()) ? Long.parseLong(settlementaccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;

        Long SrcChargeAmount = (Util.hasText(wsmodel.getSrcchargeamount()) ? Long.parseLong(wsmodel.getSrcchargeamount()) : 0L);
        Long DestChargeAmount = (Util.hasText(wsmodel.getDestchargeamount()) ? Long.parseLong(wsmodel.getDestchargeamount()) : 0L);
        Long NayaPayChargeAmount = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);
        Long BankCharges = (Util.hasText(wsmodel.getBankcharges()) ? Long.parseLong(wsmodel.getBankcharges()) : 0L);
        Long BankTaxAmount = (Util.hasText(wsmodel.getBanktaxamount()) ? Long.parseLong(wsmodel.getBanktaxamount()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getAmttranfee()) ? Long.parseLong(wsmodel.getAmttranfee()) : 0L);
        Long TxnAmount = (Util.hasText(wsmodel.getAmounttransaction()) ? Long.parseLong(wsmodel.getAmounttransaction()) : 0L);
        Long Tax = (Util.hasText(wsmodel.getNayapaytaxamount()) ? Long.parseLong(wsmodel.getNayapaytaxamount()) : 0L);

        Long AmountCollectionAccount;
        if (isChargesApply) {
            if (isApplyOnlyCharges) {
                AmountCollectionAccount = NayaPayChargeAmount + BankCharges + (SrcChargeAmount-Tax);
            } else {
                if (isChargesAdd) {
                    AmountCollectionAccount = TxnAmount + NayaPayChargeAmount + BankCharges + (SrcChargeAmount - Tax);
                } else {
                    AmountCollectionAccount = TxnAmount - (NayaPayChargeAmount + BankCharges + (SrcChargeAmount - Tax));
                }
            }
        } else {
            AmountCollectionAccount = TxnAmount;
        }

        if(applyReceiptCharges) {
            AmountCollectionAccount = TranFee;
        }

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;

//            AcctActBalance = AcctActBalance - AmountCollectionAccount;
//            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;

//            AcctActBalance = AcctActBalance + AmountCollectionAccount;
//            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating OnUs ATM Settlement Collection Account Balance ...");
        settlementaccount.setActualBalance(AcctActBalance + "");
        settlementaccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementaccount);

        logger.info("Updating OnUs ATM Settlement Wallet Balance ...");
        settlementWallet.setActualBalance(WlltActBalance + "");
        settlementWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementWallet);

        logger.info("Updating OnUs ATM Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(settlementaccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(settlementaccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(settlementaccount.getBankCode());
        balanceLog.setAccountNature(settlementaccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(settlementaccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating OnUs ATM Settlement Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(settlementWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());
        // Moiz, Date : 25th July 2024, Desc : Added for ORA-0001 issue
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================
        GeneralDao.Instance.save(wlltBalanceLog);

        logger.info("Updating OnUs ATM Settlement Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(settlementWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(settlementWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }


    @Transactional
    public static void UpdateandLogOnUsATMFeeAccount(WalletCMSWsEntity wsmodel, String txnFlag,
                                                             CMSEMIAccountCollection onUsATMFeeAccount, CMSEMIWallet settlementWallet)
    {

        Long AcctActBalance = (Util.hasText(onUsATMFeeAccount.getActualBalance()) ? Long.parseLong(onUsATMFeeAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(onUsATMFeeAccount.getAvailableBalance()) ? Long.parseLong(onUsATMFeeAccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(settlementWallet.getActualBalance()) ? Long.parseLong(settlementWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(settlementWallet.getAvailableBalance()) ? Long.parseLong(settlementWallet.getAvailableBalance()) : 0L) ;

        Long BankCharges = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);

        Long AmountCollectionAccount = BankCharges;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating OnUs ATM Fee Collection Account Balance ...");
        onUsATMFeeAccount.setActualBalance(AcctActBalance + "");
        onUsATMFeeAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(onUsATMFeeAccount);

        logger.info("Updating OnUs ATM Fee Wallet Balance ...");
        settlementWallet.setActualBalance(WlltActBalance + "");
        settlementWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(settlementWallet);

        logger.info("Updating OnUs ATM Fee Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(onUsATMFeeAccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(onUsATMFeeAccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(onUsATMFeeAccount.getBankCode());
        balanceLog.setAccountNature(onUsATMFeeAccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(onUsATMFeeAccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating OnUs ATM Fee Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(settlementWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(settlementWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());
        // Moiz, Date : 25th July 2024, Desc : Added for ORA-0001 issue
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================
        GeneralDao.Instance.save(wlltBalanceLog);

        logger.info("Updating OnUs ATM Fee Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(settlementWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(settlementWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(settlementWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }


    @Transactional
    public static void UpdateandLogOnUsATMReceiptChargesAccount(WalletCMSWsEntity wsmodel, String txnFlag,
                                                             CMSEMIAccountCollection receiptChargesAccount, CMSEMIWallet receiptChargesWallet)
    {

        Long AcctActBalance = (Util.hasText(receiptChargesAccount.getActualBalance()) ? Long.parseLong(receiptChargesAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(receiptChargesAccount.getAvailableBalance()) ? Long.parseLong(receiptChargesAccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(receiptChargesWallet.getActualBalance()) ? Long.parseLong(receiptChargesWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(receiptChargesWallet.getAvailableBalance()) ? Long.parseLong(receiptChargesWallet.getAvailableBalance()) : 0L) ;

        Long TranFee = (Util.hasText(wsmodel.getReceiptcharges()) ? Long.parseLong(wsmodel.getReceiptcharges()) : 0L);

        Long AmountCollectionAccount;
        AmountCollectionAccount = TranFee;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating OnUs ATM Receipt Charges Collection Account Balance ...");
        receiptChargesAccount.setActualBalance(AcctActBalance + "");
        receiptChargesAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(receiptChargesAccount);

        logger.info("Updating OnUs ATM Receipt Charges Wallet Balance ...");
        receiptChargesWallet.setActualBalance(WlltActBalance + "");
        receiptChargesWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(receiptChargesWallet);

        logger.info("Updating OnUs ATM Receipt Charges Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(receiptChargesAccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(receiptChargesAccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(receiptChargesAccount.getBankCode());
        balanceLog.setAccountNature(receiptChargesAccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(receiptChargesAccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating OnUs ATM Receipt Charges Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(receiptChargesWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(receiptChargesWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());
        // Moiz, Date : 25th July 2024, Desc : Added for ORA-0001 issue
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================
        GeneralDao.Instance.save(wlltBalanceLog);

        logger.info("Updating OnUs ATM Receipt Charges Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(receiptChargesWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(receiptChargesWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(receiptChargesWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }

    @Transactional
    public static void UpdateandLogOnUsATMBalanceInquiryChargesAccount(WalletCMSWsEntity wsmodel, String txnFlag,
                                                                CMSEMIAccountCollection BalanceInquiryAccount, CMSEMIWallet BalanceInquiryWallet)
    {
        //logger.info("Actual Balance: "+ BalanceInquiryAccount.getActualBalance());

        Long AcctActBalance = (Util.hasText(BalanceInquiryAccount.getActualBalance()) ? Long.parseLong(BalanceInquiryAccount.getActualBalance()) : 0L);
        Long AcctAvailBalance = (Util.hasText(BalanceInquiryAccount.getAvailableBalance()) ? Long.parseLong(BalanceInquiryAccount.getAvailableBalance()) : 0L) ;

        Long WlltActBalance = (Util.hasText(BalanceInquiryWallet.getActualBalance()) ? Long.parseLong(BalanceInquiryWallet.getActualBalance()) : 0L);
        Long WlltAvailBalance = (Util.hasText(BalanceInquiryWallet.getAvailableBalance()) ? Long.parseLong(BalanceInquiryWallet.getAvailableBalance()) : 0L) ;

//        Long TranFee = (Util.hasText(wsmodel.getReceiptcharges()) ? Long.parseLong(wsmodel.getReceiptcharges()) : 0L);
        Long TranFee = (Util.hasText(wsmodel.getNayapaycharges()) ? Long.parseLong(wsmodel.getNayapaycharges()) : 0L);

        Long AmountCollectionAccount;
        AmountCollectionAccount = TranFee;

        if(txnFlag.equals(TxnFlag.DEBIT))
        {
            AcctActBalance = AcctActBalance - AmountCollectionAccount;
            WlltActBalance = WlltActBalance - AmountCollectionAccount;
        }
        else //Credit
        {
            AcctActBalance = AcctActBalance + AmountCollectionAccount;
            WlltActBalance = WlltActBalance + AmountCollectionAccount;
        }

        logger.info("Updating OnUs ATM Balance Inquiry Charges Collection Account Balance ...");
        BalanceInquiryAccount.setActualBalance(AcctActBalance + "");
        BalanceInquiryAccount.setAvailableBalance(AcctActBalance + "");
        GeneralDao.Instance.saveOrUpdate(BalanceInquiryAccount);

        logger.info("Updating OnUs ATM Balance Inquiry Charges Wallet Balance ...");
        BalanceInquiryWallet.setActualBalance(WlltActBalance + "");
        BalanceInquiryWallet.setAvailableBalance(WlltActBalance + "");
        GeneralDao.Instance.saveOrUpdate(BalanceInquiryWallet);

        logger.info("Updating OnUs ATM Balance Inquiry Charges Collection Account Balance Log ...");
        EMICollectionBalanceLog balanceLog = new EMICollectionBalanceLog();
        if(txnFlag.equals(TxnFlag.DEBIT)) {
            balanceLog.setDebitAccount(BalanceInquiryAccount.getAccountNumber());
            balanceLog.setDebitAmount(AmountCollectionAccount + "");
        } else {
            balanceLog.setCreditAccount(BalanceInquiryAccount.getAccountNumber());
            balanceLog.setCreditAmount(AmountCollectionAccount + "");
        }
        balanceLog.setTxnId(wsmodel.getTranrefnumber());
        balanceLog.setBankId(BalanceInquiryAccount.getBankCode());
        balanceLog.setAccountNature(BalanceInquiryAccount.getAccountType());
        balanceLog.setTranDate(wsmodel.getTransdatetime());
//        balanceLog.setClosingBalance(StringUtils.leftPad(settlementaccount.getAvailableBalance(), 12, "0"));
        balanceLog.setClosingBalance(BalanceInquiryAccount.getAvailableBalance());
        balanceLog.setVoucherId(wsmodel.getFundsvoucherid());
        GeneralDao.Instance.save(balanceLog);

        logger.info("Updating OnUs ATM Balance Inquiry Charges Wallet Balance Log ...");
        EMIWalletBalanceLog wlltBalanceLog = new EMIWalletBalanceLog();
        wlltBalanceLog.setEmiwallet(BalanceInquiryWallet);
        wlltBalanceLog.setAmount(AmountCollectionAccount + "");
        wlltBalanceLog.setChannelid(wsmodel.getChannelid());
        wlltBalanceLog.setOriginalbalance(WlltAvailBalance + "");
        wlltBalanceLog.setUpdatedbalance(BalanceInquiryWallet.getAvailableBalance());
        wlltBalanceLog.setTransaction(wsmodel);
        wlltBalanceLog.setTxnname(wsmodel.getServicename());
        wlltBalanceLog.setTxnnature(txnFlag);
        wlltBalanceLog.setCreatedate(new Date());
        // Moiz, Date : 25th July 2024, Desc : Added for ORA-0001 issue
        GeneralDao.Instance.getNextValEmiCollBalLog();
        // ============================================================================================
        GeneralDao.Instance.save(wlltBalanceLog);

        logger.info("Updating OnUs ATM Balance Inquiry Charges Wallet General Ledger ...");
        WalletGeneralLedger WGL2 = new WalletGeneralLedger();
        WGL2.setTxnname(wsmodel.getServicename());
        WGL2.setTransdatetime(Util.hasText(wsmodel.getTransdatetime()) ? wsmodel.getTransdatetime() : (new SimpleDateFormat(WebServiceUtil.transdatetimeFormat)).format(new Date())  );
        WGL2.setTxnrefnum(Util.hasText(wsmodel.getTranrefnumber()) ? wsmodel.getTranrefnumber() : wsmodel.getStan());
        WGL2.setTxnflag(txnFlag);
        WGL2.setEmiaccount(BalanceInquiryWallet);
        WGL2.setAmount(AmountCollectionAccount + "");
        WGL2.setCurrency(BalanceInquiryWallet.getCurrency());
        WGL2.setWalletflag(false);
        WGL2.setMerchantid(wsmodel.getMerchantid());
        WGL2.setAgentid(wsmodel.getAgentid());
        WGL2.setBillerid(wsmodel.getBillerid());
        WGL2.setTransaction(wsmodel);
        WGL2.setPreviousBalance(WlltAvailBalance + "");
        WGL2.setClosingBalance(BalanceInquiryWallet.getAvailableBalance());
        GeneralDao.Instance.save(WGL2);
    }
    // =================================================================================================
}
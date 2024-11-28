package vaulsys.othermains.disagreement;

import vaulsys.calendar.DateTime;
import vaulsys.calendar.DayDate;
import vaulsys.clearing.AccountingService;
import vaulsys.clearing.base.SettlementReport;
import vaulsys.clearing.settlement.CoreConfigDataManager;
import vaulsys.customer.Core;
import vaulsys.entity.impl.Institution;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.Pair;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.fanap.cms.exception.BusinessException;
import com.fanap.cms.valueobjects.corecommunication.DocumentItemEntity;
import com.fanap.cms.valueobjects.corecommunication.IssueGeneralDocVO;
import com.ghasemkiani.util.icu.PersianDateFormat;

public class GeneratingSanad {
	public static final Logger logger = Logger.getLogger(GeneratingSanad.class);

	public static void main(String[] args) {
		//		List<String> errors = issueCumulativeDocs();
		//		if (errors.isEmpty()) {
		//			System.out.println("All docs issued!");
		//		} else {
		//			for(String error : errors) {
		//				System.out.println(error);
		//				System.out.println("==========================");
		//			}
		//		}

		List<String> ret = getOppositeTopics("1F109631");
		System.out.println(ret.size());
	}

	public static List<String> issueInstitutionCumulativeDocs(String institutionCode) {
		List<String> errors = new ArrayList<String>();
		List<String> topicCodes = new ArrayList<String>();
		GeneralDao.Instance.beginTransaction();
		Institution institution = GeneralDao.Instance.getObject(Institution.class, institutionCode);
		String switchAccountForInstitution = institution.getCoreAccountNumber().getAccountNumber();
		topicCodes.add(switchAccountForInstitution);
		topicCodes.addAll(getOppositeTopics(switchAccountForInstitution));
		issueCumulativeDoc(switchAccountForInstitution, topicCodes, topicCodes, 0L, errors);
		return errors;
	}

	public static List<String> issueCumulativeDocs() {
		List<String> errors = new ArrayList<String>();
		String[] topicCodes;
		final List<String> specialAccounts = Arrays.asList(CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchSpecialAccounts).split("-"));

		String switchAccountForFanap = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchAccountForFanap);
		String fanapTopicCodes = CoreConfigDataManager.getValue(CoreConfigDataManager.FanapTopicCodes);
		if (fanapTopicCodes != null && !fanapTopicCodes.trim().equals("")) {
			topicCodes = fanapTopicCodes.split("-");
			issueCumulativeDoc(switchAccountForFanap, Arrays.asList(topicCodes), specialAccounts, 0L, errors);
		}

		String switchAccountForShetab = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchAccountForShetab);
		String shetabTopicCodes = CoreConfigDataManager.getValue(CoreConfigDataManager.ShetabTopicCodes);
		if (shetabTopicCodes != null && !shetabTopicCodes.trim().equals("")) {
			topicCodes = shetabTopicCodes.split("-");
			issueCumulativeDoc(switchAccountForShetab, Arrays.asList(topicCodes), specialAccounts, 0L, errors);
		}

		String switchAccountForNegin = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchAccountForNegin);
		String neginTopicCodes = CoreConfigDataManager.getValue(CoreConfigDataManager.NeginTopicCodes);
		if (neginTopicCodes != null && !neginTopicCodes.trim().equals("")) {
			topicCodes = neginTopicCodes.split("-");
			issueCumulativeDoc(switchAccountForNegin, Arrays.asList(topicCodes), specialAccounts, 0L, errors);
		}

		String ShetabFeeTopicCodes = CoreConfigDataManager.getValue(CoreConfigDataManager.ShetabFeeTopicCodes);
		if (ShetabFeeTopicCodes != null && !ShetabFeeTopicCodes.trim().equals("")) {
			topicCodes = ShetabFeeTopicCodes.split("-");
			List<String> list = Arrays.asList(topicCodes);
			issueCumulativeDoc(topicCodes[0], list, list, Long.parseLong(CoreConfigDataManager.getValue(CoreConfigDataManager.ShetabFeeMinAmount)), errors);
		}

		String ShaparakSettlementMainTopic = CoreConfigDataManager.getValue(CoreConfigDataManager.ShaparakSettlementMainTopic);
		String ShaparakSettlementTopicCodes = CoreConfigDataManager.getValue(CoreConfigDataManager.ShaparakSettlementTopicCodes);
		if (ShaparakSettlementTopicCodes != null && !ShaparakSettlementTopicCodes.trim().equals("")) {
			topicCodes = ShaparakSettlementTopicCodes.split("-");
			issueCumulativeDoc(ShaparakSettlementMainTopic, Arrays.asList(topicCodes), specialAccounts, 0L, errors);
		}


		return errors;
	}

	private static List<String> getOppositeTopics(String topic) {
		StringBuilder query = new StringBuilder("select distinct c_code from t_topic@fcb where id in (select c_oppositetopic from t_topic@fcb where c_code = :topic and c_oppositetopic is not null)");
		GeneralDao.Instance.beginTransaction();

		Map<String, Object> params = new HashMap<String,Object>();
		params.put("topic", topic);

		List<String> result = GeneralDao.Instance.executeSqlQuery(query.toString(), params);
		GeneralDao.Instance.rollback();
		return result;
	}

	private static void issueCumulativeDoc(String switchAccountNumber, List<String> topicCodes, List<String> specialAccounts, Long minAmount, List<String> errors) {

		String switchBranchId = CoreConfigDataManager.getValue(CoreConfigDataManager.SwitchBranchId);

		DateTime day = DateTime.now();
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		String persianDate = dateFormatPers.format(day.toDate());

		Long amount = 0L;
		String branch = "";
		String account ="";
		List<DocumentItemEntity> docs = new ArrayList<DocumentItemEntity>();
		DocumentItemEntity documentItemEntity = null;
		DocumentItemEntity switchDocumentItemEntity = null;

		Map<String, Object> params = new HashMap<String,Object>();
		params.put("topics", topicCodes);

		StringBuilder query = new StringBuilder("SELECT ati.c_amount as amount, tp.c_code as topic, b.c_code as branch ")
		.append("FROM t_account@fcb ati ")
		.append("INNER JOIN t_accounttype@fcb act ON act.id = ati.c_accounttype ")
		.append("INNER JOIN t_topic@fcb tp  ON tp.id = act.c_topic ")
		.append("INNER JOIN t_basebranch@fcb b on b.id=ati.c_branch ")
		.append("WHERE tp.c_code IN (:topics) ")
		.append("and ati.c_amount <> 0 ")
		.append("order by cast(b.c_code as int), tp.c_code");

		GeneralDao.Instance.beginTransaction();
		List<Object[]> ans = GeneralDao.Instance.executeSqlQuery(query.toString(), params);

		for(int i=0; i< ans.size(); i++){
			System.out.println("row: "+i+" of "+ans.size());
			logger.debug("row: "+i+" of "+ans.size());

			docs.clear();
			amount = Long.valueOf(ans.get(i)[0].toString());
			branch = ans.get(i)[2].toString();
			account = ans.get(i)[1].toString();

			amount = amount - minAmount;
			if (amount.compareTo(0L) <= 0)
				continue;

			if(!(branch.equals(switchBranchId) && specialAccounts.contains(account))){
				if(Integer.parseInt(account.substring(0, 1)) % 2 == 1){
					logger.debug("DEBTOR account = " + account + " & branch = " + branch);
					switchDocumentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, switchAccountNumber, IssueGeneralDocVO.DocumentItemEntityType.Topic);
					documentItemEntity = new DocumentItemEntity(new Double(amount), false, branch, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, account, IssueGeneralDocVO.DocumentItemEntityType.Topic);					
				}
				else if (Integer.parseInt(account.substring(0, 1)) % 2 == 0){
					logger.debug("CREDITOR account = " + account + " & branch = " + branch);
					switchDocumentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, switchAccountNumber, IssueGeneralDocVO.DocumentItemEntityType.Topic);
					documentItemEntity = new DocumentItemEntity(new Double(amount), true, branch, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, account, IssueGeneralDocVO.DocumentItemEntityType.Topic);	
				}

				docs.add(documentItemEntity);
				docs.add(switchDocumentItemEntity);
				try {
					Pair<String, String> document = AccountingService.generateFCBDocument(  "سند تجمیعی حساب " + account + " برای شعبه " + branch + " مورخ "
							+ persianDate, docs, null, "Collect-" + branch +"-"+ account + "-" + persianDate, null, null, null);
					SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);			
					String transactionId = AccountingService.issueFCBDocument(report, false);
					if (transactionId == null)
						errors.add("بروز خطا در سند تجمیعی حساب " + account + " برای شعبه " + branch + " مورخ "	+ persianDate);
					System.out.println(transactionId);
					logger.debug(transactionId);
				} catch (Throwable e) {
					errors.add("بروز خطا در سند تجمیعی حساب " + account + " برای شعبه " + branch + " مورخ "	+ persianDate + " : " + e.getMessage());
					e.printStackTrace();
				}
			}
		}
		GeneralDao.Instance.rollback();

	}

	public static void main_(String args[])throws IOException{
		//		GlobalContext.getInstance().startup();
		//		ProcessContext.get().init();
		DocumentItemEntity documentItemEntity = null;
		DocumentItemEntity switchDocumentItemEntity = null;
		String switchBranchId = "995";
		//		String coreServer = "fcbrep90";
		String coreServer = "fcbrep";
		//2F206511: shetabIntermediate
		//1F109611: shetabIntermediate - moghabel
		//2F206521: neginIntermediate
		//1F109621: neginIntermediate - moghabel
		//2F206531: fanapIntermediate
		//1F109631: fanapIntermediate - moghabel
		final List<String> specialAccounts = Arrays.asList(new String[]{"1F109611", "1F109621", "1F109631", "2F206511", "2F206521", "2F206531"});


		//		String switchAccountNumber = "1F109611";
		//		String debtorAccounts[] = {"2F206511", "2F2065111", "2F2065112", "2F2065113", "2F2065114", "2F2065115"};
		//		String creditorAccounts[] = {"1F109611", "1F1096111", "1F1096112", "1F1096113", "1F1096114", "1F1096115"};
		//		String debtorAccounts[] = {"2F2065111"};

		//		String switchAccountNumber = "1F109611";
		//		String shetabDebtorAccounts[] = {"2F206511", "2F2065111", "2F2065112", "2F2065113", "2F2065114", "2F2065115"};
		//		String shetabCreditoraccounts[] = {"1F109611","1F1096111","1F1096112", "1F1096113", "1F1096114", "1F1096115"};


		String switchAccountNumber = "1F109631";
		//		String fanapDebtorAccounts[] = {"2F206531", "2F2065311", "2F2065312", "2F2065313", "2F2065314", "2F2065315"};
		String fanapCreditoraccounts[] = {"1F109631","1F1096311","1F1096312", "1F1096313", "1F1096314", "1F1096315"};

		//		String neginAccounts[] = {"1F109621", "2F206521"};

		String accounts[] = fanapCreditoraccounts;

		//		Institution institution= new Institution();
		//		String shetabAccount = institution.getAccount().getAccountNumber();

		DateTime day = DateTime.now();
		PersianDateFormat dateFormatPers = new PersianDateFormat("yyyyMMdd");
		String persianDate = dateFormatPers.format(day.toDate());


		Long amount = 0L;
		String branch = "";
		String account ="";
		List<DocumentItemEntity> docs = new ArrayList<DocumentItemEntity>();
		List<String> acc = new ArrayList<String>();
		for(int i = 0 ; i<accounts.length; i++){
			acc.add(accounts[i]);
		}

		DayDate date = DateTime.now().getDayDate();
		Map<String, Object> params = new HashMap<String,Object>();
		params.put("account", acc);
		//		String query = "select atit.c_totalamount as amount,tp2.c_code as topic,b.c_code as branch" +
		//		" from fcb.t_lastaccounttypeitem@"+coreServer+" atit " +
		//		" inner join t_accounttype@"+coreServer+" act2 on act2.id = atit.c_accounttypeid " +
		//		" inner join t_topic@"+coreServer+" tp2 on tp2.id= act2.c_topic " +
		//		" inner join t_basebranch@"+coreServer+" b on b.id=atit.c_branchid " +
		//		" inner join "+
		//		" (select MAX(ati.c_issuanceid) maxiss " +
		//		" from fcb.t_lastaccounttypeitem@"+coreServer+" ati "+
		//		" inner join t_accounttype@"+coreServer+" act ON act.id = ati.c_accounttypeid@"+coreServer+" " +
		//		" inner join t_topic@"+coreServer+" tp ON tp.id = act.c_topic " +
		//		" where tp.c_code in (:account) " +
		//		" and ati.c_issuanceid <= to_timestamp ('"+date+"' , 'YYYY_MM_DD') " +
		//		" and ati.c_active = 1 " +
		//		" and ati.c_enable = 1 " +
		//		" GROUP BY act.id ) nec on nec.maxiss = atit.c_issuanceid" +
		//		" where atit.c_totalamount > 0";


		String query = "SELECT ati.c_amount as amount, tp.c_code as topic, b.c_code as branch "+
				"FROM t_account@fcb ati " +
				"INNER JOIN t_accounttype@fcb act   ON act.id = ati.c_accounttype " +
				"INNER JOIN t_topic@fcb tp  ON tp.id              = act.c_topic " +
				"inner join t_basebranch@fcb b on b.id=ati.c_branch "+
				"WHERE tp.c_code      IN (:account) " +
				//						"and b.c_code = '201' " +
				"and ati.c_amount <> 0";

		GeneralDao.Instance.beginTransaction();
		List<Object[]> ans = GeneralDao.Instance.executeSqlQuery(query, params);

		for(int i=0; i< ans.size(); i++){
			//System.out.println("row: "+i+" of "+ans.size());
			logger.debug("row: "+i+" of "+ans.size());

			docs.clear();
			amount = Long.valueOf(ans.get(i)[0].toString());
			branch = ans.get(i)[2].toString();
			account = ans.get(i)[1].toString();
			if(amount > 0 && !(branch.equals("995") && specialAccounts.contains(account))){
				if(account.startsWith("1")){
					switchDocumentItemEntity = new DocumentItemEntity(new Double(amount), true, switchBranchId, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, switchAccountNumber, IssueGeneralDocVO.DocumentItemEntityType.Topic);
					documentItemEntity = new DocumentItemEntity(new Double(amount), false, branch, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, account, IssueGeneralDocVO.DocumentItemEntityType.Topic);					
				}
				else if (account.startsWith("2")){
					switchDocumentItemEntity = new DocumentItemEntity(new Double(amount), false, switchBranchId, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, switchAccountNumber, IssueGeneralDocVO.DocumentItemEntityType.Topic);
					documentItemEntity = new DocumentItemEntity(new Double(amount), true, branch, "سند تجمیعی حساب " + account + " برای شعبه " + branch+ " مورخ " + persianDate, account, IssueGeneralDocVO.DocumentItemEntityType.Topic);	
				}

				docs.add(documentItemEntity);
				docs.add(switchDocumentItemEntity);
				try {
					Pair<String, String> document = AccountingService.generateFCBDocument(  "سند تجمیعی حساب " + account + " برای شعبه " + branch + " مورخ "
							+ persianDate, docs, null, "Collect-" + branch +"-"+ account + "-" + persianDate, null, null, null);
					SettlementReport report = new SettlementReport(Core.FANAP_CORE, document.first, document.second, null);			
					String transactionId = AccountingService.issueFCBDocument(report, false);
					System.out.println(transactionId);
					logger.debug(transactionId);
				} catch (BusinessException e) {
					e.printStackTrace();
				}
			}
		}
		GeneralDao.Instance.rollback();
		System.exit(0);
	}
}

package vaulsys.initializer;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.authorization.policy.AllowedCard;
import vaulsys.authorization.policy.AllowedTranaction;
import vaulsys.authorization.policy.Bank;
import vaulsys.authorization.policy.CardGroupRestrictionPolicy;
import vaulsys.authorization.policy.CycleConstraintTransactionPolicy;
import vaulsys.authorization.policy.FITControlPolicy;
import vaulsys.authorization.policy.PanPrefixTransactionPolicy;
import vaulsys.authorization.policy.TerminalServicePolicy;
import vaulsys.calendar.DateTime;
import vaulsys.clearing.cyclecriteria.CycleCriteria;
import vaulsys.clearing.cyclecriteria.CycleType;
import vaulsys.persistence.GeneralDao;
import vaulsys.protocols.ifx.enums.TrnType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AddAuthorization {

	public static final String DEFAULT_AUTHORIZATION_PROFILE_NAME = "بدون محدودیت";
	public static final String EPAY_AUTHORIZATION_PROFILE_NAME = "پیش فرض پرداخت اینترنتی";
	public static final String POS_AUTHORIZATION_PROFILE_NAME = "پیش فرض پایانه فروش";
	public static final String ATM_AUTHORIZATION_PROFILE_NAME = "پیش فرض خودپرداز";
	public static final String SIMULATOR_AUTHORIZATION_PROFILE_NAME = "سیمولاتور";
	
	
	/*
	 * 1801		الگوی مجازشماری بدون محدودیت
	 * 1802		الگوی مجازشماری پیش فرض پرداخت اینترنتی
	 * 1803		الگوی مجازشماری پیش فرض پایانه فروش
	 * 1804		سیمولاتور
	*/
	
	
	private static final Long POS_AUTHPROF_ID = 1803L;
	private static final Long Epay_AUTHPROF_ID = 1802L;
	private static final Long Negin_AUTHPROF_ID = 1804L;
	
	public static void main(String[] args) {
		GeneralDao.Instance.beginTransaction();
		try {
			AddAuthorization initializer = new AddAuthorization();
			System.out.println("Adding PanPrefixTransactionPolicy");
			initializer.addPanPrefixTransactionPolicy(getAuthorizationProfile(POS_AUTHPROF_ID), 
						new ArrayList<Integer>(){{
								add(502229);
								add(639347);
						}});
			initializer.addPanPrefixTransactionPolicy(getAuthorizationProfile(Epay_AUTHPROF_ID), 
						new ArrayList<Integer>(){{
							add(502229);
							add(639347);
					}});
			initializer.addNeginPanPrefixTransactionPolicy(getAuthorizationProfile(Negin_AUTHPROF_ID));
			
			System.out.println("PanPrefixTransactionPolicy is added");
			
			/*System.out.println("Adding CardGroupRestrictionPolicy");
			initializer.addCardGroupRestrictionPolicy();
			System.out.println("CardGroupRestrictionPolicy is added");
			
			System.out.println("Adding CycleConstraintTransactionPolicy");
			initializer.addCycleConstraintTransactionPolicy();
			System.out.println("CycleConstraintTransactionPolicy is added");
			*/
			
			System.out.println("Adding FITControlPolicy");
			initializer.addFITControlPolicy(getAuthorizationProfile(POS_AUTHPROF_ID));
			initializer.addFITControlPolicy(getAuthorizationProfile(Epay_AUTHPROF_ID));
			System.out.println("FITControlPolicy is added");
			
			System.out.println("Adding TerminalServicePolicy");
			initializer.addPOSTerminalServicePolicy(getAuthorizationProfile(POS_AUTHPROF_ID));
			initializer.addEpayTerminalServicePolicy(getAuthorizationProfile(Epay_AUTHPROF_ID));
			
			System.out.println("TerminalServicePolicy is added");
			
			System.out.println("FINISHED!");
		} catch (Exception e) {
			e.printStackTrace();
			GeneralDao.Instance.rollback();
			System.exit(1);
		}
		GeneralDao.Instance.endTransaction();
		System.exit(0);
	}
	
	
	static public AuthorizationProfile createAuthorizationProfile(String name) throws Exception {
		AuthorizationProfile authorizationProfile = findAuthorizationProfile(name);
		if (authorizationProfile != null)
			return authorizationProfile;
		authorizationProfile = new AuthorizationProfile();
		
		authorizationProfile.setName(name);
		authorizationProfile.setCreatorUser(DBInitializeUtil.getUser());
		authorizationProfile.setCreatedDateTime(DateTime.now());
		GeneralDao.Instance.saveOrUpdate(authorizationProfile);
		return authorizationProfile;
	}
	
	
	private static AuthorizationProfile findAuthorizationProfile(String name) {
		String query = "from " + AuthorizationProfile.class.getName() + " a " + " where a.name = :name";
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("name", name);
		return (AuthorizationProfile) GeneralDao.Instance.findObject(query, param);
	}


	public void addCardGroupRestrictionPolicy(){
		
		CardGroupRestrictionPolicy policy = new CardGroupRestrictionPolicy();
		List<AllowedCard> cards = new ArrayList<AllowedCard>();
		cards.add(new AllowedCard(getBank(502229),null,5222296100100146L,5222296100100208L));
		policy.setCards(cards);
		getGeneralDao().saveOrUpdate(cards.get(0));
		getGeneralDao().saveOrUpdate(policy);
	}

	
	public PanPrefixTransactionPolicy addPanPrefixTransactionPolicy(AuthorizationProfile profile, List<Integer> myBins) {
		if (profile == null)
			return null;
		
		PanPrefixTransactionPolicy policy = new PanPrefixTransactionPolicy();
		policy.setTransactions(createAllowedTransactionType(myBins));
		policy.addProfile(profile);
		getGeneralDao().saveOrUpdate(policy);
		getGeneralDao().saveOrUpdate(profile);
		return policy;
	}

	public PanPrefixTransactionPolicy addNeginPanPrefixTransactionPolicy(AuthorizationProfile profile) {
		if (profile == null)
			return null;
		PanPrefixTransactionPolicy neginPolicy = new PanPrefixTransactionPolicy();
		neginPolicy.setTransactions(createNeginAllowedTransactionType());
		neginPolicy.addProfile(profile);
		getGeneralDao().saveOrUpdate(neginPolicy);
		getGeneralDao().saveOrUpdate(profile);
		return neginPolicy;
	}
	
	
	public CycleConstraintTransactionPolicy addCycleConstraintTransactionPolicy(){
		CycleConstraintTransactionPolicy policy = new CycleConstraintTransactionPolicy();
		
		policy.setCriteria(getCycleCriteria(CycleType.PER_DAY, 1));
		policy.setMaxAmount(10000000L);
		policy.setMaxTransaction(CycleConstraintTransactionPolicy.UNBOUNDED);
//		policy.setRequiredDelay(CycleConstraintTransactionPolicy.UNBOUNDED);
		AuthorizationProfile profile = getAuthorizationProfile(POS_AUTHPROF_ID);
		policy.addProfile(profile);
		getGeneralDao().saveOrUpdate(policy);
		getGeneralDao().saveOrUpdate(profile);
		return policy;
	}

	public FITControlPolicy addFITControlPolicy(AuthorizationProfile profile){
		if (profile == null)
			return null;
		
		FITControlPolicy policy = new FITControlPolicy();
		List<Bank> banks = getBanks();
		if (banks == null || banks.isEmpty()){
			banks.add(getBank(589210));
			banks.add(getBank(589463));
			banks.add(getBank(599999));
			banks.add(getBank(603769));
			banks.add(getBank(603770));
			banks.add(getBank(603799));
			banks.add(getBank(610433));
			banks.add(getBank(621986));
			banks.add(getBank(622106));
			banks.add(getBank(627353));
			banks.add(getBank(627412));
			banks.add(getBank(627488));
			banks.add(getBank(627648));
			banks.add(getBank(627760));
			banks.add(getBank(627961));
			banks.add(getBank(628023));
			banks.add(getBank(639607));
			banks.add(getBank(639346));
			banks.add(getBank(628157));
			banks.add(getBank(639347));
			banks.add(getBank(502229));
			banks.add(getBank(636214));
			banks.add(getBank(502908));
		}
		
		policy.setBanks(banks);
		policy.addProfile(profile);
		getGeneralDao().saveOrUpdate(policy);
		getGeneralDao().saveOrUpdate(profile);
		return policy;
	}
	
	private CycleCriteria getCycleCriteria(CycleType cycleType, Integer cycleCount) {
		CycleCriteria criteria = new CycleCriteria();
		criteria.setCycleType(cycleType);
		criteria.setCycleCount(cycleCount);
//		getGeneralDao().saveOrUpdate(criteria);
		return criteria;
	}
	
	
	private List<AllowedTranaction> createAllowedTransactionType(List<Integer> myBins) {
		List<AllowedTranaction> result = new ArrayList<AllowedTranaction>();

		result.addAll(createAllowedTransactionForALLBank());
		
		List<Bank> banks = getBanks();
		if (banks == null || banks.isEmpty()){
			banks.add(getBank(589210));
			banks.add(getBank(589463));
			banks.add(getBank(599999));
			banks.add(getBank(603769));
			banks.add(getBank(603770));
			banks.add(getBank(603799));
			banks.add(getBank(610433));
			banks.add(getBank(621986));
			banks.add(getBank(622106));
			banks.add(getBank(627353));
			banks.add(getBank(627412));
			banks.add(getBank(627488));
			banks.add(getBank(627648));
			banks.add(getBank(627760));
			banks.add(getBank(627961));
			banks.add(getBank(628023));
			banks.add(getBank(639607));
			banks.add(getBank(639346));
			banks.add(getBank(628157));
			banks.add(getBank(639347));
			banks.add(getBank(502229));
			banks.add(getBank(636214));
			banks.add(getBank(502908));
		}
		
		for (Bank b: banks){
			if (!myBins.contains(b.getBin().intValue()))
				result.addAll(createAllowedTransactionForNonPasargadBank(b));
		}
		
		for (Integer bin : myBins) 
			result.addAll(createAllowedTransactionForPasargadBank(getBank(bin)));			
		
		return result;
	}

	private List<AllowedTranaction> createNeginAllowedTransactionType() {
		List<AllowedTranaction> result = new ArrayList<AllowedTranaction>();
		result.addAll(createAllowedTransactionForALLBank());
		result.addAll(createAllowedTransactionForPasargadBank(getBank(502229)));
		return result;
	}
	
	private List<AllowedTranaction> createAllowedTransactionForALLBank() {
		List<AllowedTranaction> result = new ArrayList<AllowedTranaction>();
		Bank bank = null; 
//			PanPrefixTransactionPolicy.UNDEFINED_PREFIX;
		AllowedTranaction allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.BALANCEINQUIRY);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.BILLPAYMENT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PREPARE_BILL_PMT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PURCHASE);
		allowedTranaction.setBank(bin);
		allowedTranaction.setMinAmount(1000L);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/

		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PURCHASECHARGE);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.LASTPURCHASECHARGE);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);

		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.WITHDRAWAL);
		allowedTranaction.setMinAmount(1000L);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.RECONCILIATION);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);

		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CANCEL);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		return result;
	}

	
	private List<AllowedTranaction> createAllowedTransactionForNonPasargadBank(Bank bank) {
		List<AllowedTranaction> result = new ArrayList<AllowedTranaction>();
		AllowedTranaction allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.BALANCEINQUIRY);/*
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.BILLPAYMENT);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PREPARE_BILL_PMT);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PURCHASE);
		allowedTranaction.setBank(bank);
		allowedTranaction.setMinAmount(1000L);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);

		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PURCHASECHARGE);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.LASTPURCHASECHARGE);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/

		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.WITHDRAWAL);
		allowedTranaction.setMinAmount(1000L);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.RECONCILIATION);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);

		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CANCEL);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/
		
		return result;
	}

	
	private List<AllowedTranaction> createAllowedTransactionForPasargadBank(Bank bank) {
		List<AllowedTranaction> result = new ArrayList<AllowedTranaction>();
		AllowedTranaction allowedTranaction = new AllowedTranaction();
		/*allowedTranaction.setTrnType(TrnType.BALANCEINQUIRY);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PREPARE_BILL_PMT);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.BILLPAYMENT);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		*/
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PURCHASE);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.RETURN);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.PURCHASECHARGE);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.LASTPURCHASECHARGE);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		*/
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.WITHDRAWAL);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.DEPOSIT_CHECK_ACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.DEPOSIT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CHANGEPINBLOCK);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CHANGEINTERNETPINBLOCK);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.GETACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.BANKSTATEMENT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CANCEL);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.TRANSFER);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.INCREMENTALTRANSFER);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.DECREMENTALTRANSFER);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CHECKACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.TRANSFER_CARD_TO_ACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.INCREMENTALTRANSFER_CARD_TO_ACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.DECREMENTALTRANSFER_CARD_TO_ACCOUNT);
		allowedTranaction.setBank(bank);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);
		
		/*allowedTranaction = new AllowedTranaction();
		allowedTranaction.setTrnType(TrnType.RECONCILIATION);
		allowedTranaction.setBank(bin);
		result.add(allowedTranaction);
		getGeneralDao().saveOrUpdate(allowedTranaction);*/

		return result;
	}
	
	public void addPOSTerminalServicePolicy(AuthorizationProfile profile){
		if (profile == null)
			return;
		TerminalServicePolicy posPolicy = new TerminalServicePolicy();
		List<TrnType> alowedTypes = new ArrayList<TrnType>(){
			{
				add(TrnType.RETURN);
				add(TrnType.PURCHASE);
				add(TrnType.BALANCEINQUIRY);
				add(TrnType.BILLPAYMENT);
//				add(TrnType.PURCHASECHARGE);
//				add(TrnType.LASTPURCHASECHARGE);
			}
		};
		posPolicy.setAlowedTypes(alowedTypes);
		posPolicy.addProfile(profile);
		getGeneralDao().saveOrUpdate(posPolicy);
		getGeneralDao().saveOrUpdate(profile);
	}
	
	public void addEpayTerminalServicePolicy(AuthorizationProfile profile){
		if (profile == null)
			return;
		
		TerminalServicePolicy epayPolicy = new TerminalServicePolicy();
		List<TrnType> alowedTypes = new ArrayList<TrnType>(){
			{
				add(TrnType.RETURN);
				add(TrnType.CHECKACCOUNT);
				add(TrnType.PURCHASE);
				add(TrnType.BALANCEINQUIRY);
				add(TrnType.BILLPAYMENT);
//				add(TrnType.PURCHASECHARGE);
//				add(TrnType.CHANGEPINBLOCK);
				add(TrnType.CHANGEINTERNETPINBLOCK);
				add(TrnType.TRANSFER);
			}
		};
		epayPolicy.setAlowedTypes(alowedTypes);
		epayPolicy.addProfile(profile);
		getGeneralDao().saveOrUpdate(epayPolicy);
		getGeneralDao().saveOrUpdate(profile);
	}
	
	public void addATMTerminalServicePolicy(AuthorizationProfile profile){
		if (profile == null)
			return;
		
		TerminalServicePolicy epayPolicy = new TerminalServicePolicy();
		List<TrnType> alowedTypes = new ArrayList<TrnType>(){
			{
				add(TrnType.WITHDRAWAL);
				add(TrnType.CHECKACCOUNT);
				add(TrnType.BALANCEINQUIRY);
				add(TrnType.BILLPAYMENT);
				add(TrnType.CHANGEPINBLOCK);
				add(TrnType.CHANGEINTERNETPINBLOCK);
				add(TrnType.TRANSFER);
				add(TrnType.BANKSTATEMENT);
				add(TrnType.TRANSFER_CARD_TO_ACCOUNT);
				add(TrnType.CREDITCARDDATA);
				add(TrnType.CHECKACCOUNT_CARD_TO_ACCOUNT);
				add(TrnType.DEPOSIT_CHECK_ACCOUNT);
				add(TrnType.PREPARE_BILL_PMT);
				add(TrnType.CREDITCARDDATA);
			}
		};
		epayPolicy.setAlowedTypes(alowedTypes);
		epayPolicy.addProfile(profile);
		getGeneralDao().saveOrUpdate(epayPolicy);
		getGeneralDao().saveOrUpdate(profile);
	}

	private GeneralDao getGeneralDao() {
		return GeneralDao.Instance;
	}
	
	private Bank getBank(Integer id){
		return getGeneralDao().load(Bank.class, id);
	}
	
	private List<Bank> getBanks(){
		return getGeneralDao().find("from "+ Bank.class.getName());
	}

	private static AuthorizationProfile getAuthorizationProfile(final Long id){
		return (AuthorizationProfile) GeneralDao.Instance.findObject("from AuthorizationProfile where id = :id",new HashMap<String, Object>(){{put("id", id);}});
	}
}

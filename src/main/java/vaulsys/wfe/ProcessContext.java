package vaulsys.wfe;

import vaulsys.authorization.impl.AuthorizationProfile;
import vaulsys.authorization.policy.Bank;
import vaulsys.authorization.policy.Policy;
import vaulsys.clearing.base.ClearingProfile;
import vaulsys.cms.base.CMSProduct;
import vaulsys.cms.base.CMSProductDetail;
import vaulsys.cms.base.CMSProductKeys;
import vaulsys.cms.base.CMSTrack2Format;
import vaulsys.customer.Currency;
import vaulsys.discount.BaseDiscount;
import vaulsys.discount.DiscountProfile;
import vaulsys.entity.impl.Institution;
import vaulsys.fee.impl.BaseFee;
import vaulsys.lottery.LotteryAssignmentPolicy;
import vaulsys.message.Message;
import vaulsys.mtn.impl.GeneralChargeAssignmentPolicy;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.ProtocolType;
import vaulsys.protocols.encoding.EncodingConvertor;
import vaulsys.routing.base.RoutingTable;
import vaulsys.security.base.SecurityFunction;
import vaulsys.security.securekey.SecureKey;
import vaulsys.terminal.SwitchTerminalType;
import vaulsys.terminal.atm.ATMConfiguration;
import vaulsys.terminal.atm.ATMRequest;
import vaulsys.terminal.impl.SwitchTerminal;
import vaulsys.terminal.impl.Terminal;
import vaulsys.transaction.Transaction;
import vaulsys.user.User;
import vaulsys.util.Pair;
import org.apache.log4j.Logger;
import org.apache.mina.core.session.IoSession;

import java.util.*;

public class ProcessContext {
	private static final ThreadLocal<ProcessContext> current = new ThreadLocal<ProcessContext>();
	
	private Logger logger = Logger.getLogger(this.getClass());
	
	public static ProcessContext get() {
		if ( current.get() == null) {
			current.set(new ProcessContext());
		}
		return current.get();
	}
	
	public static void remove(){
		current.remove();
	}
	
	public void init() {
		current.set(this);
		securityFunctions = GlobalContext.getInstance().getAllSecurityFunctions();
		currencies = GlobalContext.getInstance().getAllCurrencies();
		banks = GlobalContext.getInstance().getAllBanks();
		routingTables = GlobalContext.getInstance().getAllRoutingTables(); 
		cellPhoneChargeSpecs = GlobalContext.getInstance().getAllCellPhoneChargeSpecification();
		generalChargePolicy = GlobalContext.getInstance().getGeneralChargePolicy();
		protocolConfig = GlobalContext.getInstance().getAllProtocolConfig();
		encodingConvertors = GlobalContext.getInstance().getAllConvertors();
		switchUser = GlobalContext.getInstance().getSwitchUser();
		authorizationProfiles = GlobalContext.getInstance().getAllAuthorizationProfiles();
		atmConfigurations = GlobalContext.getInstance().getAllATMConfigurations();
		atmRequests = GlobalContext.getInstance().getAllATMRequests();
		channels = GlobalContext.getInstance().getAllChannels();
		feeProfiles = GlobalContext.getInstance().getAllFeeProfiles();
		discountProfiles = GlobalContext.getInstance().getAllDiscountProfiles();
		institutions = GlobalContext.getInstance().getAllInstitutions();
		myInstitution = GlobalContext.getInstance().getMyInstitution();
		myInstitutions = GlobalContext.getInstance().getMyInstitutions();
		peerInstitutions = GlobalContext.getInstance().getPeerInstitutions();
		peerInstitutionsBin = GlobalContext.getInstance().getPeerInstitutionsBin();
		switchTerminals = GlobalContext.getInstance().getAllSwitchTerminals();
		clearingProfile = GlobalContext.getInstance().getAllClearingProfile();
		lotteryAssignmentPolicy = GlobalContext.getInstance().getAllLotteryAssignmentPolicy();
//		merchantCategory = GlobalContext.getInstance().getAllMerchantCategory();
//		logger.debug(merchantCategory);
		/* m.rehman: Get message routing map from Global Context */
        messageRouting = GlobalContext.getInstance().getAllMessageRouting();
		cmsProducts = GlobalContext.getInstance().getAllCMSProducts();
		cmsProductDetail = GlobalContext.getInstance().getAllCMSProductDetail();
		cmsTrack2Format = GlobalContext.getInstance().getAllCMSTrack2Formats();
		cmsProductKeys = GlobalContext.getInstance().getAllCMSProductKeys();
	}
	
//    static final String ORIGINATOR_TERMINAL_KEY = "originatorTerminal";
//    static final String TRANSACTION_KEY = "transaction";
//    static final String TRANSACTION_MANAGER_KEY = "db-trx-mgr";
//    static final String EXCEPTIONS_KEY = "exceptions";
//    static final String PENDING_REQUESTS_KEY = "pendingRequests";
//    static final String PENDING_RESPONSE_KEY = "pendingResponse";
//    static final String NEXT_STATE_KEY = "next-state";
//    static final String LAST_ACQ_MAC_KEY = "lastAcqMacKey";
//    static final String REVERSAL_MSGS = "reversal-msgs";
//    static final String SESSION_KEY = "session";
//    static final String INPUT_MESSAGE_KEY = "input-msg";
//    static final String OUTPUT_MESSAGE_KEY = "output-msg";

//    public static final String TO_EXCEPTION_TRANSITION = "to Exception";
    public static final String TO_ENDSTATE_TRANSITION = "to End";
//    public static final String TO_NEXT_TRANSITION = "next";
    
       
//    private Map<String, Object> variables;
    private Map<Long, Map<String, SecurityFunction>> securityFunctions;

    private Map<Integer, Currency> currencies;
    private Map<Integer, Bank> banks;
    private Map<String, RoutingTable> routingTables;
    private Map<Long, Map<Long, Long>> cellPhoneChargeSpecs;
    private GeneralChargeAssignmentPolicy generalChargePolicy;
    private Map<ProtocolType, Map<String, String>> protocolConfig;
    private Map<String, EncodingConvertor> encodingConvertors;
    private User switchUser;
    private Map<Long, ATMConfiguration> atmConfigurations;
    private Map<Long, Map<String, ATMRequest>> atmRequests;
    
    private Map<Long, Pair<AuthorizationProfile, List<Policy>>> authorizationProfiles;
    private Map<String, Channel> channels;
    private Map<Long, List<BaseFee>> feeProfiles;
    private Map<Long, Pair<DiscountProfile, List<BaseDiscount>>> discountProfiles;
    
    private Map<String, Institution> institutions;
    private Institution myInstitution;
    private Map<Long,Institution> myInstitutions;
	private List<Institution> peerInstitutions;
	private List<Long> peerInstitutionsBin;
	private Map<Long, SwitchTerminal> switchTerminals;
	private Map<Long,ClearingProfile> clearingProfile;
	private Map<Integer,LotteryAssignmentPolicy> lotteryAssignmentPolicy;
	
	private Transaction transactions;
    private Terminal originatorTerminal;
    private Set<Message> pendingRequest;
    private Set<Message> pendingResponse;
    private Message outputMessage;
    private String nextState;
    private SecureKey LastAcqMacKey;
    private IoSession IOSession;
//    private List<ScheduleMessage> reversalMessage;
    private List<Exception> exceptions;
    private Message inputMessage;
    private Object outputChannel;  
    private List<Long> cardGroupHierarchy; 
    /* m.rehman: Message Routing Object */
    private Map<String, String> messageRouting;
	private Map<String, CMSProduct> cmsProducts;
	private Map<String, CMSProductDetail> cmsProductDetail;
	private Map<String, CMSTrack2Format> cmsTrack2Format;
	private Map<String, List<CMSProductKeys>> cmsProductKeys;
    
//    private SecureKey secureKey;
    
//	private Map<Long,MerchantCategory> merchantCategory;
	
    public ProcessContext() {
    //	variables = new ConcurrentHashMap<String, Object>();
    	
    	initializer();
    	


	}

	public ProcessContext(boolean isWebService) { //Raza for NayaPay
		//	variables = new ConcurrentHashMap<String, Object>();
		if(isWebService)
		{
			exceptions = new ArrayList<Exception>();
		}
		else {
			initializer();
		}
	}
    
    public void initializer(){
    	exceptions = new ArrayList<Exception>();
    	pendingRequest = new HashSet<Message>();
    	pendingResponse = new HashSet<Message>();
    }

//    public Object getChannel2(Object channel){
//    	
//    	return outputChannel.get(channel);
//    }
//    public void setChannel(String key,Object channel){
//    	if(outputChannel == null){
//    		outputChannel = new HashMap<String, Object>();
//    	}
//    	outputChannel.put(key, channel);
//    }

    public List<Long> getCardGroupHierarchy() {
	return cardGroupHierarchy;
    }
    
    public void setCardGroupHierarchy(List<Long> cardGroupHierarchy) {
	this.cardGroupHierarchy = cardGroupHierarchy;
    }
    
    public void setOutputChannel(Object channel){
    	outputChannel = channel;    	
    }
    
    public Object getOutputChannel(Object channel){
    	if(channel == "SELF")
    		return null;
    	else
    		return outputChannel;
    }
    
        
    public Transaction getTransaction() {
//        Transaction transaction = (Transaction) getVariable(TRANSACTION_KEY);
//        return transaction;
    	return transactions;
    }

    public void setTransaction(Transaction transaction) {
//        setVariable(TRANSACTION_KEY, transaction);
    	transactions = transaction;
    }
    

    public Terminal getOriginatorTerminal() {
//    	Terminal terminal = (Terminal) getVariable(ORIGINATOR_TERMINAL_KEY);
//    	return terminal;
    	return originatorTerminal;
    }

    public void setOriginatorTerminal(Terminal terminal) {
//    	setVariable(ORIGINATOR_TERMINAL_KEY, terminal);
    	originatorTerminal = terminal;
    }

    @SuppressWarnings("unchecked")
    public Set<Message> getPendingRequests() {
//        Set<Message> pendingMessages = (Set<Message>) getVariable(PENDING_REQUESTS_KEY);
//        return pendingMessages;
    	if(pendingRequest == null)
    		pendingRequest = new HashSet<Message>();
    	return pendingRequest;
    }

    public void setPendingRequests(Set<Message> pendingMessages) {
//        setVariable(PENDING_REQUESTS_KEY, pendingMessages);
    	pendingRequest.addAll(pendingMessages);
    }

    @SuppressWarnings("unchecked")
    public Set<Message> getPendingResponses() {
//    	Set<Message> pendingMessages = (Set<Message>) getVariable(PENDING_RESPONSE_KEY);
//    	return pendingMessages;
    	return pendingResponse;
    }

    public void setPendingResponses(Set<Message> pendingMessages) {
//    	setVariable(PENDING_RESPONSE_KEY, pendingMessages);
    	pendingResponse.addAll(pendingMessages);
    }

//    public void setInputMessage(Message inputMessage) {
//        getTransaction().setInputMessage(inputMessage);
//        
//    }

    public Message getInputMessage() {
//    	Message inputMsg = (Message) getVariable(INPUT_MESSAGE_KEY);
//    	if(inputMsg == null){
//    		inputMsg = getTransaction().getInputMessage();
//        	if(inputMsg != null){
//        		setVariable(INPUT_MESSAGE_KEY, inputMsg);
//        	}
//    	}
//        return inputMsg;
    	Message inputMsg = inputMessage;
    	if(inputMsg == null){
    		inputMsg = getTransaction().getInputMessage();
    		if (inputMsg != null){
    			inputMessage = inputMsg;
    		}
    	}
    	return inputMsg;
    	
    }

    public Message getOutputMessage() {
////        return getTransaction().getOutputMessage();

//    	Message outputMsg = (Message) getVariable(OUTPUT_MESSAGE_KEY);
//    	if(outputMsg == null){
//    		outputMsg = getTransaction().getOutputMessage();
//        	if(outputMsg != null){
//        		setVariable(OUTPUT_MESSAGE_KEY, outputMsg);
//        	}
//        }
//        return outputMsg;
    	
    	if(outputMessage == null){
    		outputMessage = getTransaction().getOutputMessage();
    		if(outputMessage != null){
    			setOutputMessage(outputMessage);
    		}
    	}
    	return outputMessage;
    }

    public void setOutputMessage(Message outputMsg) {
    	if(outputMsg != null){
//    		setVariable(OUTPUT_MESSAGE_KEY, outputMsg);
    		outputMessage = outputMsg;
    	} /*else {
    		
    		
    		if (variables != null && variables.size() > 0) {
        		if (variables.containsKey(OUTPUT_MESSAGE_KEY))
        			variables.remove(OUTPUT_MESSAGE_KEY);
        	}
    	}*/
    }

    public void addOutputMessage(Message outputMessage) {
        getTransaction().addOutputMessage(outputMessage);
    }

    public void removeAllOutputMessage() {
        getTransaction().removeAllOutputMessages();
    }

    @SuppressWarnings("unchecked")
    public List<Exception> getExceptions() {
    	
    	
//    	 public void setVariable(String key, Object value) {
//           variables.put(key, value);
//       }

//       public Object getVariable(String key) {
//           return variables.get(key);
//       }
    	
    	
//        List<Exception> exceptions = (List<Exception>) getVariable(EXCEPTIONS_KEY);
//        if (exceptions == null) {
//            exceptions = new ArrayList<Exception>();
//            setVariable(EXCEPTIONS_KEY, exceptions);
//        }
//        return exceptions;
//    	
    	List<Exception> exception = exceptions;
    	if (exception == null){
    		exception = new ArrayList<Exception>();
    		if(exceptions == null){
    			exceptions = new ArrayList<Exception>();

    		}
    		exceptions.addAll(exception);
    	}
    	return exception;
    }

    public void addException(Exception ex) {
        getExceptions().add(ex);
    }

    public void addPendingRequests(Message requestMessage) {
//        Set<Long> pendingRequests = getPendingRequests();
//        if (pendingRequests == null)
//            pendingRequests = new HashSet<Long>();
//        pendingRequests.add(requestMessageId);
//        setPendingRequests(pendingRequests);
    	if (requestMessage == null)
    		return;

        Set<Message> pendingRequests = getPendingRequests();
        if (pendingRequests == null)
            pendingRequests = new HashSet<Message>();
        pendingRequests.add(requestMessage);
        setPendingRequests(pendingRequests);
    }

    public void addPendingRequests(Collection<Message> requestMessages) {
    	if (requestMessages == null)
    		return;
    	
    	Set<Message> pendingRequests = getPendingRequests();
    	if (pendingRequests == null)
    		pendingRequests = new HashSet<Message>();
    	pendingRequests.addAll(requestMessages);
    	setPendingRequests(pendingRequests);
    }
    
    public void addPendingResponses(Message responseMessageId) {
    	Set<Message> pendingResponses = getPendingResponses();
    	if (pendingResponses == null)
    		pendingResponses = new HashSet<Message>();
    	pendingResponses.add(responseMessageId);
    	setPendingResponses(pendingResponses);
    }

	public void setNextState(String stateName) {
//		this.setVariable(ProcessContext.NEXT_STATE_KEY, stateName);
		this.nextState = stateName;
	}

	public String getNextState() {
//		return (String) this.getVariable(ProcessContext.NEXT_STATE_KEY);
		return this.nextState;
	}

	public void setLastAcqMacKey(SecureKey key) {
//		this.setVariable(ProcessContext.LAST_ACQ_MAC_KEY, key);
		this.LastAcqMacKey = key;
	}

	public SecureKey getLastAcqMacKey() {
//		return (SecureKey) this.getVariable(ProcessContext.LAST_ACQ_MAC_KEY);
		return this.LastAcqMacKey;
	}

	public boolean isNextStateToEnd() {
   		return this.getNextState() != null && this.getNextState().equals(ProcessContext.TO_ENDSTATE_TRANSITION);
	}

	 public IoSession getSession() {
//		IoSession session = (IoSession) getVariable(SESSION_KEY);
//		return session;
		 return IOSession;
	}

	public void setSession(IoSession session) {
//		setVariable(SESSION_KEY, session);
		IOSession= session;






	}
	
//	public void addReversalMsg(ScheduleMessage msg){
////    	List<ScheduleMessage> revMsgs = (List<ScheduleMessage>) getVariable(REVERSAL_MSGS);
////    	if(revMsgs == null){
////    		revMsgs = new ArrayList<ScheduleMessage>();
////    		setVariable(REVERSAL_MSGS, revMsgs);
////    	}
////
////		revMsgs.add(msg);
//		List<ScheduleMessage> revMsgs = new ArrayList<ScheduleMessage>();
//		revMsgs.addAll(reversalMessage);
//		if(revMsgs == null){
//			reversalMessage.addAll(revMsgs);
//		}
//		revMsgs.add(msg);
//		
//	}

//	public List<ScheduleMessage> getReversalMsgs(){
////		return (List<ScheduleMessage>) getVariable(REVERSAL_MSGS);
//		return reversalMessage;
//	}

	public SecurityFunction getSecurityFunction(Long profileId, String functionName) {
		return securityFunctions.get(profileId).get(functionName);
	}
	
	public Currency getCurrency(Integer code) {
		return currencies.get(code);
	}
	
	public Currency getRialCurrency() {
		return currencies.get(GlobalContext.RIAL_CURRENCY_CODE);
	}
	
	
	public Bank getBank(Integer bin){
		return banks.get(bin);
	}
	
//	public MerchantCategory getMerchantCategory(Long code){
//		return merchantCategory.get(code);
//	}
	public ClearingProfile getClearingProfile(Long code){
		return clearingProfile.get(code);
	}
	
	public LotteryAssignmentPolicy getLotteryAssignmentPolicy(Integer id){
		return lotteryAssignmentPolicy.get(id);
	}
	
	public RoutingTable getRoutingTable(String routingTableName) {
		return routingTables.get(routingTableName);
	}
	
	public Long getMTNChargeTax(Long credit, Long companyCode) {
		return cellPhoneChargeSpecs.get(companyCode).get(credit);
	}
	
	public GeneralChargeAssignmentPolicy getGeneralChargePolicy(){
		return generalChargePolicy;
	}
	
	public String getProtocolConfig(ProtocolType protocolType, String key) {
		return protocolConfig.get(protocolType).get(key);
	}
	
	public List<Policy> getAllAuthorizationPolicies(Long profileId) {
		return authorizationProfiles.get(profileId).second;
	}	
	
	public AuthorizationProfile getAuthorizationProfile(Long profileId) {
		return authorizationProfiles.get(profileId).first;
	}	
	
	@SuppressWarnings("unchecked")
	public EncodingConvertor getConvertor(String name){
		return encodingConvertors.get(name);
	}
	public User getSwitchUser(){
		return switchUser;
	}
	public ATMConfiguration getATMConfiguration(Long configId){
		return atmConfigurations.get(configId);
	}
	public ATMRequest getATMRequest(Long configId, String opkey){
		Map<String, ATMRequest> request = atmRequests.get(configId);
		if (request != null)
			return request.get(opkey);
		return null;
	}

	public Channel getChannel(String channelName) {
		return channels.get(channelName);
	}

	public String getChannelIdbyName(String channelName) { //Raza Get Channel Id by Name - TransactionImportFromLog
		return channels.get(channelName).getId().toString();
	}

	@SuppressWarnings("unchecked")
	public List<BaseFee> getTransactionFees(Long profileId) {
		return feeProfiles.get(profileId);
	}
	
	public DiscountProfile getDiscountProfile(Long profileId) {
		if(profileId != null){
			return discountProfiles.get(profileId).first;
		}
		return null;
	}
	
	public List<BaseDiscount> getBaseDiscounts(Long profileId) {
		return discountProfiles.get(profileId).second;
	}
	
	public Institution getInstitution(String code) {

		/*for(Map.Entry entry : institutions.entrySet()) //Raza TEMP
		{
			System.out.println("Institution [" + entry.getKey() + ", Value [" + entry.getValue() + "]");
		}*/
		//System.out.println("Institution [" + institutions.get("9015") + "]");
		return institutions.get(code);
	}
	
	public Institution getMyInstitution() {
		return myInstitution;
	}
	public Map<Long,Institution> getMyInstitutions() { //Raza Adding for MultiInstitution
		return myInstitutions;
	}
	public Boolean isMyInstitution(long bin) { //Raza Adding for MultiInstitution
		if(myInstitutions == null || myInstitutions.isEmpty())
			return false;
		else
			return myInstitutions.containsKey(bin);
	}
	public Institution getMyInstitutionByBIN(long bin) { //Raza Adding for MultiInstitution

		if(myInstitutions == null || myInstitutions.isEmpty() || !myInstitutions.containsKey(bin))
			return  null;
		else
			return myInstitutions.get(bin);
	}

	public List<Institution> getPeerInstitutions(){
		return peerInstitutions;
	}

	public Boolean isPeerInstitution(Long bin){
		if (peerInstitutionsBin == null || peerInstitutionsBin.isEmpty())
			return false;
		else
			return peerInstitutionsBin.contains(bin);
	}
	//TODO: improve performance
	public Institution getInstitutionByBIN(Long bin) {
		Collection<Institution> inst = institutions.values();
		for (Institution i : inst)
			if (i.getBin().equals(bin))
				return i;

		return null;
	}
	public List<Institution> getPeerInstitutionsBin() {
		return peerInstitutions;
	}

	public Map<String, Institution> getAllInstitutions() {
		return institutions;
	}
	
	public Collection<SwitchTerminal> getAllSwitchTerminals() {
		if (switchTerminals == null)
			return null;
		return switchTerminals.values();
	}

	public SwitchTerminal getSwitchTerminal(Long code) {
		if (switchTerminals == null)
			return null;
		return switchTerminals.get(code);
	}
	
	public SwitchTerminal getIssuerSwitchTerminal(String institutionCode) {
		Institution institution = institutions.get(institutionCode);
		for (SwitchTerminal t : institution.getTerminals()) {
			SwitchTerminal terminal = (SwitchTerminal) t;
			if (SwitchTerminalType.ISSUER.equals(terminal.getType()))
				return terminal;
		}
		return null;
	}
	public SwitchTerminal getAcquierSwitchTerminal(String institutionCode) {
		Institution institution = institutions.get(institutionCode);
		for (SwitchTerminal t : institution.getTerminals()) {
			SwitchTerminal terminal = (SwitchTerminal) t;
			if (SwitchTerminalType.ACQUIER.equals(terminal.getType()))
				return terminal;
		}
		return null;
	}

	public SwitchTerminal getIssuerSwitchTerminal(Institution institution) {
		return getIssuerSwitchTerminal(""+institution.getCode());
	}

	public SwitchTerminal getAcquireSwitchTerminal(Institution institution) {
		return getAcquierSwitchTerminal(""+institution.getCode());
	}

    public String getMessageRoutingDestination(String key) {
        if (messageRouting.containsKey(key)) {
            return messageRouting.get(key);
        } else {
            return null;
        }
    }

	//m.rehmam
	public CMSProduct getCMSProduct(String key) {
		if (cmsProducts.containsKey(key)) {
			return cmsProducts.get(key);
		} else {
			return null;
		}
	}

	public CMSProductDetail getCMSProductDetail(String key) {
		if (cmsProductDetail.containsKey(key)) {
			return cmsProductDetail.get(key);
		} else {
			return null;
		}
	}

	public CMSTrack2Format getCMSTrack2Format(String key) {
		if (cmsTrack2Format.containsKey(key)) {
			return cmsTrack2Format.get(key);
		} else {
			return null;
		}
	}

	public List<CMSProductKeys> getCMSProductKeys(String key) {
		if (cmsProductKeys.containsKey(key)) {
			return cmsProductKeys.get(key);
		} else {
			return null;
		}
	}
}


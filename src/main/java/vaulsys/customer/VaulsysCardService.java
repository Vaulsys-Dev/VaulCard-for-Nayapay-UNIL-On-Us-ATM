package vaulsys.customer;

import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.authorization.exception.card.CardNotAllowedException;
import vaulsys.authorization.exception.card.CardNotFoundException;
import vaulsys.authorization.exception.card.DuplicateCardException;
import vaulsys.authorization.exception.card.DuplicateCardGroupException;
import vaulsys.authorization.exception.card.NoCardGroupFoundException;
import vaulsys.persistence.CMSGeneralDao;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.thoughtworks.xstream.XStream;

public class VaulsysCardService {

	private static final Logger logger = Logger.getLogger(VaulsysCardService.class);

    
	public static void authorizeCardTerminalPair(String pan, Long cardGroupId) throws CardAuthorizerException {
		
    	List<Long> cardGroupHierarchy = getCardGroupHierarchy(pan);
//    	logger.info("Cardgroup hierarchy size:" + cardGroupHierarchy.size());
		
		if(cardGroupHierarchy == null || cardGroupHierarchy.size() == 0){
			throw new CardNotAllowedException();
		}

		if (!cardGroupHierarchy.contains(cardGroupId)){
    		logger.warn("CardGroupRestrictionException! pan: "+ pan+", cardGroupId: "+ cardGroupId);
			throw new CardNotAllowedException();
    	}
	}

	public static void authorizeCardTerminalPair(String pan, List<Long> cardGroupHierarchy, Long cardGroupId) throws CardAuthorizerException {
		if(cardGroupHierarchy == null || cardGroupHierarchy.size() == 0){
			throw new CardNotAllowedException();
		}
		
    	if (!cardGroupHierarchy.contains(cardGroupId)){
    		logger.warn("CardGroupRestrictionException! pan: "+ pan+", cardGroupId: "+ cardGroupId);
			throw new CardNotAllowedException();
    	}
	}
	
    public static List<Long> getCardGroupHierarchy(String pan) throws NoCardGroupFoundException {
//    	logger.debug("Getting cardGroupHierarchy");
	List<Long> groupHierarchy = ProcessContext.get().getCardGroupHierarchy();
	if (groupHierarchy != null && !groupHierarchy.isEmpty())
		return groupHierarchy;
        
		groupHierarchy = new ArrayList<Long>();
    	
    	ArrayList<String> paramNames = new ArrayList<String>();
        ArrayList<String> paramValues = new ArrayList<String>();
        paramNames.add("userName");
        paramValues.add(ConfigUtil.getProperty(ConfigUtil.CMS_SERVICE_USERNAME));
        paramNames.add("password");
        paramValues.add(ConfigUtil.getProperty(ConfigUtil.CMS_SERVICE_PASSWORD));
        paramNames.add("serviceName");
        paramValues.add(ConfigUtil.getProperty(ConfigUtil.CMS_SERVICE_CARDGROUPHIERARCHY));
        paramNames.add("cardPAN");
        paramValues.add(pan);
        
        try {
        	//Mirkamali(Task159)
        	if(GlobalContext.CMS_THREAD_NUMBER.incrementAndGet() > ConfigUtil.getInteger(ConfigUtil.CMS_MAX_THREAD)) {
        		logger.error("Max number of  threads waiting for cms service rsponse is: " + ConfigUtil.getInteger(ConfigUtil.CMS_MAX_THREAD)
        				+ ", Number of  threads waiting for cms service in globalContext is: " + GlobalContext.CMS_THREAD_NUMBER);
        		throw new NoCardGroupFoundException();
        	}else {
        		String response = vaulsys.customer.MyHttpPost.postHttpRequest(ConfigUtil.getProperty(ConfigUtil.CMS_SERVICE_URL), paramNames, paramValues);
    	        logger.debug("response from CMS = " + response);
    	        logger.debug("CMSNumberOfWaitedThread:" + GlobalContext.CMS_THREAD_NUMBER);
    	        XStream xStream = new XStream();
    	        response = response.replace("com.fanap.cms.service.valueobjects.CardGroupHierarchyVO", "vaulsys.customer.CardGroupHierarchyVO");
    	        CardGroupHierarchyVO hierarchyVO = (CardGroupHierarchyVO) xStream.fromXML(response);
    	        groupHierarchy = hierarchyVO.getCardGroups();
    	        ProcessContext.get().setCardGroupHierarchy(groupHierarchy);
    	        
    	        GlobalContext.CMS_THREAD_NUMBER.decrementAndGet();
    	        
        	}
	    } catch (Exception e) {
	    	GlobalContext.CMS_THREAD_NUMBER.decrementAndGet();
	    	logger.error(e);
        }
	        
//        try {
//            Long groupId = getGroupId(pan);
//            groupHierarchy.add(groupId);
//
//            Long parent = groupId;
//            while ((parent = getParentOf(parent)) != null && !groupHierarchy.contains(parent)){
//                groupHierarchy.add(parent);
//                groupId = parent;
//                if(parent == 10357L )
//                	break;
//            }
//
//        } catch (Exception e) {
//            logger.error(e);
//        }
        
        return groupHierarchy;
    }
    
    public static void main(String[] args) throws Exception {
		VaulsysCardService fnp = new VaulsysCardService();
		List<Long> list = fnp.getCardGroupHierarchy("5022291002864005");
		for (Long id : list) {
			System.out.println("id == " + id);	
		}
	}
	
	
/*    public static List<Long> getCardGroupHierarchy(String pan) throws NoCardGroupFoundException {
//    	logger.debug("Getting cardGroupHierarchy");
        List<Long> groupHierarchy = new ArrayList<Long>();
        try {
            Long groupId = getGroupId(pan);
            groupHierarchy.add(groupId);

            Long parent = groupId;
            while ((parent = getParentOf(parent)) != null && !groupHierarchy.contains(parent)){
                groupHierarchy.add(parent);
                groupId = parent;
                if(parent == 10357L )
                	break;
            }

        } catch (Exception e) {
            logger.error(e);
        }
        return groupHierarchy;
    }
*/
    private static Long getGroupId(String pan) throws CardNotFoundException, DuplicateCardException {
//    	logger.debug("Getting cardGroupHierarchy: "+ pan);
        String queryString = "select C_GROUP from cms.v_card where C_CARDPAN='" + pan+"'";
        List<BigDecimal> out = CMSGeneralDao.Instance.executeSqlQuery(queryString);

        if (out.size() == 0){
        	logger.error("CardNotFound! "+ pan);
            throw new CardNotFoundException();
        }

        if (out.size() != 1){
        	logger.error("DuplicateCard! "+ pan);
            throw new DuplicateCardException();
        }
        return out.get(0).longValue();
    }

    private static Long getParentOf(Long groupId) throws DuplicateCardGroupException {
        String queryString = "select C_PARENT from cms.v_cardgroup where ID=" + groupId;
        List<BigDecimal> out = CMSGeneralDao.Instance.executeSqlQuery(queryString);

        if (out.size() == 0)
            return null;

        if (out.size() != 1){
        	logger.error("DuplicateCardGroup! groupId=" + groupId);
            throw new DuplicateCardGroupException();
        }
        if (out.get(0)== null)
        	return null;
        
        return out.get(0).longValue();
    }
}

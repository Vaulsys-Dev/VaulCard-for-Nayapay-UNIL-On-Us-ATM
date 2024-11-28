package vaulsys.customer;

//import garbage.vaulsys.authorization.policy.CardGroupCityRestrictionPolicy;
//import garbage.vaulsys.authorization.policy.CardGroupFineGroupRestrictionPolicy;
//import garbage.vaulsys.authorization.policy.CardGroupMerchCategoryRestrictionPolicy;
//import garbage.vaulsys.authorization.policy.CardGroupTerminalRestrictionPolicy;
import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.authorization.exception.card.CardNotAllowedException;
import vaulsys.authorization.exception.card.CardNotFoundException;
import vaulsys.authorization.exception.card.DuplicateCardException;
import vaulsys.authorization.exception.card.DuplicateCardGroupException;
import vaulsys.authorization.exception.card.NoCardGroupFoundException;
import vaulsys.authorization.policy.AllowedCard;
import vaulsys.entity.impl.FinancialEntityGroup;
import vaulsys.persistence.NeginGeneralDao;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.TerminalGroup;
import vaulsys.util.NotUsed;
import vaulsys.util.Util;
import vaulsys.wfe.ProcessContext;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CardService {
	private static final Logger logger = Logger.getLogger(CardService.class);

	public static void authorizeCardTerminalPair(String pan, Long cardGroupId) throws CardAuthorizerException {
		NeginCardData cd = extractCardData(pan);
    	List<Long> cardGroupHierarchy = getCardGroupHierarchy(cd);
    	logger.info("Cardgroup hierarchy size:" + cardGroupHierarchy.size());
		
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

    public static NeginCardData extractCardData(String pan) {
        // 639347 1 1 0417420    1
        int ciin, crno, cdsqno, cfcifno;
        ciin = Integer.parseInt(pan.substring(0, 6));
        crno = Integer.parseInt(pan.substring(6, 7));
        cdsqno = Integer.parseInt(pan.substring(7, 8));
        cfcifno = Integer.parseInt(pan.substring(8, 15));
        return new NeginCardData(ciin, crno, cdsqno, cfcifno);
    }

    public static List<Long> getCardGroupHierarchy(NeginCardData cardData) throws NoCardGroupFoundException {
        // Recursively query card and cardgrp and cardsubgrp to find the hierarchy
        // Throw exception i
    	logger.debug("Getting cardGroupHierarchy");
        List<Long> groupHierarchy = new ArrayList<Long>();
        try {
            Long groupId = getGroupId(cardData);
            groupHierarchy.add(groupId);

            Long parent;
            while ((parent = getParentOf(groupId)) != null && !groupHierarchy.contains(parent)){
                groupHierarchy.add(parent);
                groupId = parent;
                if(parent == 1L )
                	break;
            }

        } catch (Exception e) {
            logger.error(e);
        }
        return groupHierarchy;
    }

    private static Long getGroupId(NeginCardData cardData) throws CardNotFoundException, DuplicateCardException {
    	logger.debug("Getting cardGroupHierarchy: "+ cardData.CRNO);
        String queryString = "select GNUM from kccards where CIIN=" + cardData.CIIN + " and CRNO=" + cardData.CRNO + " and CDSQNO=" + cardData.CDSQNO + " and CFCIFNO=" + cardData.CFCIFNO;

        List<BigDecimal> out = new ArrayList<BigDecimal>();
        try {
        	out = NeginGeneralDao.Instance.executeSqlQuery(queryString);
        } catch(Exception e) {
        	logger.error("connecting to negin time out!");
        	throw new CardNotFoundException();
        }

        if (out.size() == 0){
        	logger.error("CardNotFound! CIIN=" + cardData.CIIN + " and CRNO=" + cardData.CRNO + " and CDSQNO=" + cardData.CDSQNO + " and CFCIFNO=" + cardData.CFCIFNO);
            throw new CardNotFoundException();
        }

        if (out.size() != 1){
        	logger.error("DuplicateCard! CIIN=" + cardData.CIIN + " and CRNO=" + cardData.CRNO + " and CDSQNO=" + cardData.CDSQNO + " and CFCIFNO=" + cardData.CFCIFNO);
            throw new DuplicateCardException();
        }
        return out.get(0).longValue();
    }

    private static Long getParentOf(Long groupId) throws CardNotFoundException, DuplicateCardGroupException {
        String queryString = "select KCC_GNUM from kcsubgrp where GNUM=" + groupId;
        List<BigDecimal> out = new ArrayList<BigDecimal>();
        try {
        	out = NeginGeneralDao.Instance.executeSqlQuery(queryString);
        } catch(Exception e) {
        	logger.error("connecting to negin time out!");
        	throw new CardNotFoundException();
        }

        if (out.size() == 0)
            return null;

        if (out.size() != 1){
        	logger.error("DuplicateCardGroup! GNUM=" + groupId);
            throw new DuplicateCardGroupException();
        }

        return out.get(0).longValue();
    }

    @NotUsed
    private boolean hasContainTerminalGroup(List<TerminalGroup> terminalGroups, TerminalGroup terminalGroup) {
        for (TerminalGroup group : terminalGroups) {
            if (group.getId().equals(terminalGroup.getId()))
                return true;
        }
        return false;
    }

    @NotUsed
    private boolean hasContainEntityGroup(List<FinancialEntityGroup> entityGroups, FinancialEntityGroup entityGroup) {
        for (FinancialEntityGroup group : entityGroups) {
            if (group.getId().equals(entityGroup.getId()))
                return true;
        }
        return false;
    }
    
    public static boolean isInRange(String pan, Long minCardNo, Long maxCardNo) {
		Long cardNo = Util.longValueOf(pan);
		if (minCardNo!= null && cardNo.longValue() < minCardNo.longValue())
			return false;
			
		if (maxCardNo!= null && cardNo.longValue() > maxCardNo.longValue())
			return false;
		
		return true;
	}

	public static boolean isAllowedCard(Ifx ifx, AllowedCard card) {
		if (card == null)
			return true;

		String appPAN = ifx.getAppPAN();

		List<Long> cardGroupHierarchy = null;

		try {
			if (ProcessContext.get().getMyInstitution().getBin().intValue() == Integer.parseInt(appPAN.substring(0, 6))) {
				cardGroupHierarchy = VaulsysCardService.getCardGroupHierarchy(appPAN);

			} else if (Integer.parseInt(appPAN.substring(0, 6)) == 639347) {
				NeginCardData cd = CardService.extractCardData(appPAN);
				cardGroupHierarchy = CardService.getCardGroupHierarchy(cd);
			}

			if (appPAN.startsWith(card.getBank().getBin().toString())) {
				if (card.getCardGroupId() != null) {
					try {
						if (ProcessContext.get().getMyInstitution().getBin().intValue() == card.getBank().getBin().intValue()) {
							VaulsysCardService.authorizeCardTerminalPair(appPAN, cardGroupHierarchy, card.getCardGroupId());
						} else {
							CardService.authorizeCardTerminalPair(appPAN, cardGroupHierarchy, card.getCardGroupId());
						}
						return true;

					} catch (CardAuthorizerException e) {
						return false;
					}

				} else if (CardService.isInRange(appPAN, card.getMinCardNo(), card.getMaxCardNo())) {
					return true;
				}
			}
			return false;
		} catch (CardAuthorizerException e) {
			return false;
		}
	}

	public static boolean isAllowedCard(Ifx ifx, List<AllowedCard> cards) {
		if (cards == null || cards.isEmpty())
			return true;
		
		String PAN = ifx.getAppPAN();
		try {
			List<Long> cardGroupHierarchy = null;
			
			if (ProcessContext.get().getMyInstitution().getBin().intValue() == Integer.parseInt(PAN.substring(0, 6))){
				cardGroupHierarchy = VaulsysCardService.getCardGroupHierarchy(PAN);
				
			}else if (Integer.parseInt(PAN.substring(0, 6)) == 639347){
				NeginCardData cd = CardService.extractCardData(PAN);
				cardGroupHierarchy = CardService.getCardGroupHierarchy(cd);
			}

			for (AllowedCard card: cards){
				if (PAN.startsWith(card.getBank().getBin().toString())) {
					if (card.getCardGroupId()!= null) {
						try {
							if (ProcessContext.get().getMyInstitution().getBin().intValue() == card.getBank().getBin().intValue()){
								VaulsysCardService.authorizeCardTerminalPair(PAN, cardGroupHierarchy, card.getCardGroupId());
							}else{
								CardService.authorizeCardTerminalPair(PAN, cardGroupHierarchy, card.getCardGroupId());
							}
							return true;
						} catch (CardAuthorizerException e) {
							logger.debug(e);
						}
						
					}else if (CardService.isInRange(PAN, card.getMinCardNo(), card.getMaxCardNo())){
						return true;
					}
				}
			}
			
			return false;
			
		} catch (CardAuthorizerException e) {
			return false;
		}
	}
}

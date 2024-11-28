package vaulsys.discount;

import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.customer.CardService;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import org.apache.log4j.Logger;

public class DiscountService {
	private static final Logger logger = Logger.getLogger(DiscountService.class);

	public static Double computeDiscount(Long discountProfileId, Ifx ifx) throws CardAuthorizerException {
		if (discountProfileId == null)
			return null;

		List<BaseDiscount> baseDiscounts = ProcessContext.get().getBaseDiscounts(discountProfileId);
		if (baseDiscounts == null || baseDiscounts.isEmpty())
			return null;

		// String appPAN = ifx.getAppPAN();

		// try {
		// List<Long> cardGroupHierarchy = null;
		//
		// if (ProcessContext.get().getMyInstitution().getBin().intValue() ==
		// Integer.parseInt(appPAN.substring(0, 6))) {
		// cardGroupHierarchy = VaulsysCardService.getCardGroupHierarchy(appPAN);
		//
		// } else if (Integer.parseInt(appPAN.substring(0, 6)) == 639347) {
		// NeginCardData cd = CardService.extractCardData(appPAN);
		// cardGroupHierarchy = CardService.getCardGroupHierarchy(cd);
		// }

		for (BaseDiscount item : baseDiscounts) {

			if (item.isEnabled()) {

				boolean isAllowedCard = true;
				if (Boolean.FALSE.equals(item.getIsAllowedCard()))
					isAllowedCard = false;

				if (!(CardService.isAllowedCard(ifx, item.getCards()) ^ isAllowedCard))
					return item.getDiscount();

				// for (AllowedCard card : item.getCards()) {
				// if (card == null && item.getDiscount() != null)
				// return item.getDiscount();
				//
				// if (appPAN.startsWith(card.getBank().getBin().toString())) {
				// if (card.getCardGroupId() != null) {
				// try {
				// if
				// (ProcessContext.get().getMyInstitution().getBin().intValue()
				// == card.getBank().getBin().intValue()) {
				// VaulsysCardService.authorizeCardTerminalPair(appPAN,
				// cardGroupHierarchy, card.getCardGroupId());
				// } else {
				// CardService.authorizeCardTerminalPair(appPAN,
				// cardGroupHierarchy, card.getCardGroupId());
				// }
				// return item.getDiscount();
				//
				// } catch (CardAuthorizerException e) {
				// logger.debug(e);
				// }
				//
				// } else if (CardService.isInRange(appPAN, card.getMinCardNo(),
				// card.getMaxCardNo())) {
				// return item.getDiscount();
				// }
				// }
				// }
			}
		}
		return null;

		// } catch (CardAuthorizerException e) {
		// return null;
		// }
	}

}

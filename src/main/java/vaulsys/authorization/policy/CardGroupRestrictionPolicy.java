package vaulsys.authorization.policy;

import vaulsys.authorization.exception.AuthorizationException;
import vaulsys.authorization.exception.card.CardAuthorizerException;
import vaulsys.customer.CardService;
import vaulsys.customer.VaulsysCardService;
import vaulsys.customer.NeginCardData;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.terminal.impl.Terminal;
import vaulsys.wfe.ProcessContext;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.ForeignKey;

@Entity
@DiscriminatorValue(value = "CardGrupRest")
public class CardGroupRestrictionPolicy extends Policy {
	
	@Transient
	transient private Logger logger = Logger.getLogger(CardGroupRestrictionPolicy.class);
	
	
	@ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JoinTable(name = "auth_plc_crd_grp_crd",
			joinColumns = { @JoinColumn(name = "crd_grp_rst_plc") },
			inverseJoinColumns = { @JoinColumn(name = "card") })
	@ForeignKey(name = "crd_grp_rst_plc_fk", inverseName = "crd_grp_plc_bnk_fk")
	
//	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
//	@JoinColumn(name = "crd_grp_rst_plc")
//	@ForeignKey(name = "crd_grp_rst_plc_fk")
	private List<AllowedCard> cards;

	public CardGroupRestrictionPolicy() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Override
	protected void authorizeNormalCondition(Ifx ifx, Terminal terminal) throws CardAuthorizerException {
		String PAN = ifx.getAppPAN();
		try {
			List<Long> cardGroupHierarchy = null;
			if(PAN == null || PAN.length() == 0){
				throw new CardAuthorizerException("Failed: PAN is null....");			
			}

			
//			if (GlobalContext.getInstance().getMyInstitution().getBin().intValue() == Integer.parseInt(PAN.substring(0, 6))){
//			if (ProcessContext.get().getMyInstitution().getBin().intValue() == Integer.parseInt(PAN.substring(0, 6))){
//				cardGroupHierarchy = VaulsysCardService.getCardGroupHierarchy(PAN);
//				
//			}else if (Integer.parseInt(PAN.substring(0, 6)) == 639347){
//				NeginCardData cd = CardService.extractCardData(PAN);
//				cardGroupHierarchy = CardService.getCardGroupHierarchy(cd);
//			}

			for (AllowedCard card: getCards()){
				if (PAN.startsWith(card.getBank().getBin().toString())) {
					if (card.getCardGroupId()!= null) {
						
						if (ProcessContext.get().getMyInstitution().getBin().intValue() == Integer.parseInt(PAN.substring(0, 6))){
							cardGroupHierarchy = VaulsysCardService.getCardGroupHierarchy(PAN);
							
						}else if (Integer.parseInt(PAN.substring(0, 6)) == 639347){
							NeginCardData cd = CardService.extractCardData(PAN);
							cardGroupHierarchy = CardService.getCardGroupHierarchy(cd);
						}
						
						try {
//							if (GlobalContext.getInstance().getMyInstitution().getBin().intValue() == card.getBank().getBin().intValue()){
							if (ProcessContext.get().getMyInstitution().getBin().intValue() == card.getBank().getBin().intValue()){
								VaulsysCardService.authorizeCardTerminalPair(PAN, cardGroupHierarchy, card.getCardGroupId());
							}else{
								CardService.authorizeCardTerminalPair(PAN, cardGroupHierarchy, card.getCardGroupId());
							}
							return;
						} catch (CardAuthorizerException e) {
							logger.debug(e);
						}
						
					}else if (CardService.isInRange(PAN, card.getMinCardNo(), card.getMaxCardNo())){
						return;
					}
				}
			}
			
			throw new CardAuthorizerException("CardGroupRestrictionException! pan: " + PAN + ", terminal: "
					+ terminal.getCode());
			
		} catch (CardAuthorizerException e) {
			throw e;
		}
	}

	@Override
	protected void authorizeNotCondition(Ifx ifx, Terminal terminal) throws AuthorizationException {
		try {
			authorizeNormalCondition(ifx, terminal);
		} catch (CardAuthorizerException e) {
			return;
		}
		
		throw new CardAuthorizerException("CardGroupRestrictionException! pan: " + ifx.getAppPAN() + ", terminal: "
				+ terminal.getCode());
	}
	
	@Override
	public Policy clone() {
		return null;
	}

	@Override
	public void update(Ifx ifx, Terminal terminal) {
	}

	@Override
	public boolean isSynchronized() {
		return false;
	}

	public List<AllowedCard> getCards() {
		return cards;
	}

	public void setCards(List<AllowedCard> cards) {
		this.cards = cards;
	}
}

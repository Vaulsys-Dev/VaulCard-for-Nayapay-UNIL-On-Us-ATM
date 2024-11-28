package vaulsys.authorization.data;

import vaulsys.terminal.impl.Terminal;

import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.MapKeyManyToMany;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@DiscriminatorValue(value = "cardpolicy")
public class CardPolicyData extends PolicyData {
	
	@ManyToOne(cascade = CascadeType.ALL)
    @JoinTable(name = "auth_plc_cardplcdt_carddt", 
    		joinColumns = {@JoinColumn(name = "plc_data")},
    		inverseJoinColumns = {@JoinColumn(name = "card_data")}
    		)
	@OnDelete(action=OnDeleteAction.CASCADE)
    @ForeignKey(name = "cardplcdata_plcdata_fk", inverseName = "cardplcdata_carddata_fk" )
    CardData cardData;
	

	public CardData getCardData() {
		return cardData;
	}

	public void setCardData(CardData cardData) {
		this.cardData = cardData;
	}

}

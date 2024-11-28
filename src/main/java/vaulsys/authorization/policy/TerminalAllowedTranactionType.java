package vaulsys.authorization.policy;

import vaulsys.persistence.IEntity;
import vaulsys.protocols.ifx.enums.TrnType;
import vaulsys.terminal.impl.Terminal;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.ForeignKey;

@Entity
@Table(name = "auth_plc_trm_alw_trx_typ")
public class TerminalAllowedTranactionType implements IEntity<Long>, Cloneable {

    @Id
    @GeneratedValue(generator="switch-gen")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "terminal")
    @Cascade(value = {CascadeType.ALL})
    @ForeignKey(name="termalwtrx_term_fk")
    private Terminal terminal;

    @CollectionOfElements(fetch = FetchType.LAZY)
    @ForeignKey(name="termalwtrx_term_types_fk")
    @Enumerated(value = EnumType.STRING)
    private List<TrnType> types;


    public TerminalAllowedTranactionType() {
    }

    protected TerminalAllowedTranactionType clone() {
        TerminalAllowedTranactionType type = new TerminalAllowedTranactionType();
        type.setTerminal(terminal);
        type.setTypes(new ArrayList<TrnType>(types));
        return type;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<TrnType> getTypes() {
        return types;
    }

    public void setTypes(List<TrnType> types) {
        this.types = types;
    }

	public Terminal getTerminal()
	{
		return terminal;
	}

	public void setTerminal(Terminal bank)
	{
		this.terminal = bank;
	}

}

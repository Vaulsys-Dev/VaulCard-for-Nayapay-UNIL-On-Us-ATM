package vaulsys.terminal.atm;

import vaulsys.protocols.ifx.enums.UserLanguage;
import vaulsys.protocols.ndc.constants.NDCFunctionIdentifierConstants;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.AttributeOverrides;
import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;

import org.hibernate.annotations.ForeignKey;


@Entity
@DiscriminatorValue(value = "FunctionCommand")
public class FunctionCommandResponse extends ATMResponse {

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "term_atm_response__reciept", 
    		joinColumns = {@JoinColumn(name = "response")},
    		inverseJoinColumns = {@JoinColumn(name = "receipt")}
    		)
    @ForeignKey(name = "atmfuncres_response_fk", inverseName = "atmfuncres_receipt_fk")
    private List<Receipt> receipt;

    @Column(name = "next_state")
    private String nextState;
    
    private boolean beRetain;

    @Embedded
    @AttributeOverrides({
    	@AttributeOverride(name = "code", column = @Column(name = "functionCommand"))
    })
    private NDCFunctionIdentifierConstants functionCommand;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dispense")
    @ForeignKey(name="func_command_res_dispense_fk")
    private Dispense dispense;

    /*@ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "term_atm_response__screen", 
    		joinColumns = {@JoinColumn(name = "response")},
    		inverseJoinColumns = {@JoinColumn(name = "screen")}
    		)
    @ForeignKey(name = "atmfuncres_scrresponse_fk", inverseName = "atmfuncres_screen_fk")
    private List<ResponseScreen> screen;*/ 
    
    public FunctionCommandResponse() {
    }
    
    public FunctionCommandResponse(String nextState, boolean beRetain, NDCFunctionIdentifierConstants functionCommand) {
    	this.nextState = nextState;
    	this.beRetain = beRetain;
    	this.functionCommand = functionCommand;
    }
    
    public String getNextState() {
        return nextState;
    }

    public void setNextState(String nextState) {
        this.nextState = nextState;
    }

    public Dispense getDispense() {
        return dispense;
    }

    public void setDispense(Dispense dispense) {
        this.dispense = dispense;
    }

    public boolean isBeRetain() {
        return beRetain;
    }

    public Boolean getCradRetain() {
        return beRetain;
//        return (byte) (beRetain ? '1' : '0');
    }


    public void setBeRetain(boolean beReturn) {
        this.beRetain = beReturn;
    }


    public NDCFunctionIdentifierConstants getFunctionCommand() {
        return functionCommand;
    }


    public List<Receipt> getReceipt() {
        if (this.receipt == null)
            this.receipt = new ArrayList<Receipt>();
        return this.receipt;
    }

    public ArrayList<Receipt> getReceipt(UserLanguage lang) {
    	List<Receipt> result = new ArrayList<Receipt>();
    	if (this.receipt == null)
    		this.receipt = new ArrayList<Receipt>();
    	
    	for (Receipt receipt: this.receipt) {
    		if (receipt.getLanguage() == null || lang.equals(receipt.getLanguage()))
    			result.add(receipt);
    	}
    		
    	return (ArrayList<Receipt>) result;
    }
    

    public void setFunctionCommand(NDCFunctionIdentifierConstants functionCommand) {
        this.functionCommand = functionCommand;
    }


    public void setReceipt(List<Receipt> receipt) {
        this.receipt = receipt;
    }

    public void addReceipt(Receipt receipt) {
        if (this.receipt == null)
            this.receipt = new ArrayList<Receipt>();
        this.receipt.add(receipt);
    }
    
   /* public List<ResponseScreen> getScreen() {
        if (this.screen == null)
            this.screen = new ArrayList<ResponseScreen>();
        return this.screen;
    }

    public ResponseScreen getScreen(UserLanguage lang) {
    	List<ResponseScreen> result = new ArrayList<ResponseScreen>();
    	if (this.screen == null)
    		this.screen = new ArrayList<ResponseScreen>();
    	
    	for (ResponseScreen scr: this.screen) {
    		if (scr.getLanguage() == null || lang.equals(scr.getLanguage()))
    			return scr;
    	}
    		
    	return null;
    }
    
    public void setScreen(List<ResponseScreen> screen) {
        this.screen = screen;
    }

    public void addScreen(ResponseScreen screen) {
        if (this.screen == null)
            this.screen = new ArrayList<ResponseScreen>();
        this.screen.add(screen);
    }*/
}

package vaulsys.transaction;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class LifeCycleStatus implements Serializable{
	
	private static final Integer Nothing_Recieved = null;
	private static final Integer Request_Recieved = 2;
    private static final Integer Response_Recieved = 3;
    

    public static final LifeCycleStatus NOTHING = new LifeCycleStatus(Nothing_Recieved);
    public static final LifeCycleStatus REQUEST = new LifeCycleStatus(Request_Recieved);
    public static final LifeCycleStatus RESPONSE = new LifeCycleStatus(Response_Recieved);
    
    private Integer state;

    
	public LifeCycleStatus() {
		super();
	}

	public LifeCycleStatus(Integer state) {
		super();
		this.state = state;
	}

	public int getState() {
		return state;
	}

	public void setState(Integer state) {
		this.state = state;
	}

	@Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o== null && state== null) return true; 
        if (o== null && state ==1) return true;
        if (new Integer(1).equals(o) && state ==null) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LifeCycleStatus that = (LifeCycleStatus) o;
        if (state == null && that.state!= null) return false;
        return state.equals(that.state);
    }

    @Override
    public int hashCode() {
    	if(state == null)
    		return 1;
        return state;
    }

}

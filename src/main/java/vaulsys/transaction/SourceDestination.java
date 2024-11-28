package vaulsys.transaction;

import vaulsys.persistence.IEnum;

import javax.persistence.Embeddable;

@Embeddable
public class SourceDestination implements IEnum{

	private static int UNKNOWN_VALUE = -1;
	private static int SOURCE_VALUE = 1;
    private static int DESTINATION_VALUE = 2;
    
    public static SourceDestination SOURCE =  new SourceDestination(SOURCE_VALUE);
    public static SourceDestination DESTINATION =  new SourceDestination(DESTINATION_VALUE);
    public static SourceDestination UNKNOWN =  new SourceDestination(UNKNOWN_VALUE);
	
    private int state;

    public SourceDestination() {
    }

    public SourceDestination(int state) {
        this.state = state;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceDestination that = (SourceDestination) o;

        if (state != that.state) return false;

        return true;
    }


	public int hashCode() {
        return (int) state;
    }

	public int getState() {
		return state;
	}
	
	@Override
	public String toString() {
		if (state == 1)
			return "Source";
		if (state == 2)
			return "Destination";
		return state+"";
	}

    
}

package vaulsys.persistence;

import java.io.Serializable;

/**
 * Interface marks class which can be persisted.
 *
 * @param <I> type of primary key, it must be serializable
 */
public abstract class BaseEntity<I extends Serializable> implements IEntity<I> {

    /**
     * Property which represents id.
     */
    String P_ID = "id";

    /**
     * Get primary key.
     *
     * @return primary key
     */
    public abstract I getId();

    /**
     * Set primary key.
     *
     * @param id primary key
     */
    public abstract void setId(I id);
    
    @Override
    public boolean equals(Object obj) {
    	if(obj instanceof BaseEntity){
    		return ((BaseEntity)obj).getId().equals(getId());
    	}
    	return false;
    }
    
    @Override
    public String toString() {
    	return getId().toString();
    }

}

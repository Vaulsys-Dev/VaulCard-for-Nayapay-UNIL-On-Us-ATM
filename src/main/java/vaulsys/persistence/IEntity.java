package vaulsys.persistence;

import java.io.Serializable;

/**
 * Interface marks class which can be persisted.
 *
 * @param <I> type of primary key, it must be serializable
 */
public interface IEntity<I extends Serializable> extends Serializable {

    /**
     * Property which represents id.
     */
    String P_ID = "id";

    /**
     * Get primary key.
     *
     * @return primary key
     */
    I getId();

    /**
     * Set primary key.
     *
     * @param id primary key
     */
    void setId(I id);
}

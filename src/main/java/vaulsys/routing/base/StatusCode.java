package vaulsys.routing.base;

import java.io.Serializable;

public class StatusCode implements Serializable {
    public static final String CODE = "code";
    public static final String NAME = "name";
    public static final String SITUATION = "Situation";
    private static Long lastId = 0L;

    private String id;
    private String name;
    private String description;

    public static String getLastId() {
        lastId++;
        return lastId.toString();
    }


    public StatusCode(String id, String name, String description) {
        super();
        this.id = id;
        this.name = name;
        this.description = description;
    }

    public StatusCode(String id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /*
        public BusElement toXML() {
            BusElement result = new BusElement(StatusCode.SITUATION);
            result.setAttribute(StatusCode.NAME, this.name);
            result.setAttribute(StatusCode.CODE, this.id);
            return result;
        }
    */
    @Override
    public String toString() {
        return id.toString() + "@" + getName();
    }
}

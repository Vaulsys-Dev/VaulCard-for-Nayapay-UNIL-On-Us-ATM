package vaulsys.routing.base;


import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RoutCondition implements Serializable {
    public static final String CONDITION = "Condition";
    public static final String NAME = "name";
    public static Long lastId = 0L;


    private String id;
    private String name;
    private List<StatusCode> statusCodes;


    public static String getLastId() {
        lastId++;
        return lastId.toString();
    }

    public RoutCondition(String id, String name, List<StatusCode> statusCodes) {
        super();
        this.id = id;
        this.name = name;
        this.statusCodes = statusCodes;
    }

    public RoutCondition(String id, RoutCondition routCondition) {
        super();
        this.id = id;
        this.name = routCondition.getName();
        this.statusCodes = new ArrayList<StatusCode>();
        this.statusCodes.addAll(routCondition.getStatusCodes());
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

    public List<StatusCode> getStatusCodes() {
        return statusCodes;
    }

    public void setStatusCodes(List<StatusCode> statusCodes) {
        this.statusCodes = statusCodes;
    }

    public List<StatusCode> addStatusCode(StatusCode condition) {
        if (this.statusCodes == null) {
            this.statusCodes = new ArrayList<StatusCode>();
        }

        if (!this.statusCodes.contains(condition)) {
            this.statusCodes.add(condition);
        }
        return this.statusCodes;
    }


    @Override
    public String toString() {
        return id.toString() + "@" + getName();
    }
}

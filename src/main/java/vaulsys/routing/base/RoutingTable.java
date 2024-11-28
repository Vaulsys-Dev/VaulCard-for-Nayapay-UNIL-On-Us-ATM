package vaulsys.routing.base;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class RoutingTable implements Serializable {

    public static Long lastId = 0L;

    private final String ROUTING_TABLE = "Routing_Table";
    private final String PARAMETER = "Parameter";

    private Long id;
    private String name;

    private RoutingNode routingTree;
    private Map<String, String> routingPathByParameter;
    private Map<Long, String> routingParameterByOrder;


    public RoutingTable(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public RoutingTable(Long id, RoutingTable routingTable) {
        this.id = id;
        this.name = routingTable.name;
        this.routingParameterByOrder = routingTable.routingParameterByOrder;
        this.routingPathByParameter = routingTable.routingPathByParameter;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoutingNode getRoutingTree() {
        return routingTree;
    }

    public void setRoutingTree(RoutingNode routingTree) {
        this.routingTree = routingTree;
    }

    public Map<String, String> getRoutingPathByParameter() {
        return routingPathByParameter;
    }

    public void setRoutingPathByParameter(Map<String, String> routingPathByParameter) {
        this.routingPathByParameter = routingPathByParameter;
    }

    public Map<Long, String> getRoutingParameterByOrder() {
        return routingParameterByOrder;
    }

    public void setRoutingParameterByOrder(Map<Long, String> routingParameterByOrder) {
        this.routingParameterByOrder = routingParameterByOrder;
    }

    public void addRoutingParameterByOrder(Long order, String parameter) {
        if (this.routingParameterByOrder == null) {
            this.routingParameterByOrder = new HashMap<Long, String>();
        }

        this.routingParameterByOrder.put(order, parameter);
    }

    public void addRoutingPathByParameter(String parameter, String path) {
        if (this.routingPathByParameter == null) {
            this.routingPathByParameter = new HashMap<String, String>();
        }

        this.routingPathByParameter.put(parameter, path);
    }

    public String getPathByOrder(Long order) {
        String parameter = this.routingParameterByOrder.get(order);
        return this.routingPathByParameter.get(parameter);
    }

    public int getParametersNumber() {
        return routingParameterByOrder.size();
    }

    public static Long getLastId() {
        lastId++;
        return lastId;
    }

/*	
    public BusElement toXML() {
        BusElement result = new BusElement(ROUTING_TABLE, this.name);
        result.addBusElement(this.routingTree.toXML());
        BusElement routing_parameters = new BusElement("RoutingParameters");

        for (int index =0;  index < routingParameterByOrder.size(); index++){
            BusElement parameter = new BusElement(PARAMETER);
            parameter.setAttribute("name", routingParameterByOrder.get(index+1));
            parameter.setAttribute("Order", index+1);
            parameter.setAttribute("Path", routingPathByParameter.get(routingParameterByOrder.get(index+1)));

            routing_parameters.addBusElement(parameter);
        }

        result.addBusElement(routing_parameters);
        return result;
    }
*/

}

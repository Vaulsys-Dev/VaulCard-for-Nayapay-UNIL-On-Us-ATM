package vaulsys.routing.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

public class RoutingNode implements Serializable {
    public static final String ROUTING_ENTRY = "Routing_Entry";
    public static final String TYPE = "type";
    public static final String VALUE = "value";

    public static Long lastId = 0L;

    private String id;
    private String name;

    private String parameter;
    private String value;

    private List<RoutingNode> children;
    private RoutingNode father;

    private RoutCriteria ciriteria;

    public static String getLastId() {
        lastId++;
        return lastId.toString();
    }

    public RoutingNode(String id, RoutingNode routingNode) {
        super();
        this.id = id;
        this.name = routingNode.getName();
        this.parameter = routingNode.getParameter();
        this.value = routingNode.getValue();
        this.father = routingNode.getFather();
        this.children = new ArrayList<RoutingNode>();
        this.children.addAll(routingNode.getChildren());
        this.ciriteria = routingNode.ciriteria;
    }

    public RoutingNode(String id, String name, String parameter, String value, List<RoutingNode> children, RoutingNode father) {
        super();
        this.id = id;
        this.name = name;

        this.parameter = parameter;
        this.value = value;
        this.children = children;
        this.father = father;
    }

    public RoutingNode(String id, String name) {
        super();
        this.id = id;
        this.name = name;
        this.father = null;
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

    public String getParameter() {
        return parameter;
    }

    public void setParameter(String parameter) {
        this.parameter = parameter;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public void setChildren(List<RoutingNode> children) {
        this.children = children;
    }

    public void addChild(int index, RoutingNode child) {
        if (this.children == null) {
            this.children = new ArrayList<RoutingNode>();
        }
        child.setFather(this);
        this.children.add(index, child);
    }

    public void addChild(RoutingNode child) {
        if (this.children == null) {
            this.children = new ArrayList<RoutingNode>();
        }

        this.children.add(child);
    }

    public void addChild(String parameter, String value) {
        RoutingNode child = new RoutingNode(RoutingNode.getLastId(), null, parameter, value, null, this);
        this.children.add(child);
    }

    public List<RoutingNode> getChildren() {
        return children;
    }

    public RoutingNode getChildByParameter(String parameter) {
        for (RoutingNode child : this.children) {
            if (child.getParameter().equals(parameter)) {
                return child;
            }
        }
        return null;
    }

    public List<RoutingNode> getChilderenByParameter(String parameter) {
        List<RoutingNode> result = null;

        for (RoutingNode child : this.children) {
            if (child.getParameter().equals(parameter)) {
                if (result == null) {
                    result = new ArrayList<RoutingNode>();
                }
                result.add(child);
            }
        }
        return result;
    }

    public RoutingNode getChildByValue(String value) {
        for (RoutingNode child : this.children) {

            //TODO: Tokenize the string on table parsing
            StringTokenizer tokenizer = new StringTokenizer(child.getValue(), ",");
            while (tokenizer.hasMoreTokens()) {
                //if (tokenizer.nextToken().trim().equals(value)) {
                if (value.toUpperCase().matches(tokenizer.nextToken().trim().toUpperCase())) {
                    return child;
                }
            }

//			if (child.getValue().equals(value)) {
//			return child;
//		}


        }
        return null;
    }

    public RoutingNode getChild(String parameter, String value) {
        for (RoutingNode child : this.children) {
            if (child.getParameter().equals(parameter) && child.getValue().equals(value)) {
                return child;
            }
        }
        return null;
    }

    public RoutingNode getFather() {
        return father;
    }

    public void setFather(RoutingNode father) {
        this.father = father;
    }

    public Boolean isRoot() {
        return father == null;
    }

    public RoutCriteria getCiriteria() {
        return ciriteria;
    }

    public void setCiriteria(RoutCriteria ciriteria) {
        this.ciriteria = ciriteria;
    }

    /*
        public BusElement toXML() {
            // TODO create a BusElemet TerminalGroup of routing table
            BusElement result = new BusElement(RoutingNode.ROUTING_ENTRY);
            result.setAttribute(RoutingNode.TYPE, this.parameter);
            result.setAttribute(RoutingNode.VALUE, this.value);

            if (this.children != null)
                for (RoutingNode childNode : this.children) {
                    BusElement child = childNode.toXML();
                    child.setParent(result);
                    result.addBusElement(child);
                }

            if (this.ciriteria != null) {
                BusElement criteria = this.ciriteria.toXML();
                criteria.setParent(result);
                result.addBusElement(criteria);
            }
            return result;
        }
    */
    @Override
    public String toString() {
        return id.toString() + "@" + getName();
    }
}

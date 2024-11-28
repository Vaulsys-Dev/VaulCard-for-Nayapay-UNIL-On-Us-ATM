package vaulsys.routing.components;


import vaulsys.message.Message;
import vaulsys.network.channel.base.Channel;
import vaulsys.protocols.ifx.imp.Ifx;
import vaulsys.routing.base.ConstRoutingString;
import vaulsys.routing.base.RoutCondition;
import vaulsys.routing.base.RoutCriteria;
import vaulsys.routing.base.RoutDestination;
import vaulsys.routing.base.RoutingNode;
import vaulsys.routing.base.RoutingTable;
import vaulsys.routing.base.StatusCode;
import vaulsys.routing.base.exception.NoDestinationFoundException;
import vaulsys.util.ConfigUtil;
import vaulsys.wfe.ProcessContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

public class Routing {

    private static transient Logger logger = Logger.getLogger(Routing.class);

    private static String routingConfigFile = ConfigUtil.getProperty(ConfigUtil.GLOBAL_PATH_FILE_ROUTING);

    public static final String ROUTING_TABLE = "Routing_Table";
    public static final String ROUTING_TREE = "RoutingTree";
    public static final String ROUTING_PARAMETERS = "RoutingParameters";

    private static List<RoutingTable> routingTables;
   
    private Routing() {
        super();
    }

    public static RoutingTable getRoutingTable(int index) {
        return routingTables.get(index);
    }

    public static RoutingTable getLastRoutingTable() {
        return getRoutingTable(routingTables.size() - 1);
    }

    public static RoutingTable getRoutingTable(String name) {
        for (RoutingTable table : routingTables) {
            if (table.getName().equals(name)) {
                return table;
            }
        }
        return null;
    }

    public static void setRoutingTree(List<RoutingTable> rt) {
        routingTables = rt;
    }

    public static void addRoutingTable(RoutingTable routingTable) {
        if (routingTables == null) {
            routingTables = new ArrayList<RoutingTable>();
        }
        routingTables.add(routingTable);
    }

    private static RoutingNode parseRoutingEntry(Element routing_entry) {

        Attribute type = routing_entry.attribute(RoutingNode.TYPE);
        Attribute value = routing_entry.attribute(RoutingNode.VALUE);

        RoutingNode entry = new RoutingNode(RoutingNode.getLastId(), RoutingNode.ROUTING_ENTRY);
        entry.setParameter(type.getValue().trim());
        entry.setValue(value.getValue().trim());

        for (Iterator entriesItr = routing_entry.elementIterator(); entriesItr.hasNext();) {
            Element entryElement = (Element) entriesItr.next();

            if (entryElement.getName().equals(RoutingNode.ROUTING_ENTRY)) {
                RoutingNode child = parseRoutingEntry(entryElement);
                child.setFather(entry);
                entry.addChild(child);
            } else {
                if (entryElement.getName().equals(RoutCriteria.DESTINATIONS)) {
                    entry.setCiriteria(parseDestinations(entryElement));
                }
            }
        }

        return entry;
    }

    private static RoutCriteria parseDestinations(Element destinations) {
        RoutCriteria destinationsNode = new RoutCriteria(RoutCriteria.getLastId());

        int index = 0;
        for (Iterator childrenItr = destinations.elementIterator(); childrenItr.hasNext();) {
            Element child = (Element) childrenItr.next();

            if (child.getName().equals(RoutDestination.DESTINATION)) {
                destinationsNode.addDestination(index, parseRoutingDestination(child));
                index++;
            } else {
                if (child.getName().equals(RoutCondition.CONDITION)) {
                    destinationsNode.addCondition(parseRoutingCondition(child));
                    destinationsNode.setDestToCond(index, destinationsNode.getNumberOfConditions() - 1);
                }
            }
        }
        return destinationsNode;
    }

    private static RoutCondition parseRoutingCondition(Element condition) {
        Attribute name = condition.attribute(RoutCondition.NAME);

        RoutCondition condNod = new RoutCondition(RoutCondition.getLastId(), name.getValue(), null);

        for (Iterator statusItr = condition.elementIterator(); statusItr.hasNext();) {
            Element statusCode = (Element) statusItr.next();
            if (statusCode.getName().equals(StatusCode.SITUATION)) {
                name = statusCode.attribute(StatusCode.NAME);
                Attribute code = statusCode.attribute(StatusCode.CODE);
                StatusCode status = new StatusCode(code.getValue().trim(), name.getValue().trim());
                condNod.addStatusCode(status);
            }
        }

        return condNod;
    }

    private static RoutDestination parseRoutingDestination(Element dest) {
        RoutDestination destination = new RoutDestination(RoutDestination.getLastId());

        for (Iterator entriesItr = dest.elementIterator(); entriesItr.hasNext();) {
            Element element = (Element) entriesItr.next();

            if (element.getName().equals(Channel.NAME)) {
                destination.setChannelName(element.getText().trim());
                destination.setName(element.getText().trim());
            } 
        }
        return destination;
    }

    private static void parseRoutingTableConfiguration(Element routingTable, RoutingTable table) {

        Element routingTree = routingTable.element(ROUTING_TREE);
        if (routingTree != null) {
            RoutingNode routingTreeNode = new RoutingNode(RoutingNode.getLastId(), ROUTING_TREE);
            for (Iterator entriesItr = routingTree.elementIterator(); entriesItr.hasNext();) {
                Element entryElement = (Element) entriesItr.next();

                if (entryElement.getName().equals(RoutingNode.ROUTING_ENTRY)) {
                    RoutingNode child = parseRoutingEntry(entryElement);
                    child.setFather(routingTreeNode);
                    routingTreeNode.addChild(child);
                } else {
                    if (entryElement.getName().equals(RoutCriteria.DESTINATIONS)) {
                        routingTreeNode.setCiriteria(parseDestinations(entryElement));
                    }
                }
            }

            table.setRoutingTree(routingTreeNode);

        }
    }

    private static void parseRoutingParametersConfiguration(Element routingParameters, RoutingTable table) {
        for (Iterator parameterItr = routingParameters.elementIterator("Parameter"); parameterItr.hasNext();) {
            Element parameter = (Element) parameterItr.next();
            Attribute name = parameter.attribute("name");
            Attribute order = parameter.attribute("Order");
            Attribute path = parameter.attribute("Path");

            table.addRoutingParameterByOrder(new Long(order.getValue().trim()), name.getValue().trim());
            table.addRoutingPathByParameter(name.getValue().trim(), path.getValue().trim());
        }
    }

    private static List<RoutingTable> parseRoutingConfiguration() {
        SAXReader reader = new SAXReader();
        Document document = null;
        try {
            document = reader.read(Routing.class.getResourceAsStream(routingConfigFile));
        } catch (DocumentException e) {
            e.printStackTrace();
        }
        if (document != null) {
            Element root = document.getRootElement();
            List<RoutingTable> tables = new ArrayList<RoutingTable>();

            for (Iterator tableItr = root.elementIterator(ROUTING_TABLE); tableItr.hasNext();) {

                Element routingTable = (Element) tableItr.next();
                Attribute name_attr = routingTable.attribute(0);
                if (name_attr == null) {
//					getLogger().error("\nErorr: ROUTING_TABLE must have a name\n");
                }

                RoutingTable table = new RoutingTable(RoutingTable.getLastId(), name_attr.getValue());

                parseRoutingTableConfiguration(routingTable, table);


                Element routingParameters = routingTable.element(ROUTING_PARAMETERS);
                parseRoutingParametersConfiguration(routingParameters, table);

                tables.add(table);
            }

            return tables;
        }
        return null;
    }

    public static List<RoutingTable> initiate() {
        List<RoutingTable> tables = Routing.parseRoutingConfiguration();
        return tables;
    }

    public static List<RoutDestination> getDestination(ProcessContext processContext, RoutingTable routing_table) throws NoDestinationFoundException {
        if (processContext == null) {
            logger.error("Incomplete routing: No process on Bus!");
            return null;
        }
        List<RoutDestination> destination = null;

        RoutingNode routing_entry = routing_table.getRoutingTree();

        /*INCOMING*/Message incomingMessage = /*(IncomingMessage)*/ processContext.getInputMessage();
        Ifx ifx = incomingMessage.getIfx();

        for (int index = 0; index < routing_table.getParametersNumber(); index++) {

            RoutingNode tmp = routing_entry.getChildByValue(ConstRoutingString.ALL);

            if (tmp == null) {
                String path = routing_table.getPathByOrder(new Long(index + 1));
//                String attribute = getAttribute(path);
                String value = "";
                if (path.startsWith("Ifx")) {
                    value = String.valueOf(ifx.get(path));
                } else if (path.startsWith("IncomingMessage")) {
                    value = String.valueOf(incomingMessage.get(path));
                } else {
                }

                tmp = routing_entry.getChildByValue(value);

                if (tmp == null) {
                    tmp = routing_entry.getChildByValue(ConstRoutingString.OTHERWISE);
                }
            }
            if (tmp == null) {
                throw new NoDestinationFoundException();
            }
            routing_entry = tmp;

            if (routing_entry.getCiriteria() != null) {
                RoutCriteria criteria = routing_entry.getCiriteria();
                for (int i = 0; i < criteria.getDestinations().size(); i++) {
                    if (criteria.getConditionByDest(i) == null) {
                        if (destination == null) {
                            destination = new ArrayList<RoutDestination>();
                        }
                        destination.add(criteria.getDestinations().get(i));
                    }
                }
            }
        }

        logger.debug("Routing: # Found destiontions = " + destination.size());

        if (destination.size() == 0) {
            throw new NoDestinationFoundException();
        }
        return destination;
    }

    //m.rehman: over-loading existing function to support routing for schedule message
    public static List<RoutDestination> getDestination(Message incomingMessage, RoutingTable routing_table) throws NoDestinationFoundException {
        if (incomingMessage == null) {
            logger.error("Incomplete routing: No process on Bus!");
            return null;
        }
        List<RoutDestination> destination = null;

        RoutingNode routing_entry = routing_table.getRoutingTree();

        Ifx ifx = incomingMessage.getIfx();

        for (int index = 0; index < routing_table.getParametersNumber(); index++) {

            RoutingNode tmp = routing_entry.getChildByValue(ConstRoutingString.ALL);

            if (tmp == null) {
                String path = routing_table.getPathByOrder(new Long(index + 1));
//                String attribute = getAttribute(path);
                String value = "";
                if (path.startsWith("Ifx")) {
                    value = String.valueOf(ifx.get(path));
                } else if (path.startsWith("IncomingMessage")) {
                    value = String.valueOf(incomingMessage.get(path));
                } else {
                }

                tmp = routing_entry.getChildByValue(value);

                if (tmp == null) {
                    tmp = routing_entry.getChildByValue(ConstRoutingString.OTHERWISE);
                }
            }
            if (tmp == null) {
                throw new NoDestinationFoundException();
            }
            routing_entry = tmp;

            if (routing_entry.getCiriteria() != null) {
                RoutCriteria criteria = routing_entry.getCiriteria();
                for (int i = 0; i < criteria.getDestinations().size(); i++) {
                    if (criteria.getConditionByDest(i) == null) {
                        if (destination == null) {
                            destination = new ArrayList<RoutDestination>();
                        }
                        destination.add(criteria.getDestinations().get(i));
                    }
                }
            }
        }

        logger.debug("Routing: # Found destiontions = " + destination.size());

        if (destination.size() == 0) {
            throw new NoDestinationFoundException();
        }
        return destination;
    }
}

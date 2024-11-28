package vaulsys.routing.base;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RoutCriteria implements Serializable {
    public static final String DESTINATIONS = "Destinations";
    private static Long lastId = 0L;

    private String id;
    private String name;

    private Map<Integer, Integer> destinationToCondition;
    private List<RoutDestination> destinations;
    private List<RoutCondition> conditions;

    public static String getLastId() {
        lastId++;
        return lastId.toString();
    }

    public RoutCriteria(String id) {
        super();
        this.id = id;
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

    public List<RoutDestination> getDestinations() {
        return destinations;
    }

    public void setDestinations(List<RoutDestination> destinations) {
        this.destinations = destinations;
    }

    public void addDestination(int index, RoutDestination destionation) {
        if (this.destinations == null) {
            this.destinations = new ArrayList<RoutDestination>(index + 1);
            for (int i = 0; i < index; i++) {
                this.destinations.add(null);
            }
        }
        this.destinations.add(index, destionation);
    }

    public void addDestinatio(int index, RoutDestination destination, RoutCondition condition) {
        if (this.destinations == null) {
            this.destinations = new ArrayList<RoutDestination>(index + 1);
        }

        this.destinations.add(index, destination);
        this.addCondition(index, condition);
    }

    public List<RoutCondition> getConditions() {
        return conditions;
    }

    public void setConditions(List<RoutCondition> conditions) {
        this.conditions = conditions;
    }

    public void addCondition(int index, RoutCondition routCondition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<RoutCondition>(index + 1);
            for (int i = 0; i < index; i++) {
                this.conditions.add(null);
            }
        }
        this.conditions.add(index, routCondition);
    }

    public void addCondition(RoutCondition routCondition) {
        if (this.conditions == null) {
            this.conditions = new ArrayList<RoutCondition>();
        }
        this.conditions.add(routCondition);
    }

    /*
        public BusElement toXML() {
            BusElement result = new BusElement(RoutCriteria.DESTINATIONS);
            if (destinations != null || destinations.size() != 0) {
                BusElement primary_destination = destinations.get(0).toXML();
                primary_destination.setParent(result);
                result.addBusElement(primary_destination);
            }

            for (int i = 1; i < destinations.size(); i++) {
                BusElement destination = destinations.get(i).toXML();
                destination.setParent(result);
                BusElement condition = conditions.get(i).toXML();
                condition.setParent(result);
                result.addBusElement(condition);
                result.addBusElement(destination);
            }

            return result;
        }
    */
    public RoutCondition getConditionByDest(int destIndex) {
        if (destinationToCondition == null)
            return null;

        Integer conditionIndex = (Integer) destinationToCondition.get(destIndex);

        if (conditionIndex == null) {
            return null;
        }

        return conditions.get(conditionIndex.intValue());
    }

    public List<RoutDestination> getDestinationsByCondition(int condIndex) {
        List<RoutDestination> result = null;
        for (int index = 0; index < destinationToCondition.size(); index++) {
            Integer conditionIndex = destinationToCondition.get(new Integer(index));
            if (conditionIndex != null && condIndex == conditionIndex.intValue()) {
                if (result == null) {
                    result = new ArrayList<RoutDestination>();
                }
                result.add(destinations.get(index));
            }
        }
        return result;
    }

    public void setDestToCond(int destIndex, int condIndex) {
        if (destinationToCondition == null) {
            destinationToCondition = new HashMap<Integer, Integer>();
        }
        destinationToCondition.put(new Integer(destIndex), new Integer(condIndex));
    }

    public int getNumberOfConditions() {
        return conditions != null ? conditions.size() : 0;
    }

    @Override
    public String toString() {
        return id.toString() + "@" + getName();
    }
}

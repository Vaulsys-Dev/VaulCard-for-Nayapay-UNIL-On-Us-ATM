package vaulsys.auditlog;

/**
 * Created by j.khodabande on 10/5/2015.
 */
public class ObjectNode {

    public String Name;
    public Object Value;

    @Override
    public String toString() {
        return Name + " : " + Value.toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ObjectNode) {
            ObjectNode that = (ObjectNode) obj;
            if (that.Name == that.Name && this.Value.toString() == that.Value.toString())
                return true;
            else
                return false;
        }
        return false;
    }
}


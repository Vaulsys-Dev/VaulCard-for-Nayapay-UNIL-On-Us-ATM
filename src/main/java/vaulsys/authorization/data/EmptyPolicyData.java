package vaulsys.authorization.data;

import javax.persistence.Entity;
import javax.persistence.DiscriminatorValue;


@Entity
@DiscriminatorValue(value = "Empty")
public class EmptyPolicyData extends PolicyData {

    public EmptyPolicyData() {
    }
}

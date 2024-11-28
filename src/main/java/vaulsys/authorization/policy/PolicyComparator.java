package vaulsys.authorization.policy;

import java.util.Comparator;

public class PolicyComparator implements Comparator {

	@Override
	public int compare(Object arg0, Object arg1) {
		Policy policy1 = (Policy) arg0;
		Policy policy2 = (Policy) arg1;
		
		return policy1.getId().compareTo(policy2.getId());
	}

}

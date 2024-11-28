package vaulsys.customer;

import vaulsys.contact.City;
import vaulsys.contact.Country;
import vaulsys.contact.State;
import vaulsys.persistence.GeneralDao;
import vaulsys.util.NotUsed;

import java.util.HashMap;
import java.util.Map;

public class CustomerService {
	public static Currency findCurrency(Integer code) {
		String query = "from " + Currency.class.getName() + " c where c.code= :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		return (Currency) GeneralDao.Instance.findObject(query, param);
	}

	@NotUsed
	// used only in some initializers
	public static City findCity(Long code) {
		String query = "from " + City.class.getName() + " c where c.code= :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		return (City) GeneralDao.Instance.findObject(query, param);
	}

	@NotUsed
	// used only in some initializers
	public static State findState(Long code) {
		String query = "from " + State.class.getName() + " c where c.code= :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		return (State) GeneralDao.Instance.findObject(query, param);
	}

	@NotUsed
	// used only in some initializers
	public static Country findCountry(Long code) {
		String query = "from " + Country.class.getName() + " c where c.code= :code";
		Map<String, Object> param = new HashMap<String, Object>(1);
		param.put("code", code);
		return (Country) GeneralDao.Instance.findObject(query, param);
	}
}

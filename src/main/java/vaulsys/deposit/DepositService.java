package vaulsys.deposit;

import vaulsys.persistence.GeneralDao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DepositService {
	public List<Deposit> load(DepositPK pkExample) {
		//Session session = (Session) SwitchApplication.get().getBean("neginSessionFactory");
		//Criteria cr = session.createCriteria(Deposit.class);
		//DetachedCriteria cr = DetachedCriteria.forClass(Deposit.class);
		StringBuilder builder = new StringBuilder("from vaulsys.deposit.Deposit dep where dep.state=1 ");
		Map<String, Object> params = new HashMap<String, Object>();

		if (pkExample.getAbrnchcod() != null) {
			builder.append("and dep.depositPK.abrnchcod=:pk_abrnchcod ");
			params.put("pk_abrnchcod", pkExample.getAbrnchcod());
			//cr.add(Restrictions.eq("depositPK.abrnchcod", pkExample.getAbrnchcod()));
		}

		if (pkExample.getTbdptype() != null) {
			builder.append("and dep.depositPK.tbdptype=:pk_tbdptype ");
			params.put("pk_tbdptype", pkExample.getTbdptype());
			//cr.add(Restrictions.eq("depositPK.tbdptype", pkExample.getTbdptype()));
		}

		if (pkExample.getCfcifno() != null) {
			builder.append("and dep.depositPK.cfcifno=:pk_cfcifno ");
			params.put("pk_cfcifno", pkExample.getCfcifno());
			//cr.add(Restrictions.eq("depositPK.cfcifno", pkExample.getCfcifno()));
		}

		if (pkExample.getTdserial() != null) {
			builder.append("and dep.depositPK.tdserial=:pk_tdserial ");
			params.put("pk_tdserial", pkExample.getTdserial());
			//cr.add(Restrictions.eq("depositPK.tdserial", pkExample.getTdserial()));
		}

		//cr.add(Restrictions.eq("state", 1));

		return GeneralDao.Instance.find(builder.toString(), params);
	}
}

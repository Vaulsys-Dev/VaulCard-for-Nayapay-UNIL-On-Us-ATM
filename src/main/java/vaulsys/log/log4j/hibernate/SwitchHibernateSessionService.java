package vaulsys.log.log4j.hibernate;

import vaulsys.persistence.GeneralDao;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public class SwitchHibernateSessionService implements HibernateSessionService{
	public Session openSession() throws HibernateException {
		return GeneralDao.Instance.getNewSession();
	}
}

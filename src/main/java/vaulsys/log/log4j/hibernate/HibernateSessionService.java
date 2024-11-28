package vaulsys.log.log4j.hibernate;

import org.hibernate.HibernateException;
import org.hibernate.Session;

public interface HibernateSessionService {
    public Session openSession() throws HibernateException;
}

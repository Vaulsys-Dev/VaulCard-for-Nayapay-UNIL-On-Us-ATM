package vaulsys.persistence;

import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class NeginGeneralDao {
	private static final SessionFactory sessionFactory;

	static {
		Configuration conf = new Configuration();
		conf = conf.configure(new File("/config/hibernate-negin.cfg.xml"));
		sessionFactory = conf.buildSessionFactory();
	}
	
	public static final NeginGeneralDao Instance = new NeginGeneralDao();
	
	private NeginGeneralDao(){}
	
	public List executeSqlQuery(String query) {
		Session session = sessionFactory.openSession();
		List list = session.createSQLQuery(query).list();
		session.close();
		return list;
	}
}

package vaulsys.persistence;

import java.io.File;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class CMSGeneralDao {
	private static final SessionFactory sessionFactory;

	static {
		Configuration conf = new Configuration();
		conf = conf.configure(new File("/config/hibernate-cms.cfg.xml"));
		sessionFactory = conf.buildSessionFactory();
	}
	
	public static final CMSGeneralDao Instance = new CMSGeneralDao();
	
	private CMSGeneralDao(){}
	
	public List executeSqlQuery(String query) {
		Session session = sessionFactory.openSession();
		List list = session.createSQLQuery(query).list();
		session.close();
		return list;
	}
}

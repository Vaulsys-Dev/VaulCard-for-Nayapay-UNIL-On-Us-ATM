package vaulsys.persistence;

import vaulsys.mtn.util.irancell.hibernate.HibernateUtil;
import vaulsys.util.ConfigUtil;
import vaulsys.util.DBConfigUtil;
import vaulsys.util.SwitchRuntimeException;
import vaulsys.wfe.GlobalContext;
import vaulsys.wfe.ProcessContext;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;
import org.hibernate.proxy.HibernateProxyHelper;
import org.hibernate.tool.hbm2ddl.SchemaUpdate;
import org.hibernate.transform.ResultTransformer;

@SuppressWarnings("all")
public class GeneralDao {
	private static final Log logger = LogFactory.getLog(GeneralDao.class);
	private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
	private static final ThreadLocal<String> trxBeginLog = new ThreadLocal<String>();
	private static final ThreadLocal<String> trxCloseLog = new ThreadLocal<String>();

	private static final SessionFactory sessionFactory;
	private static AnnotationConfiguration conf;
	
	public static final String OPTIMIZER_MODE_ALL_ROWS = "all_rows";
	public static final String OPTIMIZER_MODE_FIRST_ROWS = "first_rows";
	
	public static final String SID_QUERY = "select sys_context('USERENV','sid'),sys_context('USERENV','instance') from dual";
	public static final String FIRST_ROW_QUERY = "alter session set optimizer_mode=first_rows";

	//m.rehman: changing below to read db config from external file
	/*
	static {
		conf = new AnnotationConfiguration();
		conf.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
//		conf = conf.configure(new File("/config/hibernate.cfg.xml"));
//		conf = conf.configure("/config/hibernate.cfg.xml");
		conf = conf.configure("/config/hibernate.cfg.xml");
		System.out.println(conf);
		conf.setProperty("hibernate.connection.url", ConfigUtil.getProperty(ConfigUtil.DB_URL));
		if (GlobalContext.dbUserName != null && GlobalContext.dbPasswored != null) {
			conf.setProperty("hibernate.connection.username", ConfigUtil.getProperty(GlobalContext.dbUserName));
			conf.setProperty("hibernate.connection.password", ConfigUtil.getDecProperty(GlobalContext.dbPasswored));
		} else {
			conf.setProperty("hibernate.connection.username", ConfigUtil.getProperty(ConfigUtil.DB_USERNAME));
			conf.setProperty("hibernate.connection.password", ConfigUtil.getDecProperty(ConfigUtil.DB_PASSWORD));
		}
		conf.setProperty("hibernate.show_sql", ConfigUtil.getProperty(ConfigUtil.DB_SHOW_SQL));
		conf.setProperty("hibernate.default_schema", ConfigUtil.getProperty(ConfigUtil.DB_SCHEMA));
		conf.setProperty("hibernate.use_sql_comments", ConfigUtil.getProperty(ConfigUtil.DB_SHOW_SQL));
		conf.setProperty("hibernate.generate_statistics", ConfigUtil.getProperty(ConfigUtil.DB_SHOW_SQL));
		conf.setProperty("hibernate.cache.use_query_cache", "false");
		String hbm2ddl = ConfigUtil.getProperty(ConfigUtil.DB_HBM2DDL);
		if(!(hbm2ddl.equalsIgnoreCase("none") || hbm2ddl.equalsIgnoreCase("update")))
			hbm2ddl = "none";		
		conf.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);
		sessionFactory = conf.buildSessionFactory();
	}
	*/

	static {
		conf = new AnnotationConfiguration();
		conf.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
		conf = conf.configure("/config/hibernate.cfg.xml");
		System.out.println(conf);
		conf.setProperty("hibernate.connection.url", DBConfigUtil.getDecProperty(DBConfigUtil.DB_URL));
		if (GlobalContext.dbUserName != null && GlobalContext.dbPasswored != null) {
			conf.setProperty("hibernate.connection.username", ConfigUtil.getProperty(GlobalContext.dbUserName));
			conf.setProperty("hibernate.connection.password", ConfigUtil.getDecProperty(GlobalContext.dbPasswored));
		} else {
			conf.setProperty("hibernate.connection.username", DBConfigUtil.getDecProperty(DBConfigUtil.DB_USERNAME));
			conf.setProperty("hibernate.connection.password", DBConfigUtil.getDecProperty(DBConfigUtil.DB_PASSWORD));
		}
		conf.setProperty("hibernate.show_sql", ConfigUtil.getProperty(ConfigUtil.DB_SHOW_SQL));
		conf.setProperty("hibernate.default_schema", DBConfigUtil.getDecProperty(DBConfigUtil.DB_SCHEMA));
		conf.setProperty("hibernate.use_sql_comments", ConfigUtil.getProperty(ConfigUtil.DB_SHOW_SQL));
		conf.setProperty("hibernate.generate_statistics", ConfigUtil.getProperty(ConfigUtil.DB_SHOW_SQL));
		conf.setProperty("hibernate.cache.use_query_cache", "false");
		String hbm2ddl = ConfigUtil.getProperty(ConfigUtil.DB_HBM2DDL);
		if(!(hbm2ddl.equalsIgnoreCase("none") || hbm2ddl.equalsIgnoreCase("update")))
			hbm2ddl = "none";
		conf.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);
		sessionFactory = conf.buildSessionFactory();
	}
	
	public static final GeneralDao Instance = new GeneralDao();
	
	private GeneralDao(){}
	
	public SchemaUpdate getSchemaUpdate() {
		return new SchemaUpdate(conf);
	}

	public Session getCurrentSession() {
		Session session = currentSession.get();
		if (session == null || !session.isOpen()) {
			session = sessionFactory.openSession();
			currentSession.set(session);
		}
		return session;
	}

	public void beginTransaction(String optimizer_mode) {
		Session session = getCurrentSession();
		if(session.getTransaction() != null && session.getTransaction().isActive()){
			logger.error("Current thread has begun the transaction before in:\n" + trxBeginLog.get());
			throw new SwitchRuntimeException("Current thread has begun the transaction before in:\n" + trxBeginLog.get());
		}
		try{
			session.beginTransaction();
//			session.setCacheMode(CacheMode.IGNORE);
	
			//find associated connection info
			Object[] d = (Object[])session.createSQLQuery(SID_QUERY).uniqueResult();
			//logger.info("Begin transaction on sid="+d[0]+", inst_id="+d[1]); //Raza LOGGING ENHANCED
			logger.debug("Begin transaction on sid="+d[0]+", inst_id="+d[1]); //Raza LOGGING ENHANCED - removing from log File

			//if(OPTIMIZER_MODE_FIRST_ROWS.equals(optimizer_mode) && ProcessContext.get().getMyInstitution().getBin().equals(502229L)){ //Raza commenting
			if(OPTIMIZER_MODE_FIRST_ROWS.equals(optimizer_mode)){
				session.createSQLQuery(FIRST_ROW_QUERY).executeUpdate();
			}
			if (logger.isDebugEnabled()) {
				RuntimeException x = new RuntimeException();
				StringBuilder builder = new StringBuilder();
				for(int i=1; i < x.getStackTrace().length && i < 6; i++)
					builder.append("\t").append(x.getStackTrace()[i]).append("\n");
				trxBeginLog.set(builder.toString());
				
				trxCloseLog.remove();
			}
		}catch(Exception e){
			logger.error("Exception in beginTransaction",e);
			close();
			throw new SwitchRuntimeException(e);
		}		
	}

	public void beginTransaction() {
		beginTransaction(OPTIMIZER_MODE_ALL_ROWS);
	}

	public void commit(){
		assertActiveTransaction();
		//logger.info("commit transaction"); //Raza LOGGING ENHANCED - Removing from Info
		logger.debug("commit transaction"); //Raza LOGGING ENHANCED
		Session session = currentSession.get();
		if (session != null) {
			Transaction trx = session.getTransaction();
			if (trx.isActive())
				trx.commit();
			else
				throw new RuntimeException("Inactive Session Transaction!");
		}
		else
			throw new RuntimeException("No Session in Current Thread");
	}

	public void rollback(){
		assertActiveTransaction();
		logger.info("rollback");
		Session session = currentSession.get();
		if (session != null) {
			Transaction trx = session.getTransaction();
			if (trx.isActive()){
				trx.rollback();
				close();
			}else{
				close();
				throw new RuntimeException("Inactive Session Transaction!");
			}
		}else{
			close();
			throw new RuntimeException("No Session in Current Thread");
		}
	}

	public void commitOrRollback(){
		try {
			commit();
		} catch (HibernateException e) {
			logger.error("Hibernate commitOrRollback: " + e, e);
			rollback();
		}
	}

	public void close(){
		Session session = currentSession.get();
		currentSession.remove();
		trxBeginLog.remove();
		if(session != null)
			session.close();
	}
	
	public void endTransaction() {
		try {
			commitOrRollback();
		} catch (Exception ex) {
			logger.error(ex.getClass().getSimpleName() + ": " + ex.getMessage(), ex);
		} finally {
			close();
		}
	}

	private void assertActiveTransaction(){
		Transaction trx = getCurrentSession().getTransaction();
		if(!trx.isActive())
			throw new RuntimeException("No active transaction for insert/delete/update");
	}

	public void update(Object object) {
		if (object == null)
			return;
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().update(object);
	}

	public void delete(Object object) {
		if (object == null)
			return;
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().delete(object);
	}

	public void save(Object object) {
		if (object == null)
			return;
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().save(object);
	}

	public void persist(Object object) {
		if (object == null)
			return;
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().persist(object);
	}

	public void saveOrUpdate(Object object) {
		if (object == null)
			return;
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().saveOrUpdate(object);
		logger.debug("saveOrUpdate:"+object.getClass()+"("+object+":"+((IEntity) object).getId()+")");
	}

	public Object findUnique(final String query, final Map parameters) {	
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, null, null).uniqueResult();
	}
	
	public Object findUnique(final String query) {	
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, null, null, null).uniqueResult();
	}

	public Object findObject(final String query, final Map parameters) {
		assertActiveTransaction();
		logExecLineNo();
		List result = createQuery(query, parameters, null, null).list();
		if (result.size() > 0)
			return result.get(0);
		else
			return null;
	}

	public Object findUniqueObject(final String query, final Map parameters) {
		assertActiveTransaction();
		logExecLineNo();
		List result = createQuery(query, parameters, 0, 1).list();
		if (result.size() > 0)
			return result.get(0);
		else
			return null;
	}

	public List find(String query) {
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, null, null, null).list();
	}

	public List find(String query, Map parameters) {
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, null, null).list();
	}

	public List find(String query, Map parameters, Integer firstResult, Integer maxResults, ResultTransformer resultTransformer) {
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, firstResult, maxResults).setResultTransformer(resultTransformer).list();
	}

	public List find(String query, Map parameters, ResultTransformer resultTransformer) {
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, null, null).setResultTransformer(resultTransformer).list();
	}

	public List find(String query, Map parameters, Integer firstResult,	Integer maxResults) {

		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, firstResult, maxResults).list();
	}

	public List find(String query, Map parameters, String lockStr, LockMode lockMode) {
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, null, null).setLockMode(lockStr, lockMode).list();
	}

	public int executeUpdate(final String queryString, final Map parameteres) {
		assertActiveTransaction();
		logExecLineNo();
		Query query = createQuery(queryString, parameteres, null, null);
//		query.setFlushMode(FlushMode.AUTO);
		return query.executeUpdate();
	}

	private Query createQuery(String queryString, Map parameteres, Integer firstResult, Integer resultSize) {
		assertActiveTransaction();
		Query query = getCurrentSession().createQuery(queryString);
		if (parameteres != null) {
			Set keys = parameteres.keySet();
			for (Object key : keys) {
				String name = (String) key;
				Object value = parameteres.get(name);
				if (value != null) {
					if (!(value instanceof Collection))
						query.setParameter(name, value);
					else
						query.setParameterList(name, (Collection) value);
				}
			}
		}
		if (firstResult != null)
			query.setFirstResult(firstResult);
		if (resultSize != null)
			query.setMaxResults(resultSize);
		return query;
	}

	public <T> T getObject(Class<T> clazz, Serializable key) {
		assertActiveTransaction();
		logExecLineNo();
		return (T) getCurrentSession().get(clazz, key);
	}

	public <T> T load(Class<T> clazz, Serializable key) {
		assertActiveTransaction();
		logExecLineNo();
		return (T) getCurrentSession().load(clazz, key);
	}
	
	public void load(Object Object, Serializable key) {
		assertActiveTransaction();
		getCurrentSession().load(Object, key);
	}

	public <T> T load(Class<T> clazz, Serializable key, LockMode lockMode) {
		assertActiveTransaction();
		logExecLineNo();
		return (T) getCurrentSession().load(clazz, key, lockMode);
	}

	public void flush() {
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().flush();
	}

	public void clear() {
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().clear();
	}

	public void refresh(Object o) {
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().refresh(o);
	}

	public boolean contains(Object object) {
		assertActiveTransaction();
		return getCurrentSession().contains(object);
	}

	public Session getNewSession() {
		return sessionFactory.openSession();
	}

	public int executeSqlUpdate(String query) {
		assertActiveTransaction();
		logExecLineNo();
		return getCurrentSession().createSQLQuery(query).executeUpdate();
	}

	public List executeSqlQuery(String query) {
		assertActiveTransaction();
		logExecLineNo();
		return getCurrentSession().createSQLQuery(query).list();
	}

	public List executeSqlQuery(String query, ResultTransformer resultTransformer) {
		assertActiveTransaction();
		logExecLineNo();
		return getCurrentSession().createSQLQuery(query).setResultTransformer(resultTransformer).list();
	}

	public void releaseLock(Object entity) {
		assertActiveTransaction();
		logExecLineNo();
		if (entity == null)
			return;
		getCurrentSession().lock(entity, LockMode.NONE);
	}

	public void lockReadAndWrite(Object entity) {
		assertActiveTransaction();
		logExecLineNo();
		getCurrentSession().lock(entity, LockMode.UPGRADE);
	}

	private void lockReadAndWrite(Object entity, LockMode lockMode) {
		assertActiveTransaction();
		getCurrentSession().lock(entity, lockMode);
	}

	public IEntity synchObject(IEntity entity) {
		assertActiveTransaction();
		logExecLineNo();
		try {
			lockReadAndWrite(entity);
			//TODO: MNS Performance
			refresh(entity);
			return (IEntity) load(HibernateProxyHelper.getClassWithoutInitializingProxy(entity), entity.getId());
		} catch (Exception e) {
			return entity;
		}
	}
	
	public IEntity optimizedSynchObject(IEntity entity) {
		if(entity.getId() == null)
			return entity;
		
		if (LockMode.UPGRADE.equals(getCurrentLockMode(entity)) ||
				LockMode.WRITE.equals(getCurrentLockMode(entity)))
			return entity;
		
		entity = (IEntity) load(HibernateProxyHelper.getClassWithoutInitializingProxy(entity), entity.getId(), LockMode.UPGRADE);
		return entity; 
	}

	public IEntity synchObject(IEntity entity, LockMode lockMode) /*throws CannotAcquireLockException*/ {
		assertActiveTransaction();
		logExecLineNo();
		
		if (getCurrentLockMode(entity).greaterThan(lockMode) || 
				getCurrentLockMode(entity).equals(LockMode.WRITE))
			return entity;
		
		lockReadAndWrite(entity, lockMode);
		//TODO: MNS Performance
		refresh(entity);
		return (IEntity) load(HibernateProxyHelper.getClassWithoutInitializingProxy(entity), entity.getId());
	}

	public List executeSqlQuery(final String queryString, final Map<String, Object> params) {
		assertActiveTransaction();
		logExecLineNo();
		Query query = getCurrentSession().createSQLQuery(queryString);
		for(Map.Entry<String, Object> entry : params.entrySet()) {
			if(entry.getValue() instanceof Collection)
				query.setParameterList(entry.getKey(), (Collection)entry.getValue());
			else
				query.setParameter(entry.getKey(), entry.getValue());
		}
		return query.list();
	}

    public List executeSqlQuery(final String queryString, final Map<String, Object> params, ResultTransformer resultTransformer) {
        assertActiveTransaction();
        logExecLineNo();
        Query query = getCurrentSession().createSQLQuery(queryString);
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            if(entry.getValue() instanceof Collection)
                query.setParameterList(entry.getKey(), (Collection)entry.getValue());
            else
                query.setParameter(entry.getKey(), entry.getValue());
        }

        return query.setResultTransformer(resultTransformer).list();
    }

    public List executeSqlQuery(final String queryString, final Map<String, Object> params, Integer firstResult, Integer resultSize) {
        assertActiveTransaction();
        logExecLineNo();
        Query query = getCurrentSession().createSQLQuery(queryString);
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            if(entry.getValue() instanceof Collection)
                query.setParameterList(entry.getKey(), (Collection)entry.getValue());
            else
                query.setParameter(entry.getKey(), entry.getValue());
        }
        if (firstResult != null)
            query.setFirstResult(firstResult);
        if (resultSize != null)
            query.setMaxResults(resultSize);

        return query.list();
    }

    public List executeSqlQuery(final String queryString, final Map<String, Object> params, Integer firstResult, Integer resultSize, ResultTransformer resultTransformer) {
        assertActiveTransaction();
        logExecLineNo();
        Query query = getCurrentSession().createSQLQuery(queryString);
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            if(entry.getValue() instanceof Collection)
                query.setParameterList(entry.getKey(), (Collection)entry.getValue());
            else
                query.setParameter(entry.getKey(), entry.getValue());
        }
        if (firstResult != null)
            query.setFirstResult(firstResult);
        if (resultSize != null)
            query.setMaxResults(resultSize);

        return query.setResultTransformer(resultTransformer).list();
    }

    public void evict(Object obj){
		getCurrentSession().evict(obj);
	}
	
	public LockMode getCurrentLockMode(Object obj){
		return getCurrentSession().getCurrentLockMode(obj);
	}

	private static void logExecLineNo(){
		if (logger.isDebugEnabled()) {
			RuntimeException x = new RuntimeException();
			if (!x.getStackTrace()[2].getClassName().equals("vaulsys.persistence.GeneralDao")) {
				StringBuilder builder = new StringBuilder("#@# HQL: ");
				builder.append(stackTrace(x.getStackTrace()[1]));
				for (int i = 2; i < 6 && i < x.getStackTrace().length; i++)
					builder.append("->").append(stackTrace(x.getStackTrace()[i]));
				logger.debug(builder.toString());
			}
		}
	}

	private static String stackTrace(StackTraceElement ste){
		String simpleClassName = ste.getClassName();
		simpleClassName = simpleClassName.substring(simpleClassName.lastIndexOf('.')+1);
		return String.format("%s.%s(%s)", simpleClassName, ste.getMethodName(), ste.getLineNumber());
	}

	public void closeSessionFactory() {
		if(sessionFactory != null)
			sessionFactory.close();
	}

    public int executeSqlUpdate(String query, Map parameteres) {
        assertActiveTransaction();
        logExecLineNo();

        SQLQuery sqlQuery = createSqlQuery(query, parameteres, null, null);

        return sqlQuery.executeUpdate();
	}
	
	private SQLQuery createSqlQuery(String queryString, Map parameteres, Integer firstResult, Integer resultSize) {
	        assertActiveTransaction();
	        SQLQuery query = getCurrentSession().createSQLQuery(queryString);
	        if (parameteres != null) {
	                Set keys = parameteres.keySet();
	                for (Object key : keys) {
	                        String name = (String) key;
	                        Object value = parameteres.get(name);
	                        if (value != null) {
	                                if (!(value instanceof Collection))
	                                        query.setParameter(name, value);
	                                else
	                                        query.setParameterList(name, (Collection) value);
	                        }
	                }
	        }
	        if (firstResult != null)
	                query.setFirstResult(firstResult);
	        if (resultSize != null)
	                query.setMaxResults(resultSize);
	        return query;
	}

	// Asim Shahzad, Date : 16th July 2020, Desc : Added function to fetch nevalue of the sequence
	public Long getNextValEmiCollBalLog(){
		Session session = this.getCurrentSession();
		SQLQuery query = session.createSQLQuery("select EMIWLLTBAL_SEQ.nextval from dual");
		Long nextValue = ((BigDecimal)query.uniqueResult()).longValue();
		return nextValue;
	}
	// ============================================================================================

	//m.rehman: 8-11-2021, VC-NAP-202111082 - Hibernate row level locking in transaction
	public Object findObject(final String query, final Map parameters, String lockStr, LockMode lockMode) {
		assertActiveTransaction();
		logExecLineNo();
		List result = createQuery(query, parameters, null, null).setLockMode(lockStr, lockMode).list();
		if (result.size() > 0)
			return result.get(0);
		else
			return null;
	}
}

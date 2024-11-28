package vaulsys.persistence;

import vaulsys.security.component.SecurityComponent;
import vaulsys.util.ConfigUtil;
import vaulsys.util.ConfigUtil.Key;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
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

import com.sun.corba.se.spi.activation.Server;


public class GhasedakGeneralDao {/*
	private static final ThreadLocal<Session> currentSession = new ThreadLocal<Session>();
    private static final ThreadLocal<String> trxBeginLog = new ThreadLocal<String>();
	private static final ThreadLocal<String> trxCloseLog = new ThreadLocal<String>();
	
	private static final SessionFactory sessionFactory;
    private static AnnotationConfiguration conf;
    private static  Server hsqlServer;
    
    public static final String OPTIMIZER_MODE_ALL_ROWS = "all_rows";
	public static final String OPTIMIZER_MODE_FIRST_ROWS = "first_rows";
	
	public static final String SID_QUERY = "select sys_context('USERENV','sid'),sys_context('USERENV','instance') from dual";
	public static final String FIRST_ROW_QUERY = "alter session set optimizer_mode=first_rows";
	
    private static final Logger logger = Logger.getLogger(GhasedakGeneralDao.class);
    
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    static {
        conf = new AnnotationConfiguration();
        conf.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
        conf = conf.configure(ConfigUtil.getProperty(ConfigUtil.GHASEDAK_HIBERNATE));
        try {
            if(ConfigUtil.getBoolean(ConfigUtil.GHASEDAK_DB_START_HSQL)){
            	HsqlProperties props = new HsqlProperties();
                props.setProperty("hsqldb.write_delay", false);
                ServerConfiguration.translateDefaultDatabaseProperty(props);
                hsqlServer = new Server();
                hsqlServer.setRestartOnShutdown(false);
                hsqlServer.setNoSystemExit(true);
                hsqlServer.setProperties(props);
                hsqlServer.setDatabasePath(0, ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_PATH) + ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_NAME));
                hsqlServer.setDatabaseName(0, ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_NAME));
                logger.info("HSQLDB Configured!......");
                hsqlServer.start();
                logger.info("HSQLDB Started on port " + hsqlServer.getPort() + ".......");
            }
        } catch (Exception e) {
            logger.debug("HSQLDB START ERROR", e);
        }
//        conf.setProperty("hibernate.connection.url", "jdbc:hsqldb:hsql://10.21.12.36/" + ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_NAME));
        conf.setProperty("hibernate.connection.url", ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_URL));
        conf.setProperty("hibernate.connection.username", ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_USER));
        conf.setProperty("hibernate.connection.password", getDecryptPass(ConfigUtil.GHASEDAK_DB_PASS));
        conf.setProperty("hibernate.show_sql", ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_SHOWSQL));
        conf.setProperty("hibernate.default_schema", ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_SCHEMA));
        String hbm2ddl = ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_HBM2DDL);
        conf.setProperty("hibernate.hbm2ddl.auto", hbm2ddl);
        sessionFactory = conf.buildSessionFactory();
        logger.info("***** Session Factory builded ! *****");
    }
    
    public static final GhasedakGeneralDao Instance = new GhasedakGeneralDao();
	
	private GhasedakGeneralDao(){}
	
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
	
	public void beginTransaction(String optimizer_mode) throws Exception {
		Session session = getCurrentSession();
		
		try{
			if(session.getTransaction() != null && session.getTransaction().isActive()){
				logger.error("Current thread has begun the transaction before in:\n" + trxBeginLog.get());
				throw new Exception("Current thread has begun the transaction before in:\n" + trxBeginLog.get());
			}
			session.beginTransaction();
//			session.setCacheMode(CacheMode.IGNORE);
			
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
			throw new Exception(e);
		}		
	}

	public void beginTransaction() {
		try {
			beginTransaction(OPTIMIZER_MODE_ALL_ROWS);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void commit(){
		assertActiveTransaction();
		logger.info("commit transaction");
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
	
	public void commitOrRollback(){
		try {
			commit();
		} catch (HibernateException e) {
			logger.error("Hibernate commitOrRollback: " + e, e);
			rollback();
		}
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
			stop();
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
		logger.debug("saveOrUpdate:" + object.getClass()+"("+object+":"+((IEntity) object).getId()+")");
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


	public List find(String query, Map parameters, ResultTransformer resultTransformer) {
		assertActiveTransaction();
		logExecLineNo();
		return createQuery(query, parameters, null, null).setResultTransformer(resultTransformer).list();
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

	public IEntity synchObject(IEntity entity, LockMode lockMode) throws CannotAcquireLockException {
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

    private void start(final HsqlProperties props) throws Exception {
    	ServerConfiguration.translateDefaultDatabaseProperty(props);
        hsqlServer = new Server();
        hsqlServer.setRestartOnShutdown(false);
        hsqlServer.setNoSystemExit(true);
        hsqlServer.setProperties(props);
        hsqlServer.setDatabasePath(0, ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_PATH) + ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_NAME));
        hsqlServer.setDatabaseName(0, ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_NAME));
        logger.info("HSQLDB Configured!......");
        hsqlServer.start();
        logger.info("HSQLDB Started on port " + hsqlServer.getPort() + ".......");
    }

    public void start() throws Exception {
        HsqlProperties props = new HsqlProperties();
        props.setProperty("hsqldb.write_delay", false);
        start(props);
    }

    public void stop() {
        logger.info("HSQLDB SERVER Shutting down .......");
        hsqlServer.stop();
        logger.info("HSQLDB Server shutting down ....... DONE!");
    }
    
  //for ghasedak
  	public static String getDecryptPass(Key encrypted){
  		try{
  			return SecurityComponent.base64Decrypt(ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_PASS), (ConfigUtil.getProperty(ConfigUtil.GHASEDAK_DB_KEY)).getBytes(), "DES");
  		}catch(Exception e){
  			throw new RuntimeException(e);
  		}
  	}
*/}

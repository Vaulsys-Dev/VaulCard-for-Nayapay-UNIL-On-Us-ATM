package vaulsys.mtn.util.irancell.hibernate;

import vaulsys.persistence.GeneralDao;
import org.apache.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.cfg.AnnotationConfiguration;
import org.hibernate.criterion.Restrictions;

public class HibernateUtil {

    private static transient Logger logger = Logger.getLogger(HibernateUtil.class);

    private static final ThreadLocal<MySession> mySession = new ThreadLocal<MySession>() {

        @Override
        protected MySession initialValue() {
            return getMySession();
        }

        ;
    };

    //private static final Configuration cfg;
    //private static final SessionFactory sessionFactory;
    private static AnnotationConfiguration conf;
    private static GeneralDao dao;
    private static ThreadLocal<String> dbSessionId = new ThreadLocal<String>();

    static {
        dao = GeneralDao.Instance;
    }

    /*static {
        try {
            logger.info("Building main sessionfactory...");
            cfg = new AnnotationConfiguration().configure(new File("./hibernate.cfg.xml"));
            cfg.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
            sessionFactory = cfg.buildSessionFactory();

        } catch (Throwable ex) {
            logger.error(ex);
            throw new ExceptionInInitializerError(ex);
        }
    }*/

    /* public static Configuration getConfiguration() {
        return cfg;
    }*/

    private static MySession getMySession() {
        // if (mainSession == null)
        // Session mainSession = sessionFactory.getCurrentSession();

        // return new MySession(sessionFactory.getCurrentSession());
        return new MySession(getSession());
    }

    private static Session getSession() {
        Session s = dao.getCurrentSession();
        // s.setFlushMode(FlushMode.COMMIT);
        return s;
    }

    public static Object findById(Class type, Integer id) {
        MySession session = getCurrentSession();
        Object result = null;
        try {
            result = session.load(type, id);
        } catch (Exception ex) {
            logger.error("Exception in findById", ex);
            result = null;
        }
        return result;
    }

    public static Object findByProperty(Class type, String property, Object value) {
        MySession session = getCurrentSession();
        session.beginTransaction();
        Object result = null;
        try {
            Criteria criteria = session.createCriteria(type).add(Restrictions.eq(property, value));
            result = criteria.uniqueResult();
            session.commitTransaction();
        } catch (Exception ex) {
            session.rollbackTransaction();
            logger.error("Exception in findByProperty", ex);
            result = null;
        }
        return result;
    }

    /*
      * public static void save(Object obj){ MySession session = getCurrentSession(); session.beginTransaction(); try{ session.saveOrUpdate(obj);
      * session.commitTransaction(); } catch(Exception ex){ session.rollbackTransaction(); logger.error("Exception in save", ex); } }
      */
    public static void closeSession() {
        getCurrentSession().close();
    }

    /*public static MySession getCurrentSession() {

        MySession session = null;
        try {
            session = mySession.get();
        } catch (Exception e) {
            session = new MySession(dao.openSession());
            mySession.set(session);
        }

        if (session == null || !session.isOpen()) {
            session = new MySession(sessionFactory.openSession());
            mySession.set(session);
        }

        return session;
    }*/

    public static MySession getCurrentSession() {
           MySession session = mySession.get();
           if (session == null || !session.isOpen()) {
               try {
                   logger.info("HibernateUtil get Session");
                   session = new MySession(dao.getNewSession());
                   session.beginTransaction();
                   Object[] d = (Object[]) session.createSQLQuery("select sys_context('USERENV','sid')," +
                           "sys_context('USERENV','instance'),sys_context('USERENV','instance_name') from dual").uniqueResult();
                String curUser = null;//PrincipalUtil.getCurrentUser() != null ? PrincipalUtil.getCurrentUser().getUsername() : "-";
                logger.info(String.format("BeginTrx: usr=[%s], sid=[%s], inst=[%s], inst_nm=[%s]", curUser, d[0], d[1], d[2]));
                   dbSessionId.set(String.format("%s_%s_%s", d[0], d[1], d[2]));
                   if (logger.isDebugEnabled()) {
                       logger.debug("Get Current Session -> Open New Session");
                       RuntimeException x = new RuntimeException();
                       for (int i = 1; i < 6 && i < x.getStackTrace().length; i++)
                           logger.debug("\t" + x.getStackTrace()[i]);
                   }
               } catch (HibernateException e) {
                   if (session != null) {
                       session.reconnect(null);
                       session.beginTransaction();
                   } else
                       throw e;
               }
               mySession.set(session);
           }
           return mySession.get();
       }

    public static void endTransaction() {
        MySession session = mySession.get();
        if (session != null && session.isOpen()) {
            Transaction transaction = session.getTransaction();
            try {
                transaction.commit();
                /*Class cls = savedUpdatedEntity.get();
                    if (cls != null && GlobalObjects.hasClass(cls))
                        GlobalObjects.loadAll(cls);*/
            } catch (RuntimeException ex) {
                transaction.rollback();
                logger.error("Commit Problem: ", ex);
                throw ex;
            } finally {
                mySession.remove();
                //savedUpdatedEntity.remove();
                session.close();
            }
        }
    }

    public static void rollback() {
        MySession session = mySession.get();
        if (session != null && session.isOpen()) {
            Transaction transaction = session.getTransaction();
            if (transaction != null)
                transaction.rollback();
            session.close();
            mySession.set(null);
        }
    }

    public static void removeSession() {

        if (mySession.get().isOpen())
            mySession.get().close();

        mySession.remove();
    }
}

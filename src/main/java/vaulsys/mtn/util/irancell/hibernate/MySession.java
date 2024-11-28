package vaulsys.mtn.util.irancell.hibernate;

import org.apache.log4j.Logger;
import org.hibernate.*;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Vector;

public class MySession {

    static Vector<Session> openSessions = new Vector<Session>();
    static Vector<Session> closedSessions = new Vector<Session>();

    public static void logSessionHistory() {
        System.out.println("\nOpen Sessions:");
        for (Session s : openSessions) {
            System.out.println(((Object) s).toString() + " -- " + s.toString());
        }

        System.out.println("\nClosed Sessions:");
        for (Session s : closedSessions) {
            System.out.println(((Object) s).toString() + " -- " + s.toString());
        }
    }

    public Session session = null;
    private Transaction transaction = null;
    private int transactionOpened = 0;

    double i;

    private transient Logger logger = Logger.getLogger(MySession.class);

    public MySession(Session session) {
        i = Math.random();
        this.session = session;

    }

    @Deprecated
    public <T>
    T reload(Class<T> c, Integer id) {
        return (T) session.load(c, id);
    }

    public <T>
    T reload(Class<T> c, Long id) {
        return (T) session.load(c, id);
    }

    public MySession beginTransaction() {
        if (transactionOpened == 0) {
            transaction = session.beginTransaction();
        }
        transactionOpened++;
        return this;
    }

    public void commitTransaction() {
        if (transactionOpened <= 0) {
            throw new RuntimeException("No Transaction was open!");
            //logger.debug("No Transaction was open!");
        }

        transactionOpened--;
        if (transaction != null) {
            if (transactionOpened == 0) {
                session.flush();
                logger.debug("Transaction commited:" + session);
                logger.debug("Transactions opened:" + transactionOpened);
                //System.out.println("OMG!");
                transaction.commit();

                //MySession.closedSessions.add(session);
            }
        } else {
            // logger.error("DUAL:No Transaction opened to be commited!");
            throw new RuntimeException("No Transaction initialized.");
        }
    }

    public void rollbackTransaction() {
        transactionOpened = 0;
        if (transaction != null)
            transaction.rollback();
        else {
            logger.error("No Transaction opened to be rollbacked!");
            // throw new Exception("No Transaction initialized.");
        }

    }

    public Transaction getTransaction() {
        return transaction;
    }


    public Connection close() throws HibernateException {
        transactionOpened = 0;
        return session.close();
    }

    public Criteria createCriteria(Class persistentClass) {
        return session.createCriteria(persistentClass);
    }

    public Criteria createCriteria(Class persistentClass, String alias) {
        return session.createCriteria(persistentClass, alias);
    }

    public void save(Object object) throws HibernateException {
        session.save(object);
    }

    public Object load(Class theClass, Serializable id) throws HibernateException {
        return session.load(theClass, id);
    }

    public <T> T get(Class<T> theClass, Serializable id) throws HibernateException {
        return (T) session.get(theClass, id);
    }

    public Query createQuery(String queryString) throws HibernateException {
        return session.createQuery(queryString);
    }

    public SQLQuery createSQLQuery(String queryString) throws HibernateException {
        return session.createSQLQuery(queryString);
    }

    public void delete(Object object) throws HibernateException {
        session.delete(object);
    }

    public Query getNamedQuery(String queryName) throws HibernateException {
        return session.getNamedQuery(queryName);
    }

    public void saveOrUpdate(Object object) throws HibernateException {
        session.saveOrUpdate(object);
    }

    public void merge(Object object) throws HibernateException {
        session.merge(object);
    }

    public void refresh(Object object) throws HibernateException {
        session.refresh(object);
    }

    public void update(Object object) throws HibernateException {
        session.update(object);
    }

    public boolean isOpen() {
        return session.isOpen();
    }

    public void flush() throws HibernateException {
        session.flush();
    }

    public int getNumTransactionsOpened() {
        return transactionOpened;
    }

    public Session getSession() {
        return session;
    }

    public void setSession(Session session2) {
        this.session = session2;
    }

    public void reconnect(Connection connection) {
        this.session.reconnect(connection);
    }

}

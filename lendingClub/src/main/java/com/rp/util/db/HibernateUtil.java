package com.rp.util.db;

import com.rp.p2p.model.OrderConfirmation;
import com.rp.util.ApplicationProperties;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class HibernateUtil {
    private final static Logger logger_ = Logger.getLogger(HibernateUtil.class);
    private static final ConcurrentMap<String,SessionFactory> sessionFactoryMap = new ConcurrentHashMap<String, SessionFactory>();
    public enum DbId{P2P}

    private static SessionFactory buildSessionFactory(String db) {
        try {
            Configuration configuration = new Configuration();
            configuration.setProperty("hibernate.connection.driver_class","com.mysql.jdbc.Driver");
            configuration.setProperty("hibernate.connection.url",DbFactory.getInstance().getUrlWithoutCredentials(db));
            configuration.setProperty("hibernate.connection.username",ApplicationProperties.getInstance().getProperty(db+".RDS_USERNAME"));
            configuration.setProperty("hibernate.connection.password",ApplicationProperties.getInstance().getProperty(db+".RDS_PASSWORD"));
            // Create the SessionFactory from hibernate.cfg.xml
            return configuration.configure().
                    buildSessionFactory();
        }
        catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory(String db) {
        synchronized (sessionFactoryMap) {
            SessionFactory sessionFactory =sessionFactoryMap.get(db);
            if (sessionFactory == null) {
                sessionFactory = buildSessionFactory(db);
                sessionFactoryMap.put(db,sessionFactory);
            }
            return sessionFactory;
        }
    }

    public static SessionFactory getSessionFactory(DbId dbId)
    {
        return getSessionFactory(dbId.name());
    }

    public static void shutdown(String db) {
        // Close caches and connection pools
        getSessionFactory(db).close();
    }

    public static void shutdown(DbId db) {
        // Close caches and connection pools
        shutdown(db.name());
    }

    @Override
    protected void finalize() throws Throwable {
        List<String> sessionFactoryNotClosed = shutdownAll();

        for (String dbId : sessionFactoryNotClosed)
        {
            logger_.warn("The session factory ["+dbId+"] was created but not explicitly closed.  The application should proactively close the session factory");
        }


        super.finalize();
    }

    public static final List<String>  shutdownAll() {
        List<String> ret = new ArrayList<String>(sessionFactoryMap.size());

        for (Map.Entry<String,SessionFactory> entry : sessionFactoryMap.entrySet())
        {
            SessionFactory sessionFactory= entry.getValue();
            if (sessionFactory != null && !sessionFactory.isClosed())
            {
                String dbId = entry.getKey();
                ret.add(dbId);

                try {
                    shutdown(dbId);
                }
                catch(Exception ex) {
                    logger_.warn("The session factory ["+dbId+"] was created but not explicitly closed.  While trying to close the session factory, the following exception ",ex);
                }
            }
        }

        return ret;
    }

    public static final void main(String args[])
    {
        DbId db = DbId.P2P;

        {
            SessionFactory sessionFactory = getSessionFactory(db);
            Session session = sessionFactory.openSession();
            session.close();
        }
        {
            SessionFactory sessionFactory = getSessionFactory(db);
            Session session = sessionFactory.openSession();
            session.close();
        }

        shutdown(db);
    }
}

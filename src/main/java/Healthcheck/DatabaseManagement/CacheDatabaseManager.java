package Healthcheck.DatabaseManagement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class CacheDatabaseManager extends DatabaseManager
{
    private SessionFactory _sessionFactory;
    private static final CacheDatabaseManager _cacheDatabaseManager = new CacheDatabaseManager();

    private CacheDatabaseManager()
    {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate-cache.cfg.xml");

        _sessionFactory = configuration.buildSessionFactory();
    }
    public static CacheDatabaseManager GetInstance()
    {
        return _cacheDatabaseManager;
    }

    public Session GetSession()
    {
        return _sessionFactory.openSession();
    }
}

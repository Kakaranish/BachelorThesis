package Healthcheck.DatabaseManagement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class MainDatabaseManager extends DatabaseManager
{
    private SessionFactory _sessionFactory;
    private static final MainDatabaseManager _mainDatabaseManager = new MainDatabaseManager();

    private MainDatabaseManager()
    {
        Configuration configuration = new Configuration();
        configuration.configure("hibernate.cfg.xml");

        _sessionFactory = configuration.buildSessionFactory();
    }

    public static MainDatabaseManager GetInstance()
    {
        return _mainDatabaseManager;
    }

    public Session GetSession()
    {
        return _sessionFactory.openSession();
    }
}

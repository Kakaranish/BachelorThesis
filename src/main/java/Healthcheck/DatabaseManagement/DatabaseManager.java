package Healthcheck.DatabaseManagement;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DatabaseManager
{
    private SessionFactory _sessionFactory;
    private static final DatabaseManager _databaseManager = new DatabaseManager();

    private DatabaseManager()
    {
        Configuration configuration = new Configuration();
        configuration.configure();

        _sessionFactory = configuration.buildSessionFactory();
    }
    public static DatabaseManager GetInstance()
    {
        return _databaseManager;
    }

    public Session GetSession()
    {
        return _sessionFactory.openSession();
    }
}

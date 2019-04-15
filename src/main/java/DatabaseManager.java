import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class DatabaseManager
{
    private SessionFactory _sessionFactory;
    private static final DatabaseManager databaseManager = new DatabaseManager();

    private DatabaseManager()
    {
        Configuration configuration = new Configuration();
        configuration.configure();

        _sessionFactory = configuration.buildSessionFactory();
    }
    public static DatabaseManager getInstance()
    {
        return databaseManager;
    }

    public Session GetSession()
    {
        return _sessionFactory.openSession();
    }

}

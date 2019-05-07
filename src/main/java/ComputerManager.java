import Entities.ComputerEntity;
import Entities.ComputerEntityPreference;
import Entities.Preference;
import Entities.User;
import Preferences.IPreference;
import Preferences.NoPreference;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ComputerManager
{
    private LogsManager _logsManager;

    private List<Computer> _computers;
    private List<Preference> _availablePreferences;
    private List<Computer> _selectedComputers;

    public ComputerManager()
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException, DatabaseException
    {
        _availablePreferences = GetAvailablePreferencesFromDb();
        _computers = GetComputersFromDb();
        _selectedComputers = new ArrayList<>(_computers);
    }

    // TODO: Add executing in new thread
    public void StartGatheringDataForSelectedComputers()
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException
    {
        _logsManager = new LogsManager(_selectedComputers);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- ADD ----------------------------------------------------------

    public void AddComputer(Computer computer) throws DatabaseException
    {
        try
        {
            if(computer.ComputerPreferences == null || computer.ComputerPreferences.isEmpty())
            {
                computer.ComputerPreferences = new ArrayList<IPreference>(){{
                    add(new NoPreference());
                }};
            }

            AddComputerToDb(computer);
            _computers.add(computer);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void AddComputerToDb(Computer computer) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        try
        {
            session.save(computer.ComputerEntity);

            for (IPreference iPreference : computer.ComputerPreferences)
            {
                session.save(new ComputerEntityPreference(computer.ComputerEntity,
                        ConvertIPreferenceToEntityPreference(iPreference)));
            }

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to add computer.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------ UPDATE ---------------------------------------------------------

    public void UpdateComputer(Computer computerToUpdate, Computer newComputer) throws  DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            computerToUpdate.ComputerEntity.CopyFrom(newComputer.ComputerEntity);
            UpdateComputerEntityInDb(computerToUpdate.ComputerEntity, session);
            UpdateComputerPreferencesInDb(computerToUpdate, newComputer, session);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to update computer.");
        }
        finally
        {
            session.close();
        }
    }

    public void UpdateComputerEntityInDb(
            ComputerEntity computerEntityToUpdate,
            Session session)
    {
        session.update(computerEntityToUpdate);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------- PREFERENCES --------------------------------------------------------

    private void UpdateComputerPreferencesInDb(
            Computer computerToUpdate,
            Computer newComputer,
            Session session)
    {
        RemoveComputerPreferences(computerToUpdate, session);

        if(newComputer.ComputerPreferences == null || newComputer.ComputerPreferences.isEmpty())
        {
            session.save( new ComputerEntityPreference(
                    computerToUpdate.ComputerEntity , ConvertIPreferenceToEntityPreference(new NoPreference())));
            computerToUpdate.ComputerPreferences.add(new NoPreference());
        }
        else
        {
            AssignPreferencesToComputer(computerToUpdate, newComputer.ComputerPreferences, session);
        }
    }

    private void AssignPreferencesToComputer(Computer computer, List<IPreference> preferences, Session session)
    {
        AssignPreferencesToComputerInDb(computer, preferences, session);
        for (IPreference preference : preferences)
        {
            computer.ComputerPreferences.add(preference);
        }
    }

    private void AssignPreferencesToComputerInDb(Computer computer, List<IPreference> preferences, Session session)
    {
        for (IPreference preference : preferences)
        {
            session.save(new ComputerEntityPreference(computer.ComputerEntity,
                    ConvertIPreferenceToEntityPreference(preference)));
        }
    }

    public void RemoveComputerPreferences(Computer computer, Session session)
    {
        RemoveComputerPreferencesFromDb(computer, session);
        computer.ComputerPreferences.clear();
    }

    public void RemoveComputerPreferencesFromDb(Computer computer, Session session)
    {
        List<IPreference> preferences = computer.ComputerPreferences;
        for (IPreference preference : preferences)
        {
            session.remove(new ComputerEntityPreference(computer.ComputerEntity,
                    ConvertIPreferenceToEntityPreference(preference)));
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------- REMOVE ----------------------------------------------------------

    public void RemoveComputerWithoutLogs(Computer computer) throws DatabaseException
    {
        try
        {
            RemoveComputerWithoutLogsFromDb(computer);
            _computers.remove(computer);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void RemoveComputerWithoutLogsFromDb(Computer computer) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        try
        {
            RemoveComputerPreferencesFromDb(computer, session);
            RemoveComputerEntityFromDb(computer.ComputerEntity, session);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to remove computer without logs.");
        }
        finally
        {
            session.close();
        }
    }

    public void RemoveComputerWithLogs(Computer computer, LogsMaintainer logsMaintainer) throws DatabaseException
    {
        try
        {
            RemoveComputerWithLogsFromDb(computer, logsMaintainer);
            _computers.remove(computer);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void RemoveComputerWithLogsFromDb(Computer computer, LogsMaintainer logsMaintainer) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        try
        {
            logsMaintainer.RemoveAllLogsAssociatedWithComputerFromDb(computer, session);
            RemoveComputerPreferencesFromDb(computer, session);
            RemoveComputerEntityFromDb(computer.ComputerEntity, session);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to remove computer with logs.");
        }
        finally
        {
            session.close();
        }
    }

    private void RemoveComputerEntityFromDb(ComputerEntity computerEntity, Session session)
    {
        session.remove(computerEntity);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------ USER -----------------------------------------------------------

    public void AssignUserToComputer(Computer computer, User user) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        try
        {
            computer.ComputerEntity.AssignUser(user);
            session.update(computer.ComputerEntity);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to Assign User to Computer.");
        }
        finally
        {
            session.close();
        }
    }

    public void RemoveUserAssignmentFromComputer(Computer computer, boolean clearComputerConnectionFields)
            throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        session.beginTransaction();

        try
        {
            computer.ComputerEntity.RemoveUser(clearComputerConnectionFields);
            session.update(computer.ComputerEntity);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to Assign User to Computer.");
        }
        finally
        {
            session.close();
        }
    }

    public void RemoveUserAssignmentFromComputer(Computer computer, Session session, boolean clearUserFields)
    {
            computer.ComputerEntity.RemoveUser(clearUserFields);
            session.update(computer.ComputerEntity);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- MISC ---------------------------------------------------------

    private List<ComputerEntityPreference> GetComputersEntitiesWithPreferencesFromDb() throws DatabaseException
    {
        String hql = "from ComputerEntityPreference compPref";
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            Query query = session.createQuery(hql);
            List preferences = query.getResultList();

            return  preferences;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get computer entities with preferences.");
        }
        finally
        {
            session.close();
        }
    }

    private List<Preference> GetAvailablePreferencesFromDb() throws DatabaseException
    {
        String hql = "from Preference";
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            Query query = session.createQuery(hql);
            List<Preference> preferences = query.getResultList();

            return preferences;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get available preferences.");
        }
        finally
        {
            session.close();
        }
    }

    private List<Computer> GetComputersFromDb()
            throws DatabaseException, ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            InstantiationException, IllegalAccessException
    {
        List<Computer> computers = new ArrayList<>();

        try
        {
            Map<ComputerEntity, List<ComputerEntityPreference>> grouped =
                    GetComputersEntitiesWithPreferencesFromDb()
                            .stream().collect(Collectors.groupingBy(cp -> cp.ComputerEntity));

            for (Map.Entry<ComputerEntity, List<ComputerEntityPreference>> computerListEntry : grouped.entrySet())
            {
                ComputerEntity computerEntity = computerListEntry.getKey();
                List<IPreference> preferences = new ArrayList<>();
                for (ComputerEntityPreference computerEntityPreference : computerListEntry.getValue())
                {
                    Preference preferenceEntity = computerEntityPreference.Preference;
                    IPreference preference = ConvertPreferenceEntityToIPreference(preferenceEntity);
                    preferences.add(preference);
                }

                computers.add(new Computer(computerEntity, preferences));
            }

            return computers;
        }
        catch (DatabaseException e)
        {
            DatabaseException ex = new DatabaseException("Unable get computers.");
            ex.initCause(e);
            throw ex;
        }
    }

    private IPreference ConvertPreferenceEntityToIPreference(Preference preference)
            throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException,
            InvocationTargetException, InstantiationException
    {
        String iPreferenceClassName = preference.ClassName;
        Class iPreferenceClass = Class.forName(iPreferenceClassName);
        IPreference iPreference = (IPreference) iPreferenceClass.getConstructor().newInstance();
        return iPreference;
    }

    private Preference ConvertIPreferenceToEntityPreference(IPreference iPreference)
    {
        String className = iPreference.getClass().getName();
        List<Preference> preferences =
                _availablePreferences.stream().filter(p -> p.ClassName.equals(className)).collect(Collectors.toList());

        Preference preference = preferences.get(0);
        return preference;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------GETTERS --------------------------------------------------------

    public List<Computer> GetComputers()
    {
        return _computers;
    }

    public List<Preference> GetAvailablePreferences()
    {
        return _availablePreferences;
    }

    public List<Computer> GetComputersAssociatedWithUser(User user)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.ComputerEntity.User != null && c.ComputerEntity.getUser().equals(user))
                .collect(Collectors.toList());

        return results;
    }

    public Computer GetComputer(String host)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.ComputerEntity.getHost().equals(host)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }
}

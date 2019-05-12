import Entities.*;
import Preferences.IPreference;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ComputerManager
{
    private LogsManager _logsManager;

    private List<Computer> _computers;

    public ComputerManager() throws DatabaseException
    {
        _computers = GetComputersFromDb();
    }

    // TODO: Add executing in new thread
    public void StartGatheringDataForSelectedComputers()
            throws ClassNotFoundException, NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException
    {
//        _logsManager = new LogsManager(_selectedComputers);
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- ADD ----------------------------------------------------------

    public void AddComputer(Computer computer) throws DatabaseException
    {
        try
        {
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
            computer.ComputerEntity.Preferences =
                    Utilities.ConvertListOfIPreferencesToPreferences(computer.Preferences);
            session.save(computer.ComputerEntity);

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

    public void UpdateComputer(Computer computerToUpdate, ComputerEntity newComputerEntity)
            throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            computerToUpdate.ComputerEntity.CopyFrom(newComputerEntity);
            UpdateComputerEntityInDb(computerToUpdate.ComputerEntity, session);

            computerToUpdate.Preferences =
                    Utilities.ConvertListOfPreferencesToIPreferences(newComputerEntity.Preferences);

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
            throw new DatabaseException("Unable to assign user to Computer.");
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
            throw new DatabaseException("Unable to remove user from Computer.");
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

    private List<Computer> GetComputersFromDb() throws DatabaseException
    {
        List<Computer> computers = new ArrayList<>();

        try
        {
            List<ComputerEntity> computerEntities = GetComputerEntitiesFromDb();

            for (ComputerEntity computerEntity : computerEntities)
            {
                List<IPreference> computerIPreferences = new ArrayList<>();

                for (Preference preference : computerEntity.Preferences)
                {
                    IPreference iPreference =
                            Utilities.ConvertPreferenceEntityToIPreference(preference);
                    computerIPreferences.add(iPreference);
                }

                computers.add(new Computer(computerEntity, computerIPreferences));
            }

            return computers;
        }
        catch (DatabaseException e)
        {
            DatabaseException ex = new DatabaseException("Unable get computers from db.");
            ex.initCause(e);
            throw ex;
        }
    }

    private List<ComputerEntity> GetComputerEntitiesFromDb() throws DatabaseException
    {
        String hql = "from ComputerEntity";
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            Query query = session.createQuery(hql);
            List computers = query.getResultList();

            return  computers;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get computer entities from db.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------GETTERS --------------------------------------------------------

    public List<Computer> GetComputers()
    {
        return _computers;
    }

    public Computer GetComputer(String host)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.ComputerEntity.Host.equals(host)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public List<Computer> GetComputersAssociatedWithUser(User user)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.ComputerEntity.User != null && c.ComputerEntity.User.equals(user))
                .collect(Collectors.toList());

        return results;
    }

    public List<Computer> GetSelectedComputers()
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.ComputerEntity.IsSelected == true).collect(Collectors.toList());
        return results;
    }
}

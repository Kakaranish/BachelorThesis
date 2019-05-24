package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.*;
import Healthcheck.LogsManagement.LogsMaintainer;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.Preferences.IPreference;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ComputerManager
{
    private List<Computer> _computers;

    public ComputerManager() throws DatabaseException
    {
        _computers = GetComputersFromDb();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- ADD ----------------------------------------------------------

    public void AddComputer(Computer computer) throws DatabaseException
    {
        if(GetComputer(computer.ComputerEntity.Host) != null)
        {
            throw new IllegalArgumentException("[FATAL ERROR] ComputerManager: " +
                    "Unable to add computer. User with same host name exists.");
        }

        if(computer.ComputerEntity.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("[FATAL ERROR] ComputerManager: " +
                    "Unable to add computer. Computer contains empty required fields.");
        }

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
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of adding computer entity to db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        boolean persistSucceed
                = DatabaseManager.PersistWithRetryPolicy(session, computer.ComputerEntity, attemptErrorMessage);
        session.close();

        if(persistSucceed == true)
        {
            computer.ComputerEntity.Preferences =
                    Utilities.ConvertListOfIPreferencesToPreferences(computer.Preferences);
        }
        else
        {
            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to add computer entity to db.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------ UPDATE ---------------------------------------------------------

    public void UpdateComputer(Computer computerToUpdate, ComputerEntity newComputerEntity)
            throws DatabaseException, IllegalArgumentException, NothingToDoException
    {
        if(CanComputerEntityBeUpdated(computerToUpdate.ComputerEntity, newComputerEntity) == false)
        {
            throw new IllegalArgumentException("[ERROR] ComputerManager: " +
                    "Unable to update computer entity. Provided user and data connection fields are empty");
        }

        if(newComputerEntity.User != null)
        {
            newComputerEntity.ResetConnectionDataFields();
        }

        if(computerToUpdate.ComputerEntity == newComputerEntity
                || computerToUpdate.ComputerEntity.equals(newComputerEntity))
        {
            throw new NothingToDoException("[INFO] ComputerManager: Nothing to update.");
        }

        try
        {
            UpdateComputerInDb(computerToUpdate, newComputerEntity);

            computerToUpdate.Preferences =
                    Utilities.ConvertListOfPreferencesToIPreferences(newComputerEntity.Preferences);

        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    public void UpdateComputerInDb(Computer computerToUpdate, ComputerEntity newComputerEntity) throws DatabaseException
    {
        ComputerEntity beforeUpdateComputerEntity = new ComputerEntity(computerToUpdate.ComputerEntity);

        computerToUpdate.ComputerEntity.CopyFrom(newComputerEntity);
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of updating computer entity in db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        boolean updateSucceed
                = DatabaseManager.UpdateWithRetryPolicy(session, computerToUpdate.ComputerEntity, attemptErrorMessage);
        session.close();

        if(updateSucceed == false)
        {
            computerToUpdate.ComputerEntity.CopyFrom(beforeUpdateComputerEntity);
            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to update computer entity in db.");
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
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of removing computer entity from db failed.";
        Session session = DatabaseManager.GetInstance().GetSession();
        boolean removeSucceed
                = DatabaseManager.RemoveWithRetryPolicy(session, computer.ComputerEntity, attemptErrorMessage);
        session.close();

        if(removeSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to remove computer entity from db.");
        }
    }

    public void RemoveComputerWithLogs(Computer computer) throws DatabaseException
    {
        try
        {
            RemoveComputerWithLogsFromDb(computer);
            _computers.remove(computer);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void RemoveComputerWithLogsFromDb(Computer computer) throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of " +
                "removing computer entity with logs from db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        try
        {
            session.beginTransaction();

            LogsMaintainer.RemoveAllLogsAssociatedWithComputerFromDb(computer, session);
            session.remove(computer.ComputerEntity);

            session.getTransaction().commit();
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            System.out.println(attemptErrorMessage);

            int retryNum = 1;
            while(retryNum <= Utilities.RemoveNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.RemoveCooldown + randomFactor);

                    session.beginTransaction();

                    LogsMaintainer.RemoveAllLogsAssociatedWithComputerFromDb(computer, session);
                    session.remove(computer.ComputerEntity);

                    session.getTransaction().commit();
                }
                catch (InterruptedException ex)
                {
                    throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to " +
                            "remove computer entity with logs from db.");
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;
                    System.out.println(attemptErrorMessage);
                }
            }

            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to " +
                    "remove computer entity with logs from db.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------ USER -----------------------------------------------------------

    public void RemoveUserAssignmentFromComputer(Computer computer, Session session, boolean clearUserFields)
    {
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
            throw e;
        }
    }

    private List<ComputerEntity> GetComputerEntitiesFromDb() throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of getting computer entities from db failed.";
        String hql = "from ComputerEntity";

        Session session = DatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        List<ComputerEntity> computerEntities =
                DatabaseManager.ExecuteSelectQueryWithRetryPolicy(session, query, attemptErrorMessage);
        session.close();
        if(computerEntities != null)
        {
            return computerEntities;
        }
        else
        {
            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to get computer entities from db.");
        }
    }

    public boolean CanComputerEntityBeUpdated(ComputerEntity computerEntityToUpdate, ComputerEntity newComputerEntity)
    {
        if(IsUserToReset(computerEntityToUpdate, newComputerEntity) &&
            AreSomeConnectionDataFieldsEmpty(newComputerEntity))
        {
            return false;
        }

        return true;
    }

    private boolean IsUserToReset(ComputerEntity computerEntityToUpdate, ComputerEntity newComputerEntity)
    {
        return computerEntityToUpdate.User != null && newComputerEntity.User == null;
    }

    private boolean AreSomeConnectionDataFieldsEmpty(ComputerEntity computerEntityToUpdate)
    {
        return  computerEntityToUpdate.GetUsernameConnectionField() == null ||
                computerEntityToUpdate.GetEncryptedPasswordConnectionField() == null ||
                computerEntityToUpdate.GetSSHKeyConnectionField() == null;
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

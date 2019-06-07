package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.*;
import Healthcheck.LogsManagement.LogsMaintainer;
import Healthcheck.LogsManagement.NothingToDoException;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ComputerManager
{
    private SSHConfigurationsManager _sshConfigurationsManager;
    private List<Computer> _computers;

    public ComputerManager(SSHConfigurationsManager sshConfigurationsManager) throws DatabaseException
    {
        _sshConfigurationsManager = sshConfigurationsManager;
        _computers = GetComputersFromDb();
    }

    public ComputerManager() throws DatabaseException
    {
        _computers = GetComputersFromDb();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- ADD ----------------------------------------------------------

    public void AddComputer(Computer computer) throws DatabaseException, IllegalArgumentException
    {
        if(GetComputerByDisplayedName(computer.DisplayedName) != null)
        {
            throw new IllegalArgumentException("Unable to add computer - other computer has same displayed name.");
        }

        if(GetComputerByHost(computer.Host) != null)
        {
            throw new IllegalArgumentException("Unable to add computer - other computer has same host.");
        }

        if(computer.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to add computer - some required fields are empty.");
        }

        if(computer.SSHConfiguration.Scope == SSHConfigurationScope.GLOBAL)
        {
            SSHConfiguration foundGlobalSSHConfiguration =
                    _sshConfigurationsManager.GetGlobalSSHConfigurationWithIdUsingOther(computer.SSHConfiguration);

            if(foundGlobalSSHConfiguration == null)
            {
                throw new IllegalArgumentException("Unable to add computer - " +
                        "provided ssh configuration with global scope is wrong.");
            }
            else
            {
                computer.SSHConfiguration = foundGlobalSSHConfiguration;
            }
        }

        boolean addComputerToDbSucceed = AddComputerToDb(computer);
        if(addComputerToDbSucceed)
        {
            _computers.add(computer);
        }
        else
        {
            throw new DatabaseException("Unable to add computer to db.");
        }
    }

    private boolean AddComputerToDb(Computer computer)
    {
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of adding computer entity to db failed.";
        Session session = DatabaseManager.GetInstance().GetSession();

        boolean sshConfigurationPersistSucceed = true;
        if(computer.SSHConfiguration.Scope == SSHConfigurationScope.COMPUTER)
        {
            sshConfigurationPersistSucceed = AddComputerSSHConfigurationToDb(session, computer.SSHConfiguration);
        }

        boolean computerPersistSucceed =
                DatabaseManager.PersistWithRetryPolicy(session, computer, attemptErrorMessage);

        session.close();

        return computerPersistSucceed && sshConfigurationPersistSucceed;
    }

    private boolean AddComputerSSHConfigurationToDb(Session session, SSHConfiguration sshConfiguration)
    {
        try
        {
            _sshConfigurationsManager.AddSSHConfigurationWithComputerScopeToDb(session, sshConfiguration);
            return true;
        }
        catch (IllegalArgumentException e)
        {
            throw e;
        }
        catch (DatabaseException e)
        {
            return false;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ------------------------------------------------ UPDATE ---------------------------------------------------------

    private void Validate_UpdateComputer(Computer computerToUpdate, Computer newComputer)
            throws IllegalArgumentException, NothingToDoException
    {
        if(newComputer.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to update computer entity - some required fields are empty.");
        }

        if(Utilities.AreEqual(computerToUpdate.DisplayedName, newComputer.DisplayedName) == false &&
                ComputerWithGivenDisplayedNameExists(newComputer.DisplayedName))
        {
            throw new IllegalArgumentException("Unable to update computer - computer with same displayed name already exists.");
        }

        if(Utilities.AreEqual(computerToUpdate.Host, newComputer.Host) == false && ComputerWithGivenHostExists(newComputer.Host))
        {
            throw new IllegalArgumentException("Unable to update computer - computer with same host already exists.");
        }

        if(computerToUpdate == newComputer || computerToUpdate.equals(newComputer))
        {
            throw new NothingToDoException("Nothing to update.");
        }
    }

    public void UpdateComputer(Computer computerToUpdate, Computer newComputer)
            throws DatabaseException, IllegalArgumentException, NothingToDoException
    {
        Validate_UpdateComputer(computerToUpdate, newComputer);



        boolean updateSucceed = UpdateComputerInDb(computerToUpdate, newComputer);
        if(updateSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to update computer in db.");
        }
    }

    public boolean UpdateComputerInDb(Computer computerToUpdate, Computer newComputer)
            throws DatabaseException
    {
        Computer computerToUpdateBackup = new Computer(computerToUpdate);
        computerToUpdate.CopyFrom(newComputer);

        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of updating computer entity in db failed.";
        Session session = DatabaseManager.GetInstance().GetSession();
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, computerToUpdate, attemptErrorMessage);
        session.close();

        if(updateSucceed == false)
        {
            computerToUpdate.CopyFrom(computerToUpdateBackup);
        }

        return updateSucceed;
    }

    public void UpdateComputerSSHConfiguration(Session session, Computer computer, SSHConfiguration newSshConfiguration)
    {
        if(computer.SSHConfiguration.Scope == SSHConfigurationScope.COMPUTER &&
                newSshConfiguration.Scope == SSHConfigurationScope.COMPUTER)
        {
            _sshConfigurationsManager.UpdateSSHConfigurationWithComputerScopeInDb(
                    session, computer.SSHConfiguration, newSshConfiguration);
        }
        else if(computer.SSHConfiguration.Scope == SSHConfigurationScope.COMPUTER &&
                newSshConfiguration.Scope == SSHConfigurationScope.GLOBAL)
        {
            // REMOVE OLD AND ASSIGN EXISTING GLOBAL
            _sshConfigurationsManager.RemoveSSHConfigurationWithComputerScopeFromDb(session, computer.SSHConfiguration);
            computer.SSHConfiguration = newSshConfiguration;
            computer.SSHConfiguration.AddComputer(computer);

            String attemptFailed = "temp attempt failure message";
            boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, computer, attemptFailed);
        }
        else if(computer.SSHConfiguration.Scope == SSHConfigurationScope.GLOBAL &&
                newSshConfiguration.Scope == SSHConfigurationScope.COMPUTER)
        {
            computer.SSHConfiguration = newSshConfiguration;
            computer.SSHConfiguration.AddComputer(computer);
            boolean updateSucceed = AddComputerSSHConfigurationToDb(session, computer.SSHConfiguration);

            String attemptFailed = "temp attempt failure message";

            boolean updateSucceed2 = DatabaseManager.UpdateWithRetryPolicy(session, computer, attemptFailed);

        }
        else if(computer.SSHConfiguration.Scope == SSHConfigurationScope.GLOBAL &&
                newSshConfiguration.Scope == SSHConfigurationScope.GLOBAL)
        {
            computer.SSHConfiguration.RemoveComputer(computer);
            computer.SSHConfiguration = newSshConfiguration;
            computer.SSHConfiguration.AddComputer(computer);

            String attemptFailed = "temp attempt failure message";
            boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, computer, attemptFailed);
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ----------------------------------------------- REMOVE ----------------------------------------------------------

    public void RemoveComputerWithoutLogs(Computer computer) throws DatabaseException
    {
        boolean removeSucceed = RemoveComputerWithoutLogsFromDb(computer);
        if(removeSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to remove computer entity from db.");
        }
        else
        {
            _computers.remove(computer);
        }
    }

    private boolean RemoveComputerWithoutLogsFromDb(Computer computer) throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of removing computer entity from db failed.";
        Session session = DatabaseManager.GetInstance().GetSession();
        boolean removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, computer, attemptErrorMessage);
        session.close();

        return removeSucceed;
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
                "removing computer with logs from db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        try
        {
            session.beginTransaction();

            LogsMaintainer.RemoveAllLogsAssociatedWithComputerFromDb(computer, session);
            session.remove(computer);

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
                    session.remove(computer);

                    session.getTransaction().commit();
                }
                catch (InterruptedException ex)
                {
                    throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to " +
                            "remove computer with logs from db.");
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;
                    System.out.println(attemptErrorMessage);
                }
            }

            throw new DatabaseException("[FATAL ERROR] ComputerManager: Unable to " +
                    "remove computer with logs from db.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- MISC ---------------------------------------------------------

    private List<Computer> GetComputersFromDb() throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] ComputerManager: Attempt of getting computer entities from db failed.";
        String hql = "from Computer";

        Session session = DatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        List<Computer> computerEntities =
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


    private boolean ComputerWithGivenDisplayedNameExists(String displayedName)
    {
        return GetComputerByDisplayedName(displayedName) != null;
    }

    private boolean ComputerWithGivenHostExists(String host)
    {
        return GetComputerByHost(host) != null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------GETTERS --------------------------------------------------------

    public List<Computer> GetComputers()
    {
        return _computers;
    }

    public Computer GetComputerById(int id)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.Id == id).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public Computer GetComputerByHost(String host)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.Host.equals(host)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public Computer GetComputerByDisplayedName(String displayedName)
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.DisplayedName.equals(displayedName)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public List<Computer> GetSelectedComputers()
    {
        List<Computer> results = _computers.stream()
                .filter(c -> c.IsSelected == true).collect(Collectors.toList());
        return results;
    }

    //    public List<Computer> GetComputersAssociatedWithUser(User user)
    //    {
    //        List<Computer> results = _computers.stream()
    //                .filter(c -> c.User != null && c.Computer.User.equals(user))
    //                .collect(Collectors.toList());
    //
    //        return results;
    //    }
}

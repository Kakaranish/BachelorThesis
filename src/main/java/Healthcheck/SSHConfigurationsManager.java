package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SSHConfiguration;
import Healthcheck.Entities.SSHConfigurationScope;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SSHConfigurationsManager
{
    private List<SSHConfiguration> _globalSSHConfigurations;

    public SSHConfigurationsManager()
    {
        _globalSSHConfigurations = GetGlobalSSHConfigurationsFromDb();
    }

    // ---  GLOBAL SCOPE - ADD  ----------------------------------------------------------------------------------------

    public void AddSSHConfigurationWithGlobalScope(SSHConfiguration sshConfiguration)
            throws IllegalArgumentException, DatabaseException
    {
        Validate_AddSSHConfigurationWithGlobalScope(sshConfiguration);

        try
        {
            AddSSHConfigurationWithGlobalScopeToDb(sshConfiguration);
            _globalSSHConfigurations.add(sshConfiguration);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void Validate_AddSSHConfigurationWithGlobalScope(SSHConfiguration sshConfiguration)
            throws IllegalArgumentException
    {
        if(sshConfiguration.Scope != SSHConfigurationScope.GLOBAL)
        {
            throw new IllegalArgumentException("Unable to add global ssh configuration - wrong scope provided.");
        }

        if(sshConfiguration.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to add global ssh configuration - " +
                    "not all required fields are set.");
        }

        if(GlobalSSHConfigurationExists(sshConfiguration))
        {
            throw new IllegalArgumentException("Unable to add global ssh configuration - " +
                    "same configuration already exists.");
        }
    }

    private void AddSSHConfigurationWithGlobalScopeToDb(SSHConfiguration sshConfiguration) throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] SSHConfigurationsManager: " +
                "Attempt of adding global ssh configuration to db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        boolean persistSucceed = DatabaseManager.PersistWithRetryPolicy(session, sshConfiguration, attemptErrorMessage);
        session.close();

        if(persistSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] SSHConfigurationsManager: " +
                    "Unable to add global ssh configuration to db.");
        }
    }

    // ---  GLOBAL SCOPE - UPDATE  -------------------------------------------------------------------------------------

    public void UpdateSSHConfigurationWithGlobalScope(
            SSHConfiguration oldSshConfiguration, SSHConfiguration newSshConfiguration)
            throws IllegalArgumentException, DatabaseException
    {
        Validate_UpdateSSHConfigurationWithGlobalScope(oldSshConfiguration, newSshConfiguration);

        SSHConfiguration oldSshConfigurationBackup = new SSHConfiguration(oldSshConfiguration);
        try
        {
            UpdateSSHConfigurationWithGlobalScopeInDb(oldSshConfiguration, newSshConfiguration);
        }
        catch (DatabaseException e)
        {
            oldSshConfiguration.CopyFrom(oldSshConfigurationBackup);
            throw new DatabaseException("Unable to update global ssh configuration in db.");
        }
    }

    private void Validate_UpdateSSHConfigurationWithGlobalScope(
            SSHConfiguration oldSshConfiguration, SSHConfiguration newSshConfiguration)
            throws IllegalArgumentException
    {
        if(oldSshConfiguration.Id == null || GlobalSSHConfigurationExists(oldSshConfiguration) == false)
        {
            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
                    "configuration to update cannot be found in db.");
        }

        if(newSshConfiguration.Id != oldSshConfiguration.Id)
        {
            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
                    "ssh configuration have different ids..");
        }

        if( oldSshConfiguration.HasSetRequiredFields() == false ||
                newSshConfiguration.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
                    "not all required fields are set.");
        }

        if( oldSshConfiguration.Name.equals(newSshConfiguration.Name) == false &&
                GlobalSSHConfigurationExists(newSshConfiguration))
        {
            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
                    "configuration with given name already exists.");
        }

        if( oldSshConfiguration.Scope != SSHConfigurationScope.GLOBAL ||
                newSshConfiguration.Scope != SSHConfigurationScope.GLOBAL)
        {
            throw new IllegalArgumentException("Unable to update global ssh configuration - wrong scope(s) provided.");
        }
    }

    private void UpdateSSHConfigurationWithGlobalScopeInDb(
            SSHConfiguration oldSshConfiguration, SSHConfiguration newSshConfiguration)
            throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] SSHConfigurationsManager: " +
                "Attempt of updating global ssh configuration in db failed.";

        oldSshConfiguration.CopyFrom(newSshConfiguration);
        Session session = DatabaseManager.GetInstance().GetSession();
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, oldSshConfiguration, attemptErrorMessage);
        session.close();

        if(updateSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to update global ssh configuration in db.");
        }
    }

    // ---  GLOBAL SCOPE - REMOVE  -------------------------------------------------------------------------------------

    public void RemoveSSHConfigurationWithGlobalScope(SSHConfiguration sshConfiguration, ComputerManager computerManager)
    {
        Validate_RemoveSSHConfigurationWithGlobalScope(sshConfiguration);

        try
        {
            RemoveSSHConfigurationWithGlobalScopeFromDb(sshConfiguration, computerManager);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    // TODO:
    private void RemoveSSHConfigurationWithGlobalScopeFromDb(
            SSHConfiguration sshConfiguration, ComputerManager computerManager)
            throws DatabaseException
    {
        List<Computer> computersWithGivenGlobalSshConfiguration = new ArrayList<>(sshConfiguration.GetComputers());

        Session session = DatabaseManager.GetInstance().GetSession();
        for (Computer computer : computersWithGivenGlobalSshConfiguration)
        {
            sshConfiguration.RemoveComputer(computer);

            SSHConfiguration sshConfigurationWithComputerScope = new SSHConfiguration(sshConfiguration);
            sshConfigurationWithComputerScope.Id = null;
            sshConfigurationWithComputerScope.Name = null;
            sshConfigurationWithComputerScope.Scope = SSHConfigurationScope.COMPUTER;
            sshConfigurationWithComputerScope.ResetComputers();

            AddSSHConfigurationWithComputerScopeToDb(session, sshConfigurationWithComputerScope); //TODO: Handling errors
            computerManager.UpdateComputerSSHConfiguration(session, computer, sshConfigurationWithComputerScope); //TODO: Handling errors
        }

        String removeAttemptErrorMessage = "[ERROR] SSHConfigurationsManager: " +
                "Attempt of removing global ssh configuration from db failed.";
        boolean removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, sshConfiguration, removeAttemptErrorMessage);
        session.close();

        if(removeSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] SSHConfigurationsManager: " +
                    "Unable to remove global ssh configuration from db.");
        }
    }

    // TODO: Validate cases
    private void Validate_RemoveSSHConfigurationWithGlobalScope(SSHConfiguration sshConfiguration)
            throws IllegalArgumentException
    {
        if(sshConfiguration.Scope != SSHConfigurationScope.GLOBAL)
        {
            throw new IllegalArgumentException("Unable to remove global ssh configuration - wrong scope provided.");
        }

        if(sshConfiguration.Id == null || GlobalSSHConfigurationExists(sshConfiguration) == false)
        {
            throw new IllegalArgumentException("Unable to remove global ssh configuration - " +
                    "configuration cannot be found in db.");
        }
    }

    // ---  COMPUTER SCOPE - ADD  --------------------------------------------------------------------------------------

    public void AddSSHConfigurationWithComputerScopeToDb(Session session, SSHConfiguration sshConfiguration)
            throws DatabaseException, IllegalArgumentException
    {
        Validate_AddSSHConfigurationWithComputerScope(sshConfiguration);

        if(sshConfiguration.Id != null)
        {
            sshConfiguration.Id = null;
        }

        String attemptErrorMessage = "[ERROR] SSHConfigurationsManager: Attempt of adding computer ssh configuration to db failed.";
        boolean persistSucceed = DatabaseManager.PersistWithRetryPolicy(session, sshConfiguration, attemptErrorMessage);
        if(persistSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] SSHConfigurationsManager: Unable to add computer ssh configuration to db.");
        }
    }

    private void Validate_AddSSHConfigurationWithComputerScope(SSHConfiguration sshConfiguration)
            throws IllegalArgumentException
    {
        if(sshConfiguration.Scope != SSHConfigurationScope.COMPUTER)
        {
            throw new IllegalArgumentException("Unable to add computer ssh configuration - wrong scope provided.");
        }

        if(sshConfiguration.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to add computer ssh configuration - not all required fields are set.");
        }
    }


    // ---  COMPUTER SCOPE - UPDATE  -----------------------------------------------------------------------------------

    public void UpdateSSHConfigurationWithComputerScopeInDb(
            Session session, SSHConfiguration oldSshConfiguration, SSHConfiguration newSshConfiguration)
            throws DatabaseException
    {
        Validate_UpdateSSHConfigurationWithComputerScope(oldSshConfiguration, newSshConfiguration);

        if(newSshConfiguration.Name != null)
        {
            newSshConfiguration.Name = null;
        }

        oldSshConfiguration.CopyFrom(newSshConfiguration);

        String attemptErrorMessage = "[ERROR] SSHConfigurationsManager: " +
                "Attempt of updating computer ssh configuration in db failed.";
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, oldSshConfiguration, attemptErrorMessage);
        if(updateSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to update computer ssh configuration in db.");
        }
    }

    private void Validate_UpdateSSHConfigurationWithComputerScope(
            SSHConfiguration oldSshConfiguration, SSHConfiguration newSshConfiguration)
    {
        if(oldSshConfiguration.Id == null)
        {
            throw new IllegalArgumentException("Unable to update computer ssh configuration - id is null.");
        }

        if(oldSshConfiguration.Scope != SSHConfigurationScope.COMPUTER || newSshConfiguration.Scope != SSHConfigurationScope.COMPUTER)
        {
            throw new IllegalArgumentException("Unable to update computer ssh configuration - wrong scope(s) provided.");
        }

        if(oldSshConfiguration.HasSetRequiredFields() == false || newSshConfiguration.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to update computer ssh configuration - not all required fields are set.");
        }
    }

    // ---  COMPUTER SCOPE - REMOVE  -----------------------------------------------------------------------------------

    public void RemoveSSHConfigurationWithComputerScopeFromDb(Session session, SSHConfiguration sshConfiguration)
            throws DatabaseException, IllegalArgumentException
    {
        Validate_RemoveSSHConfigurationWithComputerScope(sshConfiguration);

        String attemptErrorMessage = "[ERROR] SSHConfigurationsManager: " +
                "Attempt of removing computer ssh configuration from db failed.";
        boolean removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, sshConfiguration, attemptErrorMessage);
        if(removeSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to remove computer ssh configuration from db.");
        }
    }

    private void Validate_RemoveSSHConfigurationWithComputerScope(SSHConfiguration sshConfiguration)
            throws IllegalArgumentException
    {
        if(sshConfiguration.Id == null)
        {
            throw new IllegalArgumentException("Unable to remove computer ssh configuration - id is null.");
        }

        if(sshConfiguration.Scope != SSHConfigurationScope.COMPUTER)
        {
            throw new IllegalArgumentException("Unable to remove global ssh configuration - wrong scope provided.");
        }

        if(sshConfiguration.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("Unable to remove computer ssh configuration - " +
                    "not all required fields are set.");
        }
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

    private List<SSHConfiguration> GetGlobalSSHConfigurationsFromDb()
    {
        String attemptErrorMessage = "[ERROR] SSHConfigurationsManager: " +
                "Attempt of getting global SSH configurations from db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();

        String hql = "from SSHConfiguration c where c.Scope = :ConfigScope";
        Query query = session.createQuery(hql);
        query.setParameter("ConfigScope", SSHConfigurationScope.GLOBAL);
        List<SSHConfiguration> globalSSHConfigurations
                = DatabaseManager.ExecuteSelectQueryWithRetryPolicy(session, query, attemptErrorMessage);

        session.close();

        if(globalSSHConfigurations != null)
        {
            return globalSSHConfigurations;
        }
        else
        {
            throw new DatabaseException("[FATAL ERROR] : Unable to get global SSH configurations from db.");
        }
    }

    public SSHConfiguration GetGlobalSSHConfigurationByName(String name)
    {
        List<SSHConfiguration> results = _globalSSHConfigurations.stream()
                .filter(c -> c.Name.equals(name)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public SSHConfiguration GetGlobalSSHConfigurationWithIdUsingOther(SSHConfiguration sshConfiguration)
    {
        if(sshConfiguration.Scope == SSHConfigurationScope.COMPUTER)
        {
            return null;
        }

        List<SSHConfiguration> results = _globalSSHConfigurations.stream()
                .filter(c -> c.Name.equals(sshConfiguration.Name)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public boolean GlobalSSHConfigurationExists(SSHConfiguration sshConfiguration)
    {
        if(sshConfiguration.Scope != SSHConfigurationScope.GLOBAL)
        {
            return false;
        }

        List<SSHConfiguration> results = _globalSSHConfigurations.stream()
                .filter(c -> c.Name.equals(sshConfiguration.Name)).collect(Collectors.toList());
        return results.size() != 0;
    }
}

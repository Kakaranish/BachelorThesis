package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Entities.SshConfigScope;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class SshConfigsManager
{
    private List<SshConfig> _globalSshConfigs;

    public SshConfigsManager()
    {
        _globalSshConfigs = GetGlobalSshConfigsFromDb();
    }

//    // ---  GLOBAL SCOPE - ADD  ----------------------------------------------------------------------------------------
//
//    public void AddGlobalSshConfig(SshConfig sshConfig) throws IllegalArgumentException, DatabaseException
//    {
//        Validate_AddGlobalSshConfig(sshConfig);
//
//        try
//        {
//            AddGlobalConfigToDb(sshConfig);
//            _globalSshConfigs.add(sshConfig);
//        }
//        catch (DatabaseException e)
//        {
//            throw e;
//        }
//    }
//
//    private void Validate_AddGlobalSshConfig(SshConfig sshConfig)
//            throws IllegalArgumentException
//    {
//        if(sshConfig.Scope != SshConfigScope.GLOBAL)
//        {
//            throw new IllegalArgumentException("Unable to add global ssh configuration - wrong scope provided.");
//        }
//
//        if(sshConfig.HasSetRequiredFields() == false)
//        {
//            throw new IllegalArgumentException("Unable to add global ssh configuration - " +
//                    "not all required fields are set.");
//        }
//
//        if(GlobalSshConfigExists(sshConfig))
//        {
//            throw new IllegalArgumentException("Unable to add global ssh configuration - " +
//                    "same configuration already exists.");
//        }
//    }
//
//    private void AddGlobalConfigToDb(SshConfig sshConfiguration) throws DatabaseException
//    {
//        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
//                "Attempt of adding global ssh configuration to db failed.";
//
//        Session session = DatabaseManager.GetInstance().GetSession();
//        boolean persistSucceed = DatabaseManager.PersistWithRetryPolicy(session, sshConfiguration, attemptErrorMessage);
//        session.close();
//
//        if(persistSucceed == false)
//        {
//            throw new DatabaseException("Unable to add global ssh configuration to db.");
//        }
//    }
//
//    // ---  GLOBAL SCOPE - UPDATE  -------------------------------------------------------------------------------------
//
//    public void UpdateGlobalSshConfig(SshConfig oldSshConfig, SshConfig newSshConfig)
//            throws IllegalArgumentException, DatabaseException
//    {
//        Validate_UpdateGlobalSshConfig(oldSshConfig, newSshConfig);
//
//        try
//        {
//            UpdateGlobalSshConfigInDb(oldSshConfig, newSshConfig);
//        }
//        catch (DatabaseException e)
//        {
//            throw e;
//        }
//    }
//
//    private void Validate_UpdateGlobalSshConfig(SshConfig oldSshConfig, SshConfig newSshConfig)
//            throws IllegalArgumentException
//    {
//        if(oldSshConfig.Id == null || GlobalSshConfigExists(oldSshConfig) == false)
//        {
//            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
//                    "configuration to update cannot be found in db.");
//        }
//
//        if(newSshConfig.Id != oldSshConfig.Id)
//        {
//            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
//                    "ssh configuration have different ids.");
//        }
//
//        if(oldSshConfig.HasSetRequiredFields() == false || newSshConfig.HasSetRequiredFields() == false)
//        {
//            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
//                    "not all required fields are set.");
//        }
//
//        if(oldSshConfig.Name.equals(newSshConfig.Name) == false && GlobalSshConfigExists(newSshConfig))
//        {
//            throw new IllegalArgumentException("Unable to update global ssh configuration - " +
//                    "configuration with given name already exists.");
//        }
//
//        if(oldSshConfig.HasGlobalScope() == false || newSshConfig.HasGlobalScope() == false)
//        {
//            throw new IllegalArgumentException("Unable to update global ssh configuration - wrong scope(s) provided.");
//        }
//    }
//
//    private void UpdateGlobalSshConfigInDb(SshConfig oldSshConfig, SshConfig newSshConfig)
//            throws DatabaseException
//    {
//        SshConfig oldSshConfigBackup = new SshConfig(oldSshConfig);
//
//        oldSshConfig.CopyFrom(newSshConfig);
//
//        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
//                "Attempt of updating global ssh configuration in db failed.";
//        Session session = DatabaseManager.GetInstance().GetSession();
//        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, oldSshConfig, attemptErrorMessage);
//        session.close();
//
//        if(updateSucceed == false)
//        {
//            oldSshConfig.CopyFrom(oldSshConfigBackup);
//            throw new DatabaseException("Unable to update global ssh configuration in db.");
//        }
//    }
//
//    // ---  GLOBAL SCOPE - REMOVE  -------------------------------------------------------------------------------------
//
//    public void RemoveGlobalSshConfig(SshConfig sshConfig, ComputerManager computerManager)
//    {
//        Validate_RemoveGlobalSshConfig(sshConfig);
//
//        try
//        {
//            RemoveGlobalSshConfigFromDb(sshConfig, computerManager);
//        }
//        catch (DatabaseException e)
//        {
//            throw e;
//        }
//    }
//
//    // TODO: Backup probably it's okay but check in computer manager function: RemoveComputerGlobalSshConfigAndReplaceWithLocal
//    private void RemoveGlobalSshConfigFromDb(SshConfig sshConfig, ComputerManager computerManager)
//            throws DatabaseException
//    {
//        List<Computer> computersWithGivenGlobalSshConfig = new ArrayList<>(sshConfig.GetComputers());
//        List<SshConfig> sshConfigsBackup = new ArrayList<>(
//                computersWithGivenGlobalSshConfig.stream().map(c -> c.SshConfig).collect(Collectors.toList()));
//
//        Session session = DatabaseManager.GetInstance().GetSession();
//
//        for (Computer computer : computersWithGivenGlobalSshConfig)
//        {
//            sshConfig.RemoveComputer(computer);
//            computerManager.RemoveComputerGlobalSshConfigAndReplaceWithLocal(session, computer);
//        }
//
//        String removeAttemptErrorMessage = "[ERROR] SshConfigsManager: " +
//                "Attempt of removing global ssh configuration from db failed.";
//        boolean removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, sshConfig, removeAttemptErrorMessage);
//        session.close();
//
//        if(removeSucceed == false)
//        {
//            for (int i = 0; i < computersWithGivenGlobalSshConfig.size(); ++i)
//            {
//                computersWithGivenGlobalSshConfig.get(i).SshConfig.CopyFrom(sshConfigsBackup.get(i));
//                sshConfig.AddComputer(computersWithGivenGlobalSshConfig.get(i));
//            }
//            throw new DatabaseException("Unable to remove global ssh configuration from db.");
//        }
//    }
//
//    private void Validate_RemoveGlobalSshConfig(SshConfig sshConfig)
//            throws IllegalArgumentException
//    {
//        if(sshConfig.HasGlobalScope() == false)
//        {
//            throw new IllegalArgumentException("Unable to remove global ssh configuration - " +
//                    "configuration has not global scope.");
//        }
//
//        if(sshConfig.Id == null || sshConfig.Name == null || GlobalSshConfigExists(sshConfig) == false)
//        {
//            throw new IllegalArgumentException("Unable to remove global ssh configuration - " +
//                    "configuration cannot be found in db.");
//        }
//    }
//
//    // ---  LOCAL SCOPE - ADD  --------------------------------------------------------------------------------------
//
//    public void AddLocalSshConfigToDb(Session session, SshConfig sshConfig)
//            throws DatabaseException, IllegalArgumentException
//    {
//        Validate_AddLocalSshConfig(sshConfig);
//
//        if(sshConfig.Id != null)
//        {
//            sshConfig.Id = null;
//        }
//
//        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
//                "Attempt of adding local ssh configuration to db failed.";
//        boolean persistSucceed = DatabaseManager.PersistWithRetryPolicy(session, sshConfig, attemptErrorMessage);
//        if(persistSucceed == false)
//        {
//            throw new DatabaseException("Unable to add local ssh configuration to db.");
//        }
//    }
//
//    private void Validate_AddLocalSshConfig(SshConfig sshConfig) throws IllegalArgumentException
//    {
//        if(sshConfig.HasLocalScope() == false)
//        {
//            throw new IllegalArgumentException("Unable to add local ssh configuration - wrong scope provided.");
//        }
//
//        if(sshConfig.HasSetRequiredFields() == false)
//        {
//            throw new IllegalArgumentException("Unable to add local ssh configuration - not all required fields are set.");
//        }
//    }
//
//
//    // ---  LOCAL SCOPE - UPDATE  -----------------------------------------------------------------------------------
//
//    public void UpdateLocalSshConfigInDb(Session session, SshConfig oldSshConfig, SshConfig newSshConfig)
//            throws DatabaseException, IllegalArgumentException
//    {
//        SshConfig oldSshConfigBackup = new SshConfig(oldSshConfig);
//        Validate_UpdateLocalSshConfig(oldSshConfig, newSshConfig);
//
//        if(newSshConfig.Name != null)
//        {
//            newSshConfig.Name = null;
//        }
//
//        oldSshConfig.CopyFrom(newSshConfig);
//
//        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
//                "Attempt of updating local ssh configuration in db failed.";
//        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, oldSshConfig, attemptErrorMessage);
//        if(updateSucceed == false)
//        {
//            oldSshConfig.CopyFrom(oldSshConfigBackup);
//            throw new DatabaseException("Unable to update local ssh configuration in db.");
//        }
//    }
//
//    private void Validate_UpdateLocalSshConfig(SshConfig oldSshConfig, SshConfig newSshConfig)
//            throws IllegalArgumentException
//    {
//        if(oldSshConfig.Id == null)
//        {
//            throw new IllegalArgumentException("Unable to update local ssh configuration - id is null.");
//        }
//
//        if(oldSshConfig.HasLocalScope() == false || newSshConfig.HasLocalScope() == false)
//        {
//            throw new IllegalArgumentException("Unable to update local ssh configuration - wrong scope(s) provided.");
//        }
//
//        if(oldSshConfig.HasSetRequiredFields() == false || newSshConfig.HasSetRequiredFields() == false)
//        {
//            throw new IllegalArgumentException("Unable to update local ssh configuration - " +
//                    "not all required fields are set.");
//        }
//    }
//
//    // ---  LOCAL SCOPE - REMOVE  -----------------------------------------------------------------------------------
//
//    public void RemoveLocalSshConfigFromDb(Session session, SshConfig sshConfig)
//            throws DatabaseException, IllegalArgumentException
//    {
//        Validate_RemoveLocalSshConfig(sshConfig);
//
//        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
//                "Attempt of removing local ssh configuration from db failed.";
//        boolean removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, sshConfig, attemptErrorMessage);
//        if(removeSucceed == false)
//        {
//            throw new DatabaseException("Unable to remove local ssh configuration from db.");
//        }
//    }
//
//    private void Validate_RemoveLocalSshConfig(SshConfig sshConfig)
//            throws IllegalArgumentException
//    {
//        if(sshConfig.Id == null)
//        {
//            throw new IllegalArgumentException("Unable to remove local ssh configuration - id is null.");
//        }
//
//        if(sshConfig.HasLocalScope() == false)
//        {
//            throw new IllegalArgumentException("Unable to remove local ssh configuration - wrong scope provided.");
//        }
//
//        if(sshConfig.HasSetRequiredFields() == false)
//        {
//            throw new IllegalArgumentException("Unable to remove local ssh configuration - " +
//                    "not all required fields are set.");
//        }
//    }
//
//    // ---  MISC  ------------------------------------------------------------------------------------------------------
//
//    public boolean GlobalSshConfigExists(SshConfig sshConfig)
//    {
//        if(sshConfig.HasGlobalScope() == false)
//        {
//            return false;
//        }
//
//        List<SshConfig> results = _globalSshConfigs.stream()
//                .filter(c -> c.Name.equals(sshConfig.Name)).collect(Collectors.toList());
//        return results.size() != 0;
//    }
//
//    // ---  GETTERS  ---------------------------------------------------------------------------------------------------
//
    private List<SshConfig> GetGlobalSshConfigsFromDb()
    {
        String attemptErrorMessage = "[ERROR] SshConfigsManager: " +
                "Attempt of getting global ssh configurations from db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();

        String hql = "from SshConfig c where c.Scope = :ConfigScope";
        Query query = session.createQuery(hql);
        query.setParameter("ConfigScope", SshConfigScope.GLOBAL);
        List<SshConfig> globalSSHConfigurations
                = DatabaseManager.ExecuteSelectQueryWithRetryPolicy(session, query, attemptErrorMessage);

        session.close();

        if(globalSSHConfigurations != null)
        {
            return globalSSHConfigurations;
        }
        else
        {
            throw new DatabaseException("Unable to get global ssh configurations from db.");
        }
    }

    public SshConfig GetGlobalSshConfigByName(String name)
    {
        List<SshConfig> results = _globalSshConfigs.stream()
                .filter(c -> c.GetName().equals(name)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }
//

}

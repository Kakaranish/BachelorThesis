package Healthcheck.Entities;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.Utilities;
import org.hibernate.Session;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SSH_Configurations", uniqueConstraints = {@UniqueConstraint(columnNames = {"Name"})})
public class SshConfig
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(unique = true)
    private String Name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SshConfigScope Scope;

    @Column(nullable = false)
    private Integer Port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SshAuthMethod AuthMethod;

    @Column(nullable = false)
    private String Username;

    private String PrivateKeyPath;

    private String EncryptedPassword;

    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "SshConfig", fetch = FetchType.EAGER)
    private List<Computer> _computers = new ArrayList<>();

    @Transient
    private SshConfig _prevState;

    @Transient
    private boolean _existsInDb = true;

    @Transient
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;

    public void SetComputersAndSshConfigsManager(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
    }

    private SshConfig()
    {
    }

    public static SshConfig CreateEmpty()
    {
        return new SshConfig(
                null,
                SshConfigScope.LOCAL,
                null,
                SshAuthMethod.PASSWORD,
                null,
                null);
    }

    public SshConfig(
            String name,
            SshConfigScope scope,
            Integer port,
            SshAuthMethod authMethod,
            String username,
            String keyPathOrEncryptedPassword)
            throws IllegalArgumentException
    {
        if(scope == null)
        {
            throw new IllegalArgumentException("Scope is null.");
        }

        if(authMethod == null)
        {
            throw new IllegalArgumentException("Authentication method is null.");
        }

        if(HasGlobalScope() && (name == null || name.trim().equals("")))
        {
            throw new IllegalArgumentException("If scope is global, name cannot be empty or null.");
        }

        Name = name;
        Scope = scope;
        Port = port;
        AuthMethod = authMethod;
        Username = username;

        if(HasLocalScope())
        {
            Name = null;
        }

        if(HasPasswordAuth())
        {
            EncryptedPassword = keyPathOrEncryptedPassword;
        }
        else
        {
            PrivateKeyPath = keyPathOrEncryptedPassword;
        }

        _existsInDb = false;
}

    // Copy Constructor
    public SshConfig(SshConfig otherSSHConfiguration)
    {
        Id = otherSSHConfiguration.Id;
        Name = otherSSHConfiguration.Name;
        Scope = otherSSHConfiguration.Scope;
        Port = otherSSHConfiguration.Port;
        AuthMethod = otherSSHConfiguration.AuthMethod;
        Username = otherSSHConfiguration.Username;
        PrivateKeyPath = otherSSHConfiguration.PrivateKeyPath;
        EncryptedPassword = otherSSHConfiguration.EncryptedPassword;

        _computers = otherSSHConfiguration._computers;
        _prevState = otherSSHConfiguration._prevState;
        _existsInDb = otherSSHConfiguration._existsInDb;
        _computersAndSshConfigsManager = otherSSHConfiguration._computersAndSshConfigsManager;
    }

    public void Restore()
    {
        Id = _prevState.Id;
        Name = _prevState.Name;
        Scope = _prevState.Scope;
        Port = _prevState.Port;
        AuthMethod = _prevState.AuthMethod;
        Username = _prevState.Username;
        PrivateKeyPath = _prevState.PrivateKeyPath;
        EncryptedPassword = _prevState.EncryptedPassword;

        _computers = _prevState._computers;
        _existsInDb = _prevState._existsInDb;
        _computersAndSshConfigsManager = _prevState._computersAndSshConfigsManager;

        _prevState = null;
    }

    public void CopyAdjustableFieldsFrom(SshConfig otherSSHConfiguration)
    {
        TryToSetPrevStateIfNotExisting();

        Name = otherSSHConfiguration.Name;
        Scope = otherSSHConfiguration.Scope;
        Port = otherSSHConfiguration.Port;
        AuthMethod = otherSSHConfiguration.AuthMethod;
        Username = otherSSHConfiguration.Username;
        PrivateKeyPath = otherSSHConfiguration.PrivateKeyPath;
        EncryptedPassword = otherSSHConfiguration.EncryptedPassword;
    }

    public void CopyFrom(SshConfig otherSSHConfiguration)
    {
        Name = otherSSHConfiguration.Name;
        Scope = otherSSHConfiguration.Scope;
        Port = otherSSHConfiguration.Port;
        AuthMethod = otherSSHConfiguration.AuthMethod;
        Username = otherSSHConfiguration.Username;
        PrivateKeyPath = otherSSHConfiguration.PrivateKeyPath;
        EncryptedPassword = otherSSHConfiguration.EncryptedPassword;

        _computers = new ArrayList<>(otherSSHConfiguration._computers);

        if(otherSSHConfiguration._prevState != null)
        {
            _prevState = new SshConfig(otherSSHConfiguration._prevState);
        }
        _existsInDb = otherSSHConfiguration._existsInDb;
    }

    public void CopyIdFrom(SshConfig otherSshConfig)
    {
        Id = otherSshConfig.Id;
    }

    // ---  ADD TO DB  -------------------------------------------------------------------------------------------------

    public void AddToDb() throws SshConfigException, DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        AddToDb(session);
        session.close();
    }

    public void AddToDb(Session session) throws SshConfigException, DatabaseException
    {
        Validate_AddToDb(session);

        String attemptErrorMessage = "[ERROR] SshConfig: Attempt of adding ssh config to db failed.";
        boolean addSucceed = DatabaseManager.PersistWithRetryPolicy(session, this, attemptErrorMessage);
        if(addSucceed == false)
        {
            throw new DatabaseException("Unable to save ssh config in db.");
        }

        _existsInDb = true;

        if(_computersAndSshConfigsManager != null)
        {
            _computersAndSshConfigsManager.AddedSshConfig(this);
        }
    }

    private void Validate_AddToDb(Session session)
    {
        if(_existsInDb)
        {
            throw new SshConfigException("Ssh configuration exists in db.");
        }

        if(HasLocalScope() && _computers.isEmpty())
        {
            throw new SshConfigException("Local ssh config must be associated with computer.");
        }

        if(_computersAndSshConfigsManager == null)
        {
            if(HasGlobalScope() && GlobalSshConfigWithNameExists(session, Name))
            {
                throw new SshConfigException("Ssh config with '" + Name + "' name already exists in db.");
            }
        }
        else
        {
            if(HasGlobalScope() && _computersAndSshConfigsManager.OtherGlobalSshConfigWithNameExists(this, Name))
            {
                throw new SshConfigException("Ssh config with '" + Name + "' name already exists in db.");
            }
        }
    }

    // ---  UPDATE IN DB  ----------------------------------------------------------------------------------------------

    public void UpdateInDb() throws NothingToDoException, SshConfigException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        UpdateInDb(session);
        session.close();
    }

    public void UpdateInDb(Session session) throws NothingToDoException, SshConfigException, DatabaseException
    {
        Validate_UpdateInDb();

        String attemptErrorMessage = "[ERROR] SshConfig: Attempt of update ssh config in db failed.";
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, this, attemptErrorMessage);
        if(updateSucceed == false)
        {
            throw new DatabaseException("Unable to update ssh config in db.");
        }

        _prevState = null;
    }

    public void Validate_UpdateInDb()
    {
        if(_prevState == null)
        {
            throw new NothingToDoException("Previous state is yet not available.");
        }

        if(_existsInDb == false)
        {
            throw new SshConfigException("Ssh config does not exist in db.");
        }

        if(ScopeChanged())
        {
            throw new SshConfigException("Scope of ssh config cannot be changed.");
        }

        if(this.equals(_prevState))
        {
            throw new NothingToDoException("Previous state is equal to current state.");
        }
    }

    // ---  REMOVE FROM DB  --------------------------------------------------------------------------------------------

    public void RemoveLocalFromDb(Session session) throws SshConfigException, DatabaseException
    {
        Validate_RemoveLocalFromDb();

        String removeAttemptErrorMessage = "[ERROR] SshConfig: Attempt of removing local ssh config from db failed.";
        boolean removeSucceed =
                DatabaseManager.RemoveWithRetryPolicy(session, this, removeAttemptErrorMessage);

        if(removeSucceed == false)
        {
            throw new DatabaseException("Unable to remove local ssh config in db.");
        }

        if(_computersAndSshConfigsManager != null)
        {
            _computersAndSshConfigsManager.RemovedSshConfig(this);
        }

        Id = null;
        _existsInDb = false;
        _prevState = null;
    }

    private void Validate_RemoveLocalFromDb() throws SshConfigException
    {
        if(_prevState != null)
        {
            throw new SshConfigException("Ssh config was changed. Restore changes to remove it.");
        }

        if(_existsInDb == false)
        {
            throw new SshConfigException("Ssh config does not exist in db.");
        }
    }

    public void RemoveGlobalFromDb() throws SshConfigException, DatabaseException, FatalErrorException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        RemoveGlobalFromDb(session);
        session.close();
    }

    public void RemoveGlobalFromDb(Session session) throws SshConfigException, DatabaseException, FatalErrorException
    {
        Validate_RemoveGlobalFromDb();

        List<Computer> computers = new ArrayList<>(_computers);
        for (Computer computer : computers)
        {
            SshConfig newLocalSshConfig = new SshConfig();
            newLocalSshConfig.AuthMethod = computer.GetSshConfig().AuthMethod;
            newLocalSshConfig.Scope = SshConfigScope.LOCAL;
            newLocalSshConfig.EncryptedPassword = computer.GetSshConfig().EncryptedPassword;
            newLocalSshConfig.Name = null;
            newLocalSshConfig.Port = computer.GetSshConfig().Port;
            newLocalSshConfig.PrivateKeyPath = computer.GetSshConfig().PrivateKeyPath;
            newLocalSshConfig.Username = computer.GetSshConfig().Username;
            newLocalSshConfig._existsInDb = false;
            newLocalSshConfig._computers = new ArrayList<>();
            computer.SetSshConfig(newLocalSshConfig);

            computer.UpdateInDb(session);
        }

        String removeAttemptErrorMessage = "[ERROR] SshConfig: Attempt of removing global ssh config from db failed.";
        boolean removeSucceed =
                DatabaseManager.RemoveWithRetryPolicy(session, this, removeAttemptErrorMessage);
        if(removeSucceed == false)
        {
            throw new DatabaseException("Unable to remove global ssh config in db.");
        }

        if(_computersAndSshConfigsManager != null)
        {
            _computersAndSshConfigsManager.RemovedSshConfig(this);
        }

        Id = null;
        _existsInDb = false;
        _prevState = null;
    }

    private void Validate_RemoveGlobalFromDb() throws SshConfigException
    {
        if(_prevState != null)
        {
            throw new SshConfigException("Ssh config was changed. Restore changes to remove it.");
        }

        if(HasLocalScope())
        {
            throw new SshConfigException("Ssh config has local scope.");
        }

        if(_existsInDb == false)
        {
            throw new SshConfigException("Ssh config does not exist in db.");
        }
    }

    private void RestorePersistedSshConfigs(Session session, List<SshConfig> persistedConfigs)
            throws FatalErrorException
    {
        for (SshConfig persistedSshConfig : persistedConfigs)
        {
            persistedSshConfig._computers.get(0).SetSshConfig(this);
            _computersAndSshConfigsManager.RemovedSshConfig(persistedSshConfig);

            String restoreAttemptErrorMessage =
                    "[ERROR] SshConfig: Attempt of removing new local ssh config in db failed.";
            boolean restoreSucceed = DatabaseManager
                    .RemoveWithRetryPolicy(session, persistedSshConfig, restoreAttemptErrorMessage);
            if(restoreSucceed == false)
            {
                throw new FatalErrorException("Restoring ssh configs after computer adding failed!");
            }
        }
    }

    // ---  ONE TO MANY - COMPUTERS  -----------------------------------------------------------------------------------

    public void AddComputer(Computer computer)
    {
        _computers.add(computer);
    }

    public void RemoveComputer(Computer computer)
    {
        _computers.remove(computer);
    }

    public List<Computer> GetComputers()
    {
        return _computers;
    }

    public void ResetComputers()
    {
        _computers = new ArrayList<>();
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

    public Integer GetId()
    {
        return Id;
    }

    public String GetName()
    {
        return Name;
    }

    public SshConfigScope GetScope()
    {
        return Scope;
    }

    public Integer GetPort()
    {
        return Port;
    }

    public SshAuthMethod GetAuthMethod()
    {
        return AuthMethod;
    }

    public String GetUsername()
    {
        return Username;
    }

    public String GetPrivateKeyPath()
    {
        return PrivateKeyPath;
    }

    public String GetEncryptedPassword()
    {
        return EncryptedPassword;
    }

    public SshConfig GetPreviousState()
    {
        return _prevState;
    }

    // ---  SETTERS  ---------------------------------------------------------------------------------------------------

    public void ResetPreviousState()
    {
        _prevState = null;
    }

    private void TryToSetPrevStateIfNotExisting()
    {
        if(_existsInDb &&_prevState == null)
        {
            SetPreviousState(this);
        }
    }

    private void SetPreviousState(SshConfig sshConfig)
    {
        _prevState = new SshConfig(sshConfig);
        _prevState._prevState = null;
        _prevState._computers = null;
    }

    private void ResetId()
    {
        Id = null;
    }

    public void SetUsername(String username)
    {
        if(username == null || username.trim().equals(""))
        {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }

        if(Utilities.AreEqual(Username, username))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Username = username;
    }

    public void SetPort(Integer port)
    {
        if(port < 0)
        {
            throw new IllegalArgumentException("Port cannot be negative.");
        }

        if(Port.intValue() == port)
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Port = port;
    }

    public void SetPasswordAuthMethod(String encryptedPassword)
    {
        if(encryptedPassword == null || encryptedPassword.trim().equals(""))
        {
            throw new IllegalArgumentException("Encrypted password cannot be null or empty.");
        }

        if(HasPasswordAuth() && Utilities.AreEqual(EncryptedPassword, encryptedPassword))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        AuthMethod = SshAuthMethod.PASSWORD;
        EncryptedPassword = encryptedPassword;
        PrivateKeyPath = null;
    }

    public void SetSshKeyAuthMethod(String privateKeyPath)
    {
        if(privateKeyPath == null || privateKeyPath.trim().equals(""))
        {
            throw new IllegalArgumentException("Private key path cannot be null or empty.");
        }

        if(HasPrivateKeyPath() && Utilities.AreEqual(PrivateKeyPath, privateKeyPath))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        AuthMethod = SshAuthMethod.KEY;
        PrivateKeyPath = privateKeyPath;
        EncryptedPassword = null;
    }

    public void SetLocalScope()
    {
        if(HasLocalScope())
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Scope = SshConfigScope.LOCAL;
        Name = null;
    }

    public void SetGlobalScope(String name)
    {
        if(HasGlobalScope() && Utilities.AreEqual(Name, name))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Scope = SshConfigScope.GLOBAL;
        Name = null;
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public boolean Changed()
    {
        return _prevState != null;
    }

    public void ConvertToNonExistingInDb()
    {
        Id = null;
        _computers = null;
        _existsInDb = false;
        _computersAndSshConfigsManager = null;
    }

    private boolean GlobalSshConfigWithNameExists(Session session, String name)
    {
        Query query = session.createQuery("select 1 from SshConfig t where t.DisplayedName = :name");
        query.setParameter("name", name);
        return (((org.hibernate.query.Query) query).uniqueResult() != null);
    }

    private boolean ScopeChanged()
    {
        return (HasGlobalScope() && _prevState.HasLocalScope()) || (HasLocalScope() && _prevState.HasGlobalScope());
    }

    public boolean HasPasswordAuth()
    {
        return AuthMethod == SshAuthMethod.PASSWORD;
    }

    public boolean HasPrivateKeyPath()
    {
        return AuthMethod == SshAuthMethod.KEY;
    }

    public boolean HasLocalScope()
    {
        return Scope == SshConfigScope.LOCAL;
    }

    public boolean HasGlobalScope()
    {
        return Scope == SshConfigScope.GLOBAL;
    }

    public boolean ExistsInDb()
    {
        return _existsInDb;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        if(this == obj)
        {
            return true;
        }

        SshConfig other = (SshConfig) obj;

        return  this.Id == other.Id &&
                Utilities.AreEqual(this.Name, other.Name) &&
                Utilities.AreEqual(this.Scope, other.Scope) &&
                Utilities.AreEqual(this.Port, other.Port) &&
                Utilities.AreEqual(this.AuthMethod, other.AuthMethod) &&
                Utilities.AreEqual(this.Username, other.Username) &&
                Utilities.AreEqual(this.PrivateKeyPath, other.PrivateKeyPath) &&
                Utilities.AreEqual(this.EncryptedPassword, other.EncryptedPassword);
    }

    @Override
    public String toString()
    {
        if(HasLocalScope())
        {
            return "LOCAL - Username[" + Username + "], Port[" + Port + "]";
        }
        else
        {
            return "GLOBAL - ConfigName[" + Name + "], Username[" + Username + "], Port[" + Port + "]";
        }
    }
}

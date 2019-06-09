package Healthcheck.Entities;

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

    //TODO: May be lazy?
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "SshConfig", fetch = FetchType.EAGER)
    private List<Computer> _computers = new ArrayList<>();

    @Transient
    private SshConfig _prevState;

    @Transient
    private boolean _existsInDb = true;

    private SshConfig()
    {
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

        if(scope == SshConfigScope.GLOBAL && (name == null || name.trim().equals("")))
        {
            throw new IllegalArgumentException("If scope is global, name cannot be empty or null.");
        }

        Name = name;
        Scope = scope;
        Port = port;
        AuthMethod = authMethod;
        Username = username;

        if(Scope == SshConfigScope.LOCAL)
        {
            Name = null;
        }

        if(AuthMethod == SshAuthMethod.PASSWORD)
        {
            EncryptedPassword = keyPathOrEncryptedPassword;
        }
        else if(AuthMethod == SshAuthMethod.KEY)
        {
            PrivateKeyPath = keyPathOrEncryptedPassword;
        }

        SetPreviousState(this);

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

        _computers = new ArrayList<>(otherSSHConfiguration._computers);

        if(otherSSHConfiguration._prevState != null)
        {
            _prevState = new SshConfig(otherSSHConfiguration._prevState);
        }
        _existsInDb = otherSSHConfiguration._existsInDb;

        // TODO: Check
//        _prevState
    }

    public void CopyFrom(SshConfig otherSSHConfiguration)
    {
        Id = otherSSHConfiguration.Id;
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

    // ---  ACTIONS RELATED TO DATABASE   ------------------------------------------------------------------------------

    public void AddToDb() throws SshConfigException, DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        AddToDb(session);
        session.close();
    }

    public void AddToDb(Session session) throws SshConfigException, DatabaseException
    {
        if(_existsInDb)
        {
            throw new SshConfigException("Ssh configuration exists in db.");
        }

        if(Id != null)
        {
            throw new SshConfigException("Id is not null.");
        }

        if(HasGlobalScope() && GlobalSshConfigWithNameExists(session, Name))
        {
            throw new SshConfigException("Ssh config with '" + Name + "' name already exists in db.");
        }

        String attemptErrorMessage = "[ERROR] SshConfig: Attempt of adding ssh config to db failed.";
        boolean addSucceed = DatabaseManager.PersistWithRetryPolicy(session, this, attemptErrorMessage);
        if(addSucceed == false)
        {
            throw new DatabaseException("Unable to save ssh config in db.");
        }

        _prevState = new SshConfig(this);
        _existsInDb = true;
    }

    public void UpdateInDb() throws NothingToDoException, SshConfigException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        UpdateInDb(session);
        session.close();
    }

    public void UpdateInDb(Session session) throws NothingToDoException, SshConfigException
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

        String attemptErrorMessage = "[ERROR] SshConfig: Attempt of update ssh config in db failed.";
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, this, attemptErrorMessage);
        if(updateSucceed == false)
        {
            throw new DatabaseException("Unable to update ssh config in db.");
        }
    }

    public void RemoveFromDb() throws SshConfigException, DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        RemoveFromDb(session);
        session.close();
    }

    public void RemoveFromDb(Session session) throws SshConfigException, DatabaseException
    {
        if(_existsInDb == false)
        {
            throw new SshConfigException("Ssh config does not exist in db.");
        }

        if(HasGlobalScope())
        {
            for (Computer computer : _computers)
            {
                SshConfig newLocalSshConfig = new SshConfig(this);
                newLocalSshConfig.ResetId();
                newLocalSshConfig.SetLocalScope();
                newLocalSshConfig.ResetComputers();
                newLocalSshConfig.AddComputer(computer);
                computer.SshConfig = newLocalSshConfig;

                String attemptErrorMessage = "[ERROR] SshConfig: Attempt of adding new local ssh config in db failed.";
                boolean saveSucceed =
                        DatabaseManager.PersistWithRetryPolicy(session, newLocalSshConfig, attemptErrorMessage);

                if(saveSucceed == false)
                {
                    for (Computer computerToRestore : _computers)
                    {
                        computerToRestore.SshConfig = this;
                    }

                    throw new DatabaseException("Unable to save new local ssh config(s) in db.");
                }
            }
        }

        String attemptErrorMessage = "[ERROR] SshConfig: Attempt of removing global ssh config from db failed.";
        boolean removeSucceed;
        if(_prevState != null)
        {
            removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, _prevState, attemptErrorMessage);
        }
        else
        {
            removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, this, attemptErrorMessage);
        }

        if(removeSucceed == false)
        {
            for (Computer computerToRestore : _computers)
            {
                computerToRestore.SshConfig = this;
            }

            throw new DatabaseException("Unable to remove global ssh config in db.");
        }

        Clear();
    }

    // ---  ONE TO MANY - COMPUTERS  -----------------------------------------------------------------------------------

    public void AddComputer(Computer computer)
    {
        _computers.add(computer);
        computer.SshConfig = this;
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


    // ---  SETTERS  ---------------------------------------------------------------------------------------------------

    private void TryToSetPrevStateIfNotExisting()
    {
        if(_prevState == null)
        {
            SetPreviousState(this);
        }
    }

    private void SetPreviousState(SshConfig sshConfig)
    {
        _prevState = new SshConfig(sshConfig);
        _prevState._prevState = null;
    }

    public void RestorePreviousState()
    {
        this.CopyFrom(_prevState);
    }

    private void Clear()
    {
        Id = null;
        Name = null;
        Scope = null;
        Port = null;
        AuthMethod = null;
        Username = null;
        PrivateKeyPath = null;
        EncryptedPassword = null;

        _computers = null;
        _prevState = null;
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

        TryToSetPrevStateIfNotExisting();
        SshConfigChanged();

        Username = username;
    }

    public void SetPort(int port)
    {
        if(port < 0)
        {
            throw new IllegalArgumentException("Port cannot be negative.");
        }

        TryToSetPrevStateIfNotExisting();
        SshConfigChanged();

        Port = port;
    }

    public void SetPasswordAuthMethod(String encryptedPassword)
    {
        if(encryptedPassword == null || encryptedPassword.trim().equals(""))
        {
            throw new IllegalArgumentException("Encrypted password cannot be null or empty.");
        }

        TryToSetPrevStateIfNotExisting();
        SshConfigChanged();

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

        TryToSetPrevStateIfNotExisting();
        SshConfigChanged();

        AuthMethod = SshAuthMethod.KEY;
        PrivateKeyPath = privateKeyPath;
        EncryptedPassword = null;
    }

    private void SetLocalScope()
    {
        TryToSetPrevStateIfNotExisting();
        SshConfigChanged();

        Scope = SshConfigScope.LOCAL;
        Name = null;
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

    public boolean ExistsInDb()
    {
        return _existsInDb;
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    private boolean GlobalSshConfigWithNameExists(Session session, String name)
    {
        Query query = session.createQuery("select 1 from SshConfig t where t.Name = :name");
        query.setParameter("name", name);
        return (((org.hibernate.query.Query) query).uniqueResult() != null);
    }



    private boolean ScopeChanged()
    {
        return (HasGlobalScope() && _prevState.HasLocalScope()) || (HasLocalScope() && _prevState.HasGlobalScope());
    }

    // TODO: To remove
    public boolean HasSetRequiredFields()
    {
        if(AuthMethod == null)
        {
            return false;
        }

        boolean authFieldSet;
        if(AuthMethod == SshAuthMethod.PASSWORD)
        {
            authFieldSet = EncryptedPassword != null;
        }
        else
        {
            authFieldSet = PrivateKeyPath != null;
        }

        if(Scope == null)
        {
            return false;
        }
        boolean nameIsRequired = Scope != SshConfigScope.LOCAL;

        return  !(nameIsRequired && Name == null) &&
                Scope != null &&
                Port != null &&
                Username != null &&
                authFieldSet;
    }

    public boolean HasLocalScope()
    {
        return Scope == SshConfigScope.LOCAL;
    }

    public boolean HasGlobalScope()
    {
        return Scope == SshConfigScope.GLOBAL;
    }

    public void SshConfigChanged()
    {
        for (Computer computer : _computers)
        {
            computer.SshConfigChanged();
        }
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        SshConfig other = (SshConfig) obj;
        return  this.Id == other.Id &&
                Utilities.AreEqual(this.Name, other.Name) &&
                Utilities.AreEqual(this.Scope, other.Scope) &&
                Utilities.AreEqual(this.Port, other.Port) &&
                Utilities.AreEqual(this.AuthMethod, other.AuthMethod) &&
                Utilities.AreEqual(this.Username, other.Username) &&
                Utilities.AreEqual(this.PrivateKeyPath, other.PrivateKeyPath) &&
                Utilities.AreEqual(this.EncryptedPassword, other.EncryptedPassword) &&
                (this._computers == other._computers ||
                        (this._computers.containsAll(other._computers) &&
                                other._computers.containsAll(this._computers)));
    }
}

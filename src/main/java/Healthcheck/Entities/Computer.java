package Healthcheck.Entities;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Utilities;
import org.hibernate.Session;
import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Entity
@Table(name = "Computers")
public class Computer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer Id;

    @Column(nullable = false, unique = true)
    private String DisplayedName;

    @Column(nullable = false, unique = true)
    private String Host;

    @Column(nullable = false)
    private String Classroom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SSHConfiguration_Id", referencedColumnName = "Id", nullable = false)
    private SshConfig SshConfig;

    @Column(nullable = false)
    private Duration MaintainPeriod;

    @Column(nullable = false)
    private Duration RequestInterval;

    @Column(nullable = false)
    private Duration LogExpiration;

    @Column(nullable = false)
    private Timestamp LastMaintenance;

    private boolean IsSelected;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Computer_Preference",
            inverseJoinColumns = { @JoinColumn(name = "Preference_Id", referencedColumnName = "Id") },
            joinColumns = { @JoinColumn(name = "Computer_Id", referencedColumnName = "Id") }
    )
    private List<Preference> Preferences = new ArrayList<>();

    @Transient
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;

    @Transient
    private Computer _prevState;

    @Transient
    private boolean _existsInDb = true;

    // ---  CONSTRUCTORS  ----------------------------------------------------------------------------------------------

    private Computer()
    {
    }

    public Computer(
            String displayedName,
            String host,
            String classroom,
            SshConfig sshConfig,
            Duration requestInterval,
            Duration maintainPeriod,
            Duration logExpiration,
            boolean isSelected)
            throws IllegalArgumentException
    {
        if(Utilities.EmptyOrNull(displayedName) || Utilities.EmptyOrNull(host) || Utilities.EmptyOrNull(classroom)
                || sshConfig == null || requestInterval == null || maintainPeriod == null || logExpiration == null)
        {
            throw new IllegalArgumentException("Some arguments are empty");
        }
        DisplayedName = displayedName;
        Host = host;
        Classroom = classroom;
        SshConfig = sshConfig;
        RequestInterval = requestInterval;
        MaintainPeriod = maintainPeriod;
        LogExpiration = logExpiration;
        LastMaintenance = new Timestamp(System.currentTimeMillis());
        IsSelected = isSelected;

        _existsInDb = false;
    }

    public Computer(Computer computer)
    {
        Id = computer.Id;
        DisplayedName = computer.DisplayedName;
        Host = computer.Host;
        Classroom = computer.Classroom;
        SshConfig = computer.SshConfig;
        MaintainPeriod = computer.MaintainPeriod;
        RequestInterval = computer.RequestInterval;
        LogExpiration = computer.LogExpiration;
        LastMaintenance = computer.LastMaintenance;
        IsSelected = computer.IsSelected;
        Preferences = new ArrayList<>(computer.Preferences);

        _computersAndSshConfigsManager = computer._computersAndSshConfigsManager;
        _prevState = computer._prevState;
        _existsInDb = computer._existsInDb;
    }

    public void Restore() throws SshConfigException
    {
        if(_prevState == null)
        {
            throw new SshConfigException("Nothing to restore.");
        }

        Id = _prevState.Id;
        DisplayedName = _prevState.DisplayedName;
        Host = _prevState.Host;
        Classroom = _prevState.Classroom;
        SshConfig = _prevState.SshConfig;
        MaintainPeriod = _prevState.MaintainPeriod;
        RequestInterval = _prevState.RequestInterval;
        LogExpiration = _prevState.LogExpiration;
        LastMaintenance = _prevState.LastMaintenance;
        IsSelected = _prevState.IsSelected;
        Preferences = _prevState.Preferences;

        _computersAndSshConfigsManager = _prevState._computersAndSshConfigsManager;
        _existsInDb = _prevState._existsInDb;

        _prevState = null;
    }

    // ---  ADD TO DB  -------------------------------------------------------------------------------------------------

    public void AddToDb() throws ComputerException, SshConfigException, DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        AddToDb(session);
        session.close();
    }

    public void AddToDb(Session session) throws ComputerException, SshConfigException, DatabaseException
    {
        Validate_AddToDb(session);

        SshConfig.AddComputer(this);

        if (SshConfig.HasLocalScope())
        {
            SshConfig.AddToDb(session);
            if(_computersAndSshConfigsManager != null)
            {
                _computersAndSshConfigsManager.AddedSshConfig(SshConfig);
            }
        }

        String attemptErrorMessage = "[ERROR] Computer: Attempt of adding computer to db failed.";
        boolean addSucceed = DatabaseManager.PersistWithRetryPolicy(session, this, attemptErrorMessage);
        if(addSucceed == false)
        {
            if(SshConfig.HasLocalScope())
            {
                String attemptErrorMessage2 = "[ERROR] Computer: Attempt of removing local ssh config to db failed.";
                boolean removeSucceed = DatabaseManager.PersistWithRetryPolicy(session, SshConfig, attemptErrorMessage);
                if(removeSucceed == false)
                {
                    throw new FatalErrorException("Removing local ssh config after computer adding failed!");
                }
            }

            throw new DatabaseException("Unable to save computer in db.");
        }

        _existsInDb = true;

        if(_computersAndSshConfigsManager != null)
        {
            _computersAndSshConfigsManager.AddedComputer(this);
        }
    }

    private void Validate_AddToDb(Session session) throws ComputerException, SshConfigException, DatabaseException
    {
        if(_existsInDb)
        {
            throw new ComputerException("Computer exists in db.");
        }

        if(SshConfig == null)
        {
            throw new ComputerException("Ssh config cannot be null. Computer has to have ssh config.");
        }

        if(_computersAndSshConfigsManager == null)
        {
            if(ComputerWithSameDisplayedNameExistsInDb(session, DisplayedName))
            {
                throw new ComputerException("Computer with same displayed name exists in db.");
            }

            if(ComputerWithSameHostExistsInDb(session, Host))
            {
                throw new ComputerException("Computer with same host exists in db.");
            }
        }
        else
        {
            if(_computersAndSshConfigsManager.OtherComputerWithDisplayedNameExists(this, DisplayedName))
            {
                throw new ComputerException("Computer with same displayed name exists in db.");
            }

            if(_computersAndSshConfigsManager.OtherComputerWithHostExists(this, Host))
            {
                throw new ComputerException("Computer with same host exists in db.");
            }
        }

        if(SshConfig.HasGlobalScope() && SshConfig.ExistsInDb() == false)
        {
            throw new ComputerException("Provided global ssh config does not exist in db.");
        }

        if(SshConfig.HasLocalScope() && _existsInDb)
        {
            throw new ComputerException("Provided local ssh config exists in db.");
        }
    }

    // ---  UPDATE IN DB  ----------------------------------------------------------------------------------------------

    public void UpdateInDb() throws NothingToDoException, ComputerException, SshConfigException, DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        UpdateInDb(session);
        session.close();
    }

    public void UpdateInDb(Session session) throws NothingToDoException, ComputerException, SshConfigException, DatabaseException
    {
        Validate_UpdateInDb(session);

        if(SshConfigChanged() == true)
        {
            SshConfig sshConfigBackup;

            if(_prevState.SshConfig.HasLocalScope() && SshConfig.HasLocalScope())
            {
                sshConfigBackup = new SshConfig(_prevState.SshConfig);
                _prevState.SshConfig.CopyAdjustableFieldsFrom(SshConfig);

                try
                {
                    _prevState.SshConfig.UpdateInDb(session);
                }
                catch (DatabaseException|SshConfigException e)
                {
                    _prevState.SshConfig.CopyAdjustableFieldsFrom(sshConfigBackup);
                    _prevState.SshConfig.ResetPreviousState();

                    throw e;
                }

                SshConfig = _prevState.SshConfig;
            }
            else if(_prevState.SshConfig.HasLocalScope() && SshConfig.HasGlobalScope())
            {
                sshConfigBackup = new SshConfig(_prevState.SshConfig);
                _prevState.SshConfig.RemoveLocalFromDb(session);
                _prevState.SshConfig.ResetComputers();

                UpdateComputerWithRetryAndRestorePolicy(session, sshConfigBackup, sshConfigBackupParam ->
                {
                    String attemptErrorMessage = "[ERROR] Computer: Attempt of restore computer's ssh config in db failed.";
                    boolean restoreSucceed
                            = DatabaseManager.MergeWithRetryPolicy(session, sshConfigBackupParam, attemptErrorMessage);
                    if(restoreSucceed == false)
                    {
                        throw new FatalErrorException("Restoring computer's ssh config after retries failed!");
                    }

                    _prevState.SshConfig = sshConfigBackup;
                    _computersAndSshConfigsManager.AddedSshConfig(sshConfigBackup);
                });

                SshConfig.AddComputer(this);
            }
            else if(_prevState.SshConfig.HasGlobalScope() && SshConfig.HasLocalScope())
            {
                if(SshConfig.ExistsInDb())
                {
                    throw new ComputerException("Local ssh config is already assigned to other computer.");
                }

                SshConfig.SetComputersAndSshConfigsManager(_computersAndSshConfigsManager);
                SshConfig.AddComputer(this);
                SshConfig.AddToDb(session);
                _prevState.SshConfig.RemoveComputer(this);

                UpdateComputerWithRetryAndRestorePolicy(session, null, notUsedSshConfig ->
                {
                    try
                    {
                        SshConfig.RemoveLocalFromDb(session);
                    }
                    catch (DatabaseException e)
                    {
                        throw new FatalErrorException("Restoring computer's ssh config after retries failed!");
                    }

                    _prevState.SshConfig.AddComputer(this);
                    SshConfig.RemoveComputer(this);
                    _computersAndSshConfigsManager.RemovedSshConfig(SshConfig);
                });
            }
            else if(_prevState.SshConfig.HasGlobalScope() && SshConfig.HasGlobalScope())
            {
                if(SshConfig.ExistsInDb() == false)
                {
                    throw new SshConfigException("New global ssh config does not exist in db.");
                }

                _prevState.SshConfig.RemoveComputer(this);
                SshConfig.AddComputer(this);

                UpdateComputerWithRetryAndRestorePolicy(session, null, sshConfig ->
                {
                    _prevState.SshConfig.AddComputer(this);
                    SshConfig.RemoveComputer(this);
                });
            }
        }
        else
        {
            String attemptErrorMessage = "[ERROR] Computer: " +
                    "Attempt of updating computer without ssh config changes in db failed.";
            boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, this, attemptErrorMessage);
            if(updateSucceed == false)
            {
                throw new DatabaseException("Unable to update computer without ssh config changes in db.");
            }
        }

        _prevState = null;
    }

    private void UpdateComputerWithRetryAndRestorePolicy(
            Session session, SshConfig sshConfigBackup, Consumer<SshConfig> restoreCallback)
            throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] Computer: Attempt of update computer in db failed.";
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, this, attemptErrorMessage);
        if(updateSucceed == false)
        {
            restoreCallback.accept(sshConfigBackup);

            throw new DatabaseException("Unable to update computer in db.");
        }

        _prevState = null;
    }

    private void Validate_UpdateInDb(Session session) throws NothingToDoException, ComputerException, SshConfigException
    {
        if(_prevState == null)
        {
            throw new NothingToDoException("Previous state is null.");
        }

        if(_existsInDb == false)
        {
            throw new ComputerException("Computer does not exist in db.");
        }

        if(SshConfig.HasGlobalScope() && SshConfig.ExistsInDb() == false)
        {
            throw new SshConfigException("Provided global config does not exist in db.");
        }

        if(SshConfig.HasLocalScope() && SshConfig.ExistsInDb() && SshConfig.equals(_prevState.SshConfig) == false)
        {
            throw new SshConfigException("Unable to replace existing local ssh config with other.");
        }

        if(_computersAndSshConfigsManager == null)
        {
            if(Utilities.AreEqual(_prevState.DisplayedName, this.DisplayedName) == false
                    && ComputerWithSameDisplayedNameExistsInDb(session, this.DisplayedName))
            {
                throw new ComputerException("Computer with same displayed name exists in db.");
            }

            if(Utilities.AreEqual(_prevState.Host, this.Host) == false
                    && ComputerWithSameHostExistsInDb(session, this.Host))
            {
                throw new ComputerException("Computer with same host exists in db.");
            }
        }
        else
        {
            // TODO: UNEXPECTED BEHAVIOR - USAGE OF CONNECTION WITH DB
            if(Utilities.AreEqual(_prevState.DisplayedName, this.DisplayedName) == false
                    && _computersAndSshConfigsManager.OtherComputerWithDisplayedNameExists(this, DisplayedName))
            {
                throw new ComputerException("Computer with same displayed name exists in db.");
            }

            if(Utilities.AreEqual(_prevState.Host, this.Host) == false
                    && _computersAndSshConfigsManager.OtherComputerWithHostExists(this, Host))
            {
                throw new ComputerException("Computer with same host exists in db.");
            }
        }
    }

    // ---  REMOVE FROM DB  --------------------------------------------------------------------------------------------

    public void RemoveFromDb() throws NothingToDoException, ComputerException, SshConfigException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        RemoveFromDb(session);
        session.close();
    }

    public void RemoveFromDb(Session session) throws NothingToDoException, ComputerException, SshConfigException
    {
        Validate_RemoveFromDb();

        SshConfig.RemoveComputer(this);
        if(SshConfig.HasLocalScope())
        {
            SshConfig.RemoveGlobalFromDb(session);
        }

        String attemptErrorMessage = "[ERROR] Computer: Attempt of removing computer from db failed.";
        boolean removeSucceed;
        if(_prevState != null) // TODO: Validate
        {
            removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, _prevState, attemptErrorMessage);
        }
        else
        {
            removeSucceed = DatabaseManager.RemoveWithRetryPolicy(session, this, attemptErrorMessage);
        }

        if(removeSucceed == false)
        {
            try
            {
                SshConfig.AddComputer(this);
            }
            catch (DatabaseException|SshConfigException e)
            {
                throw new FatalErrorException("Restoring ssh configs after computer adding failed!");
            }
            throw new DatabaseException("Unable to remove global ssh config in db.");
        }

        if(_computersAndSshConfigsManager != null)
        {
            _computersAndSshConfigsManager.RemovedComputer(this);
        }
    }

    private void Validate_RemoveFromDb()
    {
        if(_existsInDb == false)
        {
            throw new ComputerException("Computer does not exist in db.");
        }

        if(_prevState == null || this.equals(_prevState))
        {
            throw new ComputerException("Computer was changed. Restore changes to ");
        }
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

    public Integer GetId()
    {
        return Id;
    }

    public String GetDisplayedName()
    {
        return DisplayedName;
    }

    public String GetHost()
    {
        return Host;
    }

    public String GetClassroom()
    {
        return Classroom;
    }

    public SshConfig GetSshConfig()
    {
        return SshConfig;
    }

    public Duration GetMaintainPeriod()
    {
        return MaintainPeriod;
    }

    public Duration GetRequestInterval()
    {
        return RequestInterval;
    }

    public Duration GetLogExpiration()
    {
        return LogExpiration;
    }

    public Timestamp GetLastMaintenance()
    {
        return LastMaintenance;
    }

    public boolean IsSelected()
    {
        return IsSelected;
    }

    public List<Preference> GetPreferences()
    {
        return Preferences;
    }

    public List<IPreference> GetIPreferences()
    {
        List<IPreference> iPreferences = new ArrayList<>();
        for (Preference preference : Preferences)
        {
            iPreferences.add(Healthcheck.Preferences.Preferences.PreferenceClassNameMap.get(preference.ClassName));
        }

        return iPreferences;
    }

    // ---  SETTERS  ---------------------------------------------------------------------------------------------------

    private void TryToSetPrevStateIfNotExisting()
    {
        if(_existsInDb &&_prevState == null)
        {
            SetPreviousState(this);
        }
    }

    private void SetPreviousState(Computer computer)
    {
        _prevState = new Computer(computer);
        _prevState._prevState = null;
    }

    public void SetComputersAndSshConfigsManager(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
    }

    public void SetDisplayedName(String displayedName)
    {
        if(displayedName == null || displayedName.trim().equals(""))
        {
            throw new IllegalArgumentException("Displayed name is null or empty.");
        }

        if(Utilities.AreEqual(DisplayedName, displayedName))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        DisplayedName = displayedName;
    }

    public void SetHost(String host)
    {
        if(host == null || host.trim().equals(""))
        {
            throw new IllegalArgumentException("Host is null or empty.");
        }

        if(Utilities.AreEqual(Host, host))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Host = host;
    }

    public void SetClassroom(String classroom)
    {
        if(classroom == null || classroom.trim().equals(""))
        {
            throw new IllegalArgumentException("Classroom is null or empty.");
        }

        if(Utilities.AreEqual(Classroom, classroom))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Classroom = classroom;
    }

    public void SetSshConfig(SshConfig sshConfig)
    {
        if(sshConfig == null)
        {
            throw new IllegalArgumentException("Ssh config is null.");
        }

        if(Utilities.ReferencesAreEqual(this.SshConfig, sshConfig))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        SshConfig = sshConfig;
    }

    public void SetMaintainPeriod(Duration maintainPeriod)
    {
        if(maintainPeriod == null)
        {
            throw new IllegalArgumentException("Maintain period is null.");
        }

        if(Utilities.AreEqual(MaintainPeriod, maintainPeriod))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        MaintainPeriod = maintainPeriod;
    }

    public void SetRequestInterval(Duration requestInterval)
    {
        if(requestInterval == null)
        {
            throw new IllegalArgumentException("Request interval is null.");
        }

        if(Utilities.AreEqual(RequestInterval, requestInterval))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        RequestInterval = requestInterval;
    }

    public void SetLogExpiration(Duration logExpiration)
    {
        if(logExpiration == null)
        {
            throw new IllegalArgumentException("Log expiration is null.");
        }

        if(Utilities.AreEqual(LogExpiration, logExpiration))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        LogExpiration = logExpiration;
    }

    public void SetLastMaintenance(Timestamp lastMaintenance)
    {
        if(lastMaintenance == null)
        {
            throw new IllegalArgumentException("Last maintenance is null.");
        }

        if(Utilities.AreEqual(LastMaintenance, lastMaintenance))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        LastMaintenance = lastMaintenance;
    }

    public void SetSelected(boolean isSelected)
    {
        if(IsSelected == isSelected)
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        IsSelected = isSelected;
    }

    public void SetPreferences(List<Preference> preferences)
    {
        if(Preferences == preferences || Preferences.containsAll(preferences) && preferences.containsAll(Preferences))
        {
            return;
        }

        TryToSetPrevStateIfNotExisting();

        Preferences = preferences;
        for (Preference preference : preferences)
        {
            preference.AddComputer(this);
        }
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public boolean ExistsInDb()
    {
        return _existsInDb;
    }

    public boolean Changed()
    {
        return _prevState != null;
    }

    public boolean AreRequiredFieldsSet()
    {
        return  DisplayedName != null &&
                Host != null &&
                Classroom != null &&
                SshConfig != null &&
                RequestInterval != null &&
                MaintainPeriod != null &&
                LogExpiration != null &&
                LastMaintenance != null;
    }

    public boolean HasPreferenceWithGivenClassName(String preferenceClassName)
    {
        List<Preference> results = Preferences.stream()
                .filter(p -> p.ClassName.equals(preferenceClassName)).collect(Collectors.toList());

        return !results.isEmpty();
    }

    private boolean ComputerWithSameDisplayedNameExistsInDb(Session session, String displayedName)
    {
        Query query = session.createQuery("select 1 from Computer c where c.DisplayedName = :displayedName");
        query.setParameter("displayedName", displayedName);
        return (((org.hibernate.query.Query) query).uniqueResult() != null);
    }

    private boolean ComputerWithSameHostExistsInDb(Session session, String host)
    {
        Query query = session.createQuery("select 1 from Computer c where c.Host = :host");
        query.setParameter("host", Host);
        return (((org.hibernate.query.Query) query).uniqueResult() != null);
    }

    private boolean SshConfigChanged()
    {
        if(_prevState == null)
        {
            return false;
        }

        if(SshConfig != _prevState.SshConfig || SshConfig.equals(_prevState.SshConfig) == false)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    private boolean SshConfigChangedFromLocalToGlobal()
    {
        return _prevState.SshConfig.HasLocalScope() && SshConfig.HasGlobalScope();
    }

    private boolean SshConfigChangedFromGlobalToLocal()
    {
        return _prevState.SshConfig.HasGlobalScope() && SshConfig.HasLocalScope();
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        Computer other = (Computer) obj;
        return  this.Id == other.Id &&
                Utilities.AreEqual(this.DisplayedName, other.DisplayedName) &&
                Utilities.AreEqual(this.Host, other.Host) &&
                Utilities.AreEqual(this.Classroom, other.Classroom) &&
                Utilities.AreEqual(this.SshConfig, other.SshConfig) &&
                this.IsSelected == other.IsSelected &&
                Utilities.AreEqual(this.MaintainPeriod, other.MaintainPeriod) &&
                Utilities.AreEqual(this.LogExpiration, other.LogExpiration) &&
                Utilities.AreEqual(this.RequestInterval, other.RequestInterval) &&
                Utilities.AreEqual(this.LastMaintenance, other.LastMaintenance) &&
                (this.Preferences == other.Preferences ||
                        (this.Preferences.containsAll(other.Preferences) &&
                                other.Preferences.containsAll(this.Preferences)));
    }
}
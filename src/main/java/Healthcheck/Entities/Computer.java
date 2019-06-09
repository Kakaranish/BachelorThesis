package Healthcheck.Entities;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.LogsManagement.NothingToDoException;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Utilities;
import jdk.jshell.execution.Util;
import org.hibernate.Session;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(name = "Computers")
public class Computer
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(nullable = false, unique = true)
    public String DisplayedName;

    @Column(nullable = false, unique = true)
    public String Host;

    @Column(nullable = false)
    public String Classroom;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "SSHConfiguration_Id", referencedColumnName = "Id", nullable = false)
    public SshConfig SshConfig;

    @Column(nullable = false)
    public Duration MaintainPeriod;

    @Column(nullable = false)
    public Duration RequestInterval;

    @Column(nullable = false)
    public Duration LogExpiration;

    @Column(nullable = false)
    public Timestamp LastMaintenance;

    public boolean IsSelected;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "Computer_Preference",
            inverseJoinColumns = { @JoinColumn(name = "Preference_Id", referencedColumnName = "Id") },
            joinColumns = { @JoinColumn(name = "Computer_Id", referencedColumnName = "Id") }
    )
    public List<Preference> Preferences = new ArrayList<>();

    @Transient
    private boolean _sshConfigChanged = false;

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

        SetPreviousState(this);
        _existsInDb = false;
    }

    private void SetPreviousState(Computer computer)
    {
        _prevState = new Computer(computer);
        _prevState._prevState = null;
    }

    // Copy constructor
    public Computer(Computer computer)
    {
        Id = computer.Id;
        DisplayedName = computer.DisplayedName;
        Host = computer.Host;
        Classroom = computer.Classroom;
//        SshConfig = computer.SshConfig; // TODO: Check
        SshConfig = new SshConfig(computer.SshConfig);
        MaintainPeriod = computer.MaintainPeriod;
        RequestInterval = computer.RequestInterval;
        LogExpiration = computer.LogExpiration;
        LastMaintenance = computer.LastMaintenance;
        IsSelected = computer.IsSelected;

        Preferences = new ArrayList<>(computer.Preferences);
    }

    public void CopyFrom(Computer computer)
    {
        Id = computer.Id;
        DisplayedName = computer.DisplayedName;
        Host = computer.Host;
        Classroom = computer.Classroom;
//        SshConfig = computer.SshConfig; // TODO: Check
        RequestInterval = computer.RequestInterval;
        MaintainPeriod = computer.MaintainPeriod;
        LogExpiration = computer.LogExpiration;
        LastMaintenance = computer.LastMaintenance;
        IsSelected = computer.IsSelected;

        Preferences = new ArrayList<>(computer.Preferences);
    }

    public boolean HasSetRequiredFields()
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

    public boolean HasPreferenceWithGivenClassName(String preferenceClassName)
    {
        List<Preference> results = Preferences.stream()
                .filter(p -> p.ClassName.equals(preferenceClassName)).collect(Collectors.toList());

        return !results.isEmpty();
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

    public void AddToDb() throws ComputerException, SshConfigException, DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        AddToDb(session);
        session.close();
    }

    public void AddToDb(Session session) throws ComputerException, SshConfigException, DatabaseException
    {
        if(ComputerWithSameDisplayedNameExistsInDb(session, DisplayedName))
        {
            throw new ComputerException("Computer with same displayed name exists in db.");
        }

        if(ComputerWithSameHostExistsInDb(session, Host))
        {
            throw new ComputerException("Computer with same host exists in db.");
        }

        if(_existsInDb)
        {
            throw new ComputerException("Computer exists in db.");
        }

        if(Id != null)
        {
            throw new ComputerException("Id is not null.");
        }

        if (SshConfig.HasLocalScope() && SshConfig.ExistsInDb() == false)
        {
            SshConfig.AddToDb();
        }

        if(SshConfig.HasGlobalScope() && SshConfig.ExistsInDb() == false)
        {
            throw new ComputerException("Provided global config does not exist in db.");
        }

        String attemptErrorMessage = "[ERROR] Computer: Attempt of adding computer to db failed.";
        boolean addSucceed = DatabaseManager.PersistWithRetryPolicy(session, this, attemptErrorMessage);
        if(addSucceed == false)
        {
            throw new DatabaseException("Unable to save computer in db.");
        }

        _prevState = new Computer(this);
        _existsInDb = true;
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

    private void ValidateChanges(Session session) throws NothingToDoException, ComputerException, SshConfigException
    {
        if(_prevState == null)
        {
            throw new NothingToDoException("Previous state is null and ssh config has not changed.");
        }

        if(_existsInDb == false)
        {
            throw new ComputerException("Computer does not exist in db.");
        }

        if(SshConfig.HasGlobalScope() && SshConfig.ExistsInDb() == false)
        {
            throw new SshConfigException("Provided global config does not exist in db.");
        }

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

    public void UpdateInDb() throws NothingToDoException, ComputerException, SshConfigException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        UpdateInDb(session);
        session.close();
    }

    public void UpdateInDb(Session session) throws NothingToDoException, ComputerException, SshConfigException
    {
        ValidateChanges(session);

        if(SshConfig.equals(_prevState.SshConfig) == false)
        {
            if(_prevState.SshConfig.HasLocalScope() && SshConfig.HasLocalScope())
            {
                if(_prevState.SshConfig.GetId() != SshConfig.GetId())
                {
                    throw new SshConfigException("Local ssh configs have different id's.");
                }
                SshConfig.UpdateInDb();
            }
            else if(_prevState.SshConfig.HasLocalScope() && SshConfig.HasGlobalScope())
            {
                _prevState.SshConfig.RemoveFromDb(session);
            }
            else if(_prevState.SshConfig.HasGlobalScope() && SshConfig.HasLocalScope())
            {
                if(SshConfig.GetId() == null)
                {
                    SshConfig.AddToDb();
                }
                else
                {
                    throw new ComputerException("Local ssh config is already assigned to other computer.");
                }
            }
            else if(_prevState.SshConfig.HasGlobalScope() && SshConfig.HasGlobalScope())
            {
                if(SshConfig.ExistsInDb() == false)
                {
                    throw new SshConfigException("New global ssh config does not exist in db.");
                }
            }
        }

        String attemptErrorMessage = "[ERROR] Computer: Attempt of update computer in db failed.";
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, this, attemptErrorMessage);
        if(updateSucceed == false)
        {
            throw new DatabaseException("Unable to update computer in db.");
        }
        _sshConfigChanged = false;
    }

    public void RemoveInDb() throws NothingToDoException, ComputerException, SshConfigException
    {
        Session session = DatabaseManager.GetInstance().GetSession();
        RemoveInDb(session);
        session.close();
    }

    public void RemoveInDb(Session session) throws NothingToDoException, ComputerException, SshConfigException
    {
        if(_existsInDb == false)
        {
            throw new ComputerException("Computer does not exist in db.");
        }

        SshConfig.RemoveComputer(this);
        if(SshConfig.HasLocalScope())
        {
            SshConfig.RemoveFromDb(session);
        }

        String attemptErrorMessage = "[ERROR] Computer: Attempt of removing computer from db failed.";
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
            SshConfig.AddComputer(this);
            throw new DatabaseException("Unable to remove global ssh config in db.");
        }

        // TODO: Clear?
    }

    @Transient
    private Computer _prevState;

    @Transient
    private boolean _existsInDb = true;


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

    // ---  SETTERS  ---------------------------------------------------------------------------------------------------

    private void TryToSetPrevStateIfNotExisting()
    {
        if(_prevState == null)
        {
            SetPreviousState(this);
        }
    }

    public void SetDisplayedName(String displayedName)
    {
        TryToSetPrevStateIfNotExisting();

        if(displayedName == null || displayedName.trim().equals(""))
        {
            throw new IllegalArgumentException("Displayed name is null or empty.");
        }
        DisplayedName = displayedName;
    }

    public void SetHost(String host)
    {
        TryToSetPrevStateIfNotExisting();

        if(host == null || host.trim().equals(""))
        {
            throw new IllegalArgumentException("Host is null or empty.");
        }
        Host = host;
    }

    public void SetClassroom(String classroom)
    {
        TryToSetPrevStateIfNotExisting();

        if(classroom == null || classroom.trim().equals(""))
        {
            throw new IllegalArgumentException("Classroom is null or empty.");
        }
        Classroom = classroom;
    }

    public void SetSshConfig(SshConfig sshConfig)
    {
        TryToSetPrevStateIfNotExisting();

        if(sshConfig == null)
        {
            throw new IllegalArgumentException("Ssh config is null.");
        }
        SshConfig = sshConfig;
    }

    public void SetMaintainPeriod(Duration maintainPeriod)
    {
        TryToSetPrevStateIfNotExisting();

        if(maintainPeriod == null)
        {
            throw new IllegalArgumentException("Maintain period is null.");
        }
        MaintainPeriod = maintainPeriod;
    }

    public void SetRequestInterval(Duration requestInterval)
    {
        TryToSetPrevStateIfNotExisting();

        if(requestInterval == null)
        {
            throw new IllegalArgumentException("Request interval is null.");
        }
        RequestInterval = requestInterval;
    }

    public void SetLogExpiration(Duration logExpiration)
    {
        TryToSetPrevStateIfNotExisting();

        if(logExpiration == null)
        {
            throw new IllegalArgumentException("Log expiration is null.");
        }
        LogExpiration = logExpiration;
    }

    public void SetLastMaintenance(Timestamp lastMaintenance)
    {
        TryToSetPrevStateIfNotExisting();

        if(lastMaintenance == null)
        {
            throw new IllegalArgumentException("Last maintenance is null.");
        }
        LastMaintenance = lastMaintenance;
    }

    public void SetSelected(boolean selected)
    {
        TryToSetPrevStateIfNotExisting();

        IsSelected = selected;
    }

    // TODO: Check if work
    public void SetPreferences(List<Preference> preferences)
    {
        TryToSetPrevStateIfNotExisting();

        Preferences = preferences;
        for (Preference preference : preferences)
        {
            preference.AddComputer(this);
        }
    }


    // TODO: To remove
    public void CopyFromWithoutSshConfig(Computer computer)
    {
        Id = computer.Id;
        DisplayedName = computer.DisplayedName;
        Host = computer.Host;
        Classroom = computer.Classroom;
        RequestInterval = computer.RequestInterval;
        MaintainPeriod = computer.MaintainPeriod;
        LogExpiration = computer.LogExpiration;
        LastMaintenance = computer.LastMaintenance;
        IsSelected = computer.IsSelected;

        // TODO: Check if it's enough
        Preferences = computer.Preferences;
    }

    public void SshConfigChanged()
    {
        if(_sshConfigChanged == false)
        {
            _sshConfigChanged = true;
            TryToSetPrevStateIfNotExisting();
        }
    }
}
package Entities;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.List;

@Entity
@Table(name = "Computers")
public class ComputerEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(nullable = false, unique = true)
    public String Host;

    @ManyToOne
    @JoinColumn(name = "User_Username", referencedColumnName = "Id")
    public User User;

    @ManyToOne
    @JoinColumn(name = "Classroom_Id", referencedColumnName = "Id")
    public Classroom Classroom;

    // User data fields
    private String Username;
    private String Password; // Is encrypted
    private String SSHKey;

    public int Timeout;
    public int Port;

    @Column(nullable = false)
    public Duration MaintainPeriod; // Every some time maintenance works will be performed

    @Column(nullable = false)
    public Duration RequestInterval;

    @Column(nullable = false)
    public Duration LogExpiration;

    public boolean IsSelected;

    public Timestamp LastMaintenance;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "ComputerEntity_Preference",
            joinColumns = { @JoinColumn(name = "Computer_Id", referencedColumnName = "Id") },
            inverseJoinColumns = { @JoinColumn(name = "Preference_Id", referencedColumnName = "Id") }
    )
    public List<Preference> Preferences;

    private ComputerEntity()
    {
    }

    public ComputerEntity(
            String host,
            String username,
            String password,
            String _SSHKey,
            int timeout,
            int port,
            Duration maintainPeriod,
            Duration requestInterval,
            Duration logExpiration,
            Classroom classroom,
            boolean isSelected)
    {
        Host = host;
        Username = username;
        Password = password;
        this.SSHKey = _SSHKey;
        Timeout = timeout;
        Port = port;
        MaintainPeriod = maintainPeriod;
        RequestInterval = requestInterval;
        LogExpiration = logExpiration;
        Classroom = classroom;
        IsSelected = isSelected;
        LastMaintenance = new Timestamp(System.currentTimeMillis());
    }

    public ComputerEntity(
            String host,
            User user,
            int timeout,
            int port,
            Duration maintainPeriod,
            Duration requestInterval,
            Duration logExpiration,
            Classroom classroom,
            boolean isSelected)
    {
        Host = host;
        User = user;
        Timeout = timeout;
        Port = port;
        MaintainPeriod = maintainPeriod;
        RequestInterval = requestInterval;
        LogExpiration = logExpiration;
        Classroom = classroom;
        IsSelected = isSelected;
        LastMaintenance = new Timestamp(System.currentTimeMillis());
    }

    // Copy constructor
    public ComputerEntity(ComputerEntity computerEntity)
    {
        Username = computerEntity.Username;
        Password = computerEntity.Password;
        SSHKey = computerEntity.SSHKey;
        User = computerEntity.User != null ? new User(computerEntity.User) : null;
        Timeout = computerEntity.Timeout;
        Port = computerEntity.Port;
        MaintainPeriod = computerEntity.MaintainPeriod;
        RequestInterval = computerEntity.RequestInterval;
        LogExpiration = computerEntity.LogExpiration;
        IsSelected = computerEntity.IsSelected;
        LastMaintenance = computerEntity.LastMaintenance;
        Preferences = computerEntity.Preferences;
    }

    public void CopyFrom(ComputerEntity computerEntity)
    {
        Username = computerEntity.Username;
        Password = computerEntity.Password;
        SSHKey = computerEntity.SSHKey;
        User = computerEntity.User;
        Timeout = computerEntity.Timeout;
        Port = computerEntity.Port;
        MaintainPeriod = computerEntity.MaintainPeriod;
        RequestInterval = computerEntity.RequestInterval;
        LogExpiration = computerEntity.LogExpiration;
        IsSelected = computerEntity.IsSelected;
        LastMaintenance = computerEntity.LastMaintenance;
        Preferences = computerEntity.Preferences;
    }

    public void AssignUser(User user)
    {
        User = user;
    }

    public void RemoveUser(boolean clearUserFields)
    {
        if(User == null)
        {
            return;
        }
        else
        {
            if(clearUserFields)
            {
                Username = null;
                Password = null;
                SSHKey = null;
            }
            else
            {
                Username = User.Username;
                Password = User.Password;
                SSHKey = User.SSHKey;
            }

            User = null;
        }
    }

    public String GetUsername()
    {
        return User == null ? Username : User.Username;
    }

    public String GetPassword()
    {
        return User == null ? Password : User.Password;
    }

    public String GetSSHKey()
    {
        return User == null ? SSHKey : User.SSHKey;
    }
}

package Healthcheck.Entities;

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
    @JoinColumn(name = "User_Id", referencedColumnName = "Id")
    public User User;

    @ManyToOne
    @JoinColumn(name = "Classroom_Id", referencedColumnName = "Id")
    public Classroom Classroom;

    // SSH connection data fields
    private String SSH_Username;
    private String SSH_EncryptedPassword; // Is encrypted
    private String SSH_Key;

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
            String _SSH_Username,
            String _SSH_EncryptedPassword,
            String _SSH_Key,
            int port,
            Duration maintainPeriod,
            Duration requestInterval,
            Duration logExpiration,
            Classroom classroom,
            boolean isSelected)
    {
        Host = host;
        SSH_Username = _SSH_Username;
        SSH_EncryptedPassword = _SSH_EncryptedPassword;
        SSH_Key = _SSH_Key;
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
            int port,
            Duration maintainPeriod,
            Duration requestInterval,
            Duration logExpiration,
            Classroom classroom,
            boolean isSelected)
    {
        Host = host;
        User = user;
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
        SSH_Username = computerEntity.SSH_Username;
        SSH_EncryptedPassword = computerEntity.SSH_EncryptedPassword;
        SSH_Key = computerEntity.SSH_Key;
        User = computerEntity.User != null ? new User(computerEntity.User) : null;
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
        SSH_Username = computerEntity.SSH_Username;
        SSH_EncryptedPassword = computerEntity.SSH_EncryptedPassword;
        SSH_Key = computerEntity.SSH_Key;
        User = computerEntity.User;
        Port = computerEntity.Port;
        MaintainPeriod = computerEntity.MaintainPeriod;
        RequestInterval = computerEntity.RequestInterval;
        LogExpiration = computerEntity.LogExpiration;
        IsSelected = computerEntity.IsSelected;
        LastMaintenance = computerEntity.LastMaintenance;
        Preferences = computerEntity.Preferences;
    }

    public void ResetConnectionDataFields()
    {
        SSH_Username = null;
        SSH_EncryptedPassword = null;
        SSH_Key = null;
    }

    public void SetConnectionDataFields(String _SSH_Username, String _SSH_EncryptedPassword, String _SSH_Key)
    {
        SSH_Username = _SSH_Username;
        SSH_EncryptedPassword = _SSH_EncryptedPassword;
        SSH_Key = _SSH_Key;
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
                SSH_Username = null;
                SSH_EncryptedPassword = null;
                SSH_Key = null;
            }
            else
            {
                SSH_Username = User.SSH_Username;
                SSH_EncryptedPassword = User.SSH_EncryptedPassword;
                SSH_Key = User.SSH_Key;
            }

            User = null;
        }
    }

    public String GetUsername()
    {
        return User == null ? SSH_Username : User.SSH_Username;
    }

    public String GetEncryptedPassword()
    {
        return User == null ? SSH_EncryptedPassword : User.SSH_EncryptedPassword;
    }

    public String GetSSHKey()
    {
        return User == null ? SSH_Key : User.SSH_Key;
    }

    public String GetUsernameConnectionField()
    {
        return SSH_Username;
    }

    public String GetEncryptedPasswordConnectionField()
    {
        return SSH_EncryptedPassword;
    }

    public String GetSSHKeyConnectionField()
    {
        return SSH_Key;
    }
}

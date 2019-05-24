package Healthcheck.Entities;

import Healthcheck.Utilities;

import javax.persistence.*;
import javax.sound.sampled.Port;
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
        Id = computerEntity.Id;
        Host = computerEntity.Host;
        User = computerEntity.User != null ? new User(computerEntity.User) : null;
        Classroom = computerEntity.Classroom;
        SSH_Username = computerEntity.SSH_Username;
        SSH_EncryptedPassword = computerEntity.SSH_EncryptedPassword;
        SSH_Key = computerEntity.SSH_Key;
        Port = computerEntity.Port;
        IsSelected = computerEntity.IsSelected;
        MaintainPeriod = computerEntity.MaintainPeriod;
        RequestInterval = computerEntity.RequestInterval;
        LogExpiration = computerEntity.LogExpiration;
        LastMaintenance = computerEntity.LastMaintenance;
        Preferences = computerEntity.Preferences;
    }

    public void CopyFrom(ComputerEntity computerEntity)
    {
        Id = computerEntity.Id;
        Host = computerEntity.Host;
        User = computerEntity.User;
        Classroom = computerEntity.Classroom;
        SSH_Username = computerEntity.SSH_Username;
        SSH_EncryptedPassword = computerEntity.SSH_EncryptedPassword;
        SSH_Key = computerEntity.SSH_Key;
        Port = computerEntity.Port;
        MaintainPeriod = computerEntity.MaintainPeriod;
        IsSelected = computerEntity.IsSelected;
        RequestInterval = computerEntity.RequestInterval;
        LogExpiration = computerEntity.LogExpiration;
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

    public void RemoveUser()
    {
        if(User == null)
        {
            return;
        }
        else
        {
            SSH_Username = User.SSH_Username;
            SSH_EncryptedPassword = User.SSH_EncryptedPassword;
            SSH_Key = User.SSH_Key;

            User = null;
        }
    }

    public boolean HasSetRequiredFields()
    {
        return Host != null
                && (User != null || (SSH_Username != null && SSH_EncryptedPassword != null && SSH_Key != null));
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

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        ComputerEntity other = (ComputerEntity) obj;
        return  this.Id == other.Id &&
                Utilities.AreEqual(this.Host, other.Host) &&
                Utilities.AreEqual(this.User, other.User) &&
                Utilities.AreEqual(this.Classroom, other.Classroom) &&
                Utilities.AreEqual(this.SSH_Username, other.SSH_Username) &&
                Utilities.AreEqual(this.SSH_EncryptedPassword, other.SSH_EncryptedPassword) &&
                Utilities.AreEqual(this.SSH_Key, other.SSH_Key) &&
                this.Port == other.Port &&
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

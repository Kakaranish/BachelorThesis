package Entities;

import javax.persistence.*;
import java.sql.Timestamp;
import java.time.Duration;

@Entity
@Table(name = "Computers")
public class ComputerEntity
{
    @Id
    @Column(nullable = false, unique = true)
    public String Host;

    @ManyToOne
    @JoinColumn(referencedColumnName = "Username")
    public User User;

    @ManyToOne
    @JoinColumn(name = "ClassroomId", referencedColumnName = "Id")
    public Classroom Classroom;

    // User fields
    public String Username;
    public String Password; // Is encrypted
    public String SSHKey;

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
}

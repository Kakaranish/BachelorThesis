package Entities;

import javax.persistence.*;
import java.net.UnknownServiceException;
import java.time.Duration;

@Entity
@Table(name = "Computers")
public class ComputerEntity
{
    @Id
    @Column(nullable = false, unique = true)
    private String Host;

    @ManyToOne
    @JoinColumn(referencedColumnName = "Username")
    public User User;

    private String Username;
    private String Password; // Is encrypted
    private String SSHKey;

    // TODO: Make lowercase
    private int Timeout;
    private int Port;

    @Column(nullable = false)
    public Duration MaintainPeriod; // Every some time maintenance works will be performed

    @Column(nullable = false)
    public Duration RequestInterval;

    @Column(nullable = false)
    public Duration LogExpiration;

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
            Duration logExpiration)
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
    }

    public ComputerEntity(
            String host,
            User user,
            int timeout,
            int port,
            Duration maintainPeriod,
            Duration requestInterval,
            Duration logExpiration)
    {
        Host = host;
        User = user;
        Timeout = timeout;
        Port = port;
        MaintainPeriod = maintainPeriod;
        RequestInterval = requestInterval;
        LogExpiration = logExpiration;
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
    }

    public void AssignUser(User user)
    {
        User = user;
    }

    public void RemoveUser(boolean clearComputerConnectionFields)
    {
        if(User == null)
        {
            return;
        }
        else
        {
            if(clearComputerConnectionFields)
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

    public String getUsername()
    {
        return User == null ? Username : User.Username;
    }

    public String getPassword()
    {
        return User == null ? Password : User.Password;
    }

    public String getSSHKey()
    {
        return User == null ? SSHKey : User.SSHKey;
    }

    public String getHost()
    {
        return Host;
    }

    public Entities.User getUser()
    {
        return User;
    }

    public int getTimeout()
    {
        return Timeout;
    }

    public int getPort()
    {
        return Port;
    }

    public Duration getMaintainPeriod()
    {
        return MaintainPeriod;
    }

    public Duration getRequestInterval()
    {
        return RequestInterval;
    }

    public Duration getLogExpiration()
    {
        return LogExpiration;
    }
}

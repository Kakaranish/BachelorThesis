package Entities;

import javax.persistence.*;
import java.net.UnknownServiceException;
import java.time.Duration;

@Entity
@Table(name = "Computers")
public class Computer
{
    @Id
    @Column(nullable = false, unique = true)
    private String Host;

    @ManyToOne
    @JoinColumn(referencedColumnName = "Username")
    private User User;

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

    public Computer()
    {
    }

    public Computer(String host, String username, String password, String _SSHKey, int timeout, int port, Duration maintainPeriod, Duration requestInterval, Duration logExpiration)
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

    public Computer(String host, User user, int timeout, int port, Duration maintainPeriod, Duration requestInterval, Duration logExpiration)
    {
        Host = host;
        User = user;
        Timeout = timeout;
        Port = port;
        MaintainPeriod = maintainPeriod;
        RequestInterval = requestInterval;
        LogExpiration = logExpiration;
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

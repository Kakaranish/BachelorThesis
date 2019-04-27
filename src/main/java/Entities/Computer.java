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
    public String Host;

    @ManyToOne
    @JoinColumn(referencedColumnName = "Username")
    public User User;

    private String Username;
    private String Password; // Is hashed
    private String SSHKey;

    public int Timeout;
    public int Port;
    public Duration MaintainPeriod; // Every some time maintenance works will be performed
    public Duration RequestInterval;
    public Duration LogExpiration;

    public Computer()
    {
    }

    public Computer(String host, User computerConfig)
    {
        Host = host;
        User = computerConfig;
        Username = computerConfig.Username;
        Password = computerConfig.Password;
        SSHKey = computerConfig.SSHKey;
    }

    public Computer(String host, String username, String password, String _SSHKey)
    {
        Host = host;
        Username = username;
        Password = password;
        SSHKey = _SSHKey;
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
}

package Healthcheck.Entities;

import Healthcheck.Utilities;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "SSH_Configurations", uniqueConstraints = {@UniqueConstraint(columnNames = {"Name"})})
public class SSHConfiguration
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(unique = true)
    public String Name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SSHConfigurationScope Scope;

    @Column(nullable = false)
    public Integer Port;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    public SSHAuthMethod AuthMethod;

    @Column(nullable = false)
    public String Username;

    public String PrivateKeyPath;

    public String EncryptedPassword;

    //TODO: May be lazy?
    @Fetch(FetchMode.SUBSELECT)
    @OneToMany(mappedBy = "SSHConfiguration", fetch = FetchType.EAGER)
    private List<Computer> _computers = new ArrayList<>();

    public SSHConfiguration()
    {
    }

    public SSHConfiguration(
            String name,
            SSHConfigurationScope configurationScope,
            Integer port,
            SSHAuthMethod authMethod,
            String username,
            String keyPathOrEncryptedPassword)
    {
        Name = name;
        Scope = configurationScope;
        Port = port;
        AuthMethod = authMethod;
        Username = username;

        if(Scope == SSHConfigurationScope.COMPUTER)
        {
            Name = null;
        }

        if(AuthMethod == SSHAuthMethod.PASSWORD)
        {
            EncryptedPassword = keyPathOrEncryptedPassword;
        }
        else if(AuthMethod == SSHAuthMethod.KEY)
        {
            PrivateKeyPath = keyPathOrEncryptedPassword;
        }
    }

    // Copy Constructor
    public SSHConfiguration(SSHConfiguration otherSSHConfiguration)
    {
        Name = otherSSHConfiguration.Name;
        Scope = otherSSHConfiguration.Scope;
        Port = otherSSHConfiguration.Port;
        AuthMethod = otherSSHConfiguration.AuthMethod;
        Username = otherSSHConfiguration.Username;
        PrivateKeyPath = otherSSHConfiguration.PrivateKeyPath;
        EncryptedPassword = otherSSHConfiguration.EncryptedPassword;
    }

    public void CopyFrom(SSHConfiguration otherSSHConfiguration)
    {
        Name = otherSSHConfiguration.Name;
        Scope = otherSSHConfiguration.Scope;
        Port = otherSSHConfiguration.Port;
        AuthMethod = otherSSHConfiguration.AuthMethod;
        Username = otherSSHConfiguration.Username;
        PrivateKeyPath = otherSSHConfiguration.PrivateKeyPath;
        EncryptedPassword = otherSSHConfiguration.EncryptedPassword;
    }

    public boolean HasSetRequiredFields()
    {
        if(AuthMethod == null)
        {
            return false;
        }

        boolean authFieldSet;
        if(AuthMethod == SSHAuthMethod.PASSWORD)
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
        boolean nameIsRequired = Scope != SSHConfigurationScope.COMPUTER;

        return  !(nameIsRequired && Name == null) &&
                Scope != null &&
                Port != null &&
                Username != null &&
                authFieldSet;
    }

    public void AddComputer(Computer computer)
    {
        _computers.add(computer);
        computer.SSHConfiguration = this;
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

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        SSHConfiguration other = (SSHConfiguration) obj;
        return  Utilities.AreEqual(this.Name, other.Name) &&
                Utilities.AreEqual(this.Scope, other.Scope) &&
                Utilities.AreEqual(this.Port, other.Port) &&
                Utilities.AreEqual(this.AuthMethod, other.AuthMethod) &&
                Utilities.AreEqual(this.Username, other.Username) &&
                Utilities.AreEqual(this.PrivateKeyPath, other.PrivateKeyPath) &&
                Utilities.AreEqual(this.EncryptedPassword, other.EncryptedPassword);
    }
}

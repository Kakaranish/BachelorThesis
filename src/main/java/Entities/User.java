package Entities;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "Users")
public class User
{
    @Id
    @Column(unique = true, nullable = false)
    public String Username;

    @Column(nullable = false)
    public String Password;

    @Column(nullable = false)
    public String SSHKey;

    public User()
    {
    }

    public User(String username, String password, String _SSHKey)
    {
        Username = username;
        Password = password;
        SSHKey = _SSHKey;
    }
}

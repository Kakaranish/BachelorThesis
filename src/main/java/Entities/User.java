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

    private User()
    {
    }

    public User(String username, String password, String _SSHKey)
    {
        Username = username;
        Password = password;
        SSHKey = _SSHKey;
    }

    // Copy constructor
    public User(User user)
    {
        Username = user.Username;
        Password = user.Password;
        SSHKey = user.SSHKey;
    }

    public void CopyFrom(User user)
    {
        Username = user.Username;
        Password = user.Password;
        SSHKey = user.SSHKey;
    }

    @Override
    public boolean equals(Object obj)
    {
        User otherUser = (User) obj;

        return  this.Username.equals(otherUser.Username) &&
                this.Password.equals(otherUser.Password) &&
                this.SSHKey.equals(otherUser.SSHKey);

    }
}

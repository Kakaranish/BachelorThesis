package Entities;

import javax.persistence.*;

@Entity
@Table(name = "Users")
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

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
        Id = user.Id;
        Username = user.Username;
        Password = user.Password;
        SSHKey = user.SSHKey;
    }

    public void CopyFrom(User user)
    {
        Id = user.Id;
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

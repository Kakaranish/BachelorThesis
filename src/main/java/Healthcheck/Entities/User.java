package Healthcheck.Entities;

import Healthcheck.Utilities;
import javax.persistence.*;

@Entity
@Table(name = "Users", uniqueConstraints = {@UniqueConstraint(columnNames = {"DisplayedUsername", "SSH_Username"})})
public class User
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Integer Id;

    @Column(nullable = false)
    public String DisplayedUsername;

    @Column(nullable = false)
    public String SSH_Username;

    @Column(nullable = false)
    public String SSH_EncryptedPassword;

    @Column(nullable = false)
    public String SSH_Key;

    private User()
    {
    }

    public User(String displayedUsername, String _SSH_Username, String _SSH_EncryptedPassword, String _SSH_Key)
    {
        DisplayedUsername = displayedUsername;
        SSH_Username = _SSH_Username;
        SSH_EncryptedPassword = _SSH_EncryptedPassword;
        SSH_Key = _SSH_Key;
    }

    // Copy constructor
    public User(User user)
    {
        Id = user.Id;
        DisplayedUsername = user.DisplayedUsername;
        SSH_Username = user.SSH_Username;
        SSH_EncryptedPassword = user.SSH_EncryptedPassword;
        SSH_Key = user.SSH_Key;
    }

    public void CopyFrom(User user)
    {
        Id = user.Id;
        DisplayedUsername = user.DisplayedUsername;
        SSH_Username = user.SSH_Username;
        SSH_EncryptedPassword = user.SSH_EncryptedPassword;
        SSH_Key = user.SSH_Key;
    }

    public boolean SomeDataConnectionFieldsAreEmpty()
    {
        return  DisplayedUsername == null ||
                SSH_Username == null ||
                SSH_EncryptedPassword == null ||
                SSH_Key == null;
    }

    @Override
    public boolean equals(Object obj)
    {
        if(obj == null)
        {
            return false;
        }

        User other = (User) obj;
        return  this.Id == other.Id &&
                Utilities.AreEqual(this.DisplayedUsername, other.DisplayedUsername) &&
                Utilities.AreEqual(this.SSH_Username, other.SSH_Username) &&
                Utilities.AreEqual(this.SSH_EncryptedPassword, other.SSH_EncryptedPassword) &&
                Utilities.AreEqual(this.SSH_Key, other.SSH_Key);
    }
}

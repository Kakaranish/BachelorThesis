package Healthcheck.Entities;

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
        User otherUser = (User) obj;

        return  DisplayedUsername.equals(otherUser.DisplayedUsername) &&
                SSH_Username.equals(otherUser.SSH_Username) &&
                SSH_EncryptedPassword.equals(otherUser.SSH_EncryptedPassword) &&
                SSH_Key.equals(otherUser.SSH_Key);
    }
}

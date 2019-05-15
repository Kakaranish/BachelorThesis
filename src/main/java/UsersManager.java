import Entities.User;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.util.List;
import java.util.stream.Collectors;

public class UsersManager
{
    private List<User> _users;

    public UsersManager()
    {
        _users = GetUsersFromDb();
    }

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------- ADD ---------------------------------------------------------

    public void AddUser(User user) throws DatabaseException, IllegalArgumentException
    {
        if (UserExists(user.DisplayedUsername, user.SSH_Username))
        {
            throw new IllegalArgumentException("Unable to add user. User with same DisplayedUsername & SSH_Username exists.");
        }

        try
        {
            AddUserToDb(user);
            _users.add(user);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void AddUserToDb(User user) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            session.save(user);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to add user to db.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- UPDATE -------------------------------------------------------

    // userToUpdate.DisplayedUsername.equals(newUser.DisplayedUsername) == false -> DisplayedUsername zmienia się
    public void UpdateUser(User userToUpdate, User newUser, ComputerManager computerManager)
            throws DatabaseException, IllegalArgumentException
    {
        if ((userToUpdate.DisplayedUsername.equals(newUser.DisplayedUsername) == false ||
                userToUpdate.SSH_Username.equals(newUser.SSH_Username) == false) &&
                UserExists(newUser.DisplayedUsername, newUser.SSH_Username))
        {
            throw new IllegalArgumentException("Unable to update user. User with same DisplayerUsername & SSH_Key exists.");
        }

        try
        {
            List<Computer> computersAssociatedWithUser =
                    computerManager.GetComputersAssociatedWithUser(userToUpdate);

            UpdateUserInDb(userToUpdate, newUser, computerManager);
            UpdateUserFieldLocallyInComputersAssociatedWithUser(computersAssociatedWithUser, userToUpdate);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void UpdateUserInDb(User userToUpdate, User newUser, ComputerManager computerManager)
            throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            userToUpdate.CopyFrom(newUser);
            session.update(userToUpdate);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to update user.");
        }
        finally
        {
            session.close();
        }
    }

    private void UpdateUserFieldLocallyInComputersAssociatedWithUser(List<Computer> associatedComputers, User newUser)
    {
        for (Computer computer : associatedComputers)
        {
            computer.ComputerEntity.User = newUser;
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- REMOVE -------------------------------------------------------

    public void RemoveUser(User user, ComputerManager computerManager, boolean clearUserFields)
            throws DatabaseException
    {
        try
        {
            RemoveUserFromDb(user, computerManager, clearUserFields);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    public void RemoveUserFromDb(User user, ComputerManager computerManager, boolean clearUserFields)
            throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            List<Computer> computersAssociatedWithUser = computerManager.GetComputersAssociatedWithUser(user);
            for (Computer computer : computersAssociatedWithUser)
            {
                computerManager.RemoveUserAssignmentFromComputer(computer, session, clearUserFields);
            }

            session.remove(user);
            _users.remove(user);

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to remove user.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- GETTERS ------------------------------------------------------

    private List<User> GetUsersFromDb() throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            String hql = "from User";
            Query query = session.createQuery(hql);
            List<User> users = query.getResultList();

            return users;
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("Unable to get users from db.");
        }
        finally
        {
            session.close();
        }
    }

    public User GetUser(String displayedUsername, String SSH_Username)
    {
        List<User> results = _users.stream()
                .filter(u ->
                        u.DisplayedUsername.equals(displayedUsername) &&
                        u.SSH_Username.equals(SSH_Username)
                ).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    private boolean UserExists(String displayedUsername, String SSH_Username)
    {
        User receivedUser = GetUser(displayedUsername, SSH_Username);

        return receivedUser != null;
    }
}

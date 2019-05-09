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

    public void AddUser(User user) throws DatabaseException
    {
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
            throw new DatabaseException("Unable to add User to DB.");
        }
        finally
        {
            session.close();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- UPDATE -------------------------------------------------------

    public void UpdateUser(User userToUpdate, User newUser, ComputerManager computerManager) throws DatabaseException
    {
        try
        {
            User updatedUser = UpdateUserInDb(userToUpdate, newUser, computerManager);
            List<Computer> computersAssociatedWithUser = computerManager.GetComputersAssociatedWithUser(userToUpdate);
            UpdateUserFieldLocallyInComputersAssociatedWithUser(computersAssociatedWithUser, userToUpdate);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private User UpdateUserInDb(User userToUpdate, User newUser, ComputerManager computerManager) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            userToUpdate.CopyFrom(newUser);
            session.update(userToUpdate);

            session.getTransaction().commit();

            return userToUpdate;
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

    public void RemoveUser(User user, ComputerManager computerManager, boolean clearUserFields) throws DatabaseException
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

    public void RemoveUserFromDb(User user, ComputerManager computerManager, boolean clearUserFields) throws DatabaseException
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

    public User GetUser(String username)
    {
        List<User> results = _users.stream()
                .filter(u -> u.Username.equals(username)).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }
}

package Healthcheck;

import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.User;
import Healthcheck.LogsManagement.NothingToDoException;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.List;
import java.util.Random;
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
        if(user.HasSetRequiredFields() == false)
        {
            throw new IllegalArgumentException("[FATAL ERROR] UsersManager: " +
                    "Unable to add user. User contains not filled required fields.");
        }

        if (UserExists(user.DisplayedUsername, user.SSH_Username))
        {
            throw new IllegalArgumentException("[FATAL ERROR] UsersManager: " +
                    "Unable to add user. User with same DisplayedUsername & SSH_Username exists.");
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
        String attemptErrorMessage = "[ERROR] UsersManager: Attempt of adding user to db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        boolean persistSucceed = DatabaseManager.PersistWithRetryPolicy(session, user, attemptErrorMessage);
        session.close();

        if(persistSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to add user to db.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- UPDATE -------------------------------------------------------

    public void UpdateUser(User userToUpdate, User newUser, ComputerManager computerManager)
            throws DatabaseException, IllegalArgumentException, NothingToDoException
    {
        if(UserWithSameDisplayedUsernameAndSSHUsernameExists(userToUpdate, newUser))
        {
            throw new NothingToDoException("Unable to update user. User with same DisplayerUsername & SSH_Key exists.");
        }

        if(newUser.SomeDataConnectionFieldsAreEmpty())
        {
            throw new IllegalArgumentException("Unable to update user. Some fields are empty.");
        }

        try
        {
            List<Computer> computersAssociatedWithUser =
                    computerManager.GetComputersAssociatedWithUser(userToUpdate);

            UpdateUserInDb(userToUpdate, newUser, computerManager);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    private void UpdateUserInDb(User userToUpdate, User newUser, ComputerManager computerManager)
            throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] UsersManager: Attempt of updating user in db failed.";

        userToUpdate.CopyFrom(newUser);
        Session session = DatabaseManager.GetInstance().GetSession();
        boolean updateSucceed = DatabaseManager.UpdateWithRetryPolicy(session, userToUpdate, attemptErrorMessage);
        session.close();

        if(updateSucceed == false)
        {
            throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to update user in db.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- REMOVE -------------------------------------------------------

    public void RemoveUser(User user, ComputerManager computerManager)
            throws DatabaseException
    {
        try
        {
            List<Computer> associatedComputers = computerManager.GetComputersAssociatedWithUser(user);

            RemoveUserFromDb(user, associatedComputers);

            _users.remove(user);
            for (Computer computer : associatedComputers)
            {
                computer.ComputerEntity.RemoveUser();
            }
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }

    public void RemoveUserFromDb(User user, List<Computer> associatedComputers)
            throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] UsersManager: Attempt of removing user to db failed.";

        Session session = DatabaseManager.GetInstance().GetSession();
        try
        {
            session.beginTransaction();

            session.evict(user);

            for (Computer associatedComputer : associatedComputers)
            {
                associatedComputer.ComputerEntity = (ComputerEntity) session.merge(associatedComputer.ComputerEntity);
                associatedComputer.ComputerEntity.RemoveUser();
                session.update(associatedComputer.ComputerEntity);
            }
            user = (User) session.merge(user);
            session.remove(user);

            session.getTransaction().commit();
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            System.out.println(attemptErrorMessage);

            int retryNum = 1;
            while (retryNum <= Utilities.RemoveNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.RemoveCooldown + randomFactor);

                    session.beginTransaction();

                    for (Computer associatedComputer : associatedComputers)
                    {
                        associatedComputer.ComputerEntity = (ComputerEntity) session.merge(associatedComputer.ComputerEntity);
                        associatedComputer.ComputerEntity.RemoveUser();
                        session.update(associatedComputer.ComputerEntity);
                    }
                    user = (User) session.merge(user);
                    session.remove(user);

                    session.getTransaction().commit();
                }
                catch (InterruptedException ex)
                {
                    throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to " +
                            "remove user with associated computers from db.");
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;
                    System.out.println(attemptErrorMessage);
                    ex.printStackTrace(System.out);
                }
            }
            throw new DatabaseException("[FATAL ERROR] UsersManager: Unable to " +
                    "remove user with associated computers from db.");
        }
        finally
        {
            session.close();
        }
    }

    private boolean UserWithSameDisplayedUsernameAndSSHUsernameExists(User userToUpdate, User newUser)
    {
        return (userToUpdate.DisplayedUsername.equals(newUser.DisplayedUsername) == false ||
                userToUpdate.SSH_Username.equals(newUser.SSH_Username) == false) &&
                UserExists(newUser.DisplayedUsername, newUser.SSH_Username);
    }

    private boolean UserExists(String displayedUsername, String SSH_Username)
    {
        User receivedUser = GetUserByDisplayedUsernameAndSSHUsername(displayedUsername, SSH_Username);

        return receivedUser != null;
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------- GETTERS ------------------------------------------------------

    private List<User> GetUsersFromDb() throws DatabaseException
    {
        String attemptErrorMessage = "[ERROR] UsersManager: Attempt of getting computer users from db failed.";
        String hql = "from User";

        Session session = DatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        List<User> users = DatabaseManager.ExecuteSelectQueryWithRetryPolicy(session, query, attemptErrorMessage);
        session.close();

        if(users != null)
        {
            return users;
        }
        else
        {
            throw new DatabaseException("[FATAL ERROR] : Unable to get users from db.");
        }
    }

    public User GetUserById(int id)
    {
        List<User> results = _users.stream()
                .filter(u -> u.Id == id).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public User GetUserByDisplayedUsernameAndSSHUsername(String displayedUsername, String SSH_Username)
    {
        List<User> results = _users.stream()
                .filter(u ->
                        u.DisplayedUsername.equals(displayedUsername) &&
                        u.SSH_Username.equals(SSH_Username)
                ).collect(Collectors.toList());
        return results.isEmpty()? null : results.get(0);
    }

    public List<User> GetUsers()
    {
        return _users;
    }
}

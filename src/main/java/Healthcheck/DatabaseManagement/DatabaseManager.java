package Healthcheck.DatabaseManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Utilities;
import javafx.application.Platform;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public abstract class DatabaseManager
{
    public static boolean PersistWithRetryPolicy(
            Session session,
            Object objectToPersist,
            String moduleName,
            String attemptErrorMessage)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            session.persist(objectToPersist);
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage);

            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.PersistNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.PersistCooldown + randomFactor);

                    session.beginTransaction();
                    session.persist(objectToPersist);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage);
                }
            }

            return false;
        }
    }

    public static boolean MergeWithRetryPolicy(
            Session session,
            Object objectToMerge,
            String moduleName,
            String attemptErrorMessage)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            session.merge(objectToMerge);
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));

            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.PersistNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.PersistCooldown + randomFactor);

                    session.beginTransaction();
                    session.merge(objectToMerge);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));
                }
            }

            return false;
        }
    }

    public static boolean UpdateWithRetryPolicy(
            Session session,
            Object objectToUpdate,
            String moduleName,
            String attemptErrorMessage)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            session.update(objectToUpdate);
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));

            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.UpdateNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.UpdateCooldown + randomFactor);

                    session.beginTransaction();
                    session.update(objectToUpdate);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));
                }
            }

            return false;
        }
    }

    public static boolean RemoveWithRetryPolicy(
            Session session,
            Object objectToRemove,
            String moduleName,
            String attemptErrorMessage)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            session.remove(objectToRemove);
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));

            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.RemoveNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.RemoveCooldown + randomFactor);

                    session.beginTransaction();
                    session.remove(objectToRemove);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));
                }
            }

            return false;
        }
    }

    public static List ExecuteSelectQueryWithRetryPolicy(
            Session session,
            Query query,
            String moduleName,
            String attemptErrorMessage)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            List results = query.getResultList();
            session.getTransaction().commit();

            if(results == null)
            {
                return new ArrayList<>();
            }
            else
            {
                return results;
            }
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));

            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.SelectNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.SelectCooldown + randomFactor);

                    session.beginTransaction();
                    List results = query.getResultList();
                    session.getTransaction().commit();

                    if(results == null)
                    {
                        return new ArrayList<>();
                    }
                    else
                    {
                        return results;
                    }
                }
                catch (InterruptedException ex)
                {
                    return null;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));
                }
            }

            return null;
        }
    }

    public static boolean ExecuteDeleteQueryWithRetryPolicy(
            Session session,
            Query query,
            String moduleName,
            String attemptErrorMessage)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            query.executeUpdate();
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));

            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.DeleteNumOfRetries)
            {
                int randomFactor = new Random().ints(0,100).findFirst().getAsInt();
                try
                {
                    Thread.sleep(Utilities.DeleteCooldown + randomFactor);

                    session.beginTransaction();
                    query.executeUpdate();
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    Platform.runLater(() -> AppLogger.Log(LogType.WARNING, moduleName, attemptErrorMessage));
                }
            }

            return false;
        }
    }
}

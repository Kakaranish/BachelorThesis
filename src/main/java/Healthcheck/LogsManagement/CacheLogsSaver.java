package Healthcheck.LogsManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.DatabaseManagement.CacheDatabaseManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Utilities;
import org.hibernate.Session;

import javax.persistence.Query;
import java.util.List;
import java.util.Random;

public class CacheLogsSaver
{
    public final static String ModuleName = "CacheLogsSaver";

    public static void CacheGivenTypeLogsForComputer(Computer computer, List<LogBaseEntity> logs, IPreference preference)
            throws DatabaseException
    {
        if(logs.size() == 0)
        {
            AppLogger.Log(LogType.INFO, ModuleName, "No logs of " + preference.GetClassName()
                    + " type for '" + computer.GetUsernameAndHost() + "' to cache.");
            return;
        }

        try
        {
            RemoveAllGivenTypeCacheLogsForComputer(computer, preference);
        }
        catch (DatabaseException e)
        {
            throw e;
        }

        Session session = CacheDatabaseManager.GetInstance().GetSession();
        try
        {
            for (LogBaseEntity log : logs)
            {
                boolean persistSucceed = PersistCacheLogWithRetryPolicy(session, log, preference);
                if(persistSucceed == false)
                {
                    throw new DatabaseException("Caching logs for " + computer.GetUsernameAndHost() + " failed.");
                }
            }
        }
        catch (DatabaseException e)
        {
            throw e;
        }
        finally
        {
            session.close();
        }
    }

    private static void RemoveAllGivenTypeCacheLogsForComputer(Computer computer, IPreference preference)
            throws DatabaseException
    {
        String attemptErrorMessage = "Attempt of removing logs of " + preference.GetClassName()
                + " type for " + computer.GetUsernameAndHost() + " failed.";
        String hql = "delete from " + preference.GetClassName() + " t "+
                "where t.Computer = :computer ";

        Session session = CacheDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        query.setParameter("computer", computer);
        boolean removingSucceed = CacheDatabaseManager.ExecuteDeleteQueryWithRetryPolicy(
                session, query, ModuleName, attemptErrorMessage);
        session.close();

        if(removingSucceed == false)
        {
            throw new DatabaseException("Removing cache logs of "
                    + preference.GetClassName() + " type for " + computer.GetUsernameAndHost() + " failed.");
        }
    }

    private static boolean PersistCacheLogWithRetryPolicy(Session session, LogBaseEntity log, IPreference preference)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            session.persist(log);
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            AppLogger.Log(LogType.INFO, ModuleName, "Attempt of caching logs of " + preference.GetClassName()
                    + " type for '" + log.Computer.GetUsernameAndHost() + "' failed.");

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.PersistNumOfRetries)
            {
                try
                {
                    int perturbation = new Random().ints(0,100).findFirst().getAsInt();
                    Thread.sleep(Utilities.PersistCooldown +  perturbation);

                    session.beginTransaction();
                    session.persist(log);
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

                    AppLogger.Log(LogType.INFO, ModuleName, "Attempt of caching logs of " + preference.GetClassName()
                            + " type for '" + log.Computer.GetUsernameAndHost() + "' failed.");
                }
            }

            AppLogger.Log(LogType.INFO, ModuleName, "Caching logs of " + preference.GetClassName()
                    + " type for '" + log.Computer.GetUsernameAndHost() + "' failed after retries.");

            return false;
        }
    }
}

package Healthcheck.LogsManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.DatabaseManagement.CacheDatabaseManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBase;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Utilities;
import org.hibernate.Session;
import javax.persistence.Query;
import java.util.List;
import java.util.Random;

public class CacheLogsSaver
{
    public final static String ModuleName = "CacheLogsSaver";

    public static void CacheGivenTypeLogsForComputer(List<LogBase> logs, IPreference preference)
            throws DatabaseException
    {
        if(logs.size() == 0)
        {
            AppLogger.Log(LogType.INFO, ModuleName, "No logs of " + preference.GetClassName() + " type to cache.");
            return;
        }

        Computer computer = logs.get(0).Computer;
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
            for (LogBase log : logs)
            {
                CacheLogBase cacheLogBase = log.ToCacheLog();
                boolean persistSucceed = PersistCacheLogWithRetryPolicy(
                        session, cacheLogBase, preference, computer.GetUsernameAndHost());
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
        String cacheLogClassName = preference.GetClassName().replace("Log", "CacheLog");

        String attemptErrorMessage = "Attempt of removing logs of " + cacheLogClassName
                + " type for " + computer.GetUsernameAndHost() + " failed.";
        String hql = "delete from " + cacheLogClassName + " t "+
                "where t.ComputerId = :computerId ";

        Session session = CacheDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        query.setParameter("computerId", computer.GetId());
        boolean removingSucceed = CacheDatabaseManager.ExecuteDeleteQueryWithRetryPolicy(
                session, query, ModuleName, attemptErrorMessage);
        session.close();

        if(removingSucceed == false)
        {
            throw new DatabaseException("Removing cache logs of "
                    + cacheLogClassName + " type for " + computer.GetUsernameAndHost() + " failed.");
        }
    }

    private static boolean PersistCacheLogWithRetryPolicy(
            Session session, CacheLogBase cacheLog, IPreference preference, String computerUsernameAndHost)
    {
        String cacheLogClassName = preference.GetClassName().replace("Log", "CacheLog");

        try
        {
            // First attempt
            session.beginTransaction();
            session.persist(cacheLog);
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();

            AppLogger.Log(LogType.INFO, ModuleName, "Attempt of caching logs of " + cacheLogClassName
                    + " type for '" +computerUsernameAndHost + "' failed.");

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.PersistNumOfRetries)
            {
                try
                {
                    int perturbation = new Random()
                            .ints(0, Utilities.CooldownPerturbation).findFirst().getAsInt();
                    Thread.sleep(Utilities.PersistCooldown +  perturbation);

                    session.beginTransaction();
                    session.persist(cacheLog);
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

                    AppLogger.Log(LogType.INFO, ModuleName, "Attempt of caching logs of " + cacheLogClassName
                            + " type for '" + computerUsernameAndHost + "' failed.");
                }
            }

            AppLogger.Log(LogType.INFO, ModuleName, "Caching logs of " + cacheLogClassName
                    + " type for '" + computerUsernameAndHost + "' failed after retries.");

            return false;
        }
    }
}

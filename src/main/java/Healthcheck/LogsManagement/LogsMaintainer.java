package Healthcheck.LogsManagement;

import Healthcheck.*;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.Random;

public class LogsMaintainer extends Thread
{
    private class ComputerAndTimeToMaintainPair
    {
        public Healthcheck.Computer Computer;
        public long TimeToMaintain;

        public ComputerAndTimeToMaintainPair(Computer computer, long timeToMaintain)
        {
            Computer = computer;
            TimeToMaintain = timeToMaintain;
        }
    }

    private LogsManager _logsManager;

    private volatile boolean _isMaintaining = false;

    public LogsMaintainer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    public void StartMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == true)
        {
            throw new LogsException("[FATAL ERROR] Unable to start maintaining logs. Other maintainer currently is working.");
        }

        System.out.println("[INFO] LogsMaintainer started its work.");

        _isMaintaining = true;

        this.start();
    }

    public void StopMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("[FATAL ERROR] LogsMaintainer: Unable to stop maintaining logs. No maintainer is working.");
        }

        System.out.println("[INFO] LogsMaintainer stopped its work.");

        _isMaintaining = false;

        this.interrupt();
    }

    public void run()
    {
        while(_isMaintaining)
        {
            ArrayDeque<Computer> computersToMaintain = new ArrayDeque<>();
            ComputerAndTimeToMaintainPair computerWithLowestTimeToMaintain = null;

            for (Computer computer : _logsManager.GetConnectedComputers())
            {
                long computerTimeToMaintenance = GetComputerTimeToMaintenance(computer);
                System.out.println("time to maintain: " + computerTimeToMaintenance);

                if( computerWithLowestTimeToMaintain == null ||
                        computerTimeToMaintenance < computerWithLowestTimeToMaintain.TimeToMaintain)
                {
                    computerWithLowestTimeToMaintain = new ComputerAndTimeToMaintainPair(computer, computerTimeToMaintenance);;
                }

                if(IsComputerReadyForMaintenance(computerTimeToMaintenance))
                {
                    computersToMaintain.add(computer);
                }
            }

            if (computersToMaintain.isEmpty() == false)
            {
                while(computersToMaintain.isEmpty() == false)
                {
                    Computer computerToMaintain = computersToMaintain.remove();

                    MaintainComputer(computerToMaintain);

                    String host = computerToMaintain.ComputerEntity.Host;
                    System.out.println("[INFO] '" + host + "' was maintained.");
                }
            }
            else
            {
                long timeToNextMaintain = Duration.ofMillis(computerWithLowestTimeToMaintain.TimeToMaintain).toSeconds();
                System.out.println("[INFO] LogsMaintainer: Next maintenance will be taken in " + timeToNextMaintain + "s");

                try
                {
                    Thread.sleep(computerWithLowestTimeToMaintain.TimeToMaintain);
                }
                catch (InterruptedException|IllegalArgumentException e)
                {
                    _logsManager.Callback_Maintainer_ThreadSleepInterrupted();
                }

                MaintainComputer(computerWithLowestTimeToMaintain.Computer);

                String host = computerWithLowestTimeToMaintain.Computer.ComputerEntity.Host;
                System.out.println("[INFO] '" + host+ "' was maintained.");
            }
        }
    }

    public void MaintainComputer(Computer computer)
    {
        System.out.println("INSIDE MAINTAINER");
        long logExpiration = computer.ComputerEntity.LogExpiration.toMillis();

        for (IPreference computerPreference : computer.Preferences)
        {
            Long now = System.currentTimeMillis();

            String hql = "delete from " + computerPreference.GetClassName() + " t "+
                    "where t.ComputerEntity = :computerEntity " +
                    "and (" + now  + " - t.Timestamp) > " + logExpiration;

            Session session = DatabaseManager.GetInstance().GetSession();
            Query query = null;

            query = session.createQuery(hql);
            query.setParameter("computerEntity", computer.ComputerEntity);


            System.out.println("AFTER QUERY");
            boolean removingLogsSucceed =
                    ExecuteQueryWithRetryPolicy(session, query, _logsManager.GetComputerLoggerForComputer(computer));
            if (removingLogsSucceed == false)
            {
                System.out.println("REMOVING LOGS == FALSE");
                session.close();
                return;
            }

            session.close();
        }
        System.out.println("OUTSIDE MAINTAINING1");
        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        try
        {
            _logsManager.SetComputerLastMaintenance(computer, nowTimestamp);
        }
        catch (Exception e)
        {
            e.printStackTrace(System.out);
        }
        System.out.println("OUTSIDE MAINTAINING2");
    }

    private boolean ExecuteQueryWithRetryPolicy(Session session, Query query, ComputerLogger computerLogger)
    {
        try
        {
            session.beginTransaction();
            System.out.println("BEFORE EXECUTE");
            query.executeUpdate();
            System.out.println("AFTER EXECUTE");
            session.getTransaction().commit();
            System.out.println("AFTER COMMIT");

            return true;
        }
        catch (Exception e)
        {
            System.out.println("[ERROR] '"
                    + computerLogger.GetComputer().ComputerEntity.Host
                    + "': LogsMaintainer - executing query attempt failed. Database is locked.");

            e.printStackTrace(System.out);
            session.getTransaction().rollback();

            int retryNum = 1;
            while(retryNum <= Utilities.LogSaveNumOfRetries)
            {
                try
                {
                    Thread.sleep(Utilities.LogSaveRetryCooldown
                            + new Random().ints(0,45).findFirst().getAsInt());

                    session.beginTransaction();
                    query.executeUpdate();
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    _logsManager.Callback_Maintainer_ComputerLoggerThreadSleepInterrupted(computerLogger);
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    System.out.println("[ERROR] '"
                            + computerLogger.GetComputer().ComputerEntity.Host
                            + "': LogsMaintainer - query attempt failed. Database is locked.");
                }
            }

            _logsManager.Callback_Maintainer_ExecuteQueryFailedAfterRetries(computerLogger);
            return false;
        }
    }

    // TODO: Add retry policy
    public static void RemoveAllLogsAssociatedWithComputers(Computer computer) throws DatabaseException
    {
        Session session = DatabaseManager.GetInstance().GetSession();

        try
        {
            session.beginTransaction();

            for (IPreference computerPreference : Preferences.AllPreferencesList)
            {
                String hql = "delete from " +
                        computerPreference.GetClassName() +
                        " t where t.ComputerEntity = :computerEntity";
                Query query = session.createQuery(hql);
                query.setParameter("computerEntity", computer.ComputerEntity);

                query.executeUpdate();
            }

            session.getTransaction().commit();
        }
        catch (PersistenceException e)
        {
            throw new DatabaseException("[FATAL ERROR] Unable te remove all logs associated with computer");
        }
        finally
        {
            session.close();
        }
    }

    public static void RemoveAllLogsAssociatedWithComputerFromDb(Computer computer, Session session)
    {
        for (IPreference computerPreference : Preferences.AllPreferencesList)
        {
            String hql = "delete from " +
                    computerPreference.GetClassName() +
                    " t where t.ComputerEntity = :computerEntity";

            Query query = session.createQuery(hql);
            query.setParameter("computerEntity", computer.ComputerEntity);

            query.executeUpdate();
        }
    }

    private long GetComputerTimeToMaintenance(Computer computer)
    {
        long currentTime = System.currentTimeMillis();
        long lastMaintenanceTime = computer.ComputerEntity.LastMaintenance.getTime();
        long maintenancePeriod = computer.ComputerEntity.MaintainPeriod.toMillis();

        return lastMaintenanceTime + maintenancePeriod - currentTime;
    }

    private boolean IsComputerReadyForMaintenance(long computerTimeToMaintenance)
    {
        return computerTimeToMaintenance <= 0;
    }
}

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

    public boolean MaintainComputer(Computer computer)
    {
        long logExpiration = computer.ComputerEntity.LogExpiration.toMillis();
        String attemptErrorMessage = "[ERROR] LogsMaintainer: Attempt of deleting logs for '"
                + computer.ComputerEntity.Host + "' failed.";

        for (IPreference computerPreference : computer.Preferences)
        {
            Long now = System.currentTimeMillis();

            String hql = "delete from " + computerPreference.GetClassName() + " t "+
                    "where t.ComputerEntity = :computerEntity " +
                    "and (" + now  + " - t.Timestamp) > " + logExpiration;

            Session session = DatabaseManager.GetInstance().GetSession();
            Query query = session.createQuery(hql);
            query.setParameter("computerEntity", computer.ComputerEntity);

            boolean removingLogsSucceed =
                    DatabaseManager.ExecuteDeleteQueryWithRetryPolicy(session, query, attemptErrorMessage);
            if (removingLogsSucceed == false)
            {
                String host = computer.ComputerEntity.Host;
                System.out.println("[FATAL ERROR] LogsMaintainer: Deleting logs for '" + host + "' failed.");

                session.close();
                return false;
            }

            session.close();
        }
        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        try
        {
            _logsManager.SetComputerLastMaintenance(computer, nowTimestamp);
        }
        catch (DatabaseException e)
        {
            return false;
        }

        return true;
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

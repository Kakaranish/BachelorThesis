package Healthcheck.LogsManagement;

import Healthcheck.*;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import org.hibernate.Session;
import javax.persistence.Query;
import javax.security.auth.callback.Callback;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayDeque;

public class LogsMaintainer
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
    private Thread _maintainingThread;
    private boolean _isMaintaining = false;
    private boolean _interruptionIntended = false;
    private boolean _interruptionForRestart = false;

    private Thread GetMaintainingThread()
    {
        Thread maintainingThread = new Thread(this::run);

        return maintainingThread;
    }


    public LogsMaintainer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    public void StartMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == true)
        {
            throw new LogsException(
                    "[FATAL ERROR] Unable to start maintaining logs. Other maintainer currently is working.");
        }

        Callback_InfoMessage("Started work.");

        _isMaintaining = true;
        _maintainingThread = GetMaintainingThread();
        _maintainingThread.start();
    }

    public void StopMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("[FATAL ERROR] LogsMaintainer: Unable to stop maintaining logs. No maintainer is working.");
        }

        _interruptionIntended = true;

        _maintainingThread.interrupt();
        _maintainingThread = null;

        _interruptionIntended = false;

        _isMaintaining = false;
        Callback_InfoMessage("Stopped work.");
    }

    public void RestartMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("[FATAL ERROR] LogsMaintainer: Unable to restart maintaining logs. No maintainer is working.");
        }
        Callback_InfoMessage("Restarting.");

        _interruptionIntended = true;
        _interruptionForRestart = true;

        _maintainingThread.interrupt();
        _maintainingThread = GetMaintainingThread();
        _maintainingThread.start();
    }

    private void run()
    {
        while (_isMaintaining)
        {
            ArrayDeque<Computer> computersToMaintain = new ArrayDeque<>();
            ComputerAndTimeToMaintainPair computerWithLowestTimeToMaintain = null;

            for (Computer computer : _logsManager.GetConnectedComputers())
            {
                long computerTimeToMaintenance = GetComputerTimeToMaintenance(computer);

                if (computerWithLowestTimeToMaintain == null || computerTimeToMaintenance < computerWithLowestTimeToMaintain.TimeToMaintain)
                {
                    computerWithLowestTimeToMaintain = new ComputerAndTimeToMaintainPair(computer, computerTimeToMaintenance);
                }

                if (IsComputerReadyForMaintenance(computerTimeToMaintenance))
                {
                    computersToMaintain.add(computer);
                }
            }

            if (computersToMaintain.isEmpty() == false)
            {
                while (computersToMaintain.isEmpty() == false)
                {
                    Computer computerToMaintain = computersToMaintain.remove();

                    MaintainComputer(computerToMaintain);

                    String host = computerToMaintain.ComputerEntity.Host;
                    Callback_InfoMessage("'" + host + "' was maintained.");
                }
            }
            else
            {
                long timeToNextMaintain = Duration.ofMillis(computerWithLowestTimeToMaintain.TimeToMaintain).toSeconds();
                Callback_InfoMessage("Next maintenance will be taken in " + timeToNextMaintain + "s.");

                try
                {
                    Thread.sleep(computerWithLowestTimeToMaintain.TimeToMaintain);
                }
                catch (InterruptedException | IllegalArgumentException e)
                {
                    if (_interruptionIntended == false)
                    {
                        Callback_InfoMessage("Stopped work");
                        _isMaintaining = false;
                        _logsManager.Callback_Maintainer_InterruptionNotIntended_StopWorkForAllComputerLoggers();
                    }
                    else if (_interruptionIntended && _interruptionForRestart == false)
                    {
                        _logsManager.Callback_Maintainer_InterruptionIntended_StopWorkForAllComputerLoggers();
                        _interruptionIntended = false;
                        _isMaintaining = false;
                    }
                    else if (_interruptionIntended && _interruptionForRestart)
                    {
                        _interruptionIntended = false;
                        _interruptionForRestart = false;
                    }

                    return;
                }

                MaintainComputer(computerWithLowestTimeToMaintain.Computer);

                String host = computerWithLowestTimeToMaintain.Computer.ComputerEntity.Host;
                Callback_InfoMessage("'" + host + "' was maintained.");
            }
        }
    }

    public boolean MaintainComputer(Computer computer)
    {
        String host = computer.ComputerEntity.Host;
        long logExpiration = computer.ComputerEntity.LogExpiration.toMillis();

        String attemptErrorMessage =
                "[ERROR] LogsMaintainer: Attempt of deleting logs for '" + host+ "' failed.";

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
                Callback_FatalError(_logsManager.GetComputerLoggerForComputer(computer),
                        "Deleting logs for '" + host + "' failed.");

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
            Callback_FatalError(_logsManager.GetComputerLoggerForComputer(computer),
                    "Setting last maintenance time for '" + host + "' failed.");

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

    // -----------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------- CALLBACKS --------------------------------------------------------

    public void Callback_InfoMessage(String message)
    {
        System.out.println("[INFO] LogsMaintainer: " + message);
    }

    public void Callback_FatalError(ComputerLogger computerLogger, String message)
    {
        System.out.println("[FATAL ERROR] LogsMaintainer: " + message);

        _logsManager.Callback_Maintainer_StopWorkForComputerLogger(computerLogger);
    }
}

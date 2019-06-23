package Healthcheck.LogsManagement;

import Healthcheck.AppLogger;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.LogType;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import org.hibernate.Session;
import javax.persistence.Query;
import java.sql.Timestamp;
import java.time.Duration;
import java.util.ArrayDeque;
import java.util.List;

public class LogsMaintainer
{
    public final static String ModuleName = "LogsMaintainer";

    private class ComputerAndTimeToMaintainPair
    {
        public Computer Computer;
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

    public LogsMaintainer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    public void StartMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == true)
        {
            throw new LogsException("Unable to start maintaining logs - other maintainer currently is working.");
        }

        AppLogger.Log(LogType.INFO, ModuleName, "Started work.");

        _isMaintaining = true;
        _maintainingThread = GetMaintainingThread();
        _maintainingThread.start();
    }

    public void StopMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("Unable to stop maintaining logs. No maintainer is working.");
        }

        _interruptionIntended = true;

        _maintainingThread.interrupt();
        _maintainingThread = null;
        _isMaintaining = false;

        _interruptionIntended = false;

        AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");
    }

    public void RestartMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("Unable to restart maintaining logs. No maintainer is working.");
        }

        AppLogger.Log(LogType.INFO, ModuleName, "Restarting.");

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

                if (computerWithLowestTimeToMaintain == null
                        || computerTimeToMaintenance < computerWithLowestTimeToMaintain.TimeToMaintain)
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

                    String host = computerToMaintain.GetHost();
                    String username = computerToMaintain.GetSshConfig().GetUsername();
                    AppLogger.Log(LogType.INFO, ModuleName, "'" + username + "@" + host + "' was maintained.");
                }
            }
            else
            {
                long timeToNextMaintain = Duration.ofMillis(computerWithLowestTimeToMaintain.TimeToMaintain).toSeconds();
                AppLogger.Log(LogType.INFO, ModuleName, "Next maintenance in " + timeToNextMaintain + "s.");

                try
                {
                    Thread.sleep(computerWithLowestTimeToMaintain.TimeToMaintain);
                }
                catch (InterruptedException | IllegalArgumentException e)
                {
                    if (_interruptionIntended == false)
                    {
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

                String host = computerWithLowestTimeToMaintain.Computer.GetHost();
                String username = computerWithLowestTimeToMaintain.Computer.GetSshConfig().GetUsername();
                AppLogger.Log(LogType.INFO, ModuleName, "'" + username + "@" + host + "' was maintained.");
            }
        }
    }

    public boolean MaintainComputer(Computer computer)
    {
        String host = computer.GetHost();
        long logExpiration = computer.GetLogExpiration().toMillis();

        String attemptErrorMessage = "Attempt of deleting logs for '" + host+ "' failed.";

        List<IPreference> iPreferences = computer.GetIPreferences();
        for (IPreference computerPreference : iPreferences)
        {
            long now = System.currentTimeMillis();

            String hql = "delete from " + computerPreference.GetClassName() + " t "+
                    "where t.Computer = :computer " +
                    "and (" + now  + " - t.Timestamp) > " + logExpiration;

            Session session = DatabaseManager.GetInstance().GetSession();
            Query query = session.createQuery(hql);
            query.setParameter("computer", computer);

            boolean removingLogsSucceed =
                    DatabaseManager.ExecuteDeleteQueryWithRetryPolicy(session, query, ModuleName, attemptErrorMessage);
            if (removingLogsSucceed == false)
            {
                AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Deleting logs for '" + host + "' failed.");

                ComputerLogger computerLogger = _logsManager.GetComputerLoggerForComputer(computer);
                _logsManager.Callback_Maintainer_StopWorkForComputerLogger(computerLogger);

                session.close();
                return false;
            }

            session.close();
        }

        Timestamp nowTimestamp = new Timestamp(System.currentTimeMillis());
        try
        {
            computer.SetLastMaintenance(nowTimestamp);
            computer.UpdateInDb();
        }
        catch (DatabaseException e)
        {
            ComputerLogger computerLogger = _logsManager.GetComputerLoggerForComputer(computer);
            _logsManager.Callback_Maintainer_StopWorkForComputerLogger(computerLogger);
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Setting last maintenance time for '" + host + "' failed.");

            return false;
        }

        return true;
    }

    public static void RemoveAllLogsAssociatedWithComputerFromDb(Computer computer) throws DatabaseException
    {
        for (IPreference computerPreference : Preferences.AllPreferencesList)
        {
            String hql = "delete from " +
                    computerPreference.GetClassName() +
                    " t where t.Computer = :computer";

            Session session = DatabaseManager.GetInstance().GetSession();
            Query query = session.createQuery(hql);
            query.setParameter("computer", computer);

            String attemptErrorMessage = "Attempt of removing logs associated with '" + computer + "' failed.";
            boolean removeSucceed =
                    DatabaseManager.ExecuteDeleteQueryWithRetryPolicy(session, query, ModuleName, attemptErrorMessage);
            session.close();
            if(removeSucceed == false)
            {
                throw new DatabaseException("Unable to remove logs associated with '" + computer + "'.");
            }
        }
    }

    private Thread GetMaintainingThread()
    {
        Thread maintainingThread = new Thread(this::run);

        return maintainingThread;
    }

    private long GetComputerTimeToMaintenance(Computer computer)
    {
        long currentTime = System.currentTimeMillis();
        long lastMaintenanceTime = computer.GetLastMaintenance().getTime();
        long maintenancePeriod = computer.GetMaintainPeriod().toMillis();

        return lastMaintenanceTime + maintenancePeriod - currentTime;
    }

    private boolean IsComputerReadyForMaintenance(long computerTimeToMaintenance)
    {
        return computerTimeToMaintenance <= 0;
    }

    public boolean IsMaintaining()
    {
        return _isMaintaining;
    }
}

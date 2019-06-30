package Healthcheck.LogsManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.DatabaseManagement.MainDatabaseManager;
import Healthcheck.Entities.Computer;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Preferences.IPreference;
import Healthcheck.Preferences.Preferences;
import javafx.application.Platform;
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

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Stopped work."));
    }

    public void RestartMaintainingLogs() throws LogsException
    {
        if(_isMaintaining == false)
        {
            throw new LogsException("Unable to restart maintaining logs. No maintainer is working.");
        }

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Restarting."));

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

            /*
                Get:
                a) computers that are ready for maintain
                b) computer with lowest time to maintain
             */
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

            /*
                If queue with computers to maintain is not empty then take every of them and maintain.
                When finished try again get computers are ready for maintenance.
             */
            if (computersToMaintain.isEmpty() == false)
            {
                while (computersToMaintain.isEmpty() == false)
                {
                    Computer computerToMaintain = computersToMaintain.remove();

                    boolean maintainSucceed = MaintainComputer(computerToMaintain);
                    if(maintainSucceed == false)
                    {
                        return;
                    }

                    String usernameAndHost = computerToMaintain.GetUsernameAndHost();
                    Platform.runLater(() ->
                            AppLogger.Log(LogType.INFO, ModuleName, "'" + usernameAndHost + "' was maintained.")
                    );
                }
            }
            /*
                If queue with computers to maintain is empty then wait time T.
                T = computerWithLowestTimeToMaintain.TimeToMaintain - time after which closest maintenance will be taken
             */
            else
            {
                long timeToNextMaintain = Duration.ofMillis(computerWithLowestTimeToMaintain.TimeToMaintain).toSeconds();
                Platform.runLater(() ->
                        AppLogger.Log(LogType.INFO, ModuleName, "Next maintenance in " + timeToNextMaintain + "s.")
                );

                try
                {
                    Thread.sleep(computerWithLowestTimeToMaintain.TimeToMaintain);
                }
                catch (InterruptedException | IllegalArgumentException e)
                {
                    if (InterruptionIntended() == false)
                    {
                        _isMaintaining = false;
                        _logsManager.Callback_Maintainer_InterruptionNotIntended_StopWorkForAllComputerLoggers();
                    }
                    else if (InterruptionIntended() && RestartDesired())
                    {
                        _interruptionIntended = false;
                        _interruptionForRestart = false;
                    }
                    else if (InterruptionIntended() && RestartDesired() == false)
                    {
                        _interruptionIntended = false;
                        _isMaintaining = false;
                    }

                    return;
                }

                boolean maintainSucceed = MaintainComputer(computerWithLowestTimeToMaintain.Computer);
                if(maintainSucceed == false)
                {
                    return;
                }

                String usernameAndHost = computerWithLowestTimeToMaintain.Computer.GetUsernameAndHost();
                Platform.runLater(() ->
                        AppLogger.Log(LogType.INFO, ModuleName, "'" + usernameAndHost + "' was maintained.")
                );
            }
        }
    }

    public boolean MaintainComputer(Computer computer)
    {
        String usernameAndHost = computer.GetUsernameAndHost();
        long logExpiration = computer.GetLogExpiration().toMillis();

        List<IPreference> iPreferences = computer.GetIPreferences();
        for (IPreference computerPreference : iPreferences)
        {
            long now = System.currentTimeMillis();

            String hql = "delete from " + computerPreference.GetClassName() + " t "+
                    "where t.Computer = :computer " +
                    "and (" + now  + " - t.Timestamp) > " + logExpiration;

            Session session = MainDatabaseManager.GetInstance().GetSession();
            Query query = session.createQuery(hql);
            query.setParameter("computer", computer);

            String attemptErrorMessage = "Attempt of deleting logs for '" + usernameAndHost + "' failed.";
            boolean removingLogsSucceed =
                    MainDatabaseManager.ExecuteDeleteQueryWithRetryPolicy(session, query, ModuleName, attemptErrorMessage);
            if (removingLogsSucceed == false)
            {
                ComputerLogger computerLogger = _logsManager.GetComputerLoggerForComputer(computer);
                _logsManager.Callback_Maintainer_StopWorkForComputerLogger(computerLogger);
                session.close();

                Platform.runLater(() ->
                        AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                                "Deleting logs for '" + usernameAndHost + "' failed.")
                );
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

            Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                    "Setting last maintenance time for '" + usernameAndHost + "' failed.")
            );

            return false;
        }

        return true;
    }

    public static void RemoveAllLogsAssociatedWithComputerFromDb(Computer computer) throws DatabaseException
    {
        for (IPreference computerPreference : Preferences.AllPreferencesList)
        {
            Session session = MainDatabaseManager.GetInstance().GetSession();

            String hql = "delete from " + computerPreference.GetClassName() + " t where t.Computer = :computer";
            Query query = session.createQuery(hql);
            query.setParameter("computer", computer);

            String attemptErrorMessage = "Attempt of removing logs associated with '" + computer + "' failed.";
            boolean removeSucceed =
                    MainDatabaseManager.ExecuteDeleteQueryWithRetryPolicy(session, query, ModuleName, attemptErrorMessage);
            session.close();
            if(removeSucceed == false)
            {
                throw new DatabaseException("Unable to remove logs associated with '" + computer + "'.");
            }
        }
    }

    public static void RemoveGivenTypeLogsForComputer(
            Computer computer, IPreference preference, Timestamp from, Timestamp to)
            throws DatabaseException
    {
        String className = preference.GetClassName();

        String attemptErrorMessage = "Attempt of removing logs of " + className
                + " type for " + computer.GetUsernameAndHost() + " failed.";
        String hql = "delete from " + className + " t "+
                "where t.Computer = :computer and t.Timestamp > " + from.getTime() + " and t.Timestamp < " + to.getTime();

        Session session = MainDatabaseManager.GetInstance().GetSession();
        Query query = session.createQuery(hql);
        query.setParameter("computer", computer);

        boolean removingSucceed = MainDatabaseManager.ExecuteDeleteQueryWithRetryPolicy(
                session, query, ModuleName, attemptErrorMessage);
        session.close();

        if(removingSucceed == false)
        {
            throw new DatabaseException("Removing logs of "
                    + preference.GetClassName() + " type for " + computer.GetUsernameAndHost() + " failed.");
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

    private boolean InterruptionIntended()
    {
        return _interruptionIntended;
    }

    private boolean RestartDesired()
    {
        return _interruptionForRestart;
    }
}

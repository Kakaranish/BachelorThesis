package Healthcheck.LogsManagement;

import Healthcheck.*;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.DatabaseManagement.MainDatabaseManager;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.CpuLog;
import Healthcheck.Entities.Logs.LogBase;
import Healthcheck.Models.Info.IInfo;
import Healthcheck.Preferences.CpusInfoPreference;
import Healthcheck.Preferences.IPreference;
import Healthcheck.SSHConnectionManagement.SSHConnection;
import Healthcheck.SSHConnectionManagement.SSHConnectionException;
import javafx.application.Platform;
import org.hibernate.Session;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class ComputerLogger extends Thread
{
    private static final String ModuleName = "ComputerLogger";

    private Computer _computer;
    private String _usernameAndHost;
    private List<IPreference> _iPreferences;

    private LogsGatherer _logsGatherer;
    private SSHConnection _sshConnection;
    private boolean _isGathering = false;

    private Thread _establishingConnectionThread;

    public ComputerLogger(LogsGatherer logsGatherer, Computer computer)
    {
        _logsGatherer = logsGatherer;
        _computer = computer;
        _usernameAndHost = computer.GetUsernameAndHost();
        _iPreferences = _computer.GetIPreferences();
    }

    public boolean StartWork()
    {
        if(_isGathering == true || _sshConnection == null)
        {
            return false;
        }

        this.start();

        _isGathering = true;

        return true;
    }

    // Once stopped ComputerLogger cannot be started again
    public boolean StopWork()
    {
        if(_isGathering == false)
        {
            if(_establishingConnectionThread != null)
            {
                StopEstablishingSSHConnection();
                return true;
            }

            return false;
        }

        this.interrupt();

        _isGathering = false;
        CloseSSHConnection();

        return true;
    }

    public void run()
    {
        if(ThreadInterrupted())
        {
            return;
        }

        while(_isGathering)
        {
            boolean gatheringLogsSucceed = GatherAndSaveLogsForAllPreferenceTypes();
            if(gatheringLogsSucceed == false)
            {
                if(_sshConnection != null && _sshConnection.IsConnectionEstablished())
                {
                    _sshConnection.CloseConnection();
                    _sshConnection = null;
                }

                if(ThreadInterrupted() && InterruptionIntended())
                {
                    _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                    return;
                }

                _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this);

                boolean renewSucceed = TryToRenewConnection();
                if(renewSucceed == false)
                {
                    return;
                }
            }

            _logsGatherer.Callback_LogsGatheredSuccessfully(this);

            try
            {
                Thread.sleep(_computer.GetRequestInterval().toMillis());
            }
            catch (InterruptedException e)
            {
                _sshConnection.CloseConnection();

                if(InterruptionIntended())
                {
                    _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                    return;
                }
                else // No need to check if internet connection is available
                {
                    _sshConnection.CloseConnection();
                    _isGathering = false;

                    _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this);
                    boolean renewSucceed = TryToRenewConnection();
                    if(renewSucceed == false)
                    {
                        return;
                    }
                }
            }
        }
    }

    private boolean GatherAndSaveLogsForAllPreferenceTypes()
    {
        Timestamp now = new Timestamp (System.currentTimeMillis());

        for (IPreference preference : _iPreferences)
        {
            List<LogBase> logsToSave;
            if(preference instanceof CpusInfoPreference)
            {
                List<CpuLog> cpuLogs = new ArrayList<>();

                List<LogBase> logBaseFirstBatch = GatherLogsForGivenPreferenceTypeWithRetryPolicy(preference, now);
                if(logBaseFirstBatch == null)
                {
                    return false;
                }
                List<CpuLog> cpuLogsFirstBatch = logBaseFirstBatch.stream().map(l -> (CpuLog)l).collect(Collectors.toList());
                cpuLogsFirstBatch.forEach(c -> c.CpuInfo.FirstBatch = true);

                cpuLogs.addAll(cpuLogsFirstBatch);

                try
                {
                    Thread.sleep(Utilities.ProcStatGapCooldown);
                }
                catch(Exception e)
                {
                    Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                            "CpuLogs gathering sleep interrupted."));
                    return false;
                }

                List<LogBase> logBaseSecondBatch = GatherLogsForGivenPreferenceTypeWithRetryPolicy(preference, now);
                if(logBaseSecondBatch == null)
                {
                    return false;
                }
                List<CpuLog> cpuLogsSecondBatch
                        = logBaseSecondBatch.stream().map(l -> (CpuLog) l).collect(Collectors.toList());

                cpuLogs.addAll(cpuLogsSecondBatch);
                logsToSave = cpuLogs.stream().map(c -> (LogBase) c).collect(Collectors.toList());
            }
            else
            {
                logsToSave = GatherLogsForGivenPreferenceTypeWithRetryPolicy(preference, now);
                if(logsToSave == null)
                {
                    return false;
                }
            }

            if(ThreadInterrupted())
            {
                if(_logsGatherer.InterruptionIntended())
                {
                    _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                }

                return false;
            }

            for (LogBase log : logsToSave)
            {
                Session session = MainDatabaseManager.GetInstance().GetSession();

                boolean logSaved = SaveLogToSessionWithRetryPolicy(session, log);
                if (logSaved == false)
                {
                    session.close();

                    return false;
                }

                session.close();
            }

            CacheLogsSaver.CacheGivenTypeLogsForComputer(logsToSave, preference);
        }

        return true;
    }

    private List<LogBase> GatherLogsForGivenPreferenceTypeWithRetryPolicy(IPreference computerIPreference, Timestamp timestamp)
    {
        String attemptErrorMessage = "Attempt of getting logs for '" + _usernameAndHost + "' failed. SSH connection failed.";

        try
        {
            // First attempt
            String sshResultNotProcessed = _sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());
            IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
            List<LogBase> logs = model.ToLogList(_computer, timestamp);

            if(logs == null)
            {
                logs = new ArrayList<>();
            }
            return logs;
        }
        catch (SSHConnectionException e)
        {
            _logsGatherer.Callback_ErrorMessage(attemptErrorMessage);

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.SelectNumOfRetries)
            {
                try
                {
                    if(ThreadInterrupted())
                    {
                        if(InterruptionIntended())
                        {
                            _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                        }

                        return null;
                    }

                    Thread.sleep(Utilities.SelectCooldown);

                    String sshResultNotProcessed = _sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());

                    IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
                    List<LogBase> logs = model.ToLogList(_computer, timestamp);
                    if(logs == null)
                    {
                        logs = new ArrayList<>();
                    }

                    return logs;
                }
                catch (InterruptedException ex)
                {
                    if(_logsGatherer.InterruptionIntended())
                    {
                        _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                    }

                    return null;
                }
                catch (Exception ex)
                {
                    _logsGatherer.Callback_ErrorMessage(attemptErrorMessage);

                    ++retryNum;
                }
            }

            if(ThreadInterrupted() && InterruptionIntended())
            {
                _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
            }
            else if(ThreadInterrupted() == false && Utilities.InternetConnectionIsAvailable() == false)
            {
                String fatalErrorMessage = "Getting logs for '"
                        + _usernameAndHost + "' failed - internet connection lost.";
                _logsGatherer.Callback_StoppedComputerLogger_InternetConnectionLost(fatalErrorMessage);
            }

            return null;
        }
    }

    private boolean SaveLogToSessionWithRetryPolicy(Session session, LogBase log)
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

            _logsGatherer.Callback_ErrorMessage("Attempt of saving logs for '" + _usernameAndHost + "' failed.");

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.PersistNumOfRetries)
            {
                try
                {
                    if(ThreadInterrupted())
                    {
                        if(InterruptionIntended())
                        {
                            _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                        }

                        return false;
                    }

                    int perturbation = new Random()
                            .ints(0, Utilities.CooldownPerturbation).findFirst().getAsInt();
                    Thread.sleep(Utilities.PersistCooldown +  perturbation);

                    session.beginTransaction();
                    session.persist(log);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    if(_logsGatherer.InterruptionIntended())
                    {
                        _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                    }

                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    _logsGatherer.Callback_ErrorMessage("Attempt of saving logs for '" + _usernameAndHost + "' failed.");
                }
            }

            if(ThreadInterrupted() && InterruptionIntended())
            {
                _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
            }
            else
            {
                if(Utilities.InternetConnectionIsAvailable() == false)
                {
                    String fatalErrorMessage = "Saving logs for '" + _usernameAndHost + "' failed - internet connection lost.";
                    _logsGatherer.Callback_StoppedComputerLogger_InternetConnectionLost(fatalErrorMessage);
                }
            }

            return false;
        }
    }

    private boolean TryToRenewConnection()
    {
        while (true)
        {
            try
            {
                _sshConnection = ConnectWithComputerUsingSSH();
                if(_sshConnection != null)
                {
                    _logsGatherer.Callback_RenewedConnectionWithComputer(this);
                    _isGathering = true;
                    return true;
                }

                Thread.sleep(_computer.GetRequestInterval().toMillis());
            }
            catch (InterruptedException e)
            {
                AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Renewing connection thread interrupted.");
                return false;
            }
        }
    }

    public boolean ValidateHasPreferences()
    {
        if(_iPreferences.isEmpty())
        {
            _logsGatherer.Callback_InfoMessage("'" + _usernameAndHost + "' has no preferences. " +
                    "No need to establish SSH connection with computer.");
            return false;
        }

        return true;
    }

    public void StartEstablishingSSHConnection()
    {
        _establishingConnectionThread = new Thread(() ->
        {
            while(true)
            {
                if(_logsGatherer == null || (_logsGatherer.IsWorking() == false && InterruptionIntended() == false))
                {
                    return;
                }

                _sshConnection = ConnectWithComputerUsingSSH();
                if(_sshConnection != null)
                {
                    break;
                }

                _logsGatherer.Callback_ErrorMessage("Connection with '" + _computer.GetUsernameAndHost() +
                        "' was not established. Next attempt in " + _computer.GetRequestInterval().toSeconds() + "s.");
                try
                {
                    Thread.sleep(_computer.GetRequestInterval().toMillis());
                }
                catch (InterruptedException e)
                {
                    return;
                }
            }

            _establishingConnectionThread = null;
            _logsGatherer.Callback_ConnectedComputerLogger(this);

            StartWork();
        });

        _establishingConnectionThread.start();
    }

    public void StopEstablishingSSHConnection()
    {
        if(_establishingConnectionThread != null)
        {
            _establishingConnectionThread.interrupt();
            _establishingConnectionThread = null;
        }
    }

    private SSHConnection ConnectWithComputerUsingSSH()
    {
        try
        {
            SSHConnection sshConnection = new SSHConnection();
            sshConnection.OpenConnection(_computer.GetHost(), _computer.GetSshConfig());

            _logsGatherer.Callback_InfoMessage("SSH connection with '" + _usernameAndHost + "' established.");
            return sshConnection;
        }
        catch (EncrypterException e)
        {
            _logsGatherer.Callback_FatalErrorWithoutAction(
                    "SSH connection with '" + _usernameAndHost + "' failed. Unable to decrypt password.");
        }
        catch (SSHConnectionException e)
        {
            _logsGatherer.Callback_FatalErrorWithoutAction(
                    "Unable to establish connection with '" + _usernameAndHost + "' - timeout.");
        }
        return null;
    }

    public void CloseSSHConnection()
    {
        if(_sshConnection != null)
        {
            _sshConnection.CloseConnection();
            _sshConnection = null;
        }
    }

    public boolean IsConnectedUsingSSH()
    {
        return _sshConnection != null;
    }

    public final Computer GetComputer()
    {
        return _computer;
    }

    private boolean ThreadInterrupted()
    {
        return _isGathering == false;
    }

    private boolean InterruptionIntended()
    {
        return _logsGatherer.InterruptionIntended();
    }
}

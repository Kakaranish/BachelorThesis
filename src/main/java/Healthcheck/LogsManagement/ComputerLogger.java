package Healthcheck.LogsManagement;

import Healthcheck.*;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Models.Info.IInfo;
import Healthcheck.Preferences.IPreference;
import Healthcheck.SSHConnectionManagement.SSHConnection;
import Healthcheck.SSHConnectionManagement.SSHConnectionException;
import org.hibernate.Session;
import java.sql.Timestamp;
import java.util.List;
import java.util.Random;

public class ComputerLogger extends Thread
{
    private Computer _computer;
    private String _usernameAndHost;
    private List<IPreference> _iPreferences;

    private LogsGatherer _logsGatherer;
    private SSHConnection _sshConnection;
    private boolean _isGathering = false;

    public ComputerLogger(LogsGatherer logsGatherer, Computer computer)
    {
        _logsGatherer = logsGatherer;
        _computer = computer;
        _usernameAndHost = computer.GetUsernameAndHost();
        _iPreferences = _computer.GetIPreferences();
    }

    public boolean StartGatheringLogs()
    {
        if(_isGathering == true || _sshConnection == null)
        {
            return false;
        }

        this.start();

        _isGathering = true;

        return true;
    }

    // Once Stopped ComputerLogger cannot be started again
    public boolean StopGatheringLogs()
    {
        if(_isGathering == false)
        {
            return false;
        }

        this.interrupt();

        _isGathering = false;
        CloseSSHConnection();

        return true;
    }

    public void run()
    {
        while(_isGathering)
        {
            boolean gatheringLogsSucceed = GatherAndSaveLogsForAllPreferenceTypes();
            if(gatheringLogsSucceed == false)
            {
                return; // Getting data through ssh connection or saving to db failed - end thread
            }

            _logsGatherer.Callback_InfoMessage("Logs for '" + _usernameAndHost + "' gathered. Next gathering in "
                    + _computer.GetRequestInterval().toSeconds() + "s.");

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
                }
                else
                {
                    _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this, null);
                }

                return;
            }
        }
    }

    private boolean GatherAndSaveLogsForAllPreferenceTypes()
    {
        Timestamp now = new Timestamp (System.currentTimeMillis());

        for (IPreference computerPreference : _iPreferences)
        {
            List<BaseEntity> logsToSave = GatherLogsForGivenPreferenceTypeWithRetryPolicy(computerPreference, now);
            if(logsToSave == null)
            {
                _sshConnection.CloseConnection();
                return false;
            }

            if(ThreadInterrupted())
            {
                return false;
            }

            for (BaseEntity log : logsToSave)
            {
                Session session = DatabaseManager.GetInstance().GetSession();

                boolean logSaved = SaveLogToSessionWithRetryPolicy(session, log);
                if (logSaved == false)
                {
                    _sshConnection.CloseConnection();
                    session.close();

                    return false;
                }

                session.close();
            }
        }

        return true;
    }

    private List<BaseEntity> GatherLogsForGivenPreferenceTypeWithRetryPolicy(IPreference computerIPreference, Timestamp timestamp)
    {
        try
        {
            // First attempt
            String sshResultNotProcessed = _sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());
            IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
            List<BaseEntity> logs = model.ToLogList(_computer, timestamp);

            return logs;
        }
        catch (SSHConnectionException e)
        {
            _logsGatherer.Callback_ErrorMessage(
                    "Attempt of getting logs for '" + _usernameAndHost + "' failed. SSH connection failed");

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.SelectNumOfRetries)
            {
                try
                {
                    if(ThreadInterrupted())
                    {
                        _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);

                        return null;
                    }

                    Thread.sleep(Utilities.SelectCooldown);

                    String sshResultNotProcessed = _sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());
                    IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
                    List<BaseEntity> logs = model.ToLogList(_computer, timestamp);

                    return logs;
                }
                catch (InterruptedException ex)
                {
                    if(_logsGatherer.InterruptionIntended())
                    {
                        _logsGatherer.Callback_StoppedComputerLogger_InterruptionIntended(this);
                    }
                    else
                    {
                        _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this,
                                "Getting logs failed");
                    }

                    return null;
                }
                catch (Exception ex)
                {
                    _logsGatherer.Callback_ErrorMessage(
                            "Attempt of getting logs for '" + _usernameAndHost + "' failed. SSH connection failed.");

                    ++retryNum;
                }
            }

            _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this,
                    "Getting logs for '" + _usernameAndHost + "' failed after retries.");

            return null;
        }
    }

    private boolean SaveLogToSessionWithRetryPolicy(Session session, BaseEntity log)
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
                        AppLogger.Log(LogType.INFO, "LogsGatherer",
                                "Stopped gathering logs for '" + _usernameAndHost + "'.");

                        return false;
                    }

                    int perturbation = new Random().ints(0,100).findFirst().getAsInt();
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
                    else
                    {
                        _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this, null);
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

            _logsGatherer.Callback_StoppedComputerLogger_InterruptionNotIntended(this,
                    "Saving logs for '" + _usernameAndHost + "' failed after retries.");
            return false;
        }
    }

    public void ConnectWithComputerThroughSSH()
    {
        if(_iPreferences.isEmpty())
        {
            _logsGatherer.Callback_InfoMessage("'" + _usernameAndHost + "' has no preferences. " +
                    "No need to establish SSH connection with computer.");
            return;
        }

        new Thread(() -> _sshConnection = GetSSHConnectionWithComputer()).start();
    }

    private SSHConnection GetSSHConnectionWithComputer()
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
                    "SSH connection with '" + _usernameAndHost + "' failed because of timeout.");
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

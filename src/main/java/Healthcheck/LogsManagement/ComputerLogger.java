package Healthcheck.LogsManagement;

import Healthcheck.*;
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
    private String _host;
    private List<IPreference> _iPreferences;

    private LogsGatherer _logsGatherer;
    private SSHConnection _sshConnection;
    private boolean _isGathering = false;

    public ComputerLogger(LogsGatherer logsGatherer, Computer computer)
    {
        _logsGatherer = logsGatherer;
        _computer = computer;
        _host = _computer.Host;
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
        while(true)
        {
            Timestamp timestamp = new Timestamp (System.currentTimeMillis());

            for (IPreference computerPreference : _iPreferences)
            {
                List<BaseEntity> logsToSave =
                        GetLogsForGivenPreferenceTypeWithRetryPolicy(computerPreference, timestamp);
                if(logsToSave == null)
                {
                    _sshConnection.CloseConnection();
                    return;
                }

                for (BaseEntity log : logsToSave)
                {
                    Session session = DatabaseManager.GetInstance().GetSession();

                    boolean logSaved = SaveLogToSessionWithRetryPolicy(session, log);
                    if (logSaved == false)
                    {
                        _sshConnection.CloseConnection();
                        session.close();

                        return;
                    }

                    session.close();
                }
            }

            _logsGatherer.Callback_InfoMessage("Logs for '" + _host + "' were gathered.");

            try
            {
                Thread.sleep(_computer.RequestInterval.toMillis());
            }
            catch (InterruptedException e)
            {
                if(_logsGatherer.IsInterruptionIntended())
                {
                    _sshConnection.CloseConnection();
                    _logsGatherer.Callback_StoppedGathering(this);
                }
                else
                {
                    _logsGatherer.Callback_FatalError(this,
                            "Sleep was interrupted for '" + _host + "' in main loop.");
                    _sshConnection.CloseConnection();
                }

                return;
            }
        }
    }

    private List<BaseEntity> GetLogsForGivenPreferenceTypeWithRetryPolicy(IPreference computerIPreference, Timestamp timestamp)
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
            _logsGatherer.Callback_ErrorMessage("Attempt of getting logs for '" + _host + "' failed. SSH connection failed");

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.SelectNumOfRetries)
            {
                try
                {
                    Thread.sleep(Utilities.SelectCooldown);

                    String sshResultNotProcessed = _sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());
                    IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
                    List<BaseEntity> logs = model.ToLogList(_computer, timestamp);

                    return logs;
                }
                catch (InterruptedException ex)
                {
                    if(_logsGatherer.IsInterruptionIntended())
                    {
                        _sshConnection.CloseConnection();
                        _logsGatherer.Callback_StoppedGathering(this);
                    }
                    else
                    {
                        _sshConnection.CloseConnection();
                        _logsGatherer.Callback_FatalError(this,
                                "Sleep was interrupted for '" + _host + "' in getting logs method.");
                    }

                    return null;
                }
                catch (Exception ex)
                {
                    _logsGatherer.Callback_ErrorMessage("Attempt of getting logs for '" + _host + "' failed. SSH connection failed.");

                    ++retryNum;
                }
            }

            _logsGatherer.Callback_FatalError(this,
                    "Getting logs for '" + _host + "' failed after retries.");
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

            _logsGatherer.Callback_ErrorMessage("Attempt of saving logs for '" + _host + "' failed. Database is locked.");

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.PersistNumOfRetries)
            {
                try
                {
                    Thread.sleep(Utilities.PersistCooldown
                            + new Random().ints(0,100).findFirst().getAsInt());

                    session.beginTransaction();
                    session.persist(log);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    if(session.isOpen())
                    {
                        session.close();
                    }
                    _sshConnection.CloseConnection();

                    if(_logsGatherer.IsInterruptionIntended())
                    {
                        _logsGatherer.Callback_StoppedGathering(this);
                    }
                    else
                    {
                        _logsGatherer.Callback_FatalError(this,
                                "'" + _host + "' sleep was interrupted in saving logs method.");
                    }

                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    _logsGatherer.Callback_ErrorMessage(
                            "Attempt of saving logs for '" + _host + "' failed. Database is locked.");
                }
            }

            _logsGatherer.Callback_FatalError(this,
                    "Saving logs for '" + _host + "' failed after retries.");
            return false;
        }
    }

    public void ConnectWithComputerThroughSSH()
    {
        if(_iPreferences.isEmpty())
        {
            _logsGatherer.Callback_InfoMessage(
                    "'" + _host + "' has no preferences. No need to establish SSH connection with computer.");
            return;
        }

        new Thread(() -> {
            _sshConnection = GetSSHConnectionWithComputer();
        }).start();
    }

    private SSHConnection GetSSHConnectionWithComputer()
    {
        try
        {
            SSHConnection sshConnection = new SSHConnection();
            sshConnection.OpenConnection(_host, _computer.SSHConfiguration);

            _logsGatherer.Callback_InfoMessage("SSH connection with '" + _host + "' established.");
            return sshConnection;
        }
        catch (EncrypterException e)
        {
            _logsGatherer.Callback_FatalErrorWithoutAction(
                    "SSH connection with '" + _host + "' failed. Unable to decrypt password.");
        }
        catch (SSHConnectionException e)
        {
            _logsGatherer.Callback_FatalErrorWithoutAction(
                    "SSH connection with '" + _host + "' failed because of timeout.");
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
}

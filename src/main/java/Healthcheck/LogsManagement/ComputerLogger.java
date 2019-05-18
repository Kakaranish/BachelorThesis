package Healthcheck.LogsManagement;

import Healthcheck.*;
import Healthcheck.DatabaseManagement.DatabaseManager;
import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
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
    private LogsGatherer _logsGatherer;

    public ComputerLogger(LogsGatherer logsManager, Computer computer)
    {
        _computer = computer;
        _logsGatherer = logsManager;
    }

    public void StartGatheringLogs()
    {
        this.start();
    }

    public void StopGatheringLogs()
    {
        this.interrupt();
    }

    public void run()
    {
        if(_computer.Preferences.isEmpty())
        {
            _logsGatherer.Callback_ComputerHasNoPreferences(this);
            return;
        }

        SSHConnection sshConnection = ConnectWithComputerUsingSSH(_computer);
        if(sshConnection == null)
        {
            return;
        }

        while(true)
        {
            Timestamp timestamp = new Timestamp (System.currentTimeMillis());

            for (IPreference computerPreference : _computer.Preferences)
            {
                List<BaseEntity> logsToSave =
                        GetLogsForGivenPreferenceTypeWithRetryPolicy(sshConnection, computerPreference, timestamp);
                if(logsToSave == null)
                {
                    sshConnection.CloseConnection();
                    return;
                }

                Session session = DatabaseManager.GetInstance().GetSession();
                for (BaseEntity log : logsToSave)
                {

                    boolean logSaved = SaveLogToSessionWithRetryPolicy(session, log);
                    if (logSaved == false)
                    {
                        return;
                    }
                }
                session.close();
            }

            _logsGatherer.Callback_LogGathered(_computer.ComputerEntity.Host);

            try
            {
                Thread.sleep(_computer.ComputerEntity.RequestInterval.toMillis());
            }
            catch (InterruptedException e)
            {
                _logsGatherer.Callback_ThreadSleepInterrupted(this);

                sshConnection.CloseConnection();
                return;
            }
        }
    }

    private SSHConnection ConnectWithComputerUsingSSH(Computer computer)
    {
        String decryptedPassword;
        try
        {
            decryptedPassword = Encrypter.GetInstance().Decrypt(computer.ComputerEntity.GetEncryptedPassword());
        }
        catch (EncrypterException e)
        {
            _logsGatherer.Callback_UnableToDecryptPassword(this);

            return null;
        }

        try
        {
            SSHConnection sshConnection = new SSHConnection();
            sshConnection.OpenConnection(
                    computer.ComputerEntity.Host,
                    computer.ComputerEntity.GetUsername(),
                    decryptedPassword,
                    computer.ComputerEntity.Port,
                    Utilities.SSH_Timeout
            );

            return sshConnection;
        }
        catch (SSHConnectionException e)
        {
            _logsGatherer.Callback_SSHConnectionFailed(this);
            return null;
        }
    }

    private boolean SaveLogToSessionWithRetryPolicy(Session session, BaseEntity log)
    {
        try
        {
            // First attempt
            session.beginTransaction();
            session.save(log);
            session.flush();
            session.getTransaction().commit();

            return true;
        }
        catch (Exception e)
        {
            session.getTransaction().rollback();
            _logsGatherer.Callback_DatabaseTransactionCommitAttemptFailed(this);

            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.LogSaveNumOfRetries)
            {
                try
                {
                    Thread.sleep(Utilities.LogSaveRetryCooldown
                            + new Random().ints(0,45).findFirst().getAsInt());

                    session.beginTransaction();
                    session.save(log);
                    session.flush();
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    _logsGatherer.Callback_ThreadSleepInterrupted(this);
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    _logsGatherer.Callback_DatabaseTransactionCommitAttemptFailed(this);
                }
            }

            _logsGatherer.Callback_DatabaseTransactionCommitFailedAfterRetries(this);
            return false;
        }
    }

    private List<BaseEntity> GetLogsForGivenPreferenceTypeWithRetryPolicy(
            SSHConnection sshConnection, IPreference computerIPreference, Timestamp timestamp)
    {
        try
        {
            // First attempt
            String sshResultNotProcessed = sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());
            IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
            List<BaseEntity> logs = model.ToLogList(_computer.ComputerEntity, timestamp);

            return logs;
        }
        catch (SSHConnectionException e)
        {
            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.NumOfRetries)
            {
                try
                {
                    _logsGatherer.Callback_SSHConnectionExecuteCommandAttemptFailed(this);

                    Thread.sleep(Utilities.Cooldown);

                    String sshResultNotProcessed = sshConnection.ExecuteCommand(computerIPreference.GetCommandToExecute());
                    IInfo model = computerIPreference.GetInformationModel(sshResultNotProcessed);
                    List<BaseEntity> logs = model.ToLogList(_computer.ComputerEntity, timestamp);

                    return logs;
                }
                catch (SSHConnectionException ex)
                {
                    ++retryNum;
                }
                catch (InterruptedException ex)
                {
                    sshConnection.CloseConnection();
                    _logsGatherer.Callback_ThreadSleepInterrupted(this);
                    return null;
                }
            }

            _logsGatherer.Callback_SSHConnectionExecuteCommandFailedAfterRetries(this);
            return null;
        }
    }

    public final Computer GetComputer()
    {
        return _computer;
    }
}

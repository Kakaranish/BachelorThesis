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
import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.util.List;

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

        SSHConnection sshConnection = ConnectWithComputerUsingRetryPolicy(_computer);
        if(sshConnection == null)
        {
            return;
        }

        Session session = null;
        while(true)
        {
            Timestamp timestamp = new Timestamp (System.currentTimeMillis());

            session = DatabaseManager.GetInstance().GetSession();

            for (IPreference computerPreference : _computer.Preferences)
            {
                session.beginTransaction();

                List<BaseEntity> logsToSave =
                        GetLogsForGivenPreferenceTypeWithRetryPolicy(sshConnection, computerPreference, timestamp);
                if(logsToSave == null)
                {
                    sshConnection.CloseConnection();
                    return;
                }

                for (BaseEntity log : logsToSave)
                {
                    session.save(log);
                }

                boolean databaseTransactionSucceed = CommitTransactionWithRetryPolicy(session);
                if(databaseTransactionSucceed == false)
                {
                    session.close();
                    sshConnection.CloseConnection();
                    return;
                }
            }

            session.close();
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

    private SSHConnection ConnectWithComputerUsingRetryPolicy(Computer computer)
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

        SSHConnection sshConnection = new SSHConnection();
        try
        {
            // First attempt
            sshConnection.OpenConnection(
                    computer.ComputerEntity.Host,
                    computer.ComputerEntity.GetUsername(),
                    decryptedPassword,
                    computer.ComputerEntity.Port,
                    computer.ComputerEntity.Timeout
            );

            return sshConnection;
        }
        catch (SSHConnectionException|IllegalArgumentException e)
        {
            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.NumOfRetries)
            {
                try
                {
                    _logsGatherer.Callback_SSHConnectionAttemptFailed(this);

                    Thread.sleep(Utilities.Cooldown);

                    sshConnection.OpenConnection(
                            computer.ComputerEntity.Host,
                            computer.ComputerEntity.GetUsername(),
                            decryptedPassword,
                            computer.ComputerEntity.Port,
                            computer.ComputerEntity.Timeout
                    );

                    return sshConnection;
                }
                catch (SSHConnectionException|IllegalArgumentException ex)
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

            _logsGatherer.Callback_SSHConnectionFailedAfterRetries(this);
            return null;
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

    private boolean CommitTransactionWithRetryPolicy(Session session)
    {
        try
        {
            // First attempt
            session.getTransaction().commit();

            return true;
        }
        catch (PersistenceException e)
        {
            // Retries
            int retryNum = 1;
            while (retryNum <= Utilities.NumOfRetries)
            {
                try
                {
                    _logsGatherer.Callback_DatabaseTransactionCommitAttemptFailed(this);

                    Thread.sleep(Utilities.Cooldown);

                    session.getTransaction().commit();

                    return true;
                }
                catch (PersistenceException ex)
                {
                    ++retryNum;
                }
                catch (InterruptedException ex)
                {
                    _logsGatherer.Callback_ThreadSleepInterrupted(this);
                    return false;
                }
            }
        }

        _logsGatherer.Callback_DatabaseTransactionCommitFailedAfterRetries(this);
        return false;
    }

    public final Computer GetComputer()
    {
        return _computer;
    }
}

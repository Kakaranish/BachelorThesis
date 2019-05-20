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
    private LogsManager _logsManager;
    private SSHConnection _sshConnection;

    public ComputerLogger(LogsManager logsManager, Computer computer)
    {
        _logsManager = logsManager;
        _computer = computer;
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
        while(true)
        {
            Timestamp timestamp = new Timestamp (System.currentTimeMillis());

            for (IPreference computerPreference : _computer.Preferences)
            {
                List<BaseEntity> logsToSave =
                        GetLogsForGivenPreferenceTypeWithRetryPolicy(_sshConnection, computerPreference, timestamp);
                if(logsToSave == null)
                {
                    _sshConnection.CloseConnection();
                    return;
                }

                Session session = DatabaseManager.GetInstance().GetSession();
                for (BaseEntity log : logsToSave)
                {
                    boolean logSaved = SaveLogToSessionWithRetryPolicy(session, log);
                    if (logSaved == false)
                    {
                        session.close();
                        return;
                    }
                }
                session.close();
            }

            System.out.println("[INFO] '" + _computer.ComputerEntity.Host + "': Logs have been gathered.");

            try
            {
                Thread.sleep(_computer.ComputerEntity.RequestInterval.toMillis());
            }
            catch (InterruptedException e)
            {
                _logsManager.Callback_GatheringSSHThreadSleepInterrupted(this);

                _sshConnection.CloseConnection();
                return;
            }
        }
    }

    public void ConnectWithComputerThroughSSH()
    {
        if(_computer.Preferences.isEmpty())
        {
            System.out.println("[INFO] '" + _computer.ComputerEntity.Host
                    + "': SSH connection failed - computer has no preferences.");
            return;
        }

        new Thread(() -> {
            _sshConnection = GetSSHConnectionWithComputer(_computer);
        }).start();
    }

    public boolean IsConnectedUsingSSH()
    {
        return _sshConnection != null;
    }

    private SSHConnection GetSSHConnectionWithComputer(Computer computer)
    {
        String decryptedPassword;
        try
        {
            decryptedPassword = Encrypter.GetInstance().Decrypt(computer.ComputerEntity.GetEncryptedPassword());
        }
        catch (EncrypterException e)
        {
            System.out.println("[FATAL ERROR] '" + _computer.ComputerEntity.Host
                    + "': SSH connection failed - unable to decrypt password.");

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

            System.out.println("[INFO] '" + _computer.ComputerEntity.Host + "': SSH connection established.");
            return sshConnection;
        }
        catch (SSHConnectionException e)
        {
            System.out.println("[FATAL ERROR] '" + _computer.ComputerEntity.Host
                    + "': SSH connection failed - timeout.");

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
            System.out.println(e.getMessage());
            session.getTransaction().rollback();

            System.out.println("[ERROR] '" + _computer.ComputerEntity.Host
                    + "': Database gathering transaction commit attempt failed.");
            // Retries
            int retryNum = 1;
            while(retryNum <= Utilities.LogSaveNumOfRetries)
            {
                try
                {
                    Thread.sleep(Utilities.LogSaveRetryCooldown
                            + new Random().ints(0,100).findFirst().getAsInt());

                    session.beginTransaction();
                    session.persist(log);
                    session.getTransaction().commit();

                    return true;
                }
                catch (InterruptedException ex)
                {
                    _logsManager.Callback_GatheringThreadSleepInterrupted(this);
                    return false;
                }
                catch (Exception ex)
                {
                    session.getTransaction().rollback();
                    ++retryNum;

                    System.out.println("[ERROR] '" + _computer.ComputerEntity.Host
                            + "': Database gathering transaction commit attempt failed.");
                }
            }

            _logsManager.Callback_GatheringDatabaseTransactionCommitFailedAfterRetries(this);
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
                    System.out.println("[FATAL ERROR] '" + _computer.ComputerEntity.Host
                            + "': SSH connection command execution attempt failed.");

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
                    _logsManager.Callback_GatheringThreadSleepInterrupted(this);
                    return null;
                }
            }

            _logsManager.Callback_SSHConnectionExecuteCommandFailedAfterRetries(this);
            return null;
        }
    }

    public final Computer GetComputer()
    {
        return _computer;
    }
}

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
    public static final int NumOfRetries = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("NumOfRetries"));
    public static final int Cooldown = Integer.parseInt(AppProperties.GetInstance().Properties.getProperty("Cooldown"));

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
        SSHConnection sshConnection = ConnectWithComputerUsingRetryPolicy(_computer);
        if(sshConnection == null)
        {
            return;
        }

        Session session = null;
        while(true)
        {
            session = DatabaseManager.GetInstance().GetSession();
            try
            {
                session.beginTransaction();

                Timestamp timestamp = new Timestamp (System.currentTimeMillis());
                for (IPreference computerPreference : _computer.Preferences)
                {
                    try
                    {
                        String result = sshConnection.ExecuteCommand(computerPreference.GetCommandToExecute());
                        IInfo model = computerPreference.GetInformationModel(result);
                        List<BaseEntity> logsToSave = model.ToLogList(_computer.ComputerEntity, timestamp);

                        for (BaseEntity log : logsToSave)
                        {
                            session.save(log);
                        }
                    }
                    catch (SSHConnectionException e)
                    {
                        _logsGatherer.Callback_SSHConnectionExecuteCommandFailed(this);

                        sshConnection.CloseConnection();
                        session.close();
                        return;
                    }
                }
                _logsGatherer.Callback_LogGathered(_computer.ComputerEntity.Host);

                session.getTransaction().commit();
            }
            catch (PersistenceException e)
            {
                _logsGatherer.Callback_DatabaseTransactionFailed(_computer.ComputerEntity.Host);

                sshConnection.CloseConnection();
                return;
            }
            finally
            {
                session.close();
            }

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
            // First SSHConnection attempt
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
            while(retryNum <= NumOfRetries)
            {
                try
                {
                    _logsGatherer.Callback_SSHConnectionAttemptFailed(this);

                    Thread.sleep(Cooldown);

                    sshConnection.OpenConnection(
                            computer.ComputerEntity.Host,
                            computer.ComputerEntity.GetUsername(),
                            decryptedPassword,
                            computer.ComputerEntity.Port,
                            computer.ComputerEntity.Timeout
                    );

                    return sshConnection;
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

            _logsGatherer.Callback_SSHConnectionFailedAfterRetries(this);
            return null;
        }
    }

    public final Computer GetComputer()
    {
        return _computer;
    }
}

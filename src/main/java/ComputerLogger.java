import Entities.Logs.BaseEntity;
import Models.Info.IInfo;
import Preferences.IPreference;
import org.hibernate.Session;
import javax.persistence.PersistenceException;
import java.sql.Timestamp;
import java.util.List;

public class ComputerLogger extends Thread
{
    private Computer _computer;
    private LogsManager _logsManager;
    private SSHConnection _sshConnection;

    private boolean _isGathering = false;

    public ComputerLogger(LogsManager logsManager, Computer computer)
    {
        _computer = computer;
        _logsManager = logsManager;
        _sshConnection = new SSHConnection();
    }

    public void run()
    {
        try
        {
            boolean connectedWithComputer = OpenSSHConnectionWithComputer();

            if(connectedWithComputer == false)
            {
                int retryNum = 0;
                while(retryNum < _logsManager.NumOfRetries && !connectedWithComputer)
                {
                    _logsManager.Callback_SSHConnectionAttemptFailed(this);

                    try
                    {
                        Thread.sleep(_logsManager.Cooldown);
                    }
                    catch (InterruptedException e)
                    {
                        CloseSSHConnectionWithComputer();
                    }
                    connectedWithComputer = OpenSSHConnectionWithComputer();

                    ++retryNum;
                }

                if(!connectedWithComputer)
                {
                    _logsManager.Callback_UnableToConnectAfterRetries(this);
                    return;
                }
            }
        }
        catch (IllegalArgumentException e)
        {
            _logsManager.Callback_UnableToDecryptPassword(this);
            return;
        }

        Session session;
        _isGathering = true;
        while(_isGathering)
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
                        String result = _sshConnection.ExecuteCommand(computerPreference.GetCommandToExecute());
                        IInfo model = computerPreference.GetInformationModel(result);
                        List<BaseEntity> logsToSave = model.ToLogList(_computer.ComputerEntity, timestamp);

                        for (BaseEntity log : logsToSave)
                        {
                            session.save(log);
                        }
                    }
                    catch (SSHConnectionException e)
                    {
                        CloseSSHConnectionWithComputer();
                        session.close();
                        return;
                    }
                }
                _logsManager.Callback_LogGathered(_computer.ComputerEntity.Host);

                session.getTransaction().commit();
            }
            catch (PersistenceException e)
            {
                CloseSSHConnectionWithComputer();
                _logsManager.Callback_DatabaseTransactionFailed(_computer.ComputerEntity.Host);
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
                CloseSSHConnectionWithComputer();
                _logsManager.Callback_ThreadInterrupted(_computer.ComputerEntity.Host);
                return;
            }
        }

        CloseSSHConnectionWithComputer();
        _logsManager.Callback_LogGatheringStopped(this); //TODO: Add interruption
    }

    private boolean OpenSSHConnectionWithComputer() throws IllegalArgumentException
    {
        try
        {
            String password = Encrypter.GetInstance().Decrypt(_computer.ComputerEntity.GetEncryptedPassword());
            _sshConnection.OpenConnection(
                    _computer.ComputerEntity.Host,
                    _computer.ComputerEntity.GetUsername(),
                    password,
                    _computer.ComputerEntity.Port,
                    _computer.ComputerEntity.Timeout
            );
            return true;
        }
        catch (EncrypterException|SSHConnectionException e)
        {
            return false;
        }
    }

    public void StartGatheringLogs()
    {
        this.start();
    }

    public void StopGatheringLogs()
    {
        this.interrupt();
    }

    private void CloseSSHConnectionWithComputer()
    {
        _sshConnection.CloseConnection();
    }

    public final Computer GetComputer()
    {
        return _computer;
    }
}

import Entities.Logs.BaseEntity;
import Models.Info.IInfo;
import Preferences.IPreference;
import org.hibernate.Session;
import java.util.Date;
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
        boolean connectedWithComputer = OpenSSHConnectionWithComputer();
        if(!connectedWithComputer)
        {
            int retryNum = 0;
            while(retryNum < _logsManager.NumOfRetries && !connectedWithComputer)
            {
                System.out.println("SSH Connection with " + _computer.ComputerEntity.getHost() + " failed.");
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
                _logsManager.GatheringSSHConnectionErrorCallback(this);
                return;
            }
        }

        _isGathering = true;
        while(_isGathering)
        {
            //TODO: Callback when connection with db fails
            Session session = DatabaseManager.GetInstance().GetSession();

            session.beginTransaction();
            Date timestamp = new Date();
            for (IPreference computerPreference : _computer.ComputerPreferences)
            {
                try
                {
                    //TODO: Callback when command execution fails
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

            session.getTransaction().commit();
            session.close();

            try
            {
                Thread.sleep(_computer.ComputerEntity.RequestInterval.toMillis());
            }
            catch (InterruptedException e)
            {
                _logsManager.GatheringStoppedCallback(this);
                CloseSSHConnectionWithComputer();
                return;
            }
        }
        CloseSSHConnectionWithComputer();
        _logsManager.GatheringStoppedCallback(this);
    }

    private boolean OpenSSHConnectionWithComputer()
    {
        try
        {
            String password = Encrypter.GetInstance().Decrypt(_computer.ComputerEntity.getPassword());
            _sshConnection.OpenConnection(
                    _computer.ComputerEntity.getHost(),
                    _computer.ComputerEntity.getUsername(),
                    password,
                    _computer.ComputerEntity.getPort(),
                    _computer.ComputerEntity.getTimeout()
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
}

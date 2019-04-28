import Entities.Computer;
import Entities.Logs.BaseEntity;
import Models.Info.IInfo;
import Preferences.IPreference;
import org.hibernate.Session;
import java.util.Date;
import java.util.List;

public class ComputerManager extends Thread
{
    private Computer _computer; // Needed encapsulation of computer class
    private SSHConnection _sshConnection;
    private List<IPreference> _computerPreferences;
    private LogsManager _logsManager;

    private boolean _isGathering = false;

    public ComputerManager(LogsManager logsManager, Computer computer, List<IPreference> computerPreferences)
    {
        _logsManager = logsManager;
        _computer = computer;
        _computerPreferences = computerPreferences;
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
                try
                {
                    Thread.sleep(_logsManager.Cooldown);
                }
                catch (InterruptedException e)
                {
                    e.printStackTrace();
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
            for (IPreference computerPreference : _computerPreferences)
            {
                try
                {
                    //TODO: Callback when command execution fails
                    String result = _sshConnection.ExecuteCommand(computerPreference.GetCommandToExecute());
                    IInfo model = computerPreference.GetInformationModel(result);
                    List<BaseEntity> logsToSave = model.ToLogList(_computer, timestamp);

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
                Thread.sleep(_computer.RequestInterval.toMillis());
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }
        CloseSSHConnectionWithComputer();
        _logsManager.GatheringStoppedCallback(this);
    }

    private boolean OpenSSHConnectionWithComputer()
    {
        try
        {
            String password = Encrypter.GetInstance().Decrypt(_computer.getPassword());
            _sshConnection.OpenConnection(
                    _computer.getHost(),
                    _computer.getUsername(),
                    password,
                    _computer.getPort(),
                    _computer.getTimeout()
            );
            return true;
        }
        catch (EncrypterException|SSHConnectionException e)
        {
            return false;
        }
    }

    public void SetAsNotGathering()
    {
        _isGathering = false;
    }

    private void CloseSSHConnectionWithComputer()
    {
        _sshConnection.CloseConnection();
    }
}

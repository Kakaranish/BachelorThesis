import Entities.Computer;
import Entities.Logs.BaseEntity;
import Models.Info.IInfo;
import Preferences.IPreference;
import org.hibernate.Session;
import org.hibernate.dialect.Database;

import java.util.Date;
import java.util.List;

public class ComputerManager extends Thread
{
    private Computer _computer; // Needed encapsulation of computer class
    private SSHConnection _sshConnection;
    private List<IPreference> _computerPreferences;

    public ComputerManager(Computer computer, List<IPreference> computerPreferences)
    {
        _computer =  computer;
        _computerPreferences = computerPreferences;
        _sshConnection = new SSHConnection();
    }

    //TODO: Add break condition
    public void run()
    {
        OpenSSHConnectionWithComputer();
        while(true)
        {
            Session session = DatabaseManager.GetInstance().GetSession();

            session.beginTransaction();
            Date timestamp = new Date();
            for (IPreference computerPreference : _computerPreferences)
            {
                try
                {
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
    }

    private void OpenSSHConnectionWithComputer()
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
        }
        catch (EncrypterException|SSHConnectionException e)
        {
            e.printStackTrace(); //TODO
        }
    }

    private void CloseSSHConnectionWithComputer()
    {
        _sshConnection.CloseConnection();
    }
}

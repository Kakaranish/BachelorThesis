package Healthcheck.SSHConnectionManagement;

import Healthcheck.Encryption.Encrypter;
import Healthcheck.Encryption.EncrypterException;
import Healthcheck.Entities.SshAuthMethod;
import Healthcheck.Entities.SshConfig;
import Healthcheck.Utilities;
import com.jcraft.jsch.*;
import java.io.*;
import java.util.Properties;

public class SSHConnection
{
    private JSch _jsch;
    private Session _session = null;
    public SSHConnection()
    {
        _jsch = new JSch();
    }

    public void OpenConnection(String host, SshConfig sshConfiguration) throws EncrypterException, SSHConnectionException
    {
        if(sshConfiguration.GetAuthMethod() == SshAuthMethod.PASSWORD)
        {
            _session = GetSessionUsingPasswordAuth(host, sshConfiguration);
        }
        else if(sshConfiguration.GetAuthMethod() == SshAuthMethod.KEY)
        {
            _session = GetSessionUsingKeyAuth(host, sshConfiguration);
        }
    }

    private Session GetSessionUsingPasswordAuth(String host, SshConfig sshConfig)
            throws EncrypterException, SSHConnectionException
    {
        String decryptedPassword = Encrypter.GetInstance().Decrypt(sshConfig.GetEncryptedPassword());

        try
        {
            Session session = _jsch.getSession(sshConfig.GetUsername(), host, sshConfig.GetPort());
            session.setPassword(decryptedPassword);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(Utilities.SSHTimeout);
            return session;
        }
        catch (Exception ex)
        {
            throw new SSHConnectionException("Unable to get session.");
        }
    }

    private Session GetSessionUsingKeyAuth(String host, SshConfig sshConfig) throws SSHConnectionException
    {
        try
        {
            _jsch.addIdentity(sshConfig.GetPrivateKeyPath());
            Session session = _jsch.getSession(sshConfig.GetUsername(), host, sshConfig.GetPort());

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(Utilities.SSHTimeout);
            return session;
        }
        catch (Exception e)
        {
            throw new SSHConnectionException("Unable to get session.");
        }
    }

    public void CloseConnection(){
        if(_session != null)
        {
            _session.disconnect();
            _session = null;
        }
    }

    public String ExecuteCommand(String command) throws SSHConnectionException
    {
        if (IsConnectionEstablished() == false)
        {
            throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Unable to execute command. " +
                    "Connection is not established.");
        }

        String result = new String();
        try
        {
            ChannelExec channel = (ChannelExec) GetChannel(Utilities.SSHTimeout, "exec");
            channel.setInputStream(null);
            channel.setCommand(command);
            channel.setErrStream(System.err);

            InputStream in = channel.getInputStream();
            channel.connect();

            byte[] tmp = new byte[1024];
            while (true)
            {
                while (in.available() > 0)
                {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                    {
                        break;
                    }
                    result += new String(tmp, 0, i);
                }
                if (channel.isClosed())
                {
                    break;
                }

                try
                {
                    Thread.sleep(30);
                }
                catch (InterruptedException e)
                {
                    throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Unable to execute command. " +
                            "Thread sleep interrupted.");
                }
            }
            channel.disconnect();
        }
        catch (JSchException ex)
        {
            throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Unable to execute command. " +
                    "Channel can't be opened.");
        }
        catch (IOException ex)
        {
            throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Unable to execute command. " +
                    "InputStream can't be get from channel.");
        }
        catch (Exception ex)
        {
            throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Some problem occured.");
        }

        return result;
    }

    private Channel GetChannel(int timeout, String type) throws SSHConnectionException
    {
        Channel channel;
        try
        {
            channel = _session.openChannel(type);
        }
        catch (Exception ex)
        {
            throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Unable to get channel.");
        }

        return channel;
    }
    private boolean IsConnectionEstablished()
    {
        return _session.isConnected();
    }
}

package Healthcheck.SSHConnectionManagement;

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

    // TODO: Open connection when SSH_Key is provided
    public void OpenConnection(String host, String username, String password, int port, int timeout)
            throws SSHConnectionException
    {
        try
        {
            _session = GetSession(username, host, password, port, timeout);
        }
        catch(SSHConnectionException e)
        {
            throw e;
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

    private boolean IsConnectionEstablished()
    {
        return _session.isConnected();
    }

    private Session GetSession(String username, String host, String password, int port, int timeout)
            throws SSHConnectionException
    {
        Session session;
        try
        {
            session = _jsch.getSession(username, host, port);
            session.setPassword(password);

            Properties config = new Properties();
            config.put("StrictHostKeyChecking", "no");
            session.setConfig(config);

            session.connect(timeout);
            return session;
        }
        catch (Exception ex)
        {
            throw new SSHConnectionException("[FATAL ERROR] SSHConnection: Unable to get session.");
        }
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
}

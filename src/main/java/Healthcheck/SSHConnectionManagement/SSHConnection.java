package Healthcheck.SSHConnectionManagement;

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
        catch(SSHConnectionException ex)
        {
            throw ex;
        }
    }

    public void CloseConnection(){
        if(_session != null)
        {
            _session.disconnect();
            _session = null;
        }
    }

    public boolean IsConnectionEstablished()
    {
        return _session.isConnected();
    }

    public String ExecuteCommand(String command) throws SSHConnectionException
    {
        if (IsConnectionEstablished() == false)
        {
            throw new SSHConnectionException("Can't execute command. Connection is not established.");
        }

        String result = new String();
        try
        {
            ChannelExec channel = (ChannelExec) GetChannel(3000, "exec");
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
                catch (Exception ee)
                {
                    throw new SSHConnectionException("Unable to execute command. Sleep can't be executed.");
                }
            }
            channel.disconnect();
        }
        catch (JSchException ex)
        {
            throw new SSHConnectionException("Unable to execute command. Channel can't be opened.");
        }
        catch (IOException ex)
        {
            throw new SSHConnectionException("Unable to execute command. InputStream can't be get from channel.");
        }

        return result;
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
        }
        catch (JSchException ex)
        {
            throw new SSHConnectionException("Unable to get session.");
        }
        return session;
    }

    private Channel GetChannel(int timeout, String type) throws SSHConnectionException
    {
        Channel channel;
        try
        {
            channel = _session.openChannel(type);
        }
        catch (JSchException ex)
        {
            throw new SSHConnectionException("Unable to get channel.");
        }

        return channel;
    }
}

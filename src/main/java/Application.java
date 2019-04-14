import SystemInfo.*;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;

public class Application
{
    public static void main(String[] args)
    {
        String host = "51.68.142.57";
        String username = "root";
        String password = "some_password";
        int port = 22;
        int timeout = 2000;

        String diskCommand = "df -h";
        String processesCommand = "ps aux";
        String cpuInfoCommand = "grep 'cpu ' /proc/stat | awk '{usage=($2+$4)*100/($2+$4+$5)} END {print usage \"%\"}'";
        String swapInfoCommand = "free --mega | grep Swap";

        SSHConnection connection = new SSHConnection();

        try
        {
            connection.OpenConnection(username, host, password, port, timeout);
            System.out.println("Connection was established");

            String result = connection.ExecuteCommand(swapInfoCommand);
            System.out.println(result);

            SwapInfo swapInfo = new SwapInfo(result);
//            CpuInfo cpuInfo = new CpuInfo(result);
//            ProcessesInfo processesInfo = new ProcessesInfo(result);
//            DisksInfo disksInfo = new DisksInfo(result);

            connection.CloseConnection();
        }
        catch (SSHConnectionException ex)
        {
            ex.printStackTrace();
        }
    }
}

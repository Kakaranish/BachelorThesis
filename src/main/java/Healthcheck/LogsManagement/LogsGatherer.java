package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.LogsManagement.ComputerLogger;

import java.util.ArrayList;
import java.util.List;

public class LogsGatherer
{
    private List<ComputerLogger> _computersToGatherLogs;

    public LogsGatherer()
    {

    }

    public void StartGatheringLogs(List<Computer> selectedComputers)
    {
        List<ComputerLogger> computersToGather = new ArrayList<>();
        for (Computer computerToGather : selectedComputers)
        {
            computersToGather.add(new ComputerLogger(this, computerToGather));
        }

        _computersToGatherLogs = computersToGather;

        for (ComputerLogger computerLogger : _computersToGatherLogs)
        {
            computerLogger.StartGatheringLogs();
        }

        //        for (Healthcheck.LogsManagement.ComputerLogger computerLogger : _computersToGatherLogs)
        //        {
        //            try
        //            {
        //                computerLogger.join();
        //            }
        //            catch (InterruptedException e)
        //            {
        //                e.printStackTrace();
        //            }
        //        }
    }

    public void StopGatheringLogsForAllComputerLoggers()
    {
        for (ComputerLogger gatheredComputer : _computersToGatherLogs)
        {
            gatheredComputer.StopGatheringLogs();
        }

        _computersToGatherLogs = null;

        System.out.println("[INFO] Gathering logs for all computers has been stopped.");
    }

    public void StopGatheringLogsForSingleComputerLogger(ComputerLogger computerLogger)
    {
        computerLogger.StopGatheringLogs();

        _computersToGatherLogs.remove(computerLogger);

        if(_computersToGatherLogs.isEmpty())
        {
            _computersToGatherLogs = null;
            String host = computerLogger.GetComputer().ComputerEntity.Host;
            System.out.println("[INFO] Gathering logs for '" + host + "' has been stopped.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------  CALLBACKS  ---------------------------------------------------

    public void Callback_UnableToDecryptPassword(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': Connection failed. Unable to decrypt password.");
        //        _gatheredComputers.remove(computerLogger);
    }

    public void Callback_SSHConnectionAttemptFailed(ComputerLogger computerLogger)
    {

        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[ERROR] '" + host + "': Attempt of SSH connection failed.");
        _computersToGatherLogs.remove(computerLogger);
    }

    public void Callback_UnableToConnectAfterRetries(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection failed. Max num of retries reached.");
        _computersToGatherLogs.remove(computerLogger);
    }

    public void Callback_LogGathered(String host)
    {
        System.out.println("[INFO] '" + host + "': Logs have been gathered.");
    }

    public void Callback_DatabaseConnectionAttemptFailed(String host)
    {
        System.out.println("[ERROR] '" + host + "': attempt of connection with database failed.");
    }

    public void Callback_DatabaseTransactionFailed(String host)
    {
        System.out.println("[FATAL ERROR] '" + host + "': DatabaseManagement transaction failed.");
    }

    public void Callback_ThreadSleepInterrupted(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection failed. Thread sleep interrupted.");
    }

    public void Callback_SSHConnectionExecuteCommandFailed(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection execute command failed.");
    }

    public void Callback_LogGatheringStopped(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[INFO] '" + host + "': Logs gathering has been stopped.");
        _computersToGatherLogs.remove(computerLogger);
    }
}

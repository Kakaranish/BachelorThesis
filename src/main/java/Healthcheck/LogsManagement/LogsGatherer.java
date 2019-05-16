package Healthcheck.LogsManagement;

import Healthcheck.Computer;

import java.util.ArrayList;
import java.util.List;

public class LogsGatherer
{
    private List<ComputerLogger> _gatheredComputers;

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

        _gatheredComputers = computersToGather;

        for (ComputerLogger computerLogger : _gatheredComputers)
        {
            computerLogger.StartGatheringLogs();
        }
    }

    public void StopGatheringLogsForAllComputerLoggers()
    {
        for (ComputerLogger gatheredComputer : _gatheredComputers)
        {
            gatheredComputer.StopGatheringLogs();
        }

        _gatheredComputers = null;

        System.out.println("[INFO] Gathering logs for all computers has been stopped.");
    }

    public void StopGatheringLogsForSingleComputerLogger(ComputerLogger computerLogger)
    {
        computerLogger.StopGatheringLogs();

        _gatheredComputers.remove(computerLogger);

        if(_gatheredComputers.isEmpty())
        {
            _gatheredComputers = null;
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
    }

    public void Callback_SSHConnectionAttemptFailed(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[ERROR] '" + host + "': Attempt of SSH connection failed.");
    }

    public void Callback_SSHConnectionFailedAfterRetries(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection failed. Max num of retries reached.");
    }

    public void Callback_SSHConnectionExecuteCommandFailed(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection execute command failed.");
    }

    public void Callback_LogGathered(String host)
    {
        System.out.println("[INFO] '" + host + "': Logs have been gathered.");
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

    // -----------------------------------------------------------------------------------------------------------------
    // --------------------------------------------------- GETTERS -----------------------------------------------------

    public List<Computer> GetGatheredComputers()
    {
        List<Computer> gatheredComputers = new ArrayList<>();

        for (ComputerLogger gatheredComputer : _gatheredComputers)
        {
            gatheredComputers.add(gatheredComputer.GetComputer());
        }

        return gatheredComputers;
    }
}

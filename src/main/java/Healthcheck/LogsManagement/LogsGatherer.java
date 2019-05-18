package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import java.util.ArrayList;
import java.util.List;

public class LogsGatherer
{
    private LogsManager _logsManager;

    private List<ComputerLogger> _gatheredComputers;

    public LogsGatherer(LogsManager logsManager)
    {
        _logsManager = logsManager;
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

        System.out.println("[INFO] Gathering logs for all computers has been stopped.");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -------------------------------------------------  CALLBACKS  ---------------------------------------------------

    public void Callback_LogGathered(String host)
    {
        System.out.println("[INFO] '" + host + "': Logs have been gathered.");
    }

    /////////////////////////////////////////////////////////////////////////
    // Callbacks that may occur during attempting to make connection
    /////////////////////////////////////////////////////////////////////////

    public void Callback_UnableToDecryptPassword(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': Connection failed. Unable to decrypt password.");
        RemoveComputerFromGatheredComputers(computerLogger);
    }

    public void Callback_SSHConnectionFailed(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection failed.");
        RemoveComputerFromGatheredComputers(computerLogger);
    }

    private void RemoveComputerFromGatheredComputers(ComputerLogger computerLogger)
    {
        _gatheredComputers.remove(computerLogger);
    }

    /////////////////////////////////////////////////////////////////////////
    // Callbacks that may occur when connected with computer
    /////////////////////////////////////////////////////////////////////////

    public void Callback_ThreadSleepInterrupted(ComputerLogger computerLogger)
    {
        RemoveComputerFromGatheredComputers(computerLogger);

        String host = computerLogger.GetComputer().ComputerEntity.Host;
        String callbackMessage = "[FATAL ERROR] '" + host + "': SSH connection failed. Thread sleep interrupted.";

        _logsManager.Callback_ConnectionWithComputerHasBeenBroken(computerLogger.GetComputer(), callbackMessage); // TODO: ?
    }

    public void Callback_DatabaseTransactionCommitAttemptFailed(ComputerLogger computerLogger)
    {
        RemoveComputerFromGatheredComputers(computerLogger);

        String host = computerLogger.GetComputer().ComputerEntity.Host;
        String callbackMessage = "[ERROR] '" + host + "': DatabaseManagement transaction commit attempt failed.";
    }

    public void Callback_DatabaseTransactionCommitFailedAfterRetries(ComputerLogger computerLogger)
    {
        RemoveComputerFromGatheredComputers(computerLogger);

        String host = computerLogger.GetComputer().ComputerEntity.Host;
        String callbackMessage = "[FATAL ERROR] '" + host + "': DatabaseManagement transaction commit failed after retries.";

        _logsManager.Callback_ConnectionWithComputerHasBeenBroken(computerLogger.GetComputer(), callbackMessage); // TODO: ?
    }

    public void Callback_SSHConnectionExecuteCommandAttemptFailed(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection execute command attempt failed.");
    }

    public void Callback_SSHConnectionExecuteCommandFailedAfterRetries(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection execute command failed after retries.");
        RemoveComputerFromGatheredComputers(computerLogger);

        // TODO: Add callback to _logsManager
    }

    public void Callback_ComputerHasNoPreferences(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[INFO] '" + host + "': Maintaining & log gathering cannot be performed for computer. Computer has no preferences.");
        RemoveComputerFromGatheredComputers(computerLogger);
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

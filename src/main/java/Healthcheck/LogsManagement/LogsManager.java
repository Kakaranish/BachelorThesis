package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.ComputerManager;
import Healthcheck.Utilities;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogsManager
{
    private LogsGatherer _logsGatherer;
    private LogsMaintainer _logsMaintainer;
    private ComputerManager _computerManager;
    private List<ComputerLogger> _connectedComputerLoggers;

    public LogsManager(ComputerManager computerManager)
    {
        _computerManager = computerManager;
    }

    public void StartWork() throws LogsException, NothingToDoException
    {
        if(_logsGatherer != null && _logsMaintainer != null)
        {
            throw new LogsException("[FATAL ERROR] Unable to start LogsManager work because it's currently working.");
        }
        else
        {
            _logsGatherer = new LogsGatherer(this);
            _logsMaintainer = new LogsMaintainer(this);
        }

        _connectedComputerLoggers = GetReachableComputerLoggers(_computerManager.GetSelectedComputers());
        if(_connectedComputerLoggers.isEmpty())
        {
            throw new NothingToDoException("[INFO] LogsManager: No computers to maintenance & logs gathering.");
        }

        _logsGatherer.StartGatheringLogs();
        _logsMaintainer.StartMaintainingLogs();
    }

    public void StopWork()
    {
        // TODO
    }

    public List<ComputerLogger> GetConnectedComputerLoggers()
    {
        return _connectedComputerLoggers;
    }

    public List<Computer> GetConnectedComputers()
    {
        List<Computer> connectedComputers = _connectedComputerLoggers.stream()
                .map(ComputerLogger::GetComputer).collect(Collectors.toList());

        return connectedComputers;
    }

    public void SetComputerLastMaintenance(Computer computer, Timestamp timestamp)
    {
        Computer newComputer = new Computer(computer);
        newComputer.ComputerEntity.LastMaintenance = timestamp;
        _computerManager.UpdateComputer(computer, newComputer.ComputerEntity);
    }

    /////////////////////////////////////////////////////////////////////////
    // Callbacks connected with GATHERING
    /////////////////////////////////////////////////////////////////////////

    public void Callback_Gatherer_StopManagementForComputerLogger(ComputerLogger computerLogger)
    {
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
            System.out.println("[INFO] LogsGatherer stopped gathering logs for '"
                + computerLogger.GetComputer().ComputerEntity.Host + "'.");
            System.out.println("[INFO] LogsMaintainer stopped maintaining logs for '"
                    + computerLogger.GetComputer().ComputerEntity.Host + "'.");
        }
    }

    public void Callback_Gatherer_SSHThreadSleepInterrupted(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection failed. Thread sleep interrupted.");

        Callback_Gatherer_StopManagementForComputerLogger(computerLogger);
    }

    public void Callback_Gatherer_ThreadSleepInterrupted(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': Gathering failed. Thread sleep interrupted.");

        Callback_Gatherer_StopManagementForComputerLogger(computerLogger);
    }

    public void Callback_Gatherer_DatabaseTransactionCommitFailedAfterRetries(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': Database transaction commit failed after retries.");

        Callback_Gatherer_StopManagementForComputerLogger(computerLogger);
    }

    public void Callback_Gatherer_SSHConnectionExecuteCommandFailedAfterRetries(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[FATAL ERROR] '" + host + "': SSH connection command execution failed after retries.");

        Callback_Gatherer_StopManagementForComputerLogger(computerLogger);
    }

    /////////////////////////////////////////////////////////////////////////
    // Callbacks connected with MAINTAINING
    /////////////////////////////////////////////////////////////////////////

    public void Callback_Maintainer_StopWork()
    {
        StopWork();
    }

    public void Callback_Maintainer_StopManagementForComputerLogger(ComputerLogger computerLogger)
    {
        _logsGatherer.StopGatheringLogsForSingleComputerLogger(computerLogger);
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
        }
    }

    public void Callback_Maintainer_ThreadSleepInterrupted()
    {
        System.out.println("[FATAL ERROR] Maintaining logs for all computer loggers failed. Main thread sleep interrupted.");

        Callback_Maintainer_StopWork();
    }

    public void Callback_Maintainer_ComputerLoggerThreadSleepInterrupted(ComputerLogger computerLogger)
    {
        System.out.println("[FATAL ERROR] '" + computerLogger.GetComputer().ComputerEntity.Host
                + "': Maintaining logs failed - thread sleep interrupted.");

        Callback_Maintainer_StopManagementForComputerLogger(computerLogger);
    }

    public void Callback_Maintainer_ExecuteQueryFailedAfterRetries(ComputerLogger computerLogger)
    {
        System.out.println("[FATAL ERROR] '" + computerLogger.GetComputer().ComputerEntity.Host
                + "': Maintaining logs failed - executing query failed after retries.");

        Callback_Maintainer_StopManagementForComputerLogger(computerLogger);
    }

    private List<ComputerLogger> GetReachableComputerLoggers(List<Computer> selectedComputers) throws LogsException
    {
        List<ComputerLogger> connectedComputerLoggers = new ArrayList<>();
        for (Computer selectedComputer : selectedComputers)
        {
            ComputerLogger computerLogger = new ComputerLogger(this, selectedComputer);
            computerLogger.ConnectWithComputerThroughSSH();

            connectedComputerLoggers.add(computerLogger);
        }

        try
        {
            Thread.sleep((long)(Utilities.SSHTimeout + 500));  /* 500ms is safe time offset when delay
                                                                   related with making connections occurs */
        }
        catch (InterruptedException e)
        {
            CleanUp();
            throw new LogsException("[FATAL ERROR] Thread sleep was interrupted in LogsManager.");
        }

        connectedComputerLoggers = connectedComputerLoggers.stream()
                .filter(c -> c.IsConnectedUsingSSH() == true &&
                        c.GetComputer().ComputerEntity.Preferences.isEmpty() == false)
                .collect(Collectors.toList());

        return  connectedComputerLoggers;
    }

    public ComputerLogger GetComputerLoggerForComputer(Computer computer)
    {
        List<ComputerLogger> results = _connectedComputerLoggers.stream()
                .filter(c -> c.GetComputer() == computer).collect(Collectors.toList());

        return results.isEmpty()? null : results.get(0);
    }

    private void CleanUp()
    {
        _logsGatherer = null;
        _logsMaintainer = null;
        _connectedComputerLoggers = null;
    }
}

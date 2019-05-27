package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.ComputerManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Entities.ComputerEntity;
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
    private  boolean _isWorking = false;

    public LogsManager(ComputerManager computerManager)
    {
        _computerManager = computerManager;
    }

    public void StartWork() throws LogsException, NothingToDoException
    {
        if(_isWorking)
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

        Callback_InfoMessage("----------- Started work -----------");
        _logsGatherer.StartGatheringLogs();
        _logsMaintainer.StartMaintainingLogs();

        _isWorking = true;
    }

    public void StopWork() throws LogsException
    {
        if(_isWorking == false)
        {
            throw new LogsException("[FATAL ERROR] Unable to start LogsManager work because it's currently working.");
        }

        Callback_InfoMessage("----------- Stopped work -----------");

        _logsGatherer.StopGatheringLogs();
        _logsMaintainer.StopMaintainingLogs();

        CleanUp();
    }

    private void CleanUp()
    {
        _logsGatherer = null;
        _logsMaintainer = null;
        _connectedComputerLoggers = null;
        _isWorking = false;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    public void UpdateGatheredComputers(List<Computer> selectedComputers) throws LogsException
    {
        if(_isWorking == false)
        {
            throw new LogsException("[FATAL ERROR] LogsManager: " +
                    "Unable to update gathered computers because it's not currently working.");
        }

        Callback_InfoMessage("---- Updating list of gathered computers. ----");
//        List<Computer> newSelectedComputers = _computerManager.GetSelectedComputers();
        List<Computer> newSelectedComputers = selectedComputers;

        List<ComputerLogger> computerLoggersToBeStopped =
                GetComputerLoggersToBeStopped(GetConnectedComputers(), newSelectedComputers);
        List<ComputerLogger> reachableComputerLoggersToBeStarted =
                GetReachableComputerLoggers(GetComputersToBeStarted(GetConnectedComputers(), newSelectedComputers));

        StopGatheringLogsForBatchOfComputerLoggers(computerLoggersToBeStopped);
        StartGatheringLogsForBatchOfComputerLoggers(reachableComputerLoggersToBeStarted);

        _logsMaintainer.RestartMaintainingLogs();
    }

    private List<ComputerLogger> GetComputerLoggersToBeStopped(
            List<Computer> currentlyGatheredComputers, List<Computer> newSelectedComputers)
    {
        List<Computer> computersToBeStopped = new ArrayList<>(currentlyGatheredComputers);
        computersToBeStopped.removeAll(newSelectedComputers);
        List<ComputerLogger> computerLoggersToBeStopped = computersToBeStopped
                .stream().map(c -> GetComputerLoggerForComputer(c)).collect(Collectors.toList());

        return computerLoggersToBeStopped;
    }

    private void StopGatheringLogsForBatchOfComputerLoggers(List<ComputerLogger> computerLoggersToBeStopped)
            throws LogsException
    {
        // TODO: Handling exception
        _logsGatherer.StopGatheringLogsForBatchOfComputerLoggers(computerLoggersToBeStopped);
        _connectedComputerLoggers.removeAll(computerLoggersToBeStopped);
    }

    private void StartGatheringLogsForBatchOfComputerLoggers(List<ComputerLogger> computerLoggersToBeStarted)
            throws LogsException
    {
        // TODO: Handling exception
        _logsGatherer.StartGatheringLogsForBatchOfComputerLoggers(computerLoggersToBeStarted);
        _connectedComputerLoggers.addAll(computerLoggersToBeStarted);
    }

    private List<Computer> GetComputersToBeStarted(
            List<Computer> currentlyGatheredComputers, List<Computer> newSelectedComputers)
    {
        List<Computer> computersToBeStarted = new ArrayList<>(newSelectedComputers);
        computersToBeStarted.removeAll(currentlyGatheredComputers);

        return computersToBeStarted;
    }

    // TODO: To check if correct logic
    public void SetComputerLastMaintenance(Computer computerToUpdate, Timestamp lastMaintenance)
    {
        ComputerEntity newComputerEntity = new ComputerEntity(computerToUpdate.ComputerEntity);
        newComputerEntity.LastMaintenance = lastMaintenance;

        try
        {
            _computerManager.UpdateComputerInDb(computerToUpdate, newComputerEntity);
        }
        catch (DatabaseException e)
        {
            throw e;
        }
    }


    // -----------------------------------------------------------------------------------------------------------------
    // ---  GENERAL CALLBACKS  -----------------------------------------------------------------------------------------

    public void Callback_InfoMessage(String message)
    {
        System.out.println("[INFO] LogsManager: " + message);
    }

    public void Callback_FatalError(String message) throws LogsException
    {
        System.out.println("[FATAL ERROR] LogsManager: " + message);

        // Placeholder for callback to GUI
    }

    public void Callback_NothingToDo_StopWork()
    {
        System.out.println("[INFO] LogsManager: Nothing to do.");

        // Placeholder for callback to GUI
    }

    // ---  GATHERER - AT RUNTIME  -------------------------------------------------------------------------------------

    public void Callback_Gatherer_FatalError_StopWorkForComputerLogger(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
            Callback_InfoMessage("Stopped maintaining logs for '" + host + "'." );

            if(HasConnectedComputersLoggers() == false)
            {
                Callback_NothingToDo_StopWork();
            }
        }
        else
        {
            Callback_FatalError("Unable to stop gathering logs for '" + host + "'.");
        }
    }

    public void Callback_Gatherer_StopWorkForComputerLogger(ComputerLogger computerLogger) throws LogsException
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
            Callback_InfoMessage("Stopped maintaining logs for '" + host + "'." );

            _logsMaintainer.RestartMaintainingLogs();

            if(HasConnectedComputersLoggers() == false)
            {
                Callback_NothingToDo_StopWork();
            }
        }
        else
        {
            Callback_FatalError("Unable to stop gathering logs for '" + host + "'.");
        }
    }

    // ---  GATHERER - WHILE STARTING GATHERING LOGS  ------------------------------------------------------------------

    public void Callback_Gatherer_StartGatheringLogsFailed()
    {
        _logsMaintainer.StopMaintainingLogs();

        CleanUp();
        Callback_FatalError("Failed with starting gathering logs.");
    }

    public void Callback_Gatherer_StartGatheringLogsForBatchOfComputerLoggersFailed()
    {
        _logsMaintainer.StopMaintainingLogs();
        _logsGatherer.StopGatheringLogs();

        CleanUp();
        Callback_FatalError("Failed with starting gathering logs.");
    }

    // ---  MAINTAINER - AT RUNTIME  -----------------------------------------------------------------------------------

    public void Callback_Maintainer_StopWorkForComputerLogger(ComputerLogger computerLogger) throws LogsException
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;

        _logsGatherer.StopGatheringLogs();

        _connectedComputerLoggers.remove(computerLogger);

        Callback_InfoMessage("Stopped gathering logs for '" + host + "'.");
        Callback_InfoMessage("Stopped maintaining logs for '" + host + "'.");

        _logsMaintainer.RestartMaintainingLogs();
    }

    public void Callback_Maintainer_InterruptionIntended_StopWorkForAllComputerLoggers()
    {
        CleanUp();

        Callback_InfoMessage("Stopped work.");
    }

    public void Callback_Maintainer_InterruptionNotIntended_StopWorkForAllComputerLoggers()
    {
        _logsGatherer.StopGatheringLogs();
        CleanUp();

        Callback_FatalError("LogsMaintainer sleep interrupted.");
    }

    // ----------------------------------------------------------------------------------------------------------------

    private List<ComputerLogger> GetReachableComputerLoggers(List<Computer> selectedComputers) throws LogsException
    {
        List<ComputerLogger> connectedComputerLoggers = new ArrayList<>();
        for (Computer selectedComputer : selectedComputers)
        {
            ComputerLogger computerLogger = new ComputerLogger(_logsGatherer, selectedComputer);
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

    public ComputerLogger GetComputerLoggerForComputer(Computer computer)
    {
        List<ComputerLogger> results = _connectedComputerLoggers.stream()
                .filter(c -> c.GetComputer() == computer).collect(Collectors.toList());

        return results.isEmpty()? null : results.get(0);
    }

    public final boolean HasConnectedComputersLoggers()
    {
        return !_connectedComputerLoggers.isEmpty();
    }
}

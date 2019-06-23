package Healthcheck.LogsManagement;

import GUI.Controllers.TestController;
import Healthcheck.AppLogger;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Entities.Computer;
import Healthcheck.LogType;
import Healthcheck.Utilities;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogsManager
{
    public final static String ModuleName = "LogsManager";

    private TestController _parentController;
    private LogsGatherer _logsGatherer;
    private LogsMaintainer _logsMaintainer;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private List<ComputerLogger> _connectedComputerLoggers;
    private boolean _isWorking = false;

    public LogsManager(ComputersAndSshConfigsManager computersAndSshConfigsManager, TestController parentController)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _parentController = parentController;
    }

    public void StartWork() throws LogsException, NothingToDoException
    {
        if(_isWorking)
        {
            AppLogger.Log(LogType.ERROR, ModuleName, "Unable to star work. LogsManager is currently working.");
            Utilities.ShowErrorDialog("LogsManager is currently working.");

            return;
        }
        else
        {
            _isWorking = true;
        }

        _logsGatherer = new LogsGatherer(this);
        _logsMaintainer = new LogsMaintainer(this);

        AppLogger.Log(LogType.INFO, ModuleName, "Started work.");

        _connectedComputerLoggers = GetReachableComputerLoggers(_computersAndSshConfigsManager.GetSelectedComputers());
        if(_connectedComputerLoggers.isEmpty())
        {
            _isWorking = false;
            _parentController.ClearInitListViewOfGatheredComputers();

            AppLogger.Log(LogType.ERROR, ModuleName, "No computer is ready for maintenance & logs gathering.");
            AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");
            Utilities.ShowErrorDialog("No computer is ready for maintenance & logs gathering.");

            return;
        }

        try
        {
            _logsGatherer.StartGatheringLogs();
            _logsMaintainer.StartMaintainingLogs();
        }
        catch(LogsException e)
        {
            AppLogger.Log(LogType.ERROR, ModuleName, e.getMessage());
            Utilities.ShowErrorDialog(e.getMessage());
        }
    }

    public void StopWork() throws LogsException
    {
        if(_isWorking == false)
        {
            AppLogger.Log(LogType.ERROR, ModuleName, "Unable to stop work. No LogsManager is currently working.");
            Utilities.ShowErrorDialog("No computer is working.");

            return;
        }

        StopGatheringLogsSafely();
        StopMaintainingLogsSafely();

        CleanUp();

        AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");
    }

    private void StopGatheringLogsSafely()
    {
        try
        {
            _logsGatherer.StopGatheringLogsWithoutNotifyingMaintainer();
        }
        catch(LogsException e)
        {
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName, e.getMessage());
        }
    }

    private void StopMaintainingLogsSafely()
    {
        try
        {
            _logsMaintainer.StopMaintainingLogs();
        }
        catch(LogsException e)
        {
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName, e.getMessage());
        }
    }

    private void StopGatheringAndMaintainingSafelyWithoutLoggingIt()
    {
        if(_logsMaintainer.IsMaintaining())
        {
            _logsMaintainer.StopMaintainingLogs();
        }

        if(_logsGatherer.IsGathering())
        {
            _logsGatherer.StopGatheringLogsWithNotifyingMaintainer();
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---  GENERAL CALLBACKS  -----------------------------------------------------------------------------------------

    public void Callback_FatalError(String message) throws LogsException
    {
        AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message);

        CleanUp();

        _parentController.Callback_LogsManager_StoppedWork_FatalError();
    }

    public void Callback_NothingToDo_StopWork()
    {
        AppLogger.Log(LogType.INFO, ModuleName, "Nothing to do.");

        StopGatheringLogsSafely();
        StopMaintainingLogsSafely();

        _parentController.Callback_LogsManager_StoppedWork_NothingToDo();
    }

    // ---  GATHERER - AT RUNTIME  -------------------------------------------------------------------------------------

    public void Callback_Gatherer_StopWorkForComputerLogger_WithNotifyingMaintainer(ComputerLogger computerLogger) throws LogsException
    {
        String host = computerLogger.GetComputer().GetHost();
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
            AppLogger.Log(LogType.INFO, ModuleName,"Stopped maintaining logs for '" + host + "'.");

            if(HasConnectedComputersLoggers() == false)
            {
                Callback_NothingToDo_StopWork();
                return;
            }

            try
            {
                _logsMaintainer.RestartMaintainingLogs();
            }
            catch (LogsException e)
            {
                StopGatheringLogsSafely();

                Callback_FatalError(e.getMessage());
                return;
            }

            _parentController.Callback_LogsManager_WorkForComputerStopped(computerLogger.GetComputer());
        }
        else
        {
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Unable to stop gathering logs for '" + host + "'.");
        }
    }

    // ---  GATHERER - WHILE STARTING GATHERING LOGS  ------------------------------------------------------------------

    public void Callback_Gatherer_StartGatheringLogsFailed()
    {
        StopMaintainingLogsSafely();

        Callback_FatalError("Failed with starting gathering logs.");
    }

    public void Callback_Gatherer_StartGatheringLogsForBatchOfComputerLoggersFailed()
    {
        StopMaintainingLogsSafely();
        StopGatheringLogsSafely();

        Callback_FatalError("Failed with starting gathering logs.");
    }

    // ---  MAINTAINER - AT RUNTIME  -----------------------------------------------------------------------------------

    public void Callback_Maintainer_StopWorkForComputerLogger(ComputerLogger computerLogger) throws LogsException
    {
        try
        {
            _logsGatherer.StopGatheringLogsForComputerLogger(computerLogger);
        }
        catch (LogsException e)
        {
            Callback_FatalError(e.getMessage());
            return;
        }

        _connectedComputerLoggers.remove(computerLogger);

        String host = computerLogger.GetComputer().GetHost();
        AppLogger.Log(LogType.INFO, ModuleName, "Stopped gathering logs for '" + host + "'.");
        AppLogger.Log(LogType.INFO, ModuleName, "Stopped maintaining logs for '" + host + "'.");

        try
        {
            _logsMaintainer.RestartMaintainingLogs();
        }
        catch (LogsException e)
        {
            Callback_FatalError(e.getMessage());
        }
    }

    public void Callback_Maintainer_InterruptionIntended_StopWorkForAllComputerLoggers()
    {
        CleanUp();
    }

    public void Callback_Maintainer_InterruptionNotIntended_StopWorkForAllComputerLoggers()
    {
        StopGatheringLogsSafely();

        Callback_FatalError("LogsMaintainer sleep interrupted.");
    }

    // -----------------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------


    public void SetComputerLastMaintenance(Computer computerToUpdate, Timestamp lastMaintenance)
            throws DatabaseException
    {
        computerToUpdate.SetLastMaintenance(lastMaintenance);
        computerToUpdate.UpdateInDb();
    }

    private void CleanUp()
    {
        _logsGatherer = null;
        _logsMaintainer = null;
        _connectedComputerLoggers = null;
        _isWorking = false;
    }

    private List<ComputerLogger> GetReachableComputerLoggers(List<Computer> selectedComputerEntities) throws LogsException
    {
        List<ComputerLogger> reachableComputerLoggers = new ArrayList<>();
        for (Computer selectedComputer : selectedComputerEntities)
        {
            ComputerLogger computerLogger = new ComputerLogger(_logsGatherer, selectedComputer);
            computerLogger.ConnectWithComputerThroughSSH();

            reachableComputerLoggers.add(computerLogger);
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

        reachableComputerLoggers = reachableComputerLoggers.stream()
                .filter(c -> c.IsConnectedUsingSSH() == true &&
                        c.GetComputer().GetPreferences().isEmpty() == false)
                .collect(Collectors.toList());

        return reachableComputerLoggers;
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

    public boolean IsWorking()
    {
        return _isWorking;
    }
}

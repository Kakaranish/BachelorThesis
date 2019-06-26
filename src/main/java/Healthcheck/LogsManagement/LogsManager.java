package Healthcheck.LogsManagement;

import GUI.Controllers.MainWindowController;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Entities.Computer;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Utilities;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogsManager
{
    public final static String ModuleName = "LogsManager";

    private MainWindowController _parentController;
    private LogsGatherer _logsGatherer;
    private LogsMaintainer _logsMaintainer;
    private ComputersAndSshConfigsManager _computersAndSshConfigsManager;
    private List<ComputerLogger> _connectedComputerLoggers;
    private boolean _isWorking = false;

    public LogsManager(ComputersAndSshConfigsManager computersAndSshConfigsManager, MainWindowController parentController)
    {
        _computersAndSshConfigsManager = computersAndSshConfigsManager;
        _parentController = parentController;
    }

    // ---  START AND STOP WORK  ---------------------------------------------------------------------------------------

    public void StartWork() throws LogsException, NothingToDoException
    {
        if(_isWorking)
        {
            AppLogger.Log(LogType.ERROR, ModuleName, "Unable to start work. LogsManager is currently working.");
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

        _parentController.Callback_LogsManager_StartedWork(GetConnectedComputers());
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

        EndWorkCleanup();

        AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");

        _parentController.Callback_LogsManager_StoppedWork();
    }

    private void StopGatheringLogsSafely()
    {
        try
        {
            _logsGatherer.StopGatheringLogs();
        }
        catch(LogsException e)
        {
            e.printStackTrace(System.out);
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
            e.printStackTrace(System.out);
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName, e.getMessage());
        }
    }

    // ---  GENERAL CALLBACKS  -----------------------------------------------------------------------------------------

    public void Callback_NothingToDo_StopWork()
    {
        AppLogger.Log(LogType.INFO, ModuleName, "Stopped work. Nothing to do - no connected computer.");

        _parentController.Callback_LogsManager_StoppedWork();

        StopMaintainingLogsSafely();
        StopGatheringLogsSafely();

        _parentController.Callback_LogsManager_StoppedWork_NothingToDo();
    }

    // ---  GATHERER CALLBACKS  -----------------------------------------------------------------------------------------

    public void Callback_Gatherer_StartGatheringLogsFailed()
    {
        StopMaintainingLogsSafely();
        EndWorkCleanup();

        AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Failed with starting gathering logs.");

        _parentController.Callback_LogsManager_StartGatheringLogsFailed();
    }

    public void Callback_Gatherer_StoppedComputerLogger_NotIntendedInterruption(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
            AppLogger.Log(LogType.INFO, ModuleName,"Stopped maintaining logs for '" + usernameAndHost + "'.");

            if(HasConnectedComputersLoggers() == false)
            {
                Callback_NothingToDo_StopWork();
                return;
            }

            _logsMaintainer.RestartMaintainingLogs();

            _parentController.Callback_LogsManager_ComputerDisconnected(computerLogger.GetComputer());
        }
        else
        {
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                    "Unable to stop gathering logs for '" + usernameAndHost + "'.");
        }
    }

    public void Callback_Gatherer_StoppedComputerLogger_SshConnectionFailed(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);
            AppLogger.Log(LogType.INFO, ModuleName,"Stopped maintaining logs for '" + usernameAndHost + "'.");

            if(HasConnectedComputersLoggers() == false)
            {
                Callback_NothingToDo_StopWork();
                return;
            }

            _logsMaintainer.RestartMaintainingLogs();

            _parentController.Callback_LogsManager_ComputerDisconnected(computerLogger.GetComputer());
        }
        else
        {
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                    "Unable to stop gathering logs for '" + usernameAndHost + "'.");
        }
    }

    public void Callback_Gatherer_StoppedComputerLogger_InternetConnectionLost()
    {
        StopGatheringLogsSafely();
        StopMaintainingLogsSafely();
        EndWorkCleanup();

        AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Stopped work. Connection with internet lost.");

        _parentController.Callback_LogsManager_InternetConnectionLost();
    }

    // ---  MAINTAINER CALLBACKS  --------------------------------------------------------------------------------------

    public void Callback_Maintainer_StopWorkForComputerLogger(ComputerLogger computerLogger)
    {
        try
        {
            _logsGatherer.StopGatheringLogsForComputerLogger(computerLogger);
        }
        catch (LogsException e)
        {
            AppLogger.Log(LogType.FATAL_ERROR, e.getMessage());
            return;
        }

        _connectedComputerLoggers.remove(computerLogger);

        String host = computerLogger.GetComputer().GetHost();
        AppLogger.Log(LogType.INFO, ModuleName, "Stopped maintaining logs for '" + host + "'.");

        _logsMaintainer.RestartMaintainingLogs();
    }

    public void Callback_Maintainer_StopWork_InterruptionIntended()
    {
        AppLogger.Log(LogType.INFO, ModuleName, "LogsMaintainer intentionally interrupted.");

        EndWorkCleanup();
    }

    public void Callback_Maintainer_InterruptionNotIntended_StopWorkForAllComputerLoggers()
    {
        AppLogger.Log(LogType.INFO, ModuleName, "LogsMaintainer unintentionally interrupted.");
        StopGatheringLogsSafely();

        StopGatheringLogsSafely();
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

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
            EndWorkCleanup();
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

    // ---  PREDICATES  ------------------------------------------------------------------------------------------------

    public final boolean HasConnectedComputersLoggers()
    {
        return !_connectedComputerLoggers.isEmpty();
    }

    public boolean IsWorking()
    {
        return _isWorking;
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public void SetComputerLastMaintenance(Computer computerToUpdate, Timestamp lastMaintenance)
            throws DatabaseException
    {
        computerToUpdate.SetLastMaintenance(lastMaintenance);
        computerToUpdate.UpdateInDb();
    }

    private void EndWorkCleanup()
    {
        _logsGatherer = null;
        _logsMaintainer = null;
        _connectedComputerLoggers = null;
        _isWorking = false;
    }
}

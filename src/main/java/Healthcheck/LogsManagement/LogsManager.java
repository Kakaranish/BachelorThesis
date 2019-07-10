package Healthcheck.LogsManagement;

import GUI.Controllers.MainWindowController;
import Healthcheck.AppLogging.AppLogger;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.DatabaseManagement.DatabaseException;
import Healthcheck.Entities.Computer;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Utilities;
import javafx.application.Platform;
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
    private List<ComputerLogger> _notConnectedComputerLoggers;

    private Thread _establishingConnectionThread;
    private boolean _isWorking = false;
    private boolean _startingComputerLoggerRequiresRestartLogsMaintainer = true;

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
            AppLogger.Log(LogType.WARNING, ModuleName, "Unable to start work. LogsManager is currently working.");
            Utilities.ShowErrorDialog("LogsManager is currently working.");

            return;
        }
        else
        {
            _isWorking = true;
        }

        _connectedComputerLoggers = new ArrayList<>();
        _notConnectedComputerLoggers = new ArrayList<>();
        _logsGatherer = new LogsGatherer(this);
        _logsMaintainer = new LogsMaintainer(this);

        _parentController.Callback_LogsManager_StartedWork();

        _notConnectedComputerLoggers = _computersAndSshConfigsManager.GetSelectedComputers()
                .stream().map(computer -> new ComputerLogger(_logsGatherer, computer))
                .filter(ComputerLogger::ValidateHasPreferences).collect(Collectors.toList());
        if(_notConnectedComputerLoggers.isEmpty())
        {
            _isWorking = false;

            _parentController.Callback_LogsManager_StoppedWork();

            AppLogger.Log(LogType.WARNING, ModuleName, "No computer is ready for maintenance & logs gathering.");
            AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");
            Utilities.ShowErrorDialog("No computer is ready for maintenance & logs gathering.");

            return;
        }

        _establishingConnectionThread = new Thread(this::EstablishConnectionWithComputerLoggers);
        _establishingConnectionThread.start();
    }

    public void StopWork() throws LogsException
    {
        if(_isWorking == false)
        {
            AppLogger.Log(LogType.WARNING, ModuleName, "Unable to stop work. No LogsManager is currently working.");
            Utilities.ShowErrorDialog("No computer is working.");

            return;
        }

        if(_establishingConnectionThread != null)
        {
             _establishingConnectionThread.interrupt();
        }
        else
        {
            StopMaintainingLogsSafely();
        }

        StopGatheringLogsSafely();
        EndWorkCleanup();

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Stopped work."));
        _parentController.Callback_LogsManager_StoppedWork();
    }

    public void RestartMaintainingLogs()
    {
        if(_isWorking == false)
        {
            AppLogger.Log(LogType.WARNING, ModuleName, "Unable to restart LogsMaintainer. " +
                    "LogsManager is currently not working.");
            return;
        }

        try
        {
            _logsMaintainer.RestartMaintainingLogs();
        }
        catch (Exception e)
        {
            AppLogger.Log(LogType.WARNING, ModuleName, "Unable to restart LogsMaintainer - " + e.getMessage());
        }
    }

    private void EstablishConnectionWithComputerLoggers()
    {
        _startingComputerLoggerRequiresRestartLogsMaintainer = false;

        _logsGatherer.StartWork();

        for (ComputerLogger computerLogger: _notConnectedComputerLoggers)
        {
            computerLogger.StartEstablishingSSHConnection();
        }

        try // LogsMaintainer is started with delay
        {
            Thread.sleep((long)(Utilities.SSHTimeout
                    + _notConnectedComputerLoggers.size() * Utilities.GatheringStartDelay
                    + 500));  /* 500ms is safe time offset when delay
                                 related with making connections occurs */
        }
        catch (InterruptedException e)
        {
            AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");
            return;
        }

        // Every new connection with computer will require restart of LogsMaintainer
        _startingComputerLoggerRequiresRestartLogsMaintainer = true;

        try
        {
            _logsMaintainer.StartMaintainingLogs();
        }
        catch(LogsException e)
        {
            AppLogger.Log(LogType.WARNING, ModuleName, e.getMessage());
        }

        if(_establishingConnectionThread == null)
        {
            _logsMaintainer.StopMaintainingLogs();
        }

        // Information for LogsManager that first wave of establishing connections with computers is finished
        _establishingConnectionThread = null;
        _parentController.Callback_LogsManager_ComputersConnected(GetConnectedComputers());
    }

    private void StopGatheringLogsSafely()
    {
        try
        {
            _logsGatherer.StopWork();
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

    // ---  GATHERER CALLBACKS  -----------------------------------------------------------------------------------------

    public void Callback_Gatherer_StartGatheringLogsFailed()
    {
        StopMaintainingLogsSafely();
        EndWorkCleanup();

        Platform.runLater(() ->
        {
            AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Failed with starting gathering logs.");
            _parentController.Callback_LogsManager_StartGatheringLogsFailed();
        });
    }

    public void Callback_Gatherer_LogsGatheredSuccessfully()
    {
        _parentController.Callback_LogsManager_LogsGatheredSuccessfully();
    }

    public void Callback_Gatherer_ConnectedWithComputerLogger(ComputerLogger computerLogger)
    {
        _connectedComputerLoggers.add(computerLogger);

        _parentController.Callback_LogsManager_ConnectedWithComputerLogger(computerLogger);
    }

    public void Callback_Gatherer_ReconnectedWithComputerLogger(ComputerLogger computerLogger)
    {
        _connectedComputerLoggers.add(computerLogger);
        _logsMaintainer.RestartMaintainingLogs();
        _parentController.Callback_LogsManager_ReconnectedWithComputerLogger(computerLogger);
    }

    public void Callback_Gatherer_StoppedComputerLogger_NotIntendedInterruption(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        if(_connectedComputerLoggers.contains(computerLogger))
        {
            _connectedComputerLoggers.remove(computerLogger);

            Platform.runLater(() -> AppLogger.Log(
                    LogType.INFO, ModuleName,"Stopped maintaining logs for '" + usernameAndHost + "'.")
            );

            _logsMaintainer.RestartMaintainingLogs();

            Platform.runLater(() ->
                    _parentController.Callback_LogsManager_ComputerDisconnected(computerLogger.GetComputer())
            );
        }
        else
        {
            Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                    "Unable to stop gathering logs for '" + usernameAndHost + "'.")
            );
        }
    }

    public void Callback_Gatherer_StoppedComputerLogger_InternetConnectionLost()
    {
        StopGatheringLogsSafely();
        StopMaintainingLogsSafely();
        EndWorkCleanup();

        Platform.runLater(() ->
        {
            try
            {
                AppLogger.Log(LogType.FATAL_ERROR, ModuleName, "Stopped work. Connection with internet lost.");
                _parentController.Callback_LogsManager_InternetConnectionLost();
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        });
    }

    // ---  MAINTAINER CALLBACKS  --------------------------------------------------------------------------------------

    public void Callback_Maintainer_StopWorkForComputerLogger(ComputerLogger computerLogger)
    {
        try
        {
            _logsGatherer.StopWorkForComputerLogger(computerLogger);
        }
        catch (LogsException e)
        {
            Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName, e.getMessage()));
            return;
        }

        _connectedComputerLoggers.remove(computerLogger);

        String host = computerLogger.GetComputer().GetHost();
        Platform.runLater(() ->
            AppLogger.Log(LogType.INFO, ModuleName, "Stopped maintaining logs for '" + host + "'.")
        );

        _logsMaintainer.RestartMaintainingLogs();
    }

    public void Callback_Maintainer_InterruptionNotIntended_StopWorkForAllComputerLoggers()
    {
        Platform.runLater(() ->
                AppLogger.Log(LogType.INFO, ModuleName, "LogsMaintainer unintentionally interrupted.")
        );

        StopGatheringLogsSafely();
    }

    // ---  GETTERS  ---------------------------------------------------------------------------------------------------

//    private List<ComputerLogger> GetReachableComputerLoggers(List<Computer> selectedComputerEntities) throws LogsException
//    {
//        List<ComputerLogger> reachableComputerLoggers = new ArrayList<>();
//        for (Computer selectedComputer : selectedComputerEntities)
//        {
//            ComputerLogger computerLogger = new ComputerLogger(_logsGatherer, selectedComputer);
//            computerLogger.ConnectWithComputerThroughSSH();
//
//            reachableComputerLoggers.add(computerLogger);
//        }
//
//        try
//        {
//            Thread.sleep((long)(Utilities.SSHTimeout + 500));  /* 500ms is safe time offset when delay
//                                                                   related with making connections occurs */
//        }
//        catch (InterruptedException e)
//        {
//            EndWorkCleanup();
//            throw new LogsException("[FATAL WARNING] Thread sleep was interrupted in LogsManager.");
//        }
//
//        reachableComputerLoggers = reachableComputerLoggers.stream()
//                .filter(c -> c.IsConnectedUsingSSH() == true &&
//                        c.GetComputer().GetPreferences().isEmpty() == false)
//                .collect(Collectors.toList());
//
//        return reachableComputerLoggers;
//    }

    public List<ComputerLogger> GetConnectedComputerLoggers()
    {
        return _connectedComputerLoggers;
    }

    public List<ComputerLogger> GetNotConnectedComputerLoggers()
    {
        return _notConnectedComputerLoggers;
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
        _notConnectedComputerLoggers = null;
        _isWorking = false;
        _startingComputerLoggerRequiresRestartLogsMaintainer = true;
    }
}

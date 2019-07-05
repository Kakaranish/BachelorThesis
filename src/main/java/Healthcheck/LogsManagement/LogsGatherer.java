package Healthcheck.LogsManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Utilities;
import javafx.application.Platform;
import java.util.ArrayList;
import java.util.List;

public class LogsGatherer
{
    public final static String ModuleName = "LogsGatherer";

    private LogsManager _logsManager;
    private boolean _isGathering = false;
    private boolean _interruptionIntended = false;
    private Thread _runnerThread;

    public LogsGatherer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    private void RunnerThreadAction()
    {
        _isGathering = true;

        List<ComputerLogger> gatheredComputerLoggers = new ArrayList<>();
        for (ComputerLogger computerLogger : _logsManager.GetConnectedComputerLoggers())
        {
            try
            {
                Thread.sleep(Utilities.GatheringStartDelay);
            }
            catch (InterruptedException e)
            {
                Callback_StartGatheringLogsFailed(gatheredComputerLoggers);

                Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                        "Unable to start gathering logs. Sleep interrupted.")
                );

                _runnerThread = null;
                _isGathering = false;

                return;
            }

            boolean startSucceed = computerLogger.StartGatheringLogs();
            if(startSucceed == false)
            {
                Platform.runLater(() ->
                    AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                            "Unable to start gathering logs due to some ComputerLogger.")
                );

                Callback_StartGatheringLogsFailed(gatheredComputerLoggers);

                _runnerThread = null;
                _isGathering = false;

                return;
            }

            gatheredComputerLoggers.add(computerLogger);
        }

        _runnerThread = null;
    }

    public void StartGatheringLogs() throws LogsException
    {
        if(_isGathering)
        {
            throw new LogsException("Unable to start gathering logs. Other gatherer currently is working.");
        }

        _runnerThread = new Thread(this::RunnerThreadAction);
        _runnerThread.start();

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Started work"));
    }

    public void StopGatheringLogs() throws LogsException
    {
        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs. No gatherer currently is working.");
        }

        _interruptionIntended = true;

        if(_runnerThread != null)
        {
            _runnerThread.interrupt();
            _runnerThread = null;
            return;
        }

        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogs();
        }

        _isGathering = false;

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName,"Stopped work."));
    }

    public void StopGatheringLogsForComputerLogger(ComputerLogger computerLogger) throws LogsException
    {
        String host = computerLogger.GetComputer().GetHost();

        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs '" + host + "'. LogsGatherer is not working.");
        }

        boolean stopSucceed = computerLogger.StopGatheringLogs();
        if(stopSucceed == false)
        {
            throw new LogsException("Unable to stop gathering logs '" + host + "'.");
        }
    }

    // ---  GENERAL CALLBACKS  -----------------------------------------------------------------------------------------

    public void Callback_InfoMessage(String message)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, message));
    }

    public void Callback_ErrorMessage(String message)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.WARNING, ModuleName, message));
    }

    public void Callback_FatalErrorWithoutAction(String message)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message));
    }

    // ---  CALLBACKS TO LOGSMANAGER  ----------------------------------------------------------------------------------

    public void Callback_StartGatheringLogsFailed(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopGatheringLogsForComputerLogger(startedComputerLogger);
        }

        _logsManager.Callback_Gatherer_StartGatheringLogsFailed();

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Stopped work."));
    }

    public void Callback_StoppedComputerLogger_InterruptionIntended(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName,
                "Gathering logs stopped for '" + usernameAndHost + "'.")
        );
    }

    public void Callback_StoppedComputerLogger_InterruptionNotIntended(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                "'" + usernameAndHost + "' ComputerLogger thread was unintentionally interrupted.")
        );

        _logsManager.Callback_Gatherer_StoppedComputerLogger_NotIntendedInterruption(computerLogger);
    }

    public void Callback_StoppedComputer_SshConnectionFailed(ComputerLogger computerLogger, String message)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message));
        _logsManager.Callback_Gatherer_StoppedComputerLogger_SshConnectionFailed(computerLogger);
    }

    public void Callback_StoppedComputerLogger_InternetConnectionLost(String message)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message));
        _logsManager.Callback_Gatherer_StoppedComputerLogger_InternetConnectionLost();
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public final boolean InterruptionIntended()
    {
        return _interruptionIntended;
    }
}

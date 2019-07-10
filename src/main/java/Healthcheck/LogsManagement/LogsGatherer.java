package Healthcheck.LogsManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import javafx.application.Platform;
import java.util.List;

public class LogsGatherer
{
    public final static String ModuleName = "LogsGatherer";

    private LogsManager _logsManager;
    private boolean _isGathering = false;
    private boolean _interruptionIntended = false;

    public LogsGatherer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    public void StartWork() throws LogsException
    {
        if(_isGathering)
        {
            throw new LogsException("Unable to start gathering logs. Other gatherer currently is working.");
        }

        _isGathering = true;

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Started work"));
    }

    public void StopWork() throws LogsException
    {
        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs. No gatherer currently is working.");
        }

        _interruptionIntended = true;

        for (ComputerLogger computerLogger : _logsManager.GetConnectedComputerLoggers())
        {
            computerLogger.StopWork();
        }

        for (ComputerLogger computerLogger : _logsManager.GetNotConnectedComputerLoggers())
        {
            computerLogger.StopWork();
        }

        _isGathering = false;
        _interruptionIntended = false;

        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName,"Stopped work."));
    }

    public void StopWorkForComputerLogger(ComputerLogger computerLogger) throws LogsException
    {
        String host = computerLogger.GetComputer().GetHost();

        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs '" + host + "'. LogsGatherer is not working.");
        }

        boolean stopSucceed = computerLogger.StopWork();
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

    public void Callback_LogsGatheredSuccessfully(ComputerLogger computerLogger)
    {
        Callback_InfoMessage("Logs for '" + computerLogger.GetComputer().GetUsernameAndHost() +
                "' gathered. Next gathering in " + computerLogger.GetComputer().GetRequestInterval().toSeconds() + "s.");

        _logsManager.Callback_Gatherer_LogsGatheredSuccessfully();
    }

    public void Callback_StartGatheringLogsFailed(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopWorkForComputerLogger(startedComputerLogger);
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

    public void Callback_ConnectedComputerLogger(ComputerLogger computerLogger)
    {
        _logsManager.Callback_Gatherer_ConnectedWithComputerLogger(computerLogger);
    }

    public void Callback_StoppedComputerLogger_InterruptionNotIntended(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                "'" + usernameAndHost + "' ComputerLogger thread was unintentionally interrupted.")
        );

        _logsManager.Callback_Gatherer_StoppedComputerLogger_NotIntendedInterruption(computerLogger);
    }

    public void Callback_StoppedComputerLogger_InternetConnectionLost(String message)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message));
        _logsManager.Callback_Gatherer_StoppedComputerLogger_InternetConnectionLost();
    }

    // ---  OTHER CALLBACKS  -------------------------------------------------------------------------------------------

    public void Callback_RenewedConnectionWithComputer(ComputerLogger computerLogger)
    {
        Platform.runLater(() -> AppLogger.Log(LogType.INFO, ModuleName, "Connection with '"
                + computerLogger.GetComputer().GetUsernameAndHost() + "' has been renewed.")
        );

        _logsManager.Callback_Gatherer_ReconnectedWithComputerLogger(computerLogger);
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public final boolean InterruptionIntended()
    {
        return _interruptionIntended;
    }

    public boolean IsWorking()
    {
        return _isGathering;
    }
}

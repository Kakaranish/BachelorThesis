package Healthcheck.LogsManagement;

import Healthcheck.AppLogging.AppLogger;
import Healthcheck.AppLogging.LogType;
import Healthcheck.Utilities;
import java.util.ArrayList;
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

    public void StartGatheringLogs() throws LogsException
    {
        if(_isGathering)
        {
            throw new LogsException("Unable to start gathering logs. Other gatherer currently is working.");
        }

        AppLogger.Log(LogType.INFO, ModuleName, "Started work.");

        new Thread(() -> {
            List<ComputerLogger> gatheredComputerLoggers = new ArrayList<>();
            for (ComputerLogger computerLogger : _logsManager.GetConnectedComputerLoggers())
            {
                try
                {
                    Thread.sleep(Utilities.GatheringStartDelay);
                }
                catch (InterruptedException e)
                {
                    AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                            "Unable to start gathering logs. Sleep interrupted");

                    Callback_StartGatheringLogsFailed(gatheredComputerLoggers);

                    return;
                }

                boolean startSucceed = computerLogger.StartGatheringLogs();
                if(startSucceed == false)
                {
                    AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                            "Unable to start gathering logs due to some ComputerLogger.");

                    Callback_StartGatheringLogsFailed(gatheredComputerLoggers);

                    return;
                }

                gatheredComputerLoggers.add(computerLogger);
            }

            _isGathering = true;
        }).start();
    }

    public void StopGatheringLogs() throws LogsException
    {
        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs. No gatherer currently is working.");
        }

        _interruptionIntended = true;
        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogs();
        }

        _isGathering = false;

        AppLogger.Log(LogType.INFO, ModuleName,"Stopped work.");
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
        AppLogger.Log(LogType.INFO, ModuleName, message);
    }

    public void Callback_ErrorMessage(String message)
    {
        AppLogger.Log(LogType.ERROR, ModuleName, message);
    }

    public void Callback_FatalErrorWithoutAction(String message)
    {
        AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message);
    }

    // ---  CALLBACKS TO LOGSMANAGER  ----------------------------------------------------------------------------------

    public void Callback_StartGatheringLogsFailed(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopGatheringLogsForComputerLogger(startedComputerLogger);
        }

        _isGathering = false;
        _logsManager.Callback_Gatherer_StartGatheringLogsFailed();

        AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");
    }

    public void Callback_StoppedComputerLogger_InterruptionIntended(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        AppLogger.Log(LogType.INFO, ModuleName, "Gathering logs stopped for '" + usernameAndHost + "'.");
    }

    public void Callback_StoppedComputerLogger_InterruptionNotIntended(ComputerLogger computerLogger)
    {
        String usernameAndHost = computerLogger.GetComputer().GetUsernameAndHost();
        AppLogger.Log(LogType.FATAL_ERROR, ModuleName,
                "'" + usernameAndHost + "' ComputerLogger thread was unintentionally interrupted.");

        _logsManager.Callback_Gatherer_StoppedComputerLogger_NotIntendedInterruption(computerLogger);
    }

    public void Callback_StoppedComputer_SshConnectionFailed(ComputerLogger computerLogger, String message)
    {
        AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message);
        _logsManager.Callback_Gatherer_StoppedComputerLogger_SshConnectionFailed(computerLogger);
    }

    public void Callback_StoppedComputerLogger_InternetConnectionLost()
    {
        _logsManager.Callback_Gatherer_StoppedComputerLogger_InternetConnectionLost();
    }

    // ---  MISC  ------------------------------------------------------------------------------------------------------

    public final boolean InterruptionIntended()
    {
        return _interruptionIntended;
    }
}

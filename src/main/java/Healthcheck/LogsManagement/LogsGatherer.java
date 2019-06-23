package Healthcheck.LogsManagement;

import Healthcheck.AppLogger;
import Healthcheck.LogType;
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

    public void StopGatheringLogsWithNotifyingMaintainer() throws LogsException
    {
        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs. No gatherer currently is working.");
        }

        _interruptionIntended = true;
        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogsWithNotifyingMaintainer();
        }

        _isGathering = false;

        AppLogger.Log(LogType.INFO, ModuleName,"Stopped work.");
    }

    public void StopGatheringLogsWithoutNotifyingMaintainer() throws LogsException
    {
        if(_isGathering == false)
        {
            throw new LogsException("Unable to stop gathering logs. No gatherer currently is working.");
        }

        _interruptionIntended = true;
        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogsWithoutNotifyingMaintainer();
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

        boolean stopSucceed = computerLogger.StopGatheringLogsWithNotifyingMaintainer();
        if(stopSucceed == false)
        {
            throw new LogsException("Unable to stop gathering logs '" + host + "'.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------- CALLBACKS --------------------------------------------------------

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

    public void Callback_FatalError_StopWorkForComputerLogger(ComputerLogger computerLogger, String message)
    {
        AppLogger.Log(LogType.FATAL_ERROR, ModuleName, message);

        _logsManager.Callback_Gatherer_StopWorkForComputerLogger_WithNotifyingMaintainer(computerLogger);
    }

    public void Callback_IntentionallyStoppedGathering_WithNotifyingMaintainer(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().GetHost();

        AppLogger.Log(LogType.INFO, ModuleName, "Gathering logs stopped for '" + host + "'.");

        _logsManager.Callback_Gatherer_StopWorkForComputerLogger_WithNotifyingMaintainer(computerLogger);
    }

    public void Callback_StartGatheringLogsFailed(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopGatheringLogsForComputerLogger(startedComputerLogger);
        }

        _isGathering = false;

        AppLogger.Log(LogType.INFO, ModuleName, "Stopped work.");

        _logsManager.Callback_Gatherer_StartGatheringLogsFailed();
    }

    public void Callback_StartGatheringLogsFailedForBatchOfComputerLoggers(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopGatheringLogsForComputerLogger(startedComputerLogger);
        }

        _logsManager.Callback_Gatherer_StartGatheringLogsForBatchOfComputerLoggersFailed();
    }

    public final boolean IsInterruptionIntended()
    {
        return _interruptionIntended;
    }

    public boolean IsGathering()
    {
        return _isGathering;
    }
}

package Healthcheck.LogsManagement;

import Healthcheck.Utilities;
import java.util.ArrayList;
import java.util.List;

public class LogsGatherer
{
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
            throw new LogsException(
                    "[FATAL ERROR] LogsGatherer: Unable to start gathering logs. Other gatherer currently is working.");
        }
        Callback_InfoMessage("Started work.");

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
                    Callback_FatalErrorWithoutAction(
                            "Unable to start gathering logs. Sleep interrupted.");
                    Callback_StartGatheringLogsFailed(gatheredComputerLoggers);

                    return;
                }

                boolean startSucceed = computerLogger.StartGatheringLogs();
                if(startSucceed == false)
                {
                    Callback_FatalErrorWithoutAction("Unable to start gathering logs due to some ComputerLogger.");
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
            throw new LogsException(
                    "[FATAL ERROR] LogsGatherer: Unable to stop gathering logs. No gatherer currently is working.");
        }

        _interruptionIntended = true;
        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogs();
        }

        _isGathering = false;
        Callback_InfoMessage("Stopped work.");
    }

    // TODO
    public void StartGatheringLogsForBatchOfComputerLoggers(List<ComputerLogger> computerLoggers) throws LogsException
    {
        if (_isGathering == false)
        {
            throw new LogsException("[FATAL ERROR] LogsGatherer: Unable to start gathering logs " +
                    "for batch of ComputerLoggers. LogsGatherer is not working now.");
        }

        Callback_InfoMessage("Starting gathering for batch of computer loggers.");

        new Thread(() -> {
            for (ComputerLogger computerLogger : computerLoggers)
            {
                try
                {
                    Thread.sleep(Utilities.GatheringStartDelay);
                }
                catch (InterruptedException e)
                {
                    Callback_FatalErrorWithoutAction(
                            "Unable to start gathering logs for batch of computer loggers. Sleep interrupted.");
                    Callback_StartGatheringLogsFailedForBatchOfComputerLoggers(computerLoggers);

                    return;
                }

                boolean gatheringSucceed = computerLogger.StartGatheringLogs();
                if(gatheringSucceed == false)
                {
                    Callback_FatalErrorWithoutAction("Unable to start gathering logs for batch of computer loggers " +
                            "due to some error while start of one ComputerLogger.");
                    Callback_StartGatheringLogsFailedForBatchOfComputerLoggers(computerLoggers);

                    return;
                }
                computerLogger.StartGatheringLogs();
            }
        }).start();
    }

    public void StopGatheringLogsForBatchOfComputerLoggers(List<ComputerLogger> computerLoggers) throws LogsException
    {
        Callback_InfoMessage("Stopping work for batch of computer loggers.");

        for (ComputerLogger computerLogger : computerLoggers)
        {
            StopGatheringLogsForComputerLogger(computerLogger);
        }
    }

    private void StopGatheringLogsForComputerLogger(ComputerLogger computerLogger) throws LogsException
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;

        if(_isGathering == false)
        {
            throw new LogsException(
                    "[FATAL ERROR] LogsGatherer: Unable to stop gathering logs '" + host + "'. LogsGatherer is not working.");
        }

        boolean stopSucceed = computerLogger.StopGatheringLogs();
        if(stopSucceed == false)
        {
            throw new LogsException(
                    "[FATAL ERROR] LogsGatherer: Unable to stop gathering logs '" + host + "'.");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------
    // ---------------------------------------------- CALLBACKS --------------------------------------------------------

    public void Callback_InfoMessage(String message)
    {
        System.out.println("[INFO] LogsGatherer: " + message);
    }

    public void Callback_ErrorMessage(String message)
    {
        System.out.println("[ERROR] LogsGatherer: " + message);
    }

    public void Callback_FatalErrorWithoutAction(String message)
    {
        System.out.println("[FATAL ERROR] LogsGatherer: " + message);
    }

    public void Callback_FatalError(ComputerLogger computerLogger, String message)
    {
        System.out.println("[FATAL ERROR] LogsGatherer: " + message);

        _logsManager.Callback_Gatherer_FatalError_StopWorkForComputerLogger(computerLogger);
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void Callback_StoppedGathering(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[INFO] LogsGatherer: Gathering logs stopped for '" + host + "'.");

        _logsManager.Callback_Gatherer_StopWorkForComputerLogger(computerLogger);
    }

    public void Callback_StartGatheringLogsFailed(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopGatheringLogsForComputerLogger(startedComputerLogger);
        }

        _isGathering = false;

        Callback_InfoMessage("Stopped work.");

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
}

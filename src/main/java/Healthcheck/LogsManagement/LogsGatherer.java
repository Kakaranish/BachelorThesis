package Healthcheck.LogsManagement;

import Healthcheck.Utilities;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class LogsGatherer
{
    private LogsManager _logsManager;
    private boolean _isGathering;

    public LogsGatherer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    public void StartGatheringLogs() throws LogsException
    {
        if(_isGathering == true)
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
                    Callback_StartGatheringLogsFailedForComputerLogger(gatheredComputerLoggers);

                    throw new LogsException("[FATAL ERROR] LogsGatherer: Unable to start gathering logs. " +
                            "Gathering start sleep interrupted");
                }

                boolean gatheringSucceed = computerLogger.StartGatheringLogs();
                if(gatheringSucceed == false)
                {
                    Callback_StartGatheringLogsFailedForComputerLogger(gatheredComputerLoggers);

                    throw new LogsException(
                            "[FATAL ERROR] LogsGatherer: Unable to start gathering logs due to some ComputerLogger.");
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

        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogs();
        }

        _isGathering = false;
        Callback_InfoMessage("Stopped work.");
    }

    // TODO
    public void StartGatheringLogsForBatchOfComputerLoggers(List<ComputerLogger> computerLoggers)
    {
        System.out.println("[INFO] LogsGatherer is starting work for batch of computer loggers.");

        new Thread(() -> {
            for (ComputerLogger computerLogger : computerLoggers)
            {
                try
                {
                    Thread.sleep(Utilities.GatheringStartDelay);
                }
                catch (InterruptedException e)
                {
                    // TODO: Implement custom action. LogsManager cannot be started.
                }

                computerLogger.StartGatheringLogs();
            }
        }).start();
    }

    // TODO
    public void StopGatheringLogsForBatchOfComputerLoggers(List<ComputerLogger> computerLoggers)
    {
        System.out.println("[INFO] LogsGatherer is stopping work for batch of computer loggers.");

        for (ComputerLogger computerLogger : computerLoggers)
        {
            computerLogger.StopGatheringLogs();
        }
    }

    public boolean StopGatheringLogsForComputerLogger(ComputerLogger computerLogger)
    {
        if(_logsManager.GetConnectedComputerLoggers().contains(computerLogger) == false)
        {
            String host = computerLogger.GetComputer().ComputerEntity.Host;
            Callback_ErrorMessage(
                    "Unable to stop gathering logs for '" + host + "' is not set as gathered.");

            return false;
        }

        List<ComputerLogger> results = _logsManager.GetConnectedComputerLoggers().stream()
                .filter(c -> c == computerLogger).collect(Collectors.toList());
        if(results.isEmpty() == false)
        {
            results.get(0).StopGatheringLogs();
            computerLogger.CloseSSHConnection();

            return true;
        }
        return false;
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

    public void Callback_FatalError(ComputerLogger computerLogger, String message)
    {
        System.out.println("[FATAL ERROR] LogsGatherer: " + message);

        _logsManager.Callback_Gatherer_StopWorkForComputerLogger(computerLogger);
    }

    public void Callback_FatalErrorBeforeEstablishingConnection(String message)
    {
        System.out.println("[FATAL ERROR] LogsGatherer: " + message);
    }

    public void Callback_StoppedGathering(ComputerLogger computerLogger)
    {
        String host = computerLogger.GetComputer().ComputerEntity.Host;
        System.out.println("[INFO] LogsGatherer: Gathering logs stopped for '" + host + "'.");

        _logsManager.Callback_Gatherer_StopWorkForComputerLogger(computerLogger);
    }

    public void Callback_StartGatheringLogsFailedForComputerLogger(List<ComputerLogger> gatheredComputerLoggers)
    {
        for (ComputerLogger startedComputerLogger : gatheredComputerLoggers)
        {
            StopGatheringLogsForComputerLogger(startedComputerLogger);
        }
        _isGathering = false;

        Callback_InfoMessage("Stopped work.");

        _logsManager.Callback_Gatherer_StartGatheringLogsFailed();
    }
}

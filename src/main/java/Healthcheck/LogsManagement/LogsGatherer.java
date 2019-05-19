package Healthcheck.LogsManagement;

import java.util.List;
import java.util.stream.Collectors;

public class LogsGatherer
{
    private LogsManager _logsManager;

    public LogsGatherer(LogsManager logsManager)
    {
        _logsManager = logsManager;
    }

    public void StartGatheringLogs()
    {
        System.out.println("[INFO] LogsGatherer started its work.");

        for (ComputerLogger computerLogger : _logsManager.GetConnectedComputerLoggers())
        {
            computerLogger.StartGatheringLogs();
        }
    }

    public void StopGatheringLogsForAllComputerLoggers()
    {
        System.out.println("[INFO] LogsGatherer stopped its work.");

        for (ComputerLogger gatheredComputer : _logsManager.GetConnectedComputerLoggers())
        {
            gatheredComputer.StopGatheringLogs();
        }
    }

    public void StopGatheringLogsForSingleComputerLogger(ComputerLogger computerLogger)
    {
        if(_logsManager.GetConnectedComputerLoggers().contains(computerLogger) == false)
        {
            return;
        }

        List<ComputerLogger> results = _logsManager.GetConnectedComputerLoggers().stream()
                .filter(c -> c == computerLogger).collect(Collectors.toList());
        if(results.isEmpty() == false)
        {
            results.get(0).StopGatheringLogs();
            System.out.println("[INFO] LogsGatherer stopped gathering logs for '"
                    + computerLogger.GetComputer().ComputerEntity.Host + "'.");
        }
    }
}

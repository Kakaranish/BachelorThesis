package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.Utilities;

import java.util.List;

public class LogsManager
{
    private LogsGatherer _logsGatherer;
    private LogsMaintainer _logsMaintainer;

    public LogsManager(LogsGatherer logsGatherer, LogsMaintainer logsMaintainer)
    {
        _logsGatherer = logsGatherer;
        _logsMaintainer = logsMaintainer;
    }

    public void StartWork(List<Computer> selectedComputers) throws LogsException, NothingToDoException
    {
        _logsGatherer.StartGatheringLogs(selectedComputers);

        try
        {
            Thread.sleep((long)((Utilities.NumOfRetries + 0.5) * Utilities.Cooldown));
        }
        catch (InterruptedException e)
        {
            throw new LogsException("[FATAL ERROR] Thread sleep was interrupted in LogsManager.");
        }

        List<Computer> gatheredComputers = _logsGatherer.GetGatheredComputers();
        if(gatheredComputers.isEmpty())
        {
            _logsGatherer.StopGatheringLogsForAllComputerLoggers();
            throw new NothingToDoException("[INFO] No computers to logs gathering & maintaining.");
        }

        _logsMaintainer.StartMaintainingLogs(gatheredComputers);
    }

    public void StopWork()
    {
        // TODO
    }
}

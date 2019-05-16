package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.LogsManagement.LogsGatherer;
import Healthcheck.LogsManagement.LogsMaintainer;
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

    public void StartWork(List<Computer> selectedComputers) throws LogsException
    {
        _logsGatherer.StartGatheringLogs(selectedComputers);

        try
        {
            Thread.sleep((long)((Utilities.NumOfRetries + 0.5) * Utilities.Cooldown));
        }
        catch (InterruptedException e)
        {
            throw new LogsException("Thread sleep was interrupted in LogsManager.");
        }

        _logsMaintainer.StartMaintainingLogs(_logsGatherer.GetGatheredComputers());

        List<Computer> gatheredComputers = _logsGatherer.GetGatheredComputers();
    }
}

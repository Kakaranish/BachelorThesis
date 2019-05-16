package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.LogsManagement.LogsGatherer;
import Healthcheck.LogsManagement.LogsMaintainer;

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

    public void StartWork(List<Computer> selectedComputers)
    {
        // Get computers with which connection can be established
    }
}

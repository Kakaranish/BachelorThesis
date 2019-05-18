package Healthcheck.LogsManagement;

import Healthcheck.Computer;
import Healthcheck.Utilities;

import java.util.List;

public class LogsManager
{
    private LogsGatherer _logsGatherer;
    private LogsMaintainer _logsMaintainer;

    public LogsManager()
    {
    }

    public void StartWork(LogsGatherer logsGatherer, LogsMaintainer logsMaintainer, List<Computer> selectedComputers)
            throws LogsException, NothingToDoException
    {
        if(_logsGatherer != null && _logsMaintainer != null)
        {
            throw new LogsException("[FATAL ERROR] Unable to start LogsManager work because it's currently working.");
        }
        else
        {
            _logsGatherer = logsGatherer;
            _logsMaintainer = logsMaintainer;
        }

        _logsGatherer.StartGatheringLogs(selectedComputers);

        try
        {
            Thread.sleep((long)(Utilities.SSH_Timeout + 500)); // 500 is safe time to end logs gathering for unreachable computers
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

    public void Callback_ConnectionWithComputerHasBeenBroken(Computer computer, String callbackMessage)
    {
        System.out.println(callbackMessage);

        try
        {
            _logsMaintainer.StopMaintainingLogsForSingleComputer(computer);
            System.out.println("[INFO] Logs maintainer stopped work stopped for '" + computer.ComputerEntity.Host + "'.");

            //TODO: Check if any computer is still being maintaning and logs gathering
        }
        catch (LogsException e)
        {
            e.printStackTrace();
        }
    }
}

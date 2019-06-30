package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Entities.Logs.ProcessLog;
import Healthcheck.Models.Info.ProcessInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "ProcessesCacheLogs")
public class ProcessCacheLog extends CacheLogBaseEntity
{
    @Embedded
    public ProcessInfo ProcessInfo;

    private ProcessCacheLog()
    {
    }

    public ProcessCacheLog(ProcessLog processLog)
    {
        super(processLog);

        ProcessInfo = processLog.ProcessInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new ProcessLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBaseEntity ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new ProcessLog(this, computer);
    }
}

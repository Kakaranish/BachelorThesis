package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.CpuLog;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Models.Info.CpuInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CpuCacheLogs")
public class CpuCacheLog extends CacheLogBaseEntity
{
    @Embedded
    public CpuInfo CpuInfo;

    private CpuCacheLog()
    {
    }

    public CpuCacheLog(CpuLog cpuLog)
    {
        super(cpuLog);

        CpuInfo = cpuLog.CpuInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new CpuLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBaseEntity ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new CpuLog(this, computer);
    }
}

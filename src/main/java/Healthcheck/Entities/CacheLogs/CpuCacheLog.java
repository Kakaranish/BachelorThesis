package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.CpuLog;
import Healthcheck.Entities.Logs.LogBase;
import Healthcheck.Models.Info.CpuInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "CpuCacheLogs")
public class CpuCacheLog extends CacheLogBase
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
    public LogBase ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new CpuLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBase ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new CpuLog(this, computer);
    }
}

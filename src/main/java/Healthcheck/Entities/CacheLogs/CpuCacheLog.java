package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
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

    public CpuCacheLog(CpuLog cpuLog)
    {
        super(cpuLog);

        CpuInfo = cpuLog.CpuInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager cpComputersAndSshConfigsManager)
    {
        return new CpuLog(cpComputersAndSshConfigsManager.GetComputerById(ComputerId), CpuInfo);
    }
}

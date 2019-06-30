package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Entities.Logs.RamLog;
import Healthcheck.Models.Info.RamInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "RamCacheLogs")
public class RamCacheLog extends CacheLogBaseEntity
{
    @Embedded
    public RamInfo RamInfo;

    private RamCacheLog()
    {
    }

    public RamCacheLog(RamLog ramLog)
    {
        super(ramLog);

        RamInfo = ramLog.RamInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new RamLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBaseEntity ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new RamLog(this, computer);
    }
}

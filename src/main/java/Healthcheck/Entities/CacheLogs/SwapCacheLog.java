package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Entities.Logs.SwapLog;
import Healthcheck.Models.Info.SwapInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "SwapCacheLogs")
public class SwapCacheLog extends CacheLogBaseEntity
{
    @Embedded
    public SwapInfo SwapInfo;

    private SwapCacheLog()
    {
    }

    public SwapCacheLog(SwapLog swapLog)
    {
        super(swapLog);

        SwapInfo = swapLog.SwapInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new SwapLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBaseEntity ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new SwapLog(this, computer);
    }
}

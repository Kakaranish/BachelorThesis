package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.DiskLog;
import Healthcheck.Entities.Logs.LogBaseEntity;
import Healthcheck.Models.Info.DiskInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "DisksCacheLogs")
public class DiskCacheLog extends CacheLogBaseEntity
{
    @Embedded
    public DiskInfo DiskInfo;

    private DiskCacheLog()
    {
    }

    public DiskCacheLog(DiskLog diskLog)
    {
        super(diskLog);

        DiskInfo = diskLog.DiskInfo;
    }

    @Override
    public LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        return new DiskLog(this, computersAndSshConfigsManager);
    }

    @Override
    public LogBaseEntity ToLog(Computer computer)
    {
        if(ComputerId != computer.GetId())
        {
            return null;
        }

        return new DiskLog(this, computer);
    }
}

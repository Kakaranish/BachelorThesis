package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBase;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public abstract class CacheLogBase
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Long LogId;

    public Timestamp Timestamp;

    public int ComputerId;

    protected CacheLogBase()
    {
    }

    protected CacheLogBase(LogBase logBase)
    {
        LogId = logBase.Id;
        Timestamp = logBase.Timestamp;
        ComputerId = logBase.Computer.GetId();
    }

    public abstract LogBase ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager);

    public abstract LogBase ToLog(Computer computer);
}

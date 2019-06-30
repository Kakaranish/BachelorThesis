package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBaseEntity;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public abstract class CacheLogBaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Long LogId;

    public Timestamp Timestamp;

    public int ComputerId;

    protected CacheLogBaseEntity()
    {
    }

    protected CacheLogBaseEntity(LogBaseEntity logBaseEntity)
    {
        LogId = logBaseEntity.Id;
        Timestamp = logBaseEntity.Timestamp;
        ComputerId = logBaseEntity.Computer.GetId();
    }

    public abstract LogBaseEntity ToLog(ComputersAndSshConfigsManager computersAndSshConfigsManager);

    public abstract LogBaseEntity ToLog(Computer computer);
}

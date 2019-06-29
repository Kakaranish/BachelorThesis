package Healthcheck.Entities.CacheLogs;

import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.Logs.LogBaseEntity;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public abstract class CacheLogBaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Timestamp Timestamp;

    public int ComputerId;

    protected CacheLogBaseEntity()
    {
    }

    protected CacheLogBaseEntity(LogBaseEntity logBaseEntity)
    {
        Id = logBaseEntity.Id;
        Timestamp = logBaseEntity.Timestamp;
        ComputerId = logBaseEntity.Computer.GetId();
    }

    public abstract LogBaseEntity ToLog(ComputersAndSshConfigsManager cpComputersAndSshConfigsManager);
}

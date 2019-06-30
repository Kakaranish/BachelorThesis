package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import Healthcheck.Entities.CacheLogs.CacheLogBaseEntity;
import Healthcheck.Entities.Computer;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public abstract class LogBaseEntity
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Timestamp Timestamp;

    @ManyToOne
    @JoinColumn(name = "Computer_Id", referencedColumnName = "Id")
    public Computer Computer;

    protected LogBaseEntity()
    {
    }

    public LogBaseEntity(Computer computer)
    {
        Id = null;
        Computer = computer;
        Timestamp = new Timestamp(System.currentTimeMillis());
    }

    public LogBaseEntity(Computer computer, Timestamp timestamp)
    {
        Id = null;
        Computer = computer;
        Timestamp = timestamp;
    }

    public LogBaseEntity(Long id, Computer computer, Timestamp timestamp)
    {
        Id = id;
        Computer = computer;
        Timestamp = timestamp;
    }

    public abstract LogEntry ToEntry();

    public abstract CacheLogBaseEntity ToCacheLog();
}

package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.Computer;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public abstract class LogBase
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Timestamp Timestamp;

    @ManyToOne
    @JoinColumn(name = "Computer_Id", referencedColumnName = "Id")
    public Computer Computer;

    protected LogBase()
    {
    }

    public LogBase(Computer computer)
    {
        Id = null;
        Computer = computer;
        Timestamp = new Timestamp(System.currentTimeMillis());
    }

    public LogBase(Computer computer, Timestamp timestamp)
    {
        Id = null;
        Computer = computer;
        Timestamp = timestamp;
    }

    public LogBase(Long id, Computer computer, Timestamp timestamp)
    {
        Id = id;
        Computer = computer;
        Timestamp = timestamp;
    }

    public abstract LogEntry ToEntry();

    public abstract CacheLogBase ToCacheLog();
}

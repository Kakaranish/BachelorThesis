package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.IConvertableToEntry;
import GUI.TableViewEntries.LogEntry;
import Healthcheck.Entities.Computer;
import javax.persistence.*;
import java.sql.Timestamp;

@MappedSuperclass
public class BaseEntity implements IConvertableToEntry
{
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long Id;

    public Timestamp Timestamp;

    @ManyToOne
    @JoinColumn(name = "Computer_Id", referencedColumnName = "Id")
    public Computer Computer;

    protected BaseEntity()
    {
    }

    public BaseEntity(Computer computer)
    {
        Id = null;
        Computer = computer;
        Timestamp = new Timestamp(System.currentTimeMillis());
    }

    public BaseEntity(Computer computer, Timestamp timestamp)
    {
        Id = null;
        Computer = computer;
        Timestamp = timestamp;
    }

    @Override
    public LogEntry ToEntry()
    {
        return null;
    }
}

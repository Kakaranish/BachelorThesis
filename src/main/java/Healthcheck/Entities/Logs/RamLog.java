package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import GUI.TableViewEntries.RamEntry;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.RamInfo;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "RamLogs")
public class RamLog extends LogBaseEntity
{
    @Embedded
    public RamInfo RamInfo;

    private RamLog()
    {
    }

    public RamLog(Computer computer, RamInfo ramInfo)
    {
        super(computer);
        RamInfo = ramInfo;
    }

    public RamLog(Computer computer, RamInfo ramInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        RamInfo = ramInfo;
    }

    @Override
    public LogEntry ToEntry()
    {
        return new RamEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));
            Total = new SimpleLongProperty(RamInfo.Total);
            Used = new SimpleLongProperty(RamInfo.Used);
            Free = new SimpleLongProperty(RamInfo.Free);
            Shared = new SimpleLongProperty(RamInfo.Shared);
            Buffers = new SimpleLongProperty(RamInfo.Buffers);
            Cached = new SimpleLongProperty(RamInfo.Cached);
        }};
    }
}

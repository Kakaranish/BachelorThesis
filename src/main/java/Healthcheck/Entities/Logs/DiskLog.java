package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.DiskEntry;
import GUI.TableViewEntries.LogEntry;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.DiskInfo;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "DisksLogs")
public class DiskLog extends LogBaseEntity
{
    @Embedded
    public DiskInfo DiskInfo;

    private DiskLog()
    {
    }

    public DiskLog(Computer computer, DiskInfo diskInfo)
    {
        super(computer);
        DiskInfo = diskInfo;
    }

    public DiskLog(Computer computer, DiskInfo diskInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        DiskInfo = diskInfo;
    }

    @Override
    public LogEntry ToEntry()
    {
        return new DiskEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));
            FileSystem = new SimpleStringProperty(DiskInfo.FileSystem);
            BlocksNumber = new SimpleLongProperty(DiskInfo.BlocksNumber);
            Used = new SimpleLongProperty(DiskInfo.Used);
            Available = new SimpleLongProperty(DiskInfo.Available);
            UsePercentage = new SimpleIntegerProperty(DiskInfo.UsePercentage);
            MountedOn = new SimpleStringProperty(DiskInfo.MountedOn);
        }};
    }
}

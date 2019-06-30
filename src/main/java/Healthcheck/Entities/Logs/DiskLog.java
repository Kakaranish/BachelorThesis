package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.DiskEntry;
import GUI.TableViewEntries.LogEntry;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.CacheLogs.DiskCacheLog;
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
public class DiskLog extends LogBase
{
    @Embedded
    public DiskInfo DiskInfo;

    private DiskLog()
    {
    }

    public DiskLog(Computer computer, DiskInfo diskInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        DiskInfo = diskInfo;
    }

    public DiskLog(DiskCacheLog diskCacheLog,Computer computer)
    {
        super(diskCacheLog.LogId, computer, diskCacheLog.Timestamp);

        DiskInfo = diskCacheLog.DiskInfo;
    }

    public DiskLog(DiskCacheLog diskCacheLog, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        super(diskCacheLog.LogId, computersAndSshConfigsManager.GetComputerById(diskCacheLog.ComputerId),
                diskCacheLog.Timestamp);

        DiskInfo = diskCacheLog.DiskInfo;
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

    @Override
    public CacheLogBase ToCacheLog()
    {
        return new DiskCacheLog(this);
    }
}

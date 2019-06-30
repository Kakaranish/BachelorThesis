package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import GUI.TableViewEntries.RamEntry;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.CacheLogs.RamCacheLog;
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
public class RamLog extends LogBase
{
    @Embedded
    public RamInfo RamInfo;

    private RamLog()
    {
    }

    public RamLog(Computer computer, RamInfo ramInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        RamInfo = ramInfo;
    }

    public RamLog(RamCacheLog ramCacheLog, Computer computer)
    {
        super(ramCacheLog.LogId, computer, ramCacheLog.Timestamp);

        RamInfo = ramCacheLog.RamInfo;
    }

    public RamLog(RamCacheLog ramCacheLog, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        super(ramCacheLog.LogId, computersAndSshConfigsManager.GetComputerById(ramCacheLog.ComputerId),
                ramCacheLog.Timestamp);

        RamInfo = ramCacheLog.RamInfo;
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

    @Override
    public CacheLogBase ToCacheLog()
    {
        return new RamCacheLog(this);
    }
}

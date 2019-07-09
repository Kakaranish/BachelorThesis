package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.CpuEntry;
import GUI.TableViewEntries.LogEntry;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.CacheLogs.CpuCacheLog;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.CpuInfo;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "CpuLogs")
public class CpuLog extends LogBase
{
    @Embedded
    public CpuInfo CpuInfo;

    private CpuLog()
    {
    }

    public CpuLog(Computer computer, CpuInfo cpuInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        CpuInfo = cpuInfo;
    }

    public CpuLog(CpuCacheLog cpuCacheLog, Computer computer)
    {
        super(cpuCacheLog.LogId, computer,cpuCacheLog.Timestamp);

        CpuInfo = cpuCacheLog.CpuInfo;
    }

    public CpuLog(CpuCacheLog cpuCacheLog, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        super(cpuCacheLog.LogId, computersAndSshConfigsManager.GetComputerById(cpuCacheLog.ComputerId),
                cpuCacheLog.Timestamp);

        CpuInfo = cpuCacheLog.CpuInfo;
    }

    @Override
    public LogEntry ToEntry()
    {
        return new CpuEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));
            CpuName = new SimpleStringProperty(CpuInfo.CpuName);
            User = new SimpleLongProperty(CpuInfo.User);
            Nice = new SimpleLongProperty(CpuInfo.Nice);
            System = new SimpleLongProperty(CpuInfo.System);
            Idle = new SimpleLongProperty(CpuInfo.Idle);
            Iowait = new SimpleLongProperty(CpuInfo.Iowait);
            Irq = new SimpleLongProperty(CpuInfo.Irq);
            Softirq = new SimpleLongProperty(CpuInfo.Softirq);
            Steal = new SimpleLongProperty(CpuInfo.Steal);
            Quest = new SimpleLongProperty(CpuInfo.Quest);
            QuestNice = new SimpleLongProperty(CpuInfo.QuestNice);
            FirstBatch = new SimpleBooleanProperty(CpuInfo.FirstBatch);
        }};
    }

    @Override
    public CacheLogBase ToCacheLog()
    {
        return new CpuCacheLog(this);
    }
}

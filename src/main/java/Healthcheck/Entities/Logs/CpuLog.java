package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.CpuEntry;
import GUI.TableViewEntries.LogEntry;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.CacheLogs.CacheLogBaseEntity;
import Healthcheck.Entities.CacheLogs.CpuCacheLog;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.CpuInfo;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "CpuLogs")
public class CpuLog extends LogBaseEntity
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
            Last1MinuteAvgCpuUtil = new SimpleDoubleProperty(CpuInfo.Last1MinuteAvgCpuUtil);
            Last5MinutesAvgCpuUtil = new SimpleDoubleProperty(CpuInfo.Last5MinutesAvgCpuUtil);
            Last15MinutesAvgCpuUtil = new SimpleDoubleProperty(CpuInfo.Last15MinutesAvgCpuUtil);
            ExecutingKernelSchedulingEntitiesNum = new SimpleIntegerProperty(CpuInfo.ExecutingKernelSchedulingEntitiesNum);
            ExistingKernelSchedulingEntitiesNum = new SimpleIntegerProperty(CpuInfo.ExistingKernelSchedulingEntitiesNum);
            RecentlyCreatedProcessPID = new SimpleIntegerProperty(CpuInfo.RecentlyCreatedProcessPID);
        }};
    }

    @Override
    public CacheLogBaseEntity ToCacheLog()
    {
        return new CpuCacheLog(this);
    }
}

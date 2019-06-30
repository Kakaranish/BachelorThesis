package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import GUI.TableViewEntries.SwapEntry;
import Healthcheck.ComputersAndSshConfigsManager;
import Healthcheck.Entities.CacheLogs.CacheLogBase;
import Healthcheck.Entities.CacheLogs.SwapCacheLog;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.SwapInfo;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "SwapLogs")
public class SwapLog extends LogBase
{
    @Embedded
    public SwapInfo SwapInfo;

    private SwapLog()
    {
    }

    public SwapLog(Computer computer, SwapInfo swapInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        SwapInfo = swapInfo;
    }

    public SwapLog(SwapCacheLog swapCacheLog, Computer computer)
    {
        super(swapCacheLog.LogId, computer, swapCacheLog.Timestamp);

        SwapInfo = swapCacheLog.SwapInfo;
    }

    public SwapLog(SwapCacheLog swapCacheLog, ComputersAndSshConfigsManager computersAndSshConfigsManager)
    {
        super(swapCacheLog.LogId, computersAndSshConfigsManager.GetComputerById(swapCacheLog.ComputerId),
                swapCacheLog.Timestamp);

        SwapInfo = swapCacheLog.SwapInfo;
    }

    @Override
    public LogEntry ToEntry()
    {
        return new SwapEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));
            Total = new SimpleLongProperty(SwapInfo.Total);
            Used = new SimpleLongProperty(SwapInfo.Used);
            Free = new SimpleLongProperty(SwapInfo.Free);
        }};
    }

    @Override
    public CacheLogBase ToCacheLog()
    {
        return new SwapCacheLog(this);
    }
}

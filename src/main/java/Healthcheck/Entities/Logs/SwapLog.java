package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import GUI.TableViewEntries.SwapEntry;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.SwapInfo;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "SwapLogs")
public class SwapLog extends LogBaseEntity
{
    @Embedded
    public SwapInfo SwapInfo;

    private SwapLog()
    {
    }

    public SwapLog(Computer computer, SwapInfo swapInfo)
    {
        super(computer);
        SwapInfo = swapInfo;
    }

    public SwapLog(Computer computer, SwapInfo swapInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        SwapInfo = swapInfo;
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
}

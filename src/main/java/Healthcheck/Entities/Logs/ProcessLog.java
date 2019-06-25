package Healthcheck.Entities.Logs;

import GUI.TableViewEntries.LogEntry;
import GUI.TableViewEntries.ProcessEntry;
import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.ProcessInfo;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

@Entity
@Table(name = "ProcessesLogs")
public class ProcessLog extends BaseEntity
{
    @Embedded
    public ProcessInfo ProcessInfo;

    private ProcessLog()
    {
    }

    public ProcessLog(Computer computer, ProcessInfo processInfo)
    {
        super(computer);
        ProcessInfo = processInfo;
    }

    public ProcessLog(Computer computer, ProcessInfo processInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        ProcessInfo = processInfo;
    }

    @Override
    public LogEntry ToEntry()
    {
        return new ProcessEntry()
        {{
            Datetime = new SimpleStringProperty(new SimpleDateFormat("dd/MM/yyyy' 'HH:mm:ss:S").format(Timestamp));
            User = new SimpleStringProperty(ProcessInfo.User);
            PID = new SimpleLongProperty(ProcessInfo.PID);
            CPU_Percentage = new SimpleDoubleProperty(ProcessInfo.CPU_Percentage);
            Memory_Percentage = new SimpleDoubleProperty(ProcessInfo.Memory_Percentage);
            VSZ = new SimpleLongProperty(ProcessInfo.VSZ);
            RSS = new SimpleLongProperty(ProcessInfo.RSS);
            TTY = new SimpleStringProperty(ProcessInfo.TTY);
            Stat = new SimpleStringProperty(ProcessInfo.Stat);
            Start = new SimpleStringProperty(ProcessInfo.Start);
            Time = new SimpleStringProperty(ProcessInfo.Time);
            Command = new SimpleStringProperty(ProcessInfo.Command);
        }};
    }
}

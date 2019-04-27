package Entities.Logs;

import Entities.Computer;
import Models.Info.ProcessInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "ProcessesLogs")
public class ProcessLog extends BaseEntity
{
    @Embedded
    public ProcessInfo ProcessInfo;

    public ProcessLog(Computer computer, ProcessInfo processInfo)
    {
        super(computer);
        ProcessInfo = processInfo;
    }

    public ProcessLog(Computer computer, ProcessInfo processInfo, Date timestamp)
    {
        super(computer, timestamp);
        ProcessInfo = processInfo;
    }
}

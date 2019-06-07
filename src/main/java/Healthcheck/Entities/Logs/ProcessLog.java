package Healthcheck.Entities.Logs;

import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.ProcessInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

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
}

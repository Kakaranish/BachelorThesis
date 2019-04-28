package Entities.Logs;

import Entities.ComputerEntity;
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

    public ProcessLog(ComputerEntity computerEntity, ProcessInfo processInfo)
    {
        super(computerEntity);
        ProcessInfo = processInfo;
    }

    public ProcessLog(ComputerEntity computerEntity, ProcessInfo processInfo, Date timestamp)
    {
        super(computerEntity, timestamp);
        ProcessInfo = processInfo;
    }
}

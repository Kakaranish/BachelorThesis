package Entities.Logs;

import Entities.Computer;
import Models.Info.CPUInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "CPULogs")
public class CPULog extends BaseEntity
{
    @Embedded
    public CPUInfo CPUInfo;

    public CPULog(Computer computer, CPUInfo cpuInfo)
    {
        super(computer);
        CPUInfo = cpuInfo;
    }

    public CPULog(Computer computer, CPUInfo cpuInfo, Date timestamp)
    {
        super(computer, timestamp);
        CPUInfo = cpuInfo;
    }
}

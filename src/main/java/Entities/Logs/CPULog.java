package Entities.Logs;

import Entities.Computer;
import Models.Info.CpuInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "CPULogs")
public class CpuLog extends BaseEntity
{
    @Embedded
    public CpuInfo CPUInfo;

    public CpuLog(Computer computer, CpuInfo cpuInfo)
    {
        super(computer);
        CPUInfo = cpuInfo;
    }

    public CpuLog(Computer computer, CpuInfo cpuInfo, Date timestamp)
    {
        super(computer, timestamp);
        CPUInfo = cpuInfo;
    }
}

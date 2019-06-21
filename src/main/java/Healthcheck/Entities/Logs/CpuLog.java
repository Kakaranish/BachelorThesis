package Healthcheck.Entities.Logs;

import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.CpuInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "CpuLogs")
public class CpuLog extends BaseEntity
{
    @Embedded
    public CpuInfo CpuInfo;

    private CpuLog()
    {
    }

    public CpuLog(Computer computer, CpuInfo cpuInfo)
    {
        super(computer);
        CpuInfo = cpuInfo;
    }

    public CpuLog(Computer computer, CpuInfo cpuInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        CpuInfo = cpuInfo;
    }
}

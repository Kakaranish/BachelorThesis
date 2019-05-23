package Healthcheck.Entities.Logs;

import Healthcheck.Entities.ComputerEntity;
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
    public CpuInfo CPUInfo;

    private CpuLog()
    {
    }

    public CpuLog(ComputerEntity computerEntity, CpuInfo cpuInfo)
    {
        super(computerEntity);
        CPUInfo = cpuInfo;
    }

    public CpuLog(ComputerEntity computerEntity, CpuInfo cpuInfo, Timestamp timestamp)
    {
        super(computerEntity, timestamp);
        CPUInfo = cpuInfo;
    }
}

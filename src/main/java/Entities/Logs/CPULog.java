package Entities.Logs;

import Entities.ComputerEntity;
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

    public CpuLog(ComputerEntity computerEntity, CpuInfo cpuInfo)
    {
        super(computerEntity);
        CPUInfo = cpuInfo;
    }

    public CpuLog(ComputerEntity computerEntity, CpuInfo cpuInfo, Date timestamp)
    {
        super(computerEntity, timestamp);
        CPUInfo = cpuInfo;
    }
}

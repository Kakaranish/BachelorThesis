package Healthcheck.Entities.Logs;

import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Models.Info.RamInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "RamLogs")
public class RamLog extends BaseEntity
{
    @Embedded
    public RamInfo RamInfo;

    private RamLog()
    {
    }

    public RamLog(ComputerEntity computerEntity, RamInfo ramInfo)
    {
        super(computerEntity);
        RamInfo = ramInfo;
    }

    public RamLog(ComputerEntity computerEntity, RamInfo ramInfo, Timestamp timestamp)
    {
        super(computerEntity, timestamp);
        RamInfo = ramInfo;
    }
}

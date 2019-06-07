package Healthcheck.Entities.Logs;

import Healthcheck.Entities.Computer;
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

    public RamLog(Computer computer, RamInfo ramInfo)
    {
        super(computer);
        RamInfo = ramInfo;
    }

    public RamLog(Computer computer, RamInfo ramInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        RamInfo = ramInfo;
    }
}

package Entities.Logs;

import Entities.Computer;
import Models.Info.RamInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "RamLogs")
public class RamLog extends BaseEntity
{
    @Embedded
    public RamInfo RamInfo;

    public RamLog(Computer computer, RamInfo ramInfo)
    {
        super(computer);
        RamInfo = ramInfo;
    }

    public RamLog(Computer computer, RamInfo ramInfo, Date timestamp)
    {
        super(computer, timestamp);
        RamInfo = ramInfo;
    }
}

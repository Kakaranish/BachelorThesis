package Entities.Logs;

import Entities.ComputerEntity;
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

    public RamLog(ComputerEntity computerEntity, RamInfo ramInfo)
    {
        super(computerEntity);
        RamInfo = ramInfo;
    }

    public RamLog(ComputerEntity computerEntity, RamInfo ramInfo, Date timestamp)
    {
        super(computerEntity, timestamp);
        RamInfo = ramInfo;
    }
}

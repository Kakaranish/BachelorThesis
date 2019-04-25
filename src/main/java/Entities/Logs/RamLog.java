package Entities.Logs;

import Entities.Computer;
import Models.Info.RamInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;

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

    public RamLog(Computer computer, RamInfo ramInfo, Long id)
    {
        super(computer, id);
        RamInfo = ramInfo;
    }
}

package Entities.Logs;

import Entities.ComputerEntity;
import Models.Info.DiskInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "DisksLogs")
public class DiskLog extends BaseEntity
{
    @Embedded
    public DiskInfo DiskInfo;

    public DiskLog(ComputerEntity computerEntity, DiskInfo diskInfo)
    {
        super(computerEntity);
        DiskInfo = diskInfo;
    }

    public DiskLog(ComputerEntity computerEntity, DiskInfo diskInfo, Date timestamp)
    {
        super(computerEntity, timestamp);
        DiskInfo = diskInfo;
    }
}

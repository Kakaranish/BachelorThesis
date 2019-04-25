package Entities.Logs;

import Entities.Computer;
import Models.Info.DiskInfo;
import Models.Info.ProcessInfo;

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

    public DiskLog(Computer computer, DiskInfo diskInfo)
    {
        super(computer);
        DiskInfo = diskInfo;
    }

    public DiskLog(Computer computer, DiskInfo diskInfo, Long id, Date timestamp)
    {
        super(computer, id, timestamp);
        DiskInfo = diskInfo;
    }
}

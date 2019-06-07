package Healthcheck.Entities.Logs;

import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.DiskInfo;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.sql.Timestamp;

@Entity
@Table(name = "DisksLogs")
public class DiskLog extends BaseEntity
{
    @Embedded
    public DiskInfo DiskInfo;

    private DiskLog()
    {
    }

    public DiskLog(Computer computer, DiskInfo diskInfo)
    {
        super(computer);
        DiskInfo = diskInfo;
    }

    public DiskLog(Computer computer, DiskInfo diskInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        DiskInfo = diskInfo;
    }
}

package Healthcheck.Entities.Logs;

import Healthcheck.Entities.Computer;
import Healthcheck.Models.Info.SwapInfo;
import javax.persistence.*;
import java.sql.Timestamp;

@Entity
@Table(name = "SwapLogs")
public class SwapLog extends BaseEntity
{
    @Embedded
    public SwapInfo SwapInfo;

    private SwapLog()
    {
    }

    public SwapLog(Computer computer, SwapInfo swapInfo)
    {
        super(computer);
        SwapInfo = swapInfo;
    }

    public SwapLog(Computer computer, SwapInfo swapInfo, Timestamp timestamp)
    {
        super(computer, timestamp);
        SwapInfo = swapInfo;
    }
}

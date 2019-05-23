package Healthcheck.Entities.Logs;

import Healthcheck.Entities.ComputerEntity;
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

    public SwapLog(ComputerEntity computerEntity, SwapInfo swapInfo)
    {
        super(computerEntity);
        SwapInfo = swapInfo;
    }

    public SwapLog(ComputerEntity computerEntity, SwapInfo swapInfo, Timestamp timestamp)
    {
        super(computerEntity, timestamp);
        SwapInfo = swapInfo;
    }
}

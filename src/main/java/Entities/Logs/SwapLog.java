package Entities.Logs;

import Entities.ComputerEntity;
import Models.Info.SwapInfo;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SwapLogs")
public class SwapLog extends BaseEntity
{
    @Embedded
    public SwapInfo SwapInfo;

    public SwapLog(ComputerEntity computerEntity, SwapInfo swapInfo)
    {
        super(computerEntity);
        SwapInfo = swapInfo;
    }

    public SwapLog(ComputerEntity computerEntity, SwapInfo swapInfo, Date timestamp)
    {
        super(computerEntity, timestamp);
        SwapInfo = swapInfo;
    }
}

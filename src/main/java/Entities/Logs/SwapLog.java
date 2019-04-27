package Entities.Logs;

import Entities.Computer;
import Models.Info.SwapInfo;
import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "SwapLogs")
public class SwapLog extends BaseEntity
{
    @Embedded
    public SwapInfo SwapInfo;

    public SwapLog(Computer computer, SwapInfo swapInfo)
    {
        super(computer);
        SwapInfo = swapInfo;
    }

    public SwapLog(Computer computer, SwapInfo swapInfo, Date timestamp)
    {
        super(computer, timestamp);
        SwapInfo = swapInfo;
    }
}

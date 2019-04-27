package Entities.Logs;

import Entities.Computer;
import Models.Info.SwapInfo;
import javax.persistence.*;

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

    public SwapLog(Computer computer, SwapInfo swapInfo, Long id)
    {
        super(computer, id);
        SwapInfo = swapInfo;
    }
}

package Models.Info;

import Entities.Computer;
import Entities.Logs.BaseEntity;
import Entities.Logs.SwapLog;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embeddable
public class SwapInfo implements IInfo
{
    public long Total;
    public long Used;
    public long Free;

    /*
        commandExecutionResult looks like:
        Swap:            0          0          0
    */

    public SwapInfo()
    {
    }

    public SwapInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = Long.parseLong(commandExecutionResultSplit[1]);
        Used = Long.parseLong(commandExecutionResultSplit[2]);
        Free = Long.parseLong(commandExecutionResultSplit[3]);
    }

    public List<BaseEntity> ToLogList(Computer computer, Date timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new SwapLog(computer, this, timestamp));

        return logList;
    }
}

package Healthcheck.Models.Info;

import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Entities.Logs.SwapLog;
import javax.persistence.Embeddable;
import java.sql.Timestamp;
import java.util.ArrayList;
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

    private SwapInfo()
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

    public List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new SwapLog(computerEntity, this, timestamp));

        return logList;
    }
}

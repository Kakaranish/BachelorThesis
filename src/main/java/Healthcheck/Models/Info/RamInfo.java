package Healthcheck.Models.Info;

import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Entities.Logs.RamLog;
import javax.persistence.Embeddable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class RamInfo implements IInfo
{
    public long Total;
    public long Used;
    public long Free;
    public long Shared;
    public long Buffers;
    public long Cached;

    /*
        commandExecutionResult looks like:
        Mem:       2011984     215400    1796584          0      88300      69316
    */

    private RamInfo()
    {
    }

    public RamInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = Long.parseLong(commandExecutionResultSplit[1]);
        Used = Long.parseLong(commandExecutionResultSplit[2]);
        Free = Long.parseLong(commandExecutionResultSplit[3]);
        Shared = Long.parseLong(commandExecutionResultSplit[4]);
        Buffers = Long.parseLong(commandExecutionResultSplit[5]);
        Cached = Long.parseLong(commandExecutionResultSplit[6]);
    }

    public List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new RamLog(computerEntity, this, timestamp));

        return logList;
    }
}

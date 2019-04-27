package Models.Info;

import Entities.Computer;
import Entities.Logs.BaseEntity;
import Entities.Logs.CPULog;
import Entities.Logs.RamLog;

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Date;
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

    public RamInfo()
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

    public List<BaseEntity> ToLogList(Computer computer, Date timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new RamLog(computer, this, timestamp));

        return logList;
    }
}

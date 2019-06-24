package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
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
        total       used       free     shared    buffers     cached
        Mem:       2011984     311532    1700452          0     156348      90716
        -/+ buffers/cache:      64468    1947516
        Swap:            0          0          0

        OR

        razem       użyte       wolne    dzielone   buf/cache    dostępne
        Pamięć:     6139256      164544     5059690        1052      915021     5706805
        Wymiana:      998240           0      998240
    */

    private SwapInfo()
    {
    }

    public SwapInfo(String commandExecutionResult)
    {
        int numOfLinesInResult = commandExecutionResult.split("\n").length;
        commandExecutionResult = commandExecutionResult.split("\n")[numOfLinesInResult-1];
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = Long.parseLong(commandExecutionResultSplit[1]);
        Used = Long.parseLong(commandExecutionResultSplit[2]);
        Free = Long.parseLong(commandExecutionResultSplit[3]);
    }

    public List<BaseEntity> ToLogList(Computer computer, Timestamp timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new SwapLog(computer, this, timestamp));

        return logList;
    }
}

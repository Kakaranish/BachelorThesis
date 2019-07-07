package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.LogBase;
import Healthcheck.Entities.Logs.RamLog;
import javax.persistence.Column;
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

    @Column(nullable = true)
    public Long Buffers;

    @Column(nullable = true)
    public Long Cached;

    @Column(nullable = true)
    public Long BuffersCached;

    @Column(nullable = true)
    public Long Available;

    /*
        commandExecutionResult looks like:
                    razem       użyte       wolne    dzielone   buf/cache    dostępne
        Pamięć:     6139256      168259     5049176        1073      921821     5703008
        Wymiana:      998240           0      998240

        OR

                    total       used       free     shared       buffers     cached
        Mem:       2011984     100832    1911152          0      11012      43928
        -/+ buffers/cache:      45892    1966092
        Swap:            0          0          0


    */

    private RamInfo()
    {
    }

    public RamInfo(String commandExecutionResult)
    {
        String header = commandExecutionResult.split("\n")[0].trim();
        String[] headerSplit = header.replaceAll("\\s+", "\t").split("\t");
        commandExecutionResult = commandExecutionResult.split("\n")[1];
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = Long.parseLong(commandExecutionResultSplit[1]);
        Used = Long.parseLong(commandExecutionResultSplit[2]);
        Free = Long.parseLong(commandExecutionResultSplit[3]);
        Shared = Long.parseLong(commandExecutionResultSplit[4]);

        if(headerSplit[5].equals("dostępne") || headerSplit[5].equals("available"))
        {
            BuffersCached = Long.parseLong(commandExecutionResultSplit[5]);
            Available = Long.parseLong(commandExecutionResultSplit[6]);
        }
        else
        {
            Buffers = Long.parseLong(commandExecutionResultSplit[5]);
            Cached = Long.parseLong(commandExecutionResultSplit[6]);
        }
    }

    public List<LogBase> ToLogList(Computer computer, Timestamp timestamp)
    {
        List<LogBase> logList = new ArrayList<>();
        logList.add(new RamLog(computer, this, timestamp));

        return logList;
    }
}

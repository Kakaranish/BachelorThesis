package Models.Info;

import Entities.ComputerEntity;
import Entities.Logs.BaseEntity;
import Entities.Logs.ProcessLog;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProcessesInfo implements IInfo
{
    public List<ProcessInfo> ProcessesInfo;

    /*
        commandExecutionResults looks like:
        USER       PID %CPU %MEM    VSZ   RSS TTY      STAT START   TIME COMMAND
        root         1  0.0  0.0  10652   832 ?        Ss   Apr13   0:02 init [2]
        root         2  0.0  0.0      0     0 ?        S    Apr13   0:00 [kthreadd]
        root         3  0.0  0.0      0     0 ?        S    Apr13   0:00 [ksoftirqd/0]
        root         5  0.0  0.0      0     0 ?        S    Apr13   0:00 [kworker/u:0]
        root         6  0.0  0.0      0     0 ?        S    Apr13   0:00 [migration/0]
        root         7  0.0  0.0      0     0 ?        S    Apr13   0:00 [watchdog/0]
        root         8  0.0  0.0      0     0 ?        S<   Apr13   0:00 [cpuset]
        ... ETC
    */

    public ProcessesInfo(String commandExecutionResults)
    {
        ProcessesInfo = new ArrayList<ProcessInfo>();
        List<String> commandExecutionResultsSplit = new ArrayList<String>(Arrays.asList(commandExecutionResults.split("\\n")));
        commandExecutionResultsSplit.remove(0);

        for (String result : commandExecutionResultsSplit)
        {
            ProcessesInfo.add(new ProcessInfo(result));
        }
    }

    public List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        for (ProcessInfo processInfo: ProcessesInfo)
        {
            logList.add(new ProcessLog(computerEntity, processInfo, timestamp));
        }

        return logList;
    }
}

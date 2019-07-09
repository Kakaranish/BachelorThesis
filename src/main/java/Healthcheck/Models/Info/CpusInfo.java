package Healthcheck.Models.Info;

import Healthcheck.Entities.Computer;
import Healthcheck.Entities.Logs.CpuLog;
import Healthcheck.Entities.Logs.LogBase;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class CpusInfo implements IInfo
{
    public List<CpuInfo> CpusInfo;

    /*
        commandExecutionResults looks like:
        cpu  1861349 0 33775 66016825 13613 0 532 313 0 0
        cpu0 1861349 0 33775 66016825 13613 0 532 313 0 0
        intr 8756540 36 9 0 0 798 0 2 0 0 0 0 67972 142 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 748592 13 0 0 0 192751
        0 33 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
        0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0 0
    */

    private CpusInfo()
    {
    }

    public CpusInfo(String commandExecutionResults)
    {
        String[] commandExecutionResultsSplit = commandExecutionResults.split("\n");
        List<String> commandExecutionResultsWithCpu = new ArrayList<>();
        for (String commandExecutionResult : commandExecutionResultsSplit)
        {
            if(commandExecutionResult.contains("cpu"))
            {
                commandExecutionResultsWithCpu.add(commandExecutionResult);
            }
        }
        commandExecutionResultsWithCpu.remove(0);

        CpusInfo = new ArrayList<CpuInfo>();
        for (String result : commandExecutionResultsWithCpu)
        {
            CpusInfo.add(new CpuInfo(result));
        }
    }

    @Override
    public List<LogBase> ToLogList(Computer computer, Timestamp timestamp)
    {
        List<LogBase> logList = new ArrayList<>();
        for (CpuInfo cpuInfo: CpusInfo)
        {
            logList.add(new CpuLog(computer, cpuInfo, timestamp));
        }

        return logList;
    }
}

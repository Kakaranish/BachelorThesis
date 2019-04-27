package Models.Info;

import Entities.Computer;
import Entities.Logs.BaseEntity;
import Entities.Logs.CPULog;

import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embeddable
public class CPUInfo implements IInfo
{
    public double CpuPercentage;

    /*
        commandExecutionResult looks like:
        1.43196
    */

    public CPUInfo()
    {
    }

    public CPUInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        CpuPercentage = Double.parseDouble(commandExecutionResult);
    }

    public List<BaseEntity> ToLogList(Computer computer, Date timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new CPULog(computer, this, timestamp));

        return logList;
    }
}

package Models.Info;

import Entities.Computer;
import Entities.Logs.BaseEntity;
import Entities.Logs.CpuLog;
import javax.persistence.Embeddable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Embeddable
public class CpuInfo implements IInfo
{
    public double CpuPercentage;

    /*
        commandExecutionResult looks like:
        1.43196
    */

    public CpuInfo()
    {
    }

    public CpuInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        CpuPercentage = Double.parseDouble(commandExecutionResult);
    }

    public List<BaseEntity> ToLogList(Computer computer, Date timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new CpuLog(computer, this, timestamp));

        return logList;
    }
}

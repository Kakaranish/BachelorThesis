package Healthcheck.Models.Info;

import Healthcheck.Entities.ComputerEntity;
import Healthcheck.Entities.Logs.BaseEntity;
import Healthcheck.Entities.Logs.CpuLog;
import javax.persistence.Embeddable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Embeddable
public class CpuInfo implements IInfo
{
    public double CpuPercentage;

    /*
        commandExecutionResult looks like:
        1.43196
    */

    private CpuInfo()
    {
    }

    public CpuInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        CpuPercentage = Double.parseDouble(commandExecutionResult);
    }

    public List<BaseEntity> ToLogList(ComputerEntity computerEntity, Timestamp timestamp)
    {
        List<BaseEntity> logList = new ArrayList<>();
        logList.add(new CpuLog(computerEntity, this, timestamp));

        return logList;
    }
}

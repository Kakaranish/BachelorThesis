package Models.Info;

import javax.persistence.Embeddable;

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
}

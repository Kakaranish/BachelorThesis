package SystemInfo;

public class CpuInfo
{
    public String TotalCpu_Percentage;

    /*
        commandExecutionResult looks like:
        1.43196%
    */

    public CpuInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        TotalCpu_Percentage = commandExecutionResult;
    }
}

package Models.Info;

import javax.persistence.Embeddable;

@Embeddable
public class SwapInfo implements IInfo
{
    public long Total;
    public long Used;
    public long Free;

    /*
        commandExecutionResult looks like:
        Swap:            0          0          0
    */

    public SwapInfo()
    {
    }

    public SwapInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = Long.parseLong(commandExecutionResultSplit[1]);
        Used = Long.parseLong(commandExecutionResultSplit[2]);
        Free = Long.parseLong(commandExecutionResultSplit[3]);
    }
}

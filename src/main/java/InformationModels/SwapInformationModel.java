package InformationModels;

public class SwapInformationModel implements IInformationModel
{
    public String Total;
    public String Used;
    public String Free;

    /*
        commandExecutionResult looks like:
        Swap:            0          0          0
    */

    public SwapInformationModel(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = commandExecutionResultSplit[1];
        Used = commandExecutionResultSplit[2];
        Free = commandExecutionResultSplit[3];
    }
}

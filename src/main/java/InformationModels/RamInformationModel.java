package InformationModels;

public class RamInformationModel implements IInformationModel
{
    public String Total;
    public String Used;
    public String Free;
    public String Shared;
    public String Buffers;
    public String Cached;

    /*
        commandExecutionResult looks like:
        Mem:       2011984     215400    1796584          0      88300      69316
    */

    public RamInformationModel(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = commandExecutionResultSplit[1];
        Used = commandExecutionResultSplit[2];
        Free = commandExecutionResultSplit[3];
        Shared = commandExecutionResultSplit[4];
        Buffers = commandExecutionResultSplit[5];
        Cached = commandExecutionResultSplit[6];
    }
}

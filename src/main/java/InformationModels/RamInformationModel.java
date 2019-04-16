package InformationModels;

public class RamInformationModel implements IInformationModel
{
    public long Total;
    public long Used;
    public long Free;
    public long Shared;
    public long Buffers;
    public long Cached;

    /*
        commandExecutionResult looks like:
        Mem:       2011984     215400    1796584          0      88300      69316
    */

    public RamInformationModel(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        String[] commandExecutionResultSplit = commandExecutionResult.split("\t");

        Total = Long.parseLong(commandExecutionResultSplit[1]);
        Used = Long.parseLong(commandExecutionResultSplit[2]);
        Free = Long.parseLong(commandExecutionResultSplit[3]);
        Shared = Long.parseLong(commandExecutionResultSplit[4]);
        Buffers = Long.parseLong(commandExecutionResultSplit[5]);
        Cached = Long.parseLong(commandExecutionResultSplit[6]);
    }
}

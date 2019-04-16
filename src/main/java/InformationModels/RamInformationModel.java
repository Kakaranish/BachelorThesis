package InformationModels;

public class RamInformationModel implements IInformationModel
{
    public String Total;
    public String Used;
    public String Free;
    public String Shared;
    public String Buffers;
    public String Cached;

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

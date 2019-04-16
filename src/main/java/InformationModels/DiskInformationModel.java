package InformationModels;

import java.util.Arrays;
import java.util.List;

public class DiskInformationModel
{
    public String FileSystem;
    public String Size;
    public String Used;
    public String Available;
    public String UsePercentage;
    public String MountedOn;

    /*
        commandExecutionResult looks like:
        udev                                                     10M     0   10M   0% /dev
    */

    public DiskInformationModel(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+","\t");
        List<String> commandExecutionResultSplit = Arrays.asList(commandExecutionResult.split("\t"));

        FileSystem = commandExecutionResultSplit.get(1);
        Size = commandExecutionResultSplit.get(2);
        Used = commandExecutionResultSplit.get(3);
        Available = commandExecutionResultSplit.get(3);
        UsePercentage = commandExecutionResultSplit.get(4);
        MountedOn = commandExecutionResultSplit.get(5);
    }
}

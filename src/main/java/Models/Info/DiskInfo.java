package Models.Info;

import java.util.Arrays;
import java.util.List;

public class DiskInfo
{
    public String FileSystem;
    public long BlocksNumber;
    public long Used;
    public long Available;
    public int UsePercentage;
    public String MountedOn;

    /*
        commandExecutionResult looks like:
        udev                                                     10M     0   10M   0% /dev
    */

    public DiskInfo()
    {
    }

    public DiskInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+","\t");
        List<String> commandExecutionResultSplit = Arrays.asList(commandExecutionResult.split("\t"));

        FileSystem = commandExecutionResultSplit.get(0);
        BlocksNumber = Long.parseLong(commandExecutionResultSplit.get(1));
        Used = Long.parseLong(commandExecutionResultSplit.get(2));
        Available = Long.parseLong(commandExecutionResultSplit.get(3));
        UsePercentage = Integer.parseInt(commandExecutionResultSplit.get(4).replaceAll("%",""));
        MountedOn = commandExecutionResultSplit.get(5);
    }
}

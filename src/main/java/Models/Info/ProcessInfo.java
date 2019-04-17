package InformationModels;

import java.util.Arrays;
import java.util.List;

public class ProcessInfo
{
    public String User;
    public long PID;
    public double CPU_Perentage;
    public double Memory_Percentage;
    public long VSZ;
    public long RSS;
    public String TTY;
    public String Stat;
    public String Start;
    public String Time;
    public String Command;

    /*
        commandExecutionResult looks like:
        root         1  0.0  0.0  10652   832 ?        Ss   Apr13   0:02 init [2]
    */

    public ProcessInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+", "\t");
        List<String> commandExecutionResultSplit = Arrays.asList(commandExecutionResult.split("\t"));

        final int lastElementIndex = 10;
        String commandFieldValue = "";
        for(int i = lastElementIndex; i < commandExecutionResultSplit.size(); ++i)
            commandFieldValue += commandExecutionResultSplit.get(i) + " ";
        commandFieldValue = commandFieldValue.trim();

        User = commandExecutionResultSplit.get(0);
        PID = Long.parseLong(commandExecutionResultSplit.get(1));
        CPU_Perentage = Double.parseDouble(commandExecutionResultSplit.get(2));
        Memory_Percentage = Double.parseDouble(commandExecutionResultSplit.get(3));
        VSZ = Long.parseLong(commandExecutionResultSplit.get(4));
        RSS = Long.parseLong(commandExecutionResultSplit.get(5));
        TTY = commandExecutionResultSplit.get(6);
        Stat = commandExecutionResultSplit.get(7);
        Start = commandExecutionResultSplit.get(8);
        Time = commandExecutionResultSplit.get(9);
        Command = commandFieldValue;
    }
}

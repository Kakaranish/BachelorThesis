package Healthcheck.Models.Info;

import java.util.ArrayList;
import java.util.Arrays;

public class UserInfo
{
    public String User;
    public String TTY;
    public String FromWhere;
    public String SAT15;
    public String Idle;
    public String JCPU;
    public String PCPU;
    public String What;

    /*
        commandExecutionResult looks like:
        root     tty1                      Sat15    3days  0.12s  0.09s -bash

        or like this:
        root     pts/0    89-75-75-172.dyn 18:38    1.00s  0.02s  0.00s w
    */

    private UserInfo()
    {
    }

    public UserInfo(String commandExecutionResult)
    {
        commandExecutionResult = commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+","\t");
        ArrayList<String> commandExecutionResultSplit =
                new ArrayList<String>(Arrays.asList(commandExecutionResult.split("\t")));

        // 'FROM' field may be empty
        final int fieldsNum = 8;
        if (commandExecutionResultSplit.size() != fieldsNum)
        {
            commandExecutionResultSplit.add(2, " ");
        }

        User = commandExecutionResultSplit.get(0);
        TTY = commandExecutionResultSplit.get(1);
        FromWhere = commandExecutionResultSplit.get(2);
        SAT15 = commandExecutionResultSplit.get(3);
        Idle = commandExecutionResultSplit.get(4);
        JCPU = commandExecutionResultSplit.get(5);
        PCPU = commandExecutionResultSplit.get(6);
        What = commandExecutionResultSplit.get(7);
    }
}

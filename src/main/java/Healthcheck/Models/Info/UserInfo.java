package Healthcheck.Models.Info;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Arrays;

public class UserInfo
{
    public String User;
    public String TTY;
    public String FromWhere;
    public String LoginAt;
    public String Idle;
    public String JCPU;
    public String PCPU;
    public String What;

    /*
        commandExecutionResult looks like:
        root     pts/0    89-75-75-172.dyn 18:38    1.00s  0.02s  0.00s w
        or like this:
        root     pts/0    89-75-75-172.dyn 18:38    1.00s  0.02s  0.00s
        or like this:
        root     pts/0                     18:38    1.00s  0.02s  0.00s
    */

    private UserInfo()
    {
    }

    @Transient
    public static UserInfo GetEmptyUserInfo()
    {
        return new UserInfo();
    }

    public UserInfo(String commandExecutionResult)
    {
        boolean whatIsEmpty = false;
        if(commandExecutionResult.charAt(commandExecutionResult.length() - 1) == ' ')
        {
            whatIsEmpty = true;
        }
        commandExecutionResult.trim();
        commandExecutionResult = commandExecutionResult.replaceAll("\\s+","\t");
        ArrayList<String> commandExecutionResultSplit =
                new ArrayList<String>(Arrays.asList(commandExecutionResult.split("\t")));

        // 'FROM' and/or 'WHAT' field may be empty
        final int maxFieldsNum = 8;
        if(commandExecutionResultSplit.size() == 6)
        {
            commandExecutionResultSplit.add(2, "");
            commandExecutionResultSplit.add("");
        }
        else if (commandExecutionResultSplit.size() == 7)
        {
            if(whatIsEmpty)
            {
                commandExecutionResultSplit.add("");
            }
            else
            {
                commandExecutionResultSplit.add(2, "");
            }
        }

        User = commandExecutionResultSplit.get(0);
        TTY = commandExecutionResultSplit.get(1);
        FromWhere = commandExecutionResultSplit.get(2);
        LoginAt = commandExecutionResultSplit.get(3);
        Idle = commandExecutionResultSplit.get(4);
        JCPU = commandExecutionResultSplit.get(5);
        PCPU = commandExecutionResultSplit.get(6);
        What = commandExecutionResultSplit.get(7);
    }
}

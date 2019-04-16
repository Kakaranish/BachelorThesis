package InformationModels;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UsersInformationModel implements IInformationModel
{
    public List<UserInformationModel> UsersInfo;

    /*
        commandExecutionResults looks like:
         18:45:24 up 3 days,  3:00,  2 users,  load average: 0.00, 0.00, 0.00
        USER     TTY      FROM             LOGIN@   IDLE   JCPU   PCPU WHAT
        root     tty1                      Sat15    3days  0.12s  0.09s -bash
        root     pts/0    89-75-75-172.dyn 18:38    1.00s  0.02s  0.00s w
    */

    public UsersInformationModel(String commandExecutionResults)
    {
        UsersInfo = new ArrayList<UserInformationModel>();
        List<String> commandExecutionResultsSplit =
                new ArrayList<String>(Arrays.asList(commandExecutionResults.split("\\n")));

        /*
            We want to get rid of these two lines:
             18:45:24 up 3 days,  3:00,  2 users,  load average: 0.00, 0.00, 0.00
            USER     TTY      FROM             LOGIN@   IDLE   JCPU   PCPU WHAT
        */
        for (int i = 0; i < 2; ++i)
        {
            commandExecutionResultsSplit.remove(0);
        }

        for (String result : commandExecutionResultsSplit)
        {
            UsersInfo.add(new UserInformationModel(result));
        }
    }
}

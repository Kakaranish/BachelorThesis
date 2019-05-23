package Healthcheck.Preferences;

import Healthcheck.Models.Info.IInfo;
import Healthcheck.Models.Info.UsersInfo;

public class UsersInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "UserLog";
    }

    public String GetCommandToExecute()
    {
        return "w";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new UsersInfo(commandExecutionResult);
    }
}

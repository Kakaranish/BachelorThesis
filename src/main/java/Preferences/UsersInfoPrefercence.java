package Preferences;

import Models.Info.IInfo;
import Models.Info.UsersInfo;

public class UsersInfoPrefercence implements IPreference
{
    public String GetCommandToExecute()
    {
        return "w";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new UsersInfo(commandExecutionResult);
    }
}

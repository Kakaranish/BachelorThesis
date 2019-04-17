package Preferences;

import Models.Info.IInfo;
import Models.Info.UsersInfo;

public class UsersInformationPreference implements IPreference
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

package Preferences;

import Models.Info.DisksInfo;
import Models.Info.IInfo;

public class DisksInfoPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "df";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new DisksInfo(commandExecutionResult);
    }
}

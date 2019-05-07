package Preferences;

import Models.Info.IInfo;

public class NoPreference implements IPreference
{
    public String GetClassName()
    {
        return null;
    }

    public String GetCommandToExecute()
    {
        return null;
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return null;
    }
}

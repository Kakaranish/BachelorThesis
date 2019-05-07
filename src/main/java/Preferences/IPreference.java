package Preferences;

import Models.Info.IInfo;

public interface IPreference
{
    String GetClassName();
    String GetCommandToExecute();
    IInfo GetInformationModel(String commandExecutionResult);
}

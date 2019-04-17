package Preferences;

import Models.Info.IInfo;

public interface IPreference
{
    String GetCommandToExecute();
    IInfo GetInformationModel(String commandExecutionResult);
}

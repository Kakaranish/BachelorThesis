package Healthcheck.Preferences;

import Healthcheck.Models.Info.IInfo;

public interface IPreference
{
    String GetClassName();
    String GetCommandToExecute();
    IInfo GetInformationModel(String commandExecutionResult);
}

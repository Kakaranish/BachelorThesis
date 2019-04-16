package Preferences;

import InformationModels.IInformationModel;

public interface IPreference
{
    String GetCommandToExecute();
    IInformationModel GetInformationModel(String commandExecutionResult);
}

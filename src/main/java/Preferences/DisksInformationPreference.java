package Preferences;

import InformationModels.DisksInformationModel;
import InformationModels.IInformationModel;

public class DisksInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "df";
    }

    public IInformationModel GetInformationModel(String commandExecutionResult)
    {
        return new DisksInformationModel(commandExecutionResult);
    }
}

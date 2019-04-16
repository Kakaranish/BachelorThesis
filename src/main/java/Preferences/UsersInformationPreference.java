package Preferences;

import InformationModels.IInformationModel;
import InformationModels.UsersInformationModel;

public class UsersInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "w";
    }

    public IInformationModel GetInformationModel(String commandExecutionResult)
    {
        return new UsersInformationModel(commandExecutionResult);
    }
}

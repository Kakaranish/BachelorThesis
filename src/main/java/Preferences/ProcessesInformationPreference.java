package Preferences;

import InformationModels.IInformationModel;
import InformationModels.ProcessesInformationModel;

public class ProcessesInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "ps aux";
    }

    public IInformationModel GetInformationModel(String commandExecutionResult)
    {
        return new ProcessesInformationModel(commandExecutionResult);
    }
}

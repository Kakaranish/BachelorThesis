package Preferences;

import InformationModels.IInformationModel;
import InformationModels.RamInformationModel;

public class RamInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "free --kilo | grep Mem";
    }

    public IInformationModel GetInformationModel(String commandExecutionResult)
    {
        return new RamInformationModel(commandExecutionResult);
    }
}

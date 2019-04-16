package Preferences;

import InformationModels.IInformationModel;
import InformationModels.SwapInformationModel;

public class SwapInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "free --kilo | grep Swap";
    }

    public IInformationModel GetInformationModel(String commandExecutionResult)
    {
        return new SwapInformationModel(commandExecutionResult);
    }
}

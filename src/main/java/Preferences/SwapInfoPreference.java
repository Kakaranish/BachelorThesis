package Preferences;

import Models.Info.IInfo;
import Models.Info.SwapInfo;

public class SwapInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "SwapLog";
    }

    public String GetCommandToExecute()
    {
        return "free --kilo | grep Swap";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new SwapInfo(commandExecutionResult);
    }
}

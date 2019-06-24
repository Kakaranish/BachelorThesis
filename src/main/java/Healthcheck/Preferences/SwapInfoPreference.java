package Healthcheck.Preferences;

import Healthcheck.Models.Info.IInfo;
import Healthcheck.Models.Info.SwapInfo;

public class SwapInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "SwapLog";
    }

    public String GetCommandToExecute()
    {
        return "free --kilo";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new SwapInfo(commandExecutionResult);
    }
}

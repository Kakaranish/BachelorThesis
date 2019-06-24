package Healthcheck.Preferences;

import Healthcheck.Models.Info.IInfo;
import Healthcheck.Models.Info.RamInfo;

public class RamInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "RamLog";
    }

    public String GetCommandToExecute()
    {
        return "free --kilo";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new RamInfo(commandExecutionResult);
    }
}

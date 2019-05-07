package Preferences;

import Models.Info.IInfo;
import Models.Info.RamInfo;

public class RamInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "RamLog";
    }

    public String GetCommandToExecute()
    {
        return "free --kilo | grep Mem";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new RamInfo(commandExecutionResult);
    }
}

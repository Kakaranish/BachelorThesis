package Preferences;

import Models.Info.IInfo;
import Models.Info.RamInfo;

public class RamInfoPrefrence implements IPreference
{
    public String GetCommandToExecute()
    {
        return "free --kilo | grep Mem";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new RamInfo(commandExecutionResult);
    }
}

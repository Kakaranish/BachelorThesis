package Preferences;

import Models.Info.IInfo;
import Models.Info.RamInfo;

public class RamInformationPreference implements IPreference
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

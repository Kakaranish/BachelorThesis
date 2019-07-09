package Healthcheck.Preferences;

import Healthcheck.Models.Info.CpusInfo;
import Healthcheck.Models.Info.IInfo;

public class CpusInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "CpuLog";
    }

    public String GetCommandToExecute()
    {
        return "cat /proc/stat";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new CpusInfo(commandExecutionResult);
    }
}

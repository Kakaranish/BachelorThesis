package Healthcheck.Preferences;

import Healthcheck.Models.Info.CpuInfo;
import Healthcheck.Models.Info.IInfo;

public class CpuInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "CpuLog";
    }

    public String GetCommandToExecute()
    {
        return "cat /proc/loadavg";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new CpuInfo(commandExecutionResult);
    }
}

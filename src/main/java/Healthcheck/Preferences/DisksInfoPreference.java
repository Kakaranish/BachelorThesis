package Healthcheck.Preferences;

import Healthcheck.Models.Info.DisksInfo;
import Healthcheck.Models.Info.IInfo;

public class DisksInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "DiskLog";
    }

    public String GetCommandToExecute()
    {
        return "df | grep -v \"tmpfs\"";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new DisksInfo(commandExecutionResult);
    }
}

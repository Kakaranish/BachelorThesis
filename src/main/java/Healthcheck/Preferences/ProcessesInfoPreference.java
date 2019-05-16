package Healthcheck.Preferences;

import Healthcheck.Models.Info.IInfo;
import Healthcheck.Models.Info.ProcessesInfo;

public class ProcessesInfoPreference implements IPreference
{
    public String GetClassName()
    {
        return "ProcessLog";
    }

    public String GetCommandToExecute()
    {
        return "ps aux";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new ProcessesInfo(commandExecutionResult);
    }
}

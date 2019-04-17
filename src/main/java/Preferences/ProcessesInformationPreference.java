package Preferences;

import Models.Info.IInfo;
import Models.Info.ProcessesInfo;

public class ProcessesInformationPreference implements IPreference
{
    public String GetCommandToExecute()
    {
        return "ps aux";
    }

    public IInfo GetInformationModel(String commandExecutionResult)
    {
        return new ProcessesInfo(commandExecutionResult);
    }
}
